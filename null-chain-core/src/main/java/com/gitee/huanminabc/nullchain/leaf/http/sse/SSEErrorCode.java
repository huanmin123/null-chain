package com.gitee.huanminabc.nullchain.leaf.http.sse;

/**
 * SSE 错误码常量
 * 
 * <p>定义 SSE 错误码，用于区分不同类型的错误：
 * <ul>
 *   <li>HTTP 错误码：使用标准 HTTP 状态码（100-599）</li>
 *   <li>系统错误码：使用负数（-1 到 -999）</li>
 * </ul>
 * </p>
 * 
 * <p>错误码规则：
 * <ul>
 *   <li>100-599：HTTP 状态码（标准 HTTP 响应码）</li>
 *   <li>-1：网络异常（连接失败、超时等）</li>
 *   <li>-2：处理异常（解码失败、解析错误等）</li>
 *   <li>-3：用户终止（用户主动终止流）</li>
 *   <li>-4：重连次数已达上限</li>
 *   <li>-5：重试被中断</li>
 *   <li>-6：监听器回调异常</li>
 *   <li>-7：未知错误</li>
 * </ul>
 * </p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public final class SSEErrorCode {
    
    private SSEErrorCode() {
        // 工具类，禁止实例化
    }
    
    /** 网络异常（连接失败、超时、IO异常等） */
    public static final int NETWORK_ERROR = -1;
    
    /** 处理异常（解码失败、解析错误、处理逻辑异常等） */
    public static final int PROCESS_ERROR = -2;
    
    /** 用户终止（用户主动终止流） */
    public static final int USER_TERMINATED = -3;
    
    /** 重连次数已达上限 */
    public static final int RECONNECT_EXHAUSTED = -4;
    
    /** 重试被中断 */
    public static final int RETRY_INTERRUPTED = -5;
    
    /** 监听器回调异常 */
    public static final int LISTENER_CALLBACK_ERROR = -6;
    
    /** 未知错误 */
    public static final int UNKNOWN_ERROR = -7;
    
    /**
     * 判断是否为 HTTP 错误码
     * 
     * @param errorCode 错误码
     * @return 如果是 HTTP 错误码（100-599）返回 true，否则返回 false
     */
    public static boolean isHttpError(int errorCode) {
        return errorCode >= 100 && errorCode < 600;
    }
    
    /**
     * 判断是否为系统错误码
     * 
     * @param errorCode 错误码
     * @return 如果是系统错误码（负数）返回 true，否则返回 false
     */
    public static boolean isSystemError(int errorCode) {
        return errorCode < 0;
    }
    
    /**
     * 获取错误码描述
     * 
     * @param errorCode 错误码
     * @return 错误码描述
     */
    public static String getDescription(int errorCode) {
        if (isHttpError(errorCode)) {
            return "HTTP " + errorCode;
        }
        
        switch (errorCode) {
            case NETWORK_ERROR:
                return "网络异常";
            case PROCESS_ERROR:
                return "处理异常";
            case USER_TERMINATED:
                return "用户终止";
            case RECONNECT_EXHAUSTED:
                return "重连次数已达上限";
            case RETRY_INTERRUPTED:
                return "重试被中断";
            case LISTENER_CALLBACK_ERROR:
                return "监听器回调异常";
            case UNKNOWN_ERROR:
                return "未知错误";
            default:
                return "错误码: " + errorCode;
        }
    }
}

