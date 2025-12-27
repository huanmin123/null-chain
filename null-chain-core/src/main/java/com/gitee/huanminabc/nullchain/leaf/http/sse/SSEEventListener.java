package com.gitee.huanminabc.nullchain.leaf.http.sse;

/**
 * SSE 事件监听器接口
 * 
 * <p>定义了 SSE 流处理过程中的各种事件回调方法，包括连接建立、事件接收、
 * 错误处理、完成通知等。同时提供非 SSE 响应的处理回调。</p>
 * 
 * <h3>生命周期：</h3>
 * <ol>
 *   <li>连接建立后调用一次 {@link #onOpen()}</li>
 *   <li>如果是 SSE 流：每当解析到一个完整的 SSE 帧，先调用 {@link #shouldTerminate(EventMessage)} 判断是否终止；
 *       若未终止，再调用 {@link #onEvent(EventMessage)} 进行消息消费</li>
 *   <li>如果是非 SSE 响应：调用 {@link #onNonSseResponse(String, String)} 处理响应内容</li>
 *   <li>当会话终止或自然断流结束时，调用一次 {@link #onComplete()}</li>
 *   <li>当用户通过 {@link EventMessage#terminate()} 主动终止流时，调用一次 {@link #onInterrupt()}</li>
 *   <li>发生错误时调用 {@link #onError(int, Integer, String, Throwable)}</li>
 * </ol>
 * 
 * <h3>线程与性能：</h3>
 * <p>当前实现在执行请求的线程内同步回调，监听方法应尽量轻量，避免长时间阻塞。</p>
 * 
 * @param <T> SSE 数据类型，由 DataDecoder 解码得到
 * @author huanmin
 * @since 1.0.0
 */
public interface SSEEventListener<T> {
    /**
     * 连接建立后触发（HTTP 握手成功并开始读取）。仅调用一次。
     */
    void onOpen();
    
    /**
     * 消费一帧完整的 SSE 消息。仅在 {@link #shouldTerminate(EventMessage)} 返回 false 时调用。
     * 注意：此方法仅在响应为 SSE 流时调用，非 SSE 响应不会调用此方法。
     * 
     * @param msg 帧级消息，包含 id/event/retry，以及 dataRaw 原始文本与 data 泛型对象
     */
    void onEvent(EventMessage<T> msg);
    
    /**
     * 处理非 SSE 响应
     * 
     * <p>当 HTTP 响应的 Content-Type 不是 text/event-stream 时，会调用此方法。
     * 这避免了将非 SSE 响应通过 onEvent 发送导致的混淆。</p>
     * 
     * @param responseBody 响应体内容
     * @param contentType 响应 Content-Type
     */
    void onNonSseResponse(String responseBody, String contentType);
    
    /**
     * 发生错误时触发：非 2xx 响应、网络错误或读取异常。
     * 
     * @param attempt 当次错误发生的序号（与重试相关）
     * @param httpCode HTTP 状态码，网络异常等情况可能为 null
     * @param message 错误信息
     * @param t 相关异常，可能为 null
     */
    void onError(int attempt, Integer httpCode, String message, Throwable t);
    
    /**
     * 会话结束时触发：自定义终止或自然断流。仅调用一次。
     */
    void onComplete();
    
    /**
     * 流被用户主动终止时触发
     * 
     * <p>当用户通过 {@link EventMessage#terminate()} 方法主动终止流时，会调用此方法。
     * 与 {@link #onComplete()} 的区别是：onInterrupt() 表示用户主动中断，onComplete() 表示流自然结束。</p>
     * 
     * <p>此方法在流停止读取后、资源关闭前调用，仅调用一次。</p>
     * 
     * <p>默认实现为空，子类可以重写此方法以处理中断事件。</p>
     * 
     * @example
     * <pre>{@code
     * public void onInterrupt() {
     *     // 流被用户主动终止时的回调
     *     System.out.println("SSE 流已被用户终止");
     *     // 可以在这里进行清理工作
     * }
     * }</pre>
     */
    default void onInterrupt() {
        // 默认实现为空，子类可以重写
    }
    
    /**
     * 在分发 {@link #onEvent(EventMessage)} 之前进行终止判断。
     * 返回 true 表示应结束会话：立即调用 {@link #onComplete()}，并不再分发当前帧的 onEvent。
     * 
     * @param msg 当前帧消息
     * @return 是否终止
     */
    default boolean shouldTerminate(EventMessage<T> msg) { 
        return false; 
    }
}

