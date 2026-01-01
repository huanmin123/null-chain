package com.gitee.huanminabc.nullchain.leaf.http.websocket;

import lombok.Getter;

/**
 * WebSocket 消息封装类
 * 
 * <p>用于封装待发送的 WebSocket 消息，支持文本和二进制两种类型。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Getter
public class WebSocketMessage {
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /** 文本消息 */
        TEXT,
        /** 二进制消息 */
        BINARY
    }
    
    /** 消息类型
     * -- GETTER --
     *  获取消息类型
     *
     * @return 消息类型
     */
    private final MessageType type;
    
    /** 文本消息内容（文本消息时使用）
     * -- GETTER --
     *  获取文本内容（仅文本消息时有效）
     *
     * @return 文本内容
     */
    private final String text;
    
    /** 二进制消息内容（二进制消息时使用）
     * -- GETTER --
     *  获取二进制内容（仅二进制消息时有效）
     *
     * @return 二进制内容
     */
    private final byte[] bytes;
    
    /** 重试次数
     * -- GETTER --
     *  获取重试次数
     *
     * @return 重试次数
     */
    private volatile int retryCount = 0;
    
    /** 最大重试次数（默认3次） */
    public static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 创建文本消息
     * 
     * @param text 文本内容
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage text(String text) {
        return new WebSocketMessage(MessageType.TEXT, text, null);
    }
    
    /**
     * 创建二进制消息
     * 
     * @param bytes 二进制内容
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage binary(byte[] bytes) {
        return new WebSocketMessage(MessageType.BINARY, null, bytes);
    }
    
    /**
     * 私有构造函数
     * 
     * @param type 消息类型
     * @param text 文本内容
     * @param bytes 二进制内容
     */
    private WebSocketMessage(MessageType type, String text, byte[] bytes) {
        this.type = type;
        this.text = text;
        this.bytes = bytes;
    }

    /**
     * 增加重试次数
     * 
     * @return 增加后的重试次数
     */
    public int incrementRetryCount() {
        return ++retryCount;
    }

    /**
     * 检查是否超过最大重试次数
     * 
     * @return 如果超过最大重试次数返回 true，否则返回 false
     */
    public boolean isRetryExhausted() {
        return retryCount >= MAX_RETRY_COUNT;
    }
    
    /**
     * 获取最大重试次数
     * 
     * @return 最大重试次数
     */
    public static int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }
}

