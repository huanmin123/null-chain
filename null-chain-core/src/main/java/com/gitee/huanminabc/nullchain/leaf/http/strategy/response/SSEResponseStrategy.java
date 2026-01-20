package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEConnectionState;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEController;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEErrorCode;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEHttpException;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEReconnectManager;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEStreamController;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SSE流式响应处理策略
 * 
 * <p>处理HTTP响应为SSE流的情况。如果响应Content-Type为text/event-stream，
 * 则按照SSE协议解析并触发相应的事件回调；如果响应不是SSE格式，则触发非SSE响应回调。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class SSEResponseStrategy implements ResponseStrategy {
    
    /** SSE Content-Type 主类型 */
    private static final String SSE_CONTENT_TYPE = "text/event-stream";
    
    /** SSE 字段名：id */
    private static final String FIELD_ID = "id";
    
    /** SSE 字段名：event */
    private static final String FIELD_EVENT = "event";
    
    /** SSE 字段名：data */
    private static final String FIELD_DATA = "data";
    
    /** SSE 字段名：retry */
    private static final String FIELD_RETRY = "retry";
    
    /** SSE 注释行前缀 */
    private static final String COMMENT_PREFIX = ":";
    
    /** 换行符 */
    private static final String NEWLINE = "\n";
    
    /** SSE 专用客户端缓存（线程安全） */
    private static final AtomicReference<OkHttpClient> cachedSseClient = new AtomicReference<>();
    
    /** 共享的线程池（用于异步执行SSE请求） */
    private static final ExecutorService SHARED_EXECUTOR = createSharedExecutor();

    /** SSE Accept 请求头 */
    private static final String SSE_ACCEPT_HEADER = "text/event-stream";

    /** SSE Cache-Control 请求头（禁用缓存） */
    private static final String SSE_CACHE_CONTROL = "no-cache";

    /** SSE Accept-Encoding 请求头（禁用压缩，确保实时性） */
    private static final String SSE_ACCEPT_ENCODING = "identity";

    /**
     * 创建共享的线程池
     * 
     * <p>注意：使用 daemon 线程，JVM 关闭时会自动终止，不需要关闭钩子。</p>
     * 
     * @return 线程池
     */
    private static ExecutorService createSharedExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SSE-Request-" + threadNumber.getAndIncrement());
                t.setDaemon(true); // Daemon 线程，JVM 关闭时自动终止
                return t;
            }
        });
    }

    /**
     * 添加 SSE 标准请求头
     *
     * <p>根据 SSE 规范和最佳实践，自动添加以下请求头：
     * <ul>
     *   <li>Accept: text/event-stream - 告诉服务器客户端期望接收 SSE 流</li>
     *   <li>Cache-Control: no-cache - 禁用缓存，确保实时接收事件</li>
     *   <li>Accept-Encoding: identity - 禁用压缩，确保流式传输的实时性</li>
     * </ul>
     * </p>
     *
     * <p>特别注意：Accept-Encoding 设置为 identity 是为了防止 OkHttp 默认添加 gzip 压缩。
     * 压缩会破坏 SSE 的实时性，因为数据会被缓存直到整个压缩块完成。</p>
     *
     * <p>注意：如果用户已经设置了这些头，将被覆盖，以确保 SSE 请求的正确性。</p>
     *
     * @param request 请求构建器
     */
    private static void addSseHeaders(Request.Builder request) {
        // 设置 Accept 头（SSE 规范要求）
        request.addHeader("Accept", SSE_ACCEPT_HEADER);
        // 设置 Cache-Control 头（禁用缓存）
        request.addHeader("Cache-Control", SSE_CACHE_CONTROL);
        // 设置 Accept-Encoding 头（禁用压缩，确保实时性）
        request.addHeader("Accept-Encoding", SSE_ACCEPT_ENCODING);
    }

    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        // 添加 SSE 标准请求头
        addSseHeaders(request);

        // 参数已在调用方校验，直接使用
        @SuppressWarnings("unchecked")
        SSEEventListener<Object> listener = (SSEEventListener<Object>) params[0];

        @SuppressWarnings("unchecked")
        DataDecoder<Object> decoder = (DataDecoder<Object>) params[1];

        // 创建SSE控制器
        SSEController controller = new SSEController();
        controller.setMaxReconnectCount(retryCount);
        controller.setReconnectInterval(retryInterval);
        
        // 异步执行 SSE 请求，避免阻塞调用线程
        SHARED_EXECUTOR.execute(() -> {
            toSSE(url, okHttpClient, request, retryCount, retryInterval, listener, decoder, controller);
        });
        
        // 立即返回控制器，用户可以用于管理连接
        return controller;
    }

    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.SSE;
    }

    /**
     * 处理 SSE 流式响应
     * 
     * <p>该方法用于处理 HTTP 响应为 SSE 流的情况。如果响应 Content-Type 为 text/event-stream，
     * 则按照 SSE 协议解析并触发相应的事件回调；如果响应不是 SSE 格式，则触发非 SSE 响应回调。</p>
     * 
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>执行 HTTP 请求（支持重试）</li>
     *   <li>检查 Content-Type 是否为 text/event-stream</li>
     *   <li>如果是 SSE：按照 SSE 协议解析流，触发相应回调</li>
     *   <li>如果不是 SSE：读取响应体，调用 onNonSseResponse</li>
     *   <li>错误处理：非 2xx 响应或异常时调用 onError</li>
     * </ol>
     * 
     * <p>注意：当 retryCount=0 时，attempt 为 1（表示第一次尝试，没有重试）。</p>
     * 
     * @param url HTTP请求的URL
     * @param okHttpClient OkHttp客户端实例
     * @param request 请求构建器
     * @param retryCount 重试次数（0表示不重试，只执行一次）
     * @param retryInterval 重试间隔（毫秒），使用固定间隔
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param controller SSE 控制器
     * @param <T> SSE 数据类型
     */
    private <T> void toSSE(String url, OkHttpClient okHttpClient, Request.Builder request, 
                           int retryCount, long retryInterval, 
                           SSEEventListener<T> listener, DataDecoder<T> decoder, SSEController controller) {
        // 设置重连触发器（用于流中断后的自动重连）
        setupReconnectTrigger(url, okHttpClient, request, listener, decoder, controller);
        
        // 更新连接状态为CONNECTING
        transitionState(listener, controller, SSEConnectionState.CONNECTING);
        
        // SSE 处理使用自定义重试逻辑，错误通过回调处理，不抛出异常
        IOException lastException = executeWithRetry(url, okHttpClient, request, retryCount, retryInterval, 
                listener, decoder, controller);
        
        // 所有重试都失败，通过回调处理
        // 注意：
        // 1. 如果retryCount==0，错误已经在executeWithRetry中处理了，这里不需要再处理
        // 2. 如果遇到不可重试的HTTP错误，错误已经在executeWithRetry中处理了，返回null，这里也不会处理
        // 3. 只有当所有重试都失败（循环结束）且错误是可重试的（网络错误或可重试的HTTP错误）时，才在这里处理
        if (lastException != null && retryCount > 0) {
            int attempt = retryCount;
            int errorCode = lastException instanceof SSEHttpException 
                    ? ((SSEHttpException) lastException).getHttpCode() 
                    : SSEErrorCode.NETWORK_ERROR;
            handleErrorWithState(listener, controller, attempt, errorCode, 
                    "重试" + retryCount + "次后仍然失败: " + lastException.getMessage(), 
                    lastException, SSEConnectionState.CONNECTING);
        }
    }
    
    /**
     * 设置重连触发器
     * 
     * @param url 请求URL
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param controller SSE 控制器
     * @param <T> SSE 数据类型
     */
    private <T> void setupReconnectTrigger(String url, OkHttpClient okHttpClient, Request.Builder request,
                                           SSEEventListener<T> listener, DataDecoder<T> decoder, 
                                           SSEController controller) {
        // 注意：线程控制已在 SSEController.triggerReconnect() 中处理，这里只需要设置重连逻辑
        controller.setReconnectTrigger(() -> {
            try {
                // 增加重连次数
                int attempt = controller.incrementReconnectAttempt();
                
                // 检查是否超过最大重连次数
                if (controller.isReconnectExhausted()) {
                    int currentAttempt = controller.getReconnectAttempt();
                    log.warn("[SSE重连] 已达到最大重连次数({})，停止重连。URL: {}", currentAttempt, url);
                    handleErrorWithState(listener, controller, attempt, SSEErrorCode.RECONNECT_EXHAUSTED, 
                            "重连次数已达上限", null, null);
                    controller.setReconnecting(false);
                    return;
                }
                
                // 执行重连（带上Last-Event-ID）
                executeSseRequest(okHttpClient, request, listener, decoder, attempt, controller, true);
            } catch (SSEHttpException e) {
                handleReconnectHttpError(url, listener, controller, e);
            } catch (IOException e) {
                handleReconnectNetworkError(url, listener, controller, e);
            } catch (Exception e) {
                log.error("[SSE重连] 发生不可重试的异常，URL: {}", url, e);
                handleErrorWithState(listener, controller, controller.getReconnectAttempt(), 
                        SSEErrorCode.UNKNOWN_ERROR, "重连异常: " + e.getMessage(), e, null);
                controller.setReconnecting(false);
            }
        });
    }
    
    /**
     * 处理重连时的HTTP错误
     */
    private <T> void handleReconnectHttpError(String url, SSEEventListener<T> listener, 
                                             SSEController controller, SSEHttpException e) {
        controller.setReconnecting(false);
        
        if (e.isRetryable()) {
            // 可重试的HTTP错误（5xx、429、408），尝试继续重连
            int currentAttempt = controller.getReconnectAttempt();
            log.warn("[SSE重连] 遇到可重试的HTTP错误，状态码: {}, 当前重连次数: {}/{}, URL: {}, 错误: {}", 
                    e.getHttpCode(), currentAttempt, controller.getMaxReconnectCount(), url, e.getMessage());
            
            // 检查是否还有重连机会
            if (controller.isReconnectExhausted()) {
                log.error("[SSE重连] 已达到最大重连次数，停止重连。URL: {}, 最后错误: HTTP {}", url, e.getHttpCode());
                handleErrorWithState(listener, controller, controller.getReconnectAttempt(),
                        SSEErrorCode.RECONNECT_EXHAUSTED, "重连次数已达上限", e, null);
            } else {
                // 还有重连机会，计算延迟并通知监听器
                int nextAttempt = currentAttempt + 1;
                long delay = SSEReconnectManager.calculateDelay(nextAttempt, controller.getReconnectInterval());
                notifyRetry(listener, nextAttempt, delay);
                // 再次触发重连
                controller.triggerReconnect("HTTP错误 " + e.getHttpCode() + ": " + e.getMessage());
            }
        } else {
            // 不可重试的HTTP错误（如404、401），不继续重连
            log.warn("[SSE重连] 遇到不可重试的HTTP错误，状态码: {}, URL: {}, 错误: {}", 
                    e.getHttpCode(), url, e.getMessage());
            handleErrorWithState(listener, controller, controller.getReconnectAttempt(), e.getHttpCode(), 
                    "不可重试的HTTP错误: " + e.getMessage(), e, null);
        }
    }
    
    /**
     * 处理重连时的网络错误
     */
    private <T> void handleReconnectNetworkError(String url, SSEEventListener<T> listener, 
                                                  SSEController controller, IOException e) {
        controller.setReconnecting(false);
        int currentAttempt = controller.getReconnectAttempt();
        log.warn("[SSE重连] 网络异常，当前重连次数: {}, URL: {}, 错误: {}", currentAttempt, url, e.getMessage());
        
        // 检查是否还有重连机会
        if (controller.isReconnectExhausted()) {
            log.error("[SSE重连] 已达到最大重连次数，停止重连。URL: {}, 最后错误: {}", url, e.getMessage());
            handleErrorWithState(listener, controller, controller.getReconnectAttempt(),
                    SSEErrorCode.RECONNECT_EXHAUSTED, "重连次数已达上限", e, null);
        } else {
            // 还有重连机会，计算延迟并通知监听器
            int nextAttempt = currentAttempt + 1;
            long delay = SSEReconnectManager.calculateDelay(nextAttempt, controller.getReconnectInterval());
            notifyRetry(listener, nextAttempt, delay);
            // 再次触发重连
            controller.triggerReconnect("网络异常: " + e.getMessage());
        }
    }
    
    /**
     * 执行带重试的SSE请求
     * 
     * @param url 请求URL
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param controller SSE 控制器
     * @param <T> SSE 数据类型
     * @return 最后一次异常，如果成功返回null
     */
    private <T> IOException executeWithRetry(String url, OkHttpClient okHttpClient, Request.Builder request,
                                           int retryCount, long retryInterval, 
                                           SSEEventListener<T> listener, DataDecoder<T> decoder, 
                                           SSEController controller) {
        // 如果重试次数为0，直接执行一次（attempt=1表示第一次尝试）
        if (retryCount == 0) {
            int attempt = 1;
            try {
                executeSseRequest(okHttpClient, request, listener, decoder, attempt, controller, false);
                return null;
            } catch (SSEHttpException e) {
                log.error("[SSE请求] 请求失败，HTTP状态码: {}, 未配置重试, URL: {}", e.getHttpCode(), url, e);
                handleErrorWithState(listener, controller, attempt, e.getHttpCode(), 
                        "请求失败: " + e.getMessage(), e, SSEConnectionState.CONNECTING);
                return e;
            } catch (IOException e) {
                log.error("[SSE请求] 请求失败，未配置重试, URL: {}", url, e);
                handleErrorWithState(listener, controller, attempt, SSEErrorCode.NETWORK_ERROR, 
                        "请求失败: " + e.getMessage(), e, SSEConnectionState.CONNECTING);
                return e;
            }
        }
        
        // 执行重试逻辑（使用固定间隔）
        IOException lastException = null;
        for (int i = 1; i <= retryCount; i++) {
            try {
                executeSseRequest(okHttpClient, request, listener, decoder, i, controller, false);
                return null; // 成功则返回
            } catch (SSEHttpException e) {
                lastException = e;
                    if (e.isRetryable()) {
                    log.warn("[SSE请求] 请求失败，HTTP状态码: {} (可重试)，开始第{}/{}次重试, URL: {}",
                            e.getHttpCode(), i, retryCount, url, e);
                    // 可重试的HTTP错误，继续重试
                    if (i < retryCount) {
                        // 先检查是否被中断
                        if (checkInterrupted(url, listener, controller, i, e.getHttpCode())) {
                            return e;
                        }
                        // 确认将重试，再通知监听器
                        notifyRetry(listener, i, retryInterval);
                        // 等待重试间隔
                        if (sleepWithInterruptCheck(url, retryInterval, listener, controller, i, e.getHttpCode())) {
                            return e; // 被中断，停止重试
                        }
                    }
                } else {
                    // 不可重试的HTTP错误，停止重试
                    log.error("[SSE请求] 请求失败，HTTP状态码: {} (不可重试)，停止重试, URL: {}", 
                            e.getHttpCode(), url, e);
                    handleErrorWithState(listener, controller, i, e.getHttpCode(),
                            "请求失败: " + e.getMessage(), e, SSEConnectionState.CONNECTING);
                    // 错误已经处理，返回null避免上层重复处理
                    return null;
                }
            } catch (IOException e) {
                lastException = e;
                log.warn("[SSE请求] 请求失败，开始第{}/{}次重试, URL: {}", i, retryCount, url, e);
                // 如果还有重试机会，则等待后继续（使用固定间隔）
                if (i < retryCount) {
                    // 先检查是否被中断
                    if (checkInterrupted(url, listener, controller, i, SSEErrorCode.RETRY_INTERRUPTED)) {
                        return e;
                    }
                    // 确认将重试，再通知监听器
                    notifyRetry(listener, i, retryInterval);
                    // 等待重试间隔
                    if (sleepWithInterruptCheck(url, retryInterval, listener, controller, i, SSEErrorCode.RETRY_INTERRUPTED)) {
                        return e; // 被中断，停止重试
                    }
                }
            }
        }
        
        return lastException;
    }
    
    /**
     * 检查线程中断状态
     * 
     * @param url 请求URL
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param attempt 当前尝试次数
     * @param errorCode 错误码
     * @param <T> SSE 数据类型
     * @return 如果被中断返回true，否则返回false
     */
    private <T> boolean checkInterrupted(String url, SSEEventListener<T> listener, SSEController controller, 
                                        int attempt, int errorCode) {
        if (Thread.currentThread().isInterrupted()) {
            log.warn("[SSE请求] 重试线程被中断，URL: {}", url);
            Thread.currentThread().interrupt();
            handleErrorWithState(listener, controller, attempt, errorCode, "重试被中断", 
                    new InterruptedException(), SSEConnectionState.CONNECTING);
            return true;
        }
        return false;
    }
    
    /**
     * 休眠并检查中断状态
     * 
     * @param url 请求URL
     * @param retryInterval 重试间隔
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param attempt 当前尝试次数
     * @param errorCode 错误码
     * @param <T> SSE 数据类型
     * @return 如果被中断返回true，否则返回false
     */
    private <T> boolean sleepWithInterruptCheck(String url, long retryInterval, 
                                            SSEEventListener<T> listener, SSEController controller, 
                                            int attempt, int errorCode) {
        try {
            Thread.sleep(retryInterval);
            return checkInterrupted(url, listener, controller, attempt, errorCode);
        } catch (InterruptedException e1) {
            log.warn("[SSE请求] 重试线程被中断，URL: {}", url, e1);
            Thread.currentThread().interrupt();
            handleErrorWithState(listener, controller, attempt, errorCode, "重试被中断: " + e1.getMessage(), 
                    e1, SSEConnectionState.CONNECTING);
            return true;
        }
    }
    
    /**
     * 处理错误并更新状态为FAILED
     * 
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param attempt 当前尝试次数
     * @param errorCode 错误码
     * @param message 错误消息
     * @param throwable 异常对象
     * @param oldState 旧状态（如果为null则使用当前状态）
     * @param <T> SSE 数据类型
     */
    private <T> void handleErrorWithState(SSEEventListener<T> listener, SSEController controller, 
                                         int attempt, int errorCode, String message, Throwable throwable,
                                         SSEConnectionState oldState) {
        if (oldState != null) {
            transitionState(listener, controller, oldState, SSEConnectionState.FAILED);
        } else {
            SSEConnectionState currentState = controller.getConnectionState();
            controller.setConnectionState(SSEConnectionState.FAILED);
            notifyStateChanged(listener, controller, currentState, SSEConnectionState.FAILED);
        }
        listener.onError(attempt, errorCode, message, throwable);
    }
    
    /**
     * 转换连接状态
     * 
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param newState 新状态
     * @param <T> SSE 数据类型
     */
    private <T> void transitionState(SSEEventListener<T> listener, SSEController controller, 
                                     SSEConnectionState newState) {
        SSEConnectionState oldState = controller.getConnectionState();
        controller.setConnectionState(newState);
        notifyStateChanged(listener, controller, oldState, newState);
    }
    
    /**
     * 转换连接状态（指定旧状态）
     * 
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param oldState 旧状态
     * @param newState 新状态
     * @param <T> SSE 数据类型
     */
    private <T> void transitionState(SSEEventListener<T> listener, SSEController controller, 
                                     SSEConnectionState oldState, SSEConnectionState newState) {
        controller.setConnectionState(newState);
        notifyStateChanged(listener, controller, oldState, newState);
    }
    
    /**
     * 通知状态变化
     * 
     * @param listener SSE 事件监听器
     * @param controller SSE 控制器
     * @param oldState 旧状态
     * @param newState 新状态
     * @param <T> SSE 数据类型
     */
    private <T> void notifyStateChanged(SSEEventListener<T> listener, SSEController controller,
                                        SSEConnectionState oldState, SSEConnectionState newState) {
        try {
            listener.onStateChanged(controller, oldState, newState);
        } catch (Exception e) {
            log.warn("[SSE] 状态变化回调异常", e);
        }
    }

    /**
     * 通知重试事件
     *
     * @param listener SSE 事件监听器
     * @param attempt 当前重试次数
     * @param delayMillis 重试延迟时间（毫秒）
     * @param <T> SSE 数据类型
     */
    private <T> void notifyRetry(SSEEventListener<T> listener, int attempt, long delayMillis) {
        try {
            listener.onRetry(attempt, delayMillis);
        } catch (Exception e) {
            log.warn("[SSE] 重试回调异常", e);
        }
    }

    /**
     * 执行 SSE 请求
     * 
     * <p>SSE是长连接流式响应，需要将读取超时设置为无限，避免读取过程中超时中断。</p>
     * 
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param attempt 当前尝试次数
     * @param controller SSE 控制器
     * @param isReconnect 是否为重连请求
     * @param <T> SSE 数据类型
     * @throws IOException 网络异常（可重试的异常）
     */
    private <T> void executeSseRequest(OkHttpClient okHttpClient, Request.Builder request,
                                      SSEEventListener<T> listener, DataDecoder<T> decoder, int attempt,
                                      SSEController controller, boolean isReconnect) throws IOException {
        // 如果是重连，添加Last-Event-ID请求头
        if (isReconnect) {
            String lastEventId = controller.getLastEventId();
            if (lastEventId != null && !lastEventId.trim().isEmpty()) {
                request.addHeader("Last-Event-ID", lastEventId);
                log.info("[SSE重连] 添加Last-Event-ID请求头: {}", lastEventId);
            }
            // 更新连接状态为RECONNECTING
            SSEConnectionState oldState = controller.getConnectionState();
            controller.setConnectionState(SSEConnectionState.RECONNECTING);
            notifyStateChanged(listener, controller, oldState, SSEConnectionState.RECONNECTING);
        }
        // 获取或创建SSE专用客户端（读取超时设置为无限）
        OkHttpClient sseClient = getOrCreateSseClient(okHttpClient);
        
        Response response = null;
        ResponseBody body = null;
        try {
            response = sseClient.newCall(request.build()).execute();
            body = response.body();
            
            // 检查响应状态
            if (!response.isSuccessful()) {
                int httpCode = response.code();
                String errorBody = body != null ? body.string() : "无响应体";
                // body.string() 已经关闭了body，设置为null避免重复关闭
                body = null;
                closeResponse(response, body);
                
                // 判断HTTP错误是否可重试
                boolean retryable = SSEReconnectManager.isRetryableHttpError(httpCode);
                
                // 如果是重连请求，重置重连状态以便可以继续重连
                if (isReconnect) {
                    controller.setReconnecting(false);
                }
                
                // 不在这里调用onError，让上层统一处理，避免重复调用
                // 使用自定义异常类，明确标识是否可重试
                throw new SSEHttpException(httpCode, "HTTP请求失败: " + httpCode + " - " + errorBody, retryable);
            }
            
            if (body == null) {
                closeResponse(response, null);
                // 如果是重连请求，重置重连状态以便可以继续重连
                if (isReconnect) {
                    controller.setReconnecting(false);
                }
                // 不在这里调用onError，让上层统一处理，避免重复调用
                // 响应体为空通常不应该重连，抛出IOException让调用方决定
                throw new IOException("响应体为空");
            }
            
            // 检查 Content-Type，判断是否为 SSE 流（使用更严格的检查）
            String contentType = response.header("Content-Type");
            boolean isSseStream = isSseContentType(contentType);
            
            if (isSseStream) {
                // 处理 SSE 流（传递attempt和controller参数）
                handleSseStream(response, body, listener, decoder, attempt, controller, isReconnect);
            } else {
                // 处理非 SSE 响应（使用try-finally确保资源关闭）
                String responseBody = null;
                try {
                    responseBody = body.string();
                    // body.string() 已经关闭了body，设置为null避免重复关闭
                    body = null;
                    // 如果是重连请求，重置重连状态
                    if (isReconnect) {
                        controller.setReconnecting(false);
                    }
                    // 更新连接状态为CONNECTED（因为成功建立了连接，只是响应不是SSE格式）
                    SSEConnectionState currentState = controller.getConnectionState();
                    if (currentState != SSEConnectionState.CONNECTED) {
                        transitionState(listener, controller, SSEConnectionState.CONNECTED);
                    }
                    listener.onOpen();
                    listener.onNonSseResponse(responseBody, contentType);
                    // 非SSE响应处理完成，更新状态为CLOSED
                    controller.setConnectionState(SSEConnectionState.CLOSING);
                    notifyStateChanged(listener, controller, SSEConnectionState.CONNECTED, SSEConnectionState.CLOSING);
                    listener.onComplete();
                    controller.setConnectionState(SSEConnectionState.CLOSED);
                    notifyStateChanged(listener, controller, SSEConnectionState.CLOSING, SSEConnectionState.CLOSED);
                } catch (Exception e) {
                    // 监听器回调异常不应该触发重试
                    log.error("[SSE请求] 监听器回调异常", e);
                    // 如果是重连请求，重置重连状态
                    if (isReconnect) {
                        controller.setReconnecting(false);
                    }
                    listener.onError(attempt, SSEErrorCode.LISTENER_CALLBACK_ERROR, "监听器回调异常: " + e.getMessage(), e);
                    // 不抛出异常，避免触发重试
                } finally {
                    closeResponse(response, body);
                }
            }
        } catch (IOException e) {
            closeResponse(response, body);
            throw e; // 重新抛出，让重试逻辑处理（网络异常可重试）
        } catch (Exception e) {
            closeResponse(response, body);
            // 非IOException异常（如监听器回调异常）不应该触发重试
            log.error("[SSE请求] 处理异常（不可重试）", e);
            listener.onError(attempt, SSEErrorCode.PROCESS_ERROR, "处理异常: " + e.getMessage(), e);
            // 包装为IOException但不应该被重试，这里抛出是为了终止执行
            throw new IOException("SSE处理异常（不可重试）", e);
        }
    }
    
    /**
     * 获取或创建SSE专用客户端
     * 如果原客户端已经配置了无限读取超时，则复用；否则创建新的客户端
     * 
     * @param okHttpClient 原始OkHttp客户端
     * @return SSE专用客户端
     */
    private OkHttpClient getOrCreateSseClient(OkHttpClient okHttpClient) {
        // 检查原客户端是否已经配置了无限读取超时
        if (okHttpClient.readTimeoutMillis() == 0) {
            return okHttpClient; // 复用原客户端
        }
        
        // 尝试从缓存获取
        OkHttpClient cached = cachedSseClient.get();
        if (cached != null && cached.readTimeoutMillis() == 0) {
            return cached;
        }
        
        // 创建新的SSE客户端并缓存
        OkHttpClient sseClient = okHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
        
        // 尝试缓存（如果失败也不影响功能）
        cachedSseClient.compareAndSet(null, sseClient);
        
        return sseClient;
    }
    
    /**
     * 检查Content-Type是否为SSE流
     * 使用严格的检查：先按分号分割，再检查主类型
     * 
     * @param contentType Content-Type头值
     * @return 是否为SSE流
     */
    private boolean isSseContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        // 按分号分割，获取主类型
        String mainType = contentType.split(";")[0].trim().toLowerCase();
        return SSE_CONTENT_TYPE.equals(mainType);
    }

    /**
     * 处理 SSE 流响应
     * 按照 SSE 协议解析流式数据，逐帧分发事件
     * 
     * @param response HTTP 响应对象
     * @param body 响应体对象
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param attempt 当前尝试次数（用于错误回调）
     * @param controller SSE 控制器
     * @param isReconnect 是否为重连请求
     * @param <T> SSE 数据类型
     */
    private <T> void handleSseStream(Response response, ResponseBody body, 
                                     SSEEventListener<T> listener, DataDecoder<T> decoder, int attempt,
                                     SSEController controller, boolean isReconnect) {
        // 更新连接状态为CONNECTED
        SSEConnectionState oldState = controller.getConnectionState();
        controller.setConnectionState(SSEConnectionState.CONNECTED);
        notifyStateChanged(listener, controller, oldState, SSEConnectionState.CONNECTED);
        
        // 如果是重连成功，重置重连状态和计数器
        if (isReconnect) {
            controller.setReconnecting(false);
            controller.resetReconnectAttempt();
            log.info("[SSE重连] 重连成功，已重置重连状态和计数器");
        }
        
        // 创建流控制器，用于支持用户主动终止
        SSEStreamController streamController = SSEStreamController.create();
        boolean finished = false;
        boolean interrupted = false;
        try (BufferedSource source = body.source()) {
            listener.onOpen();
            
            String id = null;
            String event = null;
            StringBuilder dataBuilder = new StringBuilder();
            Long retry = null;

            while (true) {
                // 检查线程中断状态
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("[SSE流] 读取线程被中断");
                    interrupted = true;
                    break;
                }
                
                // 每次读取一行后检查终止标志
                if (streamController.isTerminated()) {
                    interrupted = true;
                    break;
                }
                
                String line = source.readUtf8Line();
                if (line == null) {
                    // 流结束
                    break;
                }
                
                // 读取完当前行后再次检查终止标志和线程中断状态
                if (Thread.currentThread().isInterrupted() || streamController.isTerminated()) {
                    interrupted = true;
                    break;
                }
                
                if (line.isEmpty()) {
                    // 空行表示一个完整事件帧的结束
                    EventMessage<T> msg = buildEventMessage(id, event, dataBuilder, retry, streamController, decoder);
                    
                    // 记录Last-Event-ID（如果事件有id字段）
                    if (id != null && !id.trim().isEmpty()) {
                        controller.updateLastEventId(id);
                    }
                    
                    // 判断是否终止（通过 shouldTerminate 方法）
                    boolean terminate;
                    try {
                        terminate = listener.shouldTerminate(msg);
                    } catch (Exception e) {
                        log.error("[SSE流] shouldTerminate判断异常", e);
                        terminate = false;
                    }
                    
                    if (terminate) {
                        listener.onComplete();
                        finished = true;
                        break;
                    }
                    
                    // 检查用户是否主动终止（通过 msg.terminate() 调用）
                    if (streamController.isTerminated()) {
                        interrupted = true;
                        break;
                    }
                    
                    // 分发事件
                    try {
                        listener.onEvent(msg);
                    } catch (Exception e) {
                        log.error("[SSE流] onEvent回调处理异常", e);
                    }
                    
                    // 检查用户是否在 onEvent 中调用了 terminate()
                    if (streamController.isTerminated()) {
                        interrupted = true;
                        break;
                    }
                    
                    // 重置状态，准备下一个事件帧
                    id = null;
                    event = null;
                    dataBuilder.setLength(0);
                    retry = null;
                    continue;
                }
                
                // 跳过注释行（以冒号开头）
                if (line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }
                
                // 解析字段：field: value（使用更健壮的解析）
                SseFieldParseResult result = parseSseField(line);
                if (result != null) {
                    switch (result.getField()) {
                        case FIELD_ID:
                            id = result.getValue();
                            break;
                        case FIELD_EVENT:
                            event = result.getValue();
                            break;
                        case FIELD_DATA:
                            // 支持多行 data，用换行符连接
                            dataBuilder.append(result.getValue()).append(NEWLINE);
                            break;
                        case FIELD_RETRY:
                            try {
                                retry = Long.parseLong(result.getValue().trim());
                            } catch (NumberFormatException e) {
                                log.warn("[SSE流] retry字段格式错误，值: {}", result.getValue());
                                retry = null;
                            }
                            break;
                        default:
                            // 忽略未知字段
                            break;
                    }
                }
            }

            // 根据结束方式调用不同的回调
            SSEConnectionState currentState = controller.getConnectionState();
            if (interrupted) {
                // 用户主动终止，调用 onInterrupt
                controller.setConnectionState(SSEConnectionState.CLOSING);
                notifyStateChanged(listener, controller, currentState, SSEConnectionState.CLOSING);
                listener.onInterrupt();
                controller.setConnectionState(SSEConnectionState.CLOSED);
                notifyStateChanged(listener, controller, SSEConnectionState.CLOSING, SSEConnectionState.CLOSED);
            } else if (finished) {
                // 通过 shouldTerminate 正常结束，onComplete 已经在 shouldTerminate 返回 true 时调用过了
                // 只需要更新状态为 CLOSED
                controller.setConnectionState(SSEConnectionState.CLOSING);
                notifyStateChanged(listener, controller, currentState, SSEConnectionState.CLOSING);
                controller.setConnectionState(SSEConnectionState.CLOSED);
                notifyStateChanged(listener, controller, SSEConnectionState.CLOSING, SSEConnectionState.CLOSED);
            } else {
                // 流正常结束（服务器关闭连接），调用 onComplete
                controller.setConnectionState(SSEConnectionState.CLOSING);
                notifyStateChanged(listener, controller, currentState, SSEConnectionState.CLOSING);
                listener.onComplete();
                controller.setConnectionState(SSEConnectionState.CLOSED);
                notifyStateChanged(listener, controller, SSEConnectionState.CLOSING, SSEConnectionState.CLOSED);
            }
        } catch (IOException e) {
            // 网络异常，可能是连接断开，尝试自动重连
            // 注意：EOFException 通常是服务器主动关闭连接导致的，这是正常情况，使用 WARN 级别
            if (e instanceof java.io.EOFException) {
                log.warn("[SSE流] 连接断开（可能是服务器主动关闭），将尝试自动重连: {}", e.getMessage());
            } else {
                log.error("[SSE流] 读取异常（可能是连接断开）", e);
            }
            
            SSEConnectionState currentState = controller.getConnectionState();
            // 检查是否为用户主动终止
            if (streamController.isTerminated() || controller.isDestroyed()) {
                // 用户主动终止，不重连
                controller.setConnectionState(SSEConnectionState.CLOSED);
                notifyStateChanged(listener, controller, currentState, SSEConnectionState.CLOSED);
                listener.onError(attempt, SSEErrorCode.USER_TERMINATED, "SSE流被用户终止", e);
            } else {
                // 连接意外断开，尝试自动重连
                // 检查是否还有重连机会
                if (controller.isReconnectExhausted() || controller.getMaxReconnectCount() <= 0) {
                    // 已达到最大重连次数或未配置重连，不再重连
                    controller.setConnectionState(SSEConnectionState.FAILED);
                    notifyStateChanged(listener, controller, currentState, SSEConnectionState.FAILED);
                    listener.onError(attempt, SSEErrorCode.RECONNECT_EXHAUSTED, "SSE流处理异常: " + e.getMessage(), e);
                } else {
                    // 还有重连机会，计算延迟并通知监听器
                    int nextAttempt = controller.getReconnectAttempt() + 1;
                    long delay = SSEReconnectManager.calculateDelay(nextAttempt, controller.getReconnectInterval());
                    notifyRetry(listener, nextAttempt, delay);
                    if (controller.triggerReconnect("流读取异常: " + e.getMessage())) {
                        // 重连已触发，状态会在重连逻辑中更新
                        log.info("[SSE流] 连接断开，已触发自动重连");
                    } else {
                        // 无法重连（其他原因，如控制器已销毁）
                        controller.setConnectionState(SSEConnectionState.FAILED);
                        notifyStateChanged(listener, controller, currentState, SSEConnectionState.FAILED);
                        listener.onError(attempt, SSEErrorCode.NETWORK_ERROR, "SSE流处理异常: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            // 其他异常，不重连
            log.error("[SSE流] 处理异常", e);
            SSEConnectionState currentState = controller.getConnectionState();
            controller.setConnectionState(SSEConnectionState.FAILED);
            notifyStateChanged(listener, controller, currentState, SSEConnectionState.FAILED);
            listener.onError(attempt, SSEErrorCode.PROCESS_ERROR, "SSE流处理异常: " + e.getMessage(), e);
        } finally {
            // 确保响应资源被关闭
            closeResponse(response, body);
        }
    }
    
    /**
     * 构建SSE事件消息
     * 
     * @param id 消息ID
     * @param event 事件类型
     * @param dataBuilder 数据构建器
     * @param retry 重试间隔
     * @param controller 流控制器
     * @param decoder 数据解码器
     * @param <T> 数据类型
     * @return 事件消息对象
     */
    private <T> EventMessage<T> buildEventMessage(String id, String event, StringBuilder dataBuilder, 
                                                   Long retry, SSEStreamController controller, 
                                                   DataDecoder<T> decoder) {
        EventMessage<T> msg = new EventMessage<>();
        msg.setId(id);
        msg.setEvent(event);
        
        // 处理多行data：根据SSE规范，多个data行应该用单个换行符连接
        // 移除最后一个换行符（这是我们在append时添加的）
        String dataRaw = dataBuilder.length() > 0 ? dataBuilder.toString() : null;
        if (dataRaw != null && dataRaw.endsWith(NEWLINE)) {
            dataRaw = dataRaw.substring(0, dataRaw.length() - NEWLINE.length());
        }
        msg.setDataRaw(dataRaw);
        
        // 使用 decoder 解码数据
        T parsed = null;
        if (dataRaw != null) {
            try {
                parsed = decoder.decode(dataRaw);
            } catch (Exception e) {
                log.error("[SSE流] 数据解码失败: {}", e.getMessage(), e);
                // 解码失败时保持 parsed 为 null
            }
        }
        msg.setData(parsed);
        msg.setRetry(retry);
        // 设置控制器引用，允许用户通过 msg.terminate() 主动终止
        msg.setController(controller);
        
        return msg;
    }
    
    /**
     * 解析SSE字段行
     * 按照SSE规范：字段名大小写不敏感，字段值前可以有多个空格
     * 
     * @param line 字段行
     * @return 解析结果，如果解析失败返回null
     */
    private SseFieldParseResult parseSseField(String line) {
        int idx = line.indexOf(":");
        if (idx <= 0) {
            // 没有冒号或冒号在开头，忽略
            return null;
        }
        
        // 提取字段名（去除前后空格，转换为小写）
        String field = line.substring(0, idx).trim().toLowerCase();
        
        // 验证字段名格式（不应该包含空格）
        if (field.contains(" ")) {
            // 字段名包含空格，不符合规范，忽略
            return null;
        }
        
        // 提取字段值（去除前导空格，SSE规范允许字段值前有多个空格）
        String value = line.substring(idx + 1);
        if (value.startsWith(" ")) {
            // 移除所有前导空格
            value = value.replaceFirst("^ +", "");
        }
        
        return new SseFieldParseResult(field, value);
    }
    
    /**
     * SSE字段解析结果
     */
    private static class SseFieldParseResult {
        private final String field;
        private final String value;
        
        public SseFieldParseResult(String field, String value) {
            this.field = field;
            this.value = value;
        }
        
        public String getField() {
            return field;
        }
        
        public String getValue() {
            return value;
        }
    }

    /**
     * 安全关闭响应资源
     * 
     * @param response HTTP响应对象
     * @param body 响应体对象
     */
    private void closeResponse(Response response, ResponseBody body) {
        if (body != null) {
            try {
                body.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
        if (response != null) {
            try {
                response.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
    }
}

