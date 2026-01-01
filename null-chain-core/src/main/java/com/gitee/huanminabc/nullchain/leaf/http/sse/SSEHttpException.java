package com.gitee.huanminabc.nullchain.leaf.http.sse;

import java.io.IOException;

/**
 * SSE HTTP 异常
 * 
 * <p>用于区分可重试和不可重试的HTTP错误，避免通过错误消息文字判断。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public class SSEHttpException extends IOException {
    
    /** HTTP状态码 */
    private final int httpCode;
    
    /** 是否可重试 */
    private final boolean retryable;
    
    /**
     * 创建SSE HTTP异常
     * 
     * @param httpCode HTTP状态码
     * @param message 错误消息
     * @param retryable 是否可重试
     */
    public SSEHttpException(int httpCode, String message, boolean retryable) {
        super(message);
        this.httpCode = httpCode;
        this.retryable = retryable;
    }
    
    /**
     * 创建SSE HTTP异常
     * 
     * @param httpCode HTTP状态码
     * @param message 错误消息
     * @param cause 原因异常
     * @param retryable 是否可重试
     */
    public SSEHttpException(int httpCode, String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.httpCode = httpCode;
        this.retryable = retryable;
    }
    
    /**
     * 获取HTTP状态码
     * 
     * @return HTTP状态码
     */
    public int getHttpCode() {
        return httpCode;
    }
    
    /**
     * 判断是否可重试
     * 
     * @return 如果可重试返回 true，否则返回 false
     */
    public boolean isRetryable() {
        return retryable;
    }
}

