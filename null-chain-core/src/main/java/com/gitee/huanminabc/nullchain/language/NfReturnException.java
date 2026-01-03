package com.gitee.huanminabc.nullchain.language;

/**
 * 函数返回异常
 *
 * <p>当执行return语句时抛出此异常，用于提前终止函数体的执行。</p>
 * <p>此异常被FunCallSyntaxNode捕获，不应该传播到外部调用者。</p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
public class NfReturnException extends NfException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param line 行号
     */
    public NfReturnException(int line) {
        super("Line:{} ,Return from function", line);
    }

    /**
     * 构造函数（带消息）
     *
     * @param line 行号
     * @param message 消息
     */
    public NfReturnException(int line, String message) {
        super("Line:{} ,{}", line, message);
    }
}
