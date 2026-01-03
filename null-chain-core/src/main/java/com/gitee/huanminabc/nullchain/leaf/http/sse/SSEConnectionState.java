package com.gitee.huanminabc.nullchain.leaf.http.sse;

/**
 * SSE 连接状态枚举
 * 
 * <p>定义 SSE 连接的各种状态，用于统一管理连接状态。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public enum SSEConnectionState {
    /** 初始状态，未连接 */
    INITIAL,
    
    /** 正在连接中 */
    CONNECTING,
    
    /** 已连接 */
    CONNECTED,
    
    /** 正在重连中 */
    RECONNECTING,
    
    /** 正在关闭中 */
    CLOSING,
    
    /** 已关闭 */
    CLOSED,
    
    /** 连接失败 */
    FAILED
}



