package com.gitee.huanminabc.nullchain.language;

/**
 * NF脚本执行超时异常
 * 
 * <p>当脚本执行时间超过全局超时限制时抛出此异常。</p>
 * 
 * @author huanmin
 * @date 2024/12/XX
 */
public class NfTimeoutException extends NfException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public NfTimeoutException(String message) {
        super(message);
    }

    /**
     * 构造函数（带参数格式化）
     *
     * @param message 错误消息模板
     * @param args 参数
     */
    public NfTimeoutException(String message, Object... args) {
        super(message, args);
    }
}

