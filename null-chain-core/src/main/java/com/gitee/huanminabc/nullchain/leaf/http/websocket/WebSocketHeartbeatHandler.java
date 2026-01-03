package com.gitee.huanminabc.nullchain.leaf.http.websocket;

/**
 * WebSocket 心跳处理器接口
 * 
 * <p>定义心跳消息的生成和回复判断逻辑，由用户实现自定义的心跳格式。
 * 支持文本和二进制两种格式的心跳消息。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>生成心跳消息：系统会定期调用 {@link #generateHeartbeat()} 生成心跳消息并发送</li>
 *   <li>判断心跳回复：系统在收到消息时调用 {@link #isHeartbeatResponse(String)} 或
 *       {@link #isHeartbeatResponse(byte[])} 判断是否为心跳回复</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public interface WebSocketHeartbeatHandler {
    /**
     * 生成心跳消息（文本格式）
     * 
     * <p>系统会定期调用此方法生成心跳消息并发送给服务器。</p>
     * 
     * @return 心跳消息文本内容
     */
    String generateHeartbeat();
    
    /**
     * 生成心跳消息（二进制格式）
     * 
     * <p>如果使用二进制格式的心跳，实现此方法；如果只使用文本格式，返回 null。</p>
     * 
     * @return 心跳消息二进制内容，如果使用文本格式返回 null
     */
    default byte[] generateHeartbeatBytes() {
        return null;
    }
    
    /**
     * 判断文本消息是否为心跳回复
     * 
     * <p>系统在收到文本消息时会调用此方法判断是否为心跳回复。
     * 如果是心跳回复，系统会更新最后回复时间，用于超时检测。</p>
     * 
     * @param text 接收到的文本消息
     * @return 如果是心跳回复返回 true，否则返回 false
     */
    boolean isHeartbeatResponse(String text);
    
    /**
     * 判断二进制消息是否为心跳回复
     * 
     * <p>系统在收到二进制消息时会调用此方法判断是否为心跳回复。
     * 如果是心跳回复，系统会更新最后回复时间，用于超时检测。</p>
     * 
     * @param bytes 接收到的二进制消息
     * @return 如果是心跳回复返回 true，否则返回 false
     */
    default boolean isHeartbeatResponse(byte[] bytes) {
        return false;
    }
}



