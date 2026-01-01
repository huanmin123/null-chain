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
     * 创建适用于 WebSocket 的 OkHttpClient
     * 
     * <p>WebSocket 连接建立后，读取超时应该设置为无限，但连接超时应该保留。
     * 此方法确保初始连接和重连使用相同的客户端配置。</p>
     * 
     * @param okHttpClient 原始 OkHttp 客户端
     * @return 配置好的 WebSocket 客户端
     */
    private OkHttpClient createWebSocketClient(OkHttpClient okHttpClient) {
        // 创建 WebSocket 连接（需要设置读取超时为无限，但保留连接超时）
        // 注意：WebSocket 连接建立后，读取超时应该设置为无限，但连接超时应该保留
        return okHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.SECONDS) // WebSocket 连接建立后，读取超时设置为无限
                .build();
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
        // 创建适用于 WebSocket 的客户端（统一配置）
        OkHttpClient wsClient = createWebSocketClient(okHttpClient);
        
        // 在 controller 中设置重连参数
        controller.setMaxReconnectCount(retryCount);
        controller.setReconnectInterval(retryInterval);
        controller.resetReconnectAttempt();
        
        // 设置重连触发器：当连接关闭后发送消息时，触发重连
        // 注意：triggerReconnect 方法已经做了重复检查，这里不需要再次检查
        // 注意：重连状态会在 onOpen 成功时或重连失败且不再重试时重置，不在 finally 中重置
        controller.setReconnectTrigger(() -> {
            if (retryCount > 0 && controller.getPendingMessageCount() > 0) {
                // 使用共享线程池执行重连任务
                WebSocketController.getSharedExecutor().execute(() -> {
                    controller.incrementReconnectAttempt();
                    reconnectWebSocket(url, okHttpClient, request, retryCount, retryInterval,
                            listener, controller, new IOException("连接已关闭，有待发送消息"),
                            heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
                    // 注意：不在 finally 中重置状态，状态会在 onOpen 成功时或重连失败且不再重试时重置
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
        
        // 确保 attempt 至少为 1（防止首次重连时 attempt 为 0 导致无延迟）
        if (attempt <= 0) {
            attempt = controller.incrementReconnectAttempt();
        }
        
        log.info("{} WebSocket开始第{}次重连", url, attempt);
        
        // 更新连接状态为正在重连
        controller.setConnectionState(WebSocketConnectionState.RECONNECTING);
        
        // 使用重连管理器计算延迟（指数退避策略）
        long delay = WebSocketReconnectManager.calculateDelay(attempt, retryInterval);
        log.debug("重连延迟: {}ms (第{}次重连)", delay, attempt);
        
        // 使用 ScheduledExecutorService 延迟执行重连，避免阻塞线程
        // 注意：所有重连都应该有延迟，包括首次重连
        WebSocketController.getSharedHeartbeatExecutor().schedule(() -> {
            // 检查控制器是否已销毁（可能在延迟期间被关闭）
            if (controller.isDestroyed()) {
                log.debug("控制器已销毁，取消重连");
                controller.setReconnecting(false);
                return;
            }
            
            // 再次检查是否超过最大重连次数（可能在延迟期间状态已改变）
            if (controller.isReconnectExhausted()) {
                log.debug("重连次数已用完，取消重连");
                if (lastException != null) {
                    listener.onError(controller, lastException, 
                            "重试" + retryCount + "次后仍然失败: " + lastException.getMessage());
                }
                controller.setReconnecting(false);
                return;
            }
            
            // 执行实际的重连逻辑
            performReconnect(url, okHttpClient, request, retryCount, retryInterval,
                    listener, controller, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        }, delay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 执行实际的重连操作
     * 
     * @param url WebSocket URL
     * @param okHttpClient OkHttp 客户端
     * @param request WebSocket 请求
     * @param retryCount 总重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     */
    private void performReconnect(String url, OkHttpClient okHttpClient, Request request,
                                  int retryCount, long retryInterval,
                                  WebSocketEventListener listener, WebSocketController controller,
                                  WebSocketHeartbeatHandler heartbeatHandler, long heartbeatInterval, long heartbeatTimeout,
                                  List<String> subprotocols) {
        
        // 在创建新连接前，先关闭旧的 WebSocket 连接（如果存在）
        // 注意：这可以防止资源泄漏，确保旧的连接被正确关闭
        WebSocket oldWebSocket = controller.getWebSocket();
        if (oldWebSocket != null) {
            try {
                // 只关闭连接，不影响消息队列和重连状态
                oldWebSocket.close(1000, "重连前关闭旧连接");
                log.debug("重连前已关闭旧连接");
            } catch (Exception e) {
                // 连接可能已经关闭，忽略异常
                log.debug("关闭旧连接时发生异常（可能已经关闭）", e);
            }
        }
        
        // 创建适用于 WebSocket 的客户端（统一配置，与初始连接保持一致）
        OkHttpClient wsClient = createWebSocketClient(okHttpClient);
        
        // 创建包装的 WebSocketListener
        WebSocketListener wrapperListener = createWrapperListener(listener, controller,
                url, wsClient, request, retryCount, retryInterval, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
        
        // 重新建立连接（异步）
        WebSocket webSocket = wsClient.newWebSocket(request, wrapperListener);
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
                handleOnOpen(webSocket, response, listener, controller, subprotocols, 
                        heartbeatHandler, heartbeatInterval, heartbeatTimeout);
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleOnMessageText(listener, controller, text, heartbeatHandler);
            }
            
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                handleOnMessageBytes(listener, controller, bytes, heartbeatHandler);
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                handleOnClosing(webSocket, code, reason, listener, controller, url);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                handleOnClosed(webSocket, code, reason, listener, controller, url, retryCount);
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                handleOnFailure(webSocket, t, response, listener, controller, url, retryCount);
            }
        };
    }
    
    /**
     * 处理连接打开事件
     * 
     * @param webSocket WebSocket 实例
     * @param response HTTP 响应
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param subprotocols 子协议列表
     * @param heartbeatHandler 心跳处理器
     * @param heartbeatInterval 心跳间隔（毫秒）
     * @param heartbeatTimeout 心跳超时（毫秒）
     */
    private void handleOnOpen(WebSocket webSocket, Response response,
                             WebSocketEventListener listener, WebSocketController controller,
                             List<String> subprotocols,
                             WebSocketHeartbeatHandler heartbeatHandler, long heartbeatInterval, long heartbeatTimeout) {
        // 验证子协议
        if (!validateSubprotocol(response, controller, subprotocols)) {
            handleSubprotocolValidationFailure(webSocket, listener, controller);
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
    
    /**
     * 处理子协议验证失败
     * 
     * @param webSocket WebSocket 实例
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     */
    private void handleSubprotocolValidationFailure(WebSocket webSocket,
                                                    WebSocketEventListener listener,
                                                    WebSocketController controller) {
        // 验证失败，设置连接状态为 FAILED
        WebSocketConnectionState oldState = controller.getConnectionState();
        controller.setConnectionState(WebSocketConnectionState.FAILED);
        controller.setOpen(false);
        
        // 如果正在重连，重置重连状态（避免状态不一致）
        if (controller.isReconnecting()) {
            controller.setReconnecting(false);
        }
        
        // 停止心跳检测（如果已启动）
        controller.stopHeartbeat();
        
        // 关闭连接
        try {
            webSocket.close(1002, "Sec-WebSocket-Protocol 验证失败");
        } catch (Exception e) {
            log.warn("关闭连接时发生异常", e);
        }
        
        // 调用用户监听器
        String errorMsg = "服务器返回的子协议不在客户端支持的列表中";
        listener.onError(controller, new IllegalStateException(errorMsg), errorMsg);
        listener.onStateChanged(controller, oldState, WebSocketConnectionState.FAILED);
    }
    
    /**
     * 处理文本消息
     * 
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param text 文本消息
     * @param heartbeatHandler 心跳处理器
     */
    private void handleOnMessageText(WebSocketEventListener listener, WebSocketController controller,
                                     String text, WebSocketHeartbeatHandler heartbeatHandler) {
        // 检测心跳回复
        if (heartbeatHandler != null && heartbeatHandler.isHeartbeatResponse(text)) {
            controller.updateHeartbeatResponseTime(heartbeatHandler);
            // 心跳回复不传递给用户监听器
            return;
        }
        
        // 调用用户监听器
        listener.onMessage(controller, text);
    }
    
    /**
     * 处理二进制消息
     * 
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param bytes 二进制消息
     * @param heartbeatHandler 心跳处理器
     */
    private void handleOnMessageBytes(WebSocketEventListener listener, WebSocketController controller,
                                     ByteString bytes, WebSocketHeartbeatHandler heartbeatHandler) {
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
    
    /**
     * 处理连接正在关闭事件
     * 
     * @param webSocket WebSocket 实例
     * @param code 关闭状态码
     * @param reason 关闭原因
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param url WebSocket URL
     */
    private void handleOnClosing(WebSocket webSocket, int code, String reason,
                                 WebSocketEventListener listener, WebSocketController controller, String url) {
        log.info("WebSocket onClosing 被调用: code={}, reason={}, url={}", code, reason, url);
        
        // 检查是否已经在关闭流程中，防止重复处理
        if (isAlreadyClosing(controller)) {
            // 如果已经在关闭流程中，仍然需要调用用户监听器（因为这是 OkHttp 的回调）
            // 但不要重复设置状态和执行其他逻辑
            log.debug("连接已在关闭流程中，但仍需调用用户监听器: currentState={}", controller.getConnectionState());
            listener.onClosing(controller, code, reason);
            return;
        }
        
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
    
    /**
     * 处理连接已关闭事件
     * 
     * @param webSocket WebSocket 实例
     * @param code 关闭状态码
     * @param reason 关闭原因
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param url WebSocket URL
     * @param retryCount 重试次数
     */
    private void handleOnClosed(WebSocket webSocket, int code, String reason,
                                WebSocketEventListener listener, WebSocketController controller,
                                String url, int retryCount) {
        int pendingCount = controller.getPendingMessageCount();
        log.info("WebSocket onClosed 被调用: code={}, reason={}, url={}, 待发送消息数={}", 
                code, reason, url, pendingCount);
        
        // 检查是否已经处理过关闭，防止重复处理（可能由 onClosing 调用 close() 触发）
        if (isAlreadyClosed(controller)) {
            // 如果已经关闭，仍然需要调用用户监听器（因为这是 OkHttp 的回调）
            // 但不要重复设置状态和执行其他逻辑（如重连判断、消息队列处理等）
            log.debug("连接已关闭，但仍需调用用户监听器: currentState={}", controller.getConnectionState());
            listener.onClose(controller, code, reason);
            return;
        }
        
        // WebSocket 已关闭
        WebSocketConnectionState oldState = controller.getConnectionState();
        controller.setConnectionState(WebSocketConnectionState.CLOSED);
        controller.setOpen(false);
        
        // 停止心跳检测
        controller.stopHeartbeat();
        
        // 处理重连决策和消息队列
        handleReconnectDecision(controller, listener, code, reason, retryCount, pendingCount);
        
        // 调用用户监听器
        listener.onClose(controller, code, reason);
        listener.onStateChanged(controller, oldState, WebSocketConnectionState.CLOSED);
    }
    
    /**
     * 处理连接失败事件
     * 
     * @param webSocket WebSocket 实例
     * @param t 异常
     * @param response HTTP 响应
     * @param listener 用户的事件监听器
     * @param controller WebSocket 控制器
     * @param url WebSocket URL
     * @param retryCount 重试次数
     */
    private void handleOnFailure(WebSocket webSocket, Throwable t, Response response,
                                WebSocketEventListener listener, WebSocketController controller,
                                String url, int retryCount) {
        int pendingCount = controller.getPendingMessageCount();
        log.info("WebSocket onFailure 被调用: url={}, error={}, response={}, 待发送消息数={}", 
                url, t != null ? t.getMessage() : "null", response, pendingCount);
        
        // 检查是否已经在关闭或关闭状态，防止重复处理
        // 如果 onClosed 已经处理过，这里不应该再处理重连
        if (isAlreadyClosingOrClosed(controller)) {
            log.debug("连接已在关闭流程中，跳过 onFailure 处理: currentState={}", controller.getConnectionState());
            return;
        }
        
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
        // 注意：onFailure 通常在连接建立前失败，此时 onClosed 不会被调用
        // 但如果连接建立后失败，可能会先触发 onFailure，然后触发 onClosed
        // 为了安全，只在连接未关闭时处理重连
        boolean needReconnect = shouldReconnect(controller, listener, e);
        if (needReconnect && retryCount > 0) {
            // 网络异常且用户同意重连，保留消息并触发重连
            log.info("连接失败，但需要重连（网络异常或用户同意），保留 {} 条消息等待重连", pendingCount);
            controller.triggerReconnect("连接失败: " + (e != null ? e.getMessage() : "未知错误"));
        } else if (pendingCount > 0) {
            // 用户不同意重连或未配置重连，丢弃消息
            int clearedCount = controller.clearMessageQueue();
            log.info("连接失败，但不需要重连，丢弃 {} 条未发送消息", clearedCount);
        }
    }
    
    /**
     * 检查是否已经在关闭流程中
     * 
     * @param controller WebSocket 控制器
     * @return 如果已经在关闭流程中返回 true，否则返回 false
     */
    private boolean isAlreadyClosing(WebSocketController controller) {
        WebSocketConnectionState currentState = controller.getConnectionState();
        return currentState == WebSocketConnectionState.CLOSING || 
               currentState == WebSocketConnectionState.CLOSED;
    }
    
    /**
     * 检查是否已经关闭
     * 
     * @param controller WebSocket 控制器
     * @return 如果已经关闭返回 true，否则返回 false
     */
    private boolean isAlreadyClosed(WebSocketController controller) {
        return controller.getConnectionState() == WebSocketConnectionState.CLOSED;
    }
    
    /**
     * 检查是否已经在关闭或关闭状态
     * 
     * @param controller WebSocket 控制器
     * @return 如果已经在关闭或关闭状态返回 true，否则返回 false
     */
    private boolean isAlreadyClosingOrClosed(WebSocketController controller) {
        WebSocketConnectionState currentState = controller.getConnectionState();
        return currentState == WebSocketConnectionState.CLOSED || 
               currentState == WebSocketConnectionState.CLOSING;
    }
    
    /**
     * 处理重连决策和消息队列
     * 
     * @param controller WebSocket 控制器
     * @param listener 用户的事件监听器
     * @param code 关闭状态码
     * @param reason 关闭原因
     * @param retryCount 重试次数
     * @param pendingCount 待发送消息数
     */
    private void handleReconnectDecision(WebSocketController controller, WebSocketEventListener listener,
                                        int code, String reason, int retryCount, int pendingCount) {
        // 判断是否需要保留消息并重连
        // 只有网络异常或用户通过 shouldReconnect 同意重连时，才保留消息
        // 注意：重连逻辑只在这里处理，onFailure 中不再处理重连（避免重复）
        if (pendingCount > 0 && retryCount > 0) {
            // 判断是否是网络异常关闭（code 1006 通常表示异常关闭）
            boolean isNetworkError = (code == 1006) || (code == 1001) || (code == 1011);
            
            // 创建异常对象用于 shouldReconnect 判断
            Exception closeException;
            if (isNetworkError) {
                closeException = new IOException("网络异常导致连接关闭: code=" + code + ", reason=" + reason);
            } else {
                // 服务端正常关闭，创建普通异常
                closeException = new IOException("连接关闭: code=" + code + ", reason=" + reason);
            }
            
            // 调用用户监听器判断是否应该重连
            boolean needReconnect = shouldReconnect(controller, listener, closeException);
            
            if (needReconnect) {
                // 网络异常或用户同意重连，保留消息并触发重连
                log.info("连接关闭，但需要重连（网络异常或用户同意），保留 {} 条消息等待重连", pendingCount);
                controller.triggerReconnect("连接关闭，但需要重连");
            } else {
                // 服务端正常关闭且用户不同意重连，丢弃消息
                int clearedCount = controller.clearMessageQueue();
                log.info("连接关闭（服务端主动断开），丢弃 {} 条未发送消息。关闭原因: code={}, reason={}", 
                        clearedCount, code, reason);
            }
        } else if (pendingCount > 0) {
            // 有未发送消息但未配置重连，丢弃消息
            int clearedCount = controller.clearMessageQueue();
            log.info("连接关闭，未配置重连机制，丢弃 {} 条未发送消息。关闭原因: code={}, reason={}", 
                    clearedCount, code, reason);
        }
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

