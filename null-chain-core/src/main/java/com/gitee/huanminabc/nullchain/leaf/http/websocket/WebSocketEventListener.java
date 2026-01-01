package com.gitee.huanminabc.nullchain.leaf.http.websocket;

/**
 * WebSocket 事件监听器接口
 * 
 * <p>定义了 WebSocket 连接处理过程中的各种事件回调方法，包括连接建立、消息接收、
 * 错误处理、连接关闭等。所有方法都接收 WebSocketController 作为第一个参数，
 * 方便在任何回调中发送消息。</p>
 * 
 * <h3>生命周期：</h3>
 * <ol>
 *   <li>连接建立后调用一次 {@link #onOpen(WebSocketController)}</li>
 *   <li>接收到消息时调用 {@link #onMessage(WebSocketController, String)} 或 {@link #onMessage(WebSocketController, byte[])}</li>
 *   <li>发生错误时调用 {@link #onError(WebSocketController, Throwable, String)}</li>
 *   <li>连接正在关闭时调用 {@link #onClosing(WebSocketController, int, String)}（收到关闭帧）</li>
 *   <li>连接关闭时调用 {@link #onClose(WebSocketController, int, String)}（连接完全关闭后）</li>
 * </ol>
 * 
 * <h3>线程与性能：</h3>
 * <p>当前实现在执行请求的线程内同步回调，监听方法应尽量轻量，避免长时间阻塞。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public interface WebSocketEventListener {
    /**
     * 连接建立后触发（WebSocket 握手成功）。仅调用一次。
     * 
     * @param controller WebSocket 控制器，可用于发送消息
     */
    void onOpen(WebSocketController controller);
    
    /**
     * 接收到文本消息时触发
     * 
     * @param controller WebSocket 控制器，可用于发送消息
     * @param text 接收到的文本消息内容
     */
    void onMessage(WebSocketController controller, String text);
    
    /**
     * 接收到二进制消息时触发
     * 
     * @param controller WebSocket 控制器，可用于发送消息
     * @param bytes 接收到的二进制消息内容
     */
    void onMessage(WebSocketController controller, byte[] bytes);
    
    /**
     * 发生错误时触发：网络错误、协议错误等
     * 
     * @param controller WebSocket 控制器，可用于发送消息
     * @param t 相关异常
     * @param message 错误信息
     */
    void onError(WebSocketController controller, Throwable t, String message);
    
    /**
     * 连接正在关闭时触发（收到关闭帧）
     * 
     * <p>当收到对端的关闭帧时，会先触发此方法，此时连接正在关闭过程中。
     * 可以在此方法中进行一些清理工作，但此时连接可能仍可发送消息。</p>
     * 
     * <p>默认实现为空，子类可以重写此方法以处理关闭中的事件。</p>
     * 
     * @param controller WebSocket 控制器
     * @param code 关闭状态码
     * @param reason 关闭原因
     */
    default void onClosing(WebSocketController controller, int code, String reason) {
        // 默认实现为空
    }
    
    /**
     * 连接关闭时触发（连接完全关闭后）
     * 
     * <p>当连接完全关闭后触发此方法，此时连接已不可用。</p>
     * 
     * @param controller WebSocket 控制器
     * @param code 关闭状态码
     * @param reason 关闭原因
     */
    void onClose(WebSocketController controller, int code, String reason);
}

