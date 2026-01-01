package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;

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
import java.util.concurrent.atomic.AtomicInteger;

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
        // 创建 WebSocket 连接（需要设置读取超时为无限）
        OkHttpClient wsClient = okHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
        
        // 创建包装的 WebSocketListener（重连逻辑在回调中处理）
        WebSocketListener wrapperListener = createWrapperListener(listener, controller, 
                url, wsClient, request, retryCount, retryInterval, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
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
     * @param currentAttempt 当前尝试次数（通过 AtomicInteger 传递，用于跟踪重连次数）
     * @param lastException 上次异常
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     */
    private void reconnectWebSocket(String url, OkHttpClient okHttpClient, Request request,
                                  int retryCount, long retryInterval,
                                  WebSocketEventListener listener, WebSocketController controller,
                                  AtomicInteger currentAttempt, Exception lastException,
                                  WebSocketHeartbeatHandler heartbeatHandler, long heartbeatInterval, long heartbeatTimeout,
                                  List<String> subprotocols) {
        int attempt = currentAttempt.get();
        if (attempt > retryCount) {
            // 重试次数用完
            if (lastException != null) {
                listener.onError(controller, lastException, 
                        "重试" + retryCount + "次后仍然失败: " + lastException.getMessage());
            }
            return;
        }
        
        log.info("{} WebSocket开始第{}次重连", url, attempt);
        
        // 等待后重连
        if (attempt > 1) {
            try {
                Thread.sleep(retryInterval * (attempt - 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                listener.onError(controller, e, "重试被中断: " + e.getMessage());
                return;
            }
        }
        
        // 创建包装的 WebSocketListener（传入重连计数器）
        WebSocketListener wrapperListener = createWrapperListener(listener, controller,
                url, okHttpClient, request, retryCount, retryInterval, currentAttempt,
                heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
        // 重新建立连接（异步）
        WebSocket webSocket = okHttpClient.newWebSocket(request, wrapperListener);
        controller.setWebSocket(webSocket);
        
        // 注意：连接是异步的，成功或失败会在回调中处理
    }
    
    /**
     * 判断是否需要重连
     * 
     * @param controller WebSocket 控制器
     * @param e 异常
     * @return 如果需要重连返回 true，否则返回 false
     */
    private boolean shouldReconnect(WebSocketController controller, Exception e) {
        // 如果消息队列不为空，需要重连以发送消息（消息重发场景）
        if (controller.getPendingMessageCount() > 0) {
            return true;
        }
        
        // 如果是网络异常，需要重连（连接重连场景）
        if (e instanceof IOException) {
            return true;
        }
        
        // 其他情况不重连
        return false;
    }
    
    /**
     * 创建包装的 WebSocketListener（首次连接）
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
        // 创建重连计数器
        AtomicInteger reconnectAttempt = new AtomicInteger(0);
        return createWrapperListener(listener, controller, url, okHttpClient, request, 
                retryCount, retryInterval, reconnectAttempt, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
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
     * @param reconnectAttempt 重连计数器
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
                                                   AtomicInteger reconnectAttempt,
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
                controller.setOpen(true);
                
                // 启动心跳检测（如果配置了）
                if (heartbeatHandler != null) {
                    controller.startHeartbeat(heartbeatHandler, heartbeatInterval, heartbeatTimeout);
                }
                
                // 调用用户监听器
                listener.onOpen(controller);
                
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
                // WebSocket 正在关闭
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                // 调用用户监听器的 onClosing 方法
                listener.onClosing(controller, code, reason);
                
                // 根据 OkHttp WebSocket 规范，当收到关闭帧时，需要在 onClosing 中调用 webSocket.close() 来确认关闭
                // 这样才能确保触发 onClosed 回调
                try {
                    webSocket.close(code, reason);
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // WebSocket 已关闭
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                // 调用用户监听器
                listener.onClose(controller, code, reason);
                
                // 检查是否需要重连（如果有待发送消息）
                if (controller.getPendingMessageCount() > 0 && retryCount > 0) {
                    log.info("WebSocket连接关闭，但有待发送消息，将触发重连");
                    // 在后台线程中执行重连
                    new Thread(() -> {
                        reconnectAttempt.incrementAndGet();
                        reconnectWebSocket(url, okHttpClient, request, retryCount, retryInterval,
                                listener, controller, reconnectAttempt, new IOException("连接已关闭"),
                                heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
                    }).start();
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // 连接失败
                controller.setOpen(false);
                
                // 停止心跳检测
                controller.stopHeartbeat();
                
                Exception e = t != null ? (t instanceof Exception ? (Exception) t : new Exception(t)) : new IOException("连接失败");
                
                // 调用用户监听器
                String message = t != null ? t.getMessage() : "连接失败";
                listener.onError(controller, t, message);
                
                // 检查是否需要重连（如果有待发送消息或网络异常）
                boolean needReconnect = shouldReconnect(controller, e);
                if (needReconnect && retryCount > 0) {
                    log.info("WebSocket连接失败，将触发重连");
                    // 在后台线程中执行重连
                    new Thread(() -> {
                        reconnectAttempt.incrementAndGet();
                        reconnectWebSocket(url, okHttpClient, request, retryCount, retryInterval,
                                listener, controller, reconnectAttempt, e,
                                heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
                    }).start();
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

