package com.gitee.huanminabc.nullchain.common;

import lombok.extern.slf4j.Slf4j;

/**
 * Null链检查异常类 - 提供链式操作的检查异常
 * 
 * <p>该类提供了链式操作的检查异常，继承自Exception，用于处理链式操作过程中的检查异常。
 * 通过专门的检查异常，为Null链操作提供清晰的异常处理机制。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>检查异常：提供链式操作的检查异常</li>
 *   <li>异常信息：提供详细的异常信息</li>
 *   <li>异常处理：支持异常的处理和传播</li>
 *   <li>日志记录：支持异常日志记录</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>检查异常：继承自Exception的检查异常</li>
 *   <li>信息丰富：提供详细的异常信息</li>
 *   <li>日志支持：支持日志记录</li>
 *   <li>异常传播：支持异常的传播和处理</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Exception 异常基类
 */
@Slf4j
public class NullChainCheckException extends Exception {

    public NullChainCheckException() {
    }
    public NullChainCheckException(String message) {
        super(message);
    }
    public NullChainCheckException(Throwable e) {
        super(e);
    }
    //format
    public NullChainCheckException(String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*\\}", "%s"), args));
    }
    //有些错误是通过e.getMessage()获取不到的,所以需要传入e拿到堆栈信息
    //但是直接打印堆栈信息是加锁的导致有性能问题,所以我们需要通过让printStackTrace接收一个输出流来避免加锁并且按照日志的方式打印堆栈信息
    public NullChainCheckException(Throwable e, String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*\\}", "%s"), args),e);
    }
}
