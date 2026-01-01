package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketConnectionState;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketReconnectManager;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 响应处理策略
 * 
 * <p>处理 WebSocket 连接，封装 OkHttp 原生 WebSocket API，提供自定义事件监听器接口，
 * 实现智能重连机制：区分消息重发（有消息队列时）和连接重连（无消息队列时）。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, 
                        int retryCount, long retryInterval, Object... params) throws Exception {
        // 参数已在调用方校验，直接使用
        WebSocketEventListener listener = (WebSocketEventListener) params[0];
        WebSocketHeartbeatHandler heartbeatHandler = params.length > 1 ? (WebSocketHeartbeatHandler) params[1] : null;
        long heartbeatInterval = params.length > 2 ? (Long) params[2] : 30000;
        long heartbeatTimeout = params.length > 3 ? (Long) params[3] : 10000;
        @SuppressWarnings("unchecked")
        List<String> subprotocols = params.length > 4 ? (List<String>) params[4] : null;
        
        // 创建 WebSocketController
        WebSocketController controller = new WebSocketController();
        
        // URL 转换：http -> ws, https -> wss
        String wsUrl = convertToWebSocketUrl(url);
        
        // 创建 WebSocket 请求，如果配置了子协议，设置请求头
        Request.Builder wsRequestBuilder = request.url(wsUrl);
        if (subprotocols != null && !subprotocols.isEmpty()) {
            // 多个协议用逗号分隔
            String protocolHeader = String.join(", ", subprotocols);
            wsRequestBuilder.addHeader("Sec-WebSocket-Protocol", protocolHeader);
        }
        Request wsRequest = wsRequestBuilder.build();
        
        // 处理 WebSocket 连接（支持重连）
        connectWebSocket(wsUrl, okHttpClient, wsRequest, retryCount, retryInterval, 
                listener, controller, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
        // 返回控制器，用户可以继续发送消息
        return controller;
    }
    
    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.WEBSOCKET;
    }
    
    /**
     * 将 HTTP URL 转换为 WebSocket URL
     * 
     * @param url 原始 URL
     * @return WebSocket URL
     */
    private String convertToWebSocketUrl(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("http://")) {
            return url.replace("http://", "ws://");
        } else if (url.startsWith("https://")) {
            return url.replace("https://", "wss://");
        } else if (url.startsWith("ws://") || url.startsWith("wss://")) {
            return url;
        } else {
            // 默认使用 ws://
            return "ws://" + url;
        }
    }
    
    /**
     * 连接 WebSocket（支持重连）
     * 
     * <p>WebSocket 连接是异步的，该方法会立即返回，连接在后台建立。
     * 如果连接失败，会在 onFailure 回调中触发重连。</p>
     * 
     * @param url WebSocket URL
     * @param okHttpClient OkHttp 客户端
     * @param request WebSocket 请求
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     */
    private void connectWebSocket(String url, OkHttpClient okHttpClient, Request request,
                                 int retryCount, long retryInterval,
                                 WebSocketEventListener listener, WebSocketController controller,
                                 WebSocketHeartbeatHandler heartbeatHandler, long heartbeatInterval, long heartbeatTimeout,
                                 List<String> subprotocols) {
        // 创建 WebSocket 连接（需要设置读取超时为无限，但保留连接超时）
        // 注意：WebSocket 连接建立后，读取超时应该设置为无限，但连接超时应该保留
        OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.SECONDS); // WebSocket 连接建立后，读取超时设置为无限

        OkHttpClient wsClient = clientBuilder.build();
        
        // 在 controller 中设置重连参数
        controller.setMaxReconnectCount(retryCount);
        controller.setReconnectInterval(retryInterval);
        controller.resetReconnectAttempt();
        
        // 设置重连触发器：当连接关闭后发送消息时，触发重连
        // 注意：triggerReconnect 方法已经做了重复检查，这里不需要再次检查
        controller.setReconnectTrigger(() -> {
            if (retryCount > 0 && controller.getPendingMessageCount() > 0) {
                // 使用共享线程池执行重连任务
                WebSocketController.getSharedExecutor().execute(() -> {
                    try {
                        controller.incrementReconnectAttempt();
                        reconnectWebSocket(url, okHttpClient, request, retryCount, retryInterval,
                                listener, controller, new IOException("连接已关闭，有待发送消息"),
                                heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
                    } finally {
                        controller.setReconnecting(false);
                    }
                });
            }
        });
        
        // 创建包装的 WebSocketListener（重连逻辑在回调中处理）
        WebSocketListener wrapperListener = createWrapperListener(listener, controller, 
                url, wsClient, request, retryCount, retryInterval, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
        // 更新连接状态为正在连接
        controller.setConnectionState(WebSocketConnectionState.CONNECTING);
        
        // 建立 WebSocket 连接（异步，立即返回）
        WebSocket webSocket = wsClient.newWebSocket(request, wrapperListener);
        controller.setWebSocket(webSocket);
    }
    
    /**
     * 重连 WebSocket
     * 
     * @param url WebSocket URL
     * @param okHttpClient OkHttp 客户端
     * @param request WebSocket 请求
     * @param retryCount 总重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param lastException 上次异常
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     */
    private void reconnectWebSocket(String url, OkHttpClient okHttpClient, Request request,
                                  int retryCount, long retryInterval,
                                  WebSocketEventListener listener, WebSocketController controller,
                                  Exception lastException,
                                  WebSocketHeartbeatHandler heartbeatHandler, long heartbeatInterval, long heartbeatTimeout,
                                  List<String> subprotocols) {
        // 使用 controller 统一管理的重连计数器
        int attempt = controller.getReconnectAttempt();
        
        // 检查是否超过最大重连次数
        if (controller.isReconnectExhausted()) {
            // 重试次数用完
            if (lastException != null) {
                listener.onError(controller, lastException, 
                        "重试" + retryCount + "次后仍然失败: " + lastException.getMessage());
            }
            controller.setReconnecting(false);
            return;
        }
        
        log.info("{} WebSocket开始第{}次重连", url, attempt);
        
        // 更新连接状态为正在重连
        controller.setConnectionState(WebSocketConnectionState.RECONNECTING);
        
        // 等待后重连（使用重连管理器计算延迟）
        if (attempt > 0) {
            try {
                // 使用重连管理器计算延迟（指数退避策略）
                long delay = WebSocketReconnectManager.calculateDelay(attempt, retryInterval);
                log.debug("重连延迟: {}ms (第{}次重连)", delay, attempt);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                listener.onError(controller, e, "重试被中断: " + e.getMessage());
                controller.setReconnecting(false);
                return;
            }
        }
        
        // 创建包装的 WebSocketListener
        WebSocketListener wrapperListener = createWrapperListener(listener, controller,
                url, okHttpClient, request, retryCount, retryInterval, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
        // 重新建立连接（异步）
        WebSocket webSocket = okHttpClient.newWebSocket(request, wrapperListener);
        controller.setWebSocket(webSocket);
        
        // 注意：连接是异步的，成功或失败会在回调中处理
    }
    
    /**
     * 判断是否需要重连（调用用户监听器的方法）
     * 
     * @param controller WebSocket 控制器
     * @param listener 用户监听器
     * @param e 异常
     * @return 如果需要重连返回 true，否则返回 false
     */
    private boolean shouldReconnect(WebSocketController controller, WebSocketEventListener listener, Exception e) {
        // 调用用户监听器的方法判断是否应该重连
        boolean hasPendingMessages = controller.getPendingMessageCount() > 0;
        return listener.shouldReconnect(controller, e, hasPendingMessages);
    }
    
    /**
     * 创建包装的 WebSocketListener
     * 
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param url WebSocket URL
     * @param okHttpClient OkHttp 客户端
     * @param request WebSocket 请求
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     * @return 包装后的 WebSocketListener
     */
    private WebSocketListener createWrapperListener(WebSocketEventListener listener, 
                                                   WebSocketController controller,
                                                   String url,
                                                   OkHttpClient okHttpClient,
                                                   Request request,
                                                   int retryCount,
                                                   long retryInterval,
                                                   WebSocketHeartbeatHandler heartbeatHandler,
                                                   long heartbeatInterval,
                                                   long heartbeatTimeout,
                                                   List<String> subprotocols) {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // 验证子协议
                if (!validateSubprotocol(response, controller, subprotocols)) {
                    // 验证失败，关闭连接
                    webSocket.close(1002, "Sec-WebSocket-Protocol 验证失败");
                    String errorMsg = "服务器返回的子协议不在客户端支持的列表中";
                    listener.onError(controller, new IllegalStateException(errorMsg), errorMsg);
                    return;
                }
                
                controller.setWebSocket(webSocket);
                
                // 更新连接状态
                WebSocketConnectionState oldState = controller.getConnectionState();
                controller.setConnectionState(WebSocketConnectionState.CONNECTED);
                controller.setOpen(true);
                
                // 连接成功后，重置重连状态
                controller.resetReconnectAttempt();
                controller.setReconnecting(false);
                
                // 启动心跳检测（如果配置了）
                if (heartbeatHandler != null) {
                    controller.startHeartbeat(heartbeatHandler, heartbeatInterval, heartbeatTimeout);
                }
                
                // 调用用户监听器
                listener.onOpen(controller);
                listener.onStateChanged(controller, oldState, WebSocketConnectionState.CONNECTED);
                
                log.info("WebSocket 连接建立成功");
                
                // 连接建立后，尝试发送队列中的消息
                controller.trySend();
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 检测心跳回复
                if (heartbeatHandler != null && heartbeatHandler.isHeartbeatResponse(text)) {
                    controller.updateHeartbeatResponseTime(heartbeatHandler);
                    // 心跳回复不传递给用户监听器
                    return;
                }
                
                // 调用用户监听器
                listener.onMessage(controller, text);
            }
            
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                byte[] byteArray = bytes.toByteArray();
                
                // 检测心跳回复
                if (heartbeatHandler != null && heartbeatHandler.isHeartbeatResponse(byteArray)) {
                    controller.updateHeartbeatResponseTime(heartbeatHandler);
                    // 心跳回复不传递给用户监听器
                    return;
                }
                
                // 调用用户监听器
                listener.onMessage(controller, byteArray);
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                log.info("WebSocket onClosing 被调用: code={}, reason={}, url={}", code, reason, url);
                // WebSocket 正在关闭
                WebSocketConnectionState oldState = controller.getConnectionState();
                controller.setConnectionState(WebSocketConnectionState.CLOSING);
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                // 调用用户监听器的 onClosing 方法
                listener.onClosing(controller, code, reason);
                listener.onStateChanged(controller, oldState, WebSocketConnectionState.CLOSING);
                
                // 根据 OkHttp WebSocket 规范，当收到关闭帧时，需要在 onClosing 中调用 webSocket.close() 来确认关闭
                // 这样才能确保触发 onClosed 回调
                try {
                    webSocket.close(code, reason);
                    log.debug("WebSocket 已确认关闭: code={}, reason={}", code, reason);
                } catch (Exception e) {
                    log.warn("WebSocket 确认关闭时发生异常", e);
                }
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.info("WebSocket onClosed 被调用: code={}, reason={}, url={}, 待发送消息数={}", 
                        code, reason, url, controller.getPendingMessageCount());
                // WebSocket 已关闭
                WebSocketConnectionState oldState = controller.getConnectionState();
                controller.setConnectionState(WebSocketConnectionState.CLOSED);
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                // 调用用户监听器
                listener.onClose(controller, code, reason);
                listener.onStateChanged(controller, oldState, WebSocketConnectionState.CLOSED);
                
                // 检查是否需要重连（如果有待发送消息）
                if (controller.getPendingMessageCount() > 0 && retryCount > 0) {
                    // 使用统一的重连触发方法
                    controller.triggerReconnect("连接关闭，但有待发送消息");
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.info("WebSocket onFailure 被调用: url={}, error={}, response={}, 待发送消息数={}", 
                        url, t != null ? t.getMessage() : "null", response, controller.getPendingMessageCount());
                // 连接失败
                WebSocketConnectionState oldState = controller.getConnectionState();
                controller.setConnectionState(WebSocketConnectionState.FAILED);
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                Exception e = t != null ? (t instanceof Exception ? (Exception) t : new Exception(t)) : new IOException("连接失败");
                
                // 调用用户监听器
                String message = t != null ? t.getMessage() : "连接失败";
                listener.onError(controller, t, message);
                listener.onStateChanged(controller, oldState, WebSocketConnectionState.FAILED);
                
                // 检查是否需要重连（调用用户监听器的方法）
                boolean needReconnect = shouldReconnect(controller, listener, e);
                if (needReconnect && retryCount > 0) {
                    // 使用统一的重连触发方法
                    controller.triggerReconnect("连接失败: " + (e != null ? e.getMessage() : "未知错误"));
                }
            }
        };
    }
    
    /**
     * 验证子协议
     * 
     * @param response HTTP 响应
     * @param controller WebSocket 控制器
     * @param clientProtocols 客户端配置的子协议列表
     * @return 如果验证通过返回 true，否则返回 false
     */
    private boolean validateSubprotocol(Response response, WebSocketController controller, List<String> clientProtocols) {
        String serverProtocol = response.header("Sec-WebSocket-Protocol");
        
        if (serverProtocol != null && !serverProtocol.isEmpty()) {
            String trimmedServerProtocol = serverProtocol.trim();
            
            if (clientProtocols != null && !clientProtocols.isEmpty()) {
                // 客户端配置了协议，需要验证
                if (clientProtocols.contains(trimmedServerProtocol)) {
                    // 验证成功，保存协议
                    controller.setSelectedSubprotocol(trimmedServerProtocol);
                    return true;
                } else {
                    // 验证失败，协议不在支持的列表中
                    log.warn("服务器返回的子协议 '{}' 不在客户端支持的列表中: {}", trimmedServerProtocol, clientProtocols);
                    return false;
                }
            } else {
                // 客户端未配置协议，但服务器返回了协议，也保存（可选）
                controller.setSelectedSubprotocol(trimmedServerProtocol);
                return true;
            }
        } else {
            // 服务器未返回协议
            if (clientProtocols != null && !clientProtocols.isEmpty()) {
                // 客户端配置了协议，但服务器未返回，验证失败
                log.warn("客户端配置了子协议 {}，但服务器未返回子协议", clientProtocols);
                return false;
            }
            // 客户端未配置协议，服务器也未返回，验证通过
            return true;
        }
    }
}

