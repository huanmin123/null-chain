package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.reflect.FieldUtil;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;

/**
 * Null链运行时异常类 - 提供链式操作的运行时异常
 * 
 * <p>该类提供了链式操作的运行时异常，继承自RuntimeException，用于处理链式操作过程中的运行时异常。
 * 通过专门的运行时异常，为Null链操作提供灵活的异常处理机制。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>运行时异常：提供链式操作的运行时异常</li>
 *   <li>异常信息：提供详细的异常信息</li>
 *   <li>异常处理：支持异常的处理和传播</li>
 *   <li>日志记录：支持异常日志记录</li>
 *   <li>字段处理：支持异常字段的处理</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>运行时异常：继承自RuntimeException的运行时异常</li>
 *   <li>信息丰富：提供详细的异常信息</li>
 *   <li>日志支持：支持日志记录</li>
 *   <li>字段处理：支持异常字段的处理</li>
 *   <li>异常传播：支持异常的传播和处理</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see RuntimeException 运行时异常基类
 * @see FieldUtil 字段工具
 */
@Slf4j
public class NullChainException extends RuntimeException {

    public NullChainException() {
    }

    public NullChainException(String message) {
        super(message);
    }

    public NullChainException(Throwable e) {
        super(e);
    }

    //format
    public NullChainException(String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*}", "%s"), args));
    }

    //有些错误是通过e.getMessage()获取不到的,所以需要传入e拿到堆栈信息
    //但是直接打印堆栈信息是加锁的导致有性能问题,所以我们需要通过让printStackTrace接收一个输出流来避免加锁并且按照日志的方式打印堆栈信息
    public NullChainException(Throwable e, String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*}", "%s"), args), e);
    }

    //设置message
    public void setMessage(String message, Object... args) {
        String format = String.format(message.replaceAll("\\{\\s*}", "%s"), args);
        Class<? extends NullChainException> aClass = this.getClass();
        //添加detailMessage
        try {
            Field detailMessage = FieldUtil.getField(aClass, "detailMessage");
            detailMessage.setAccessible(true);
            detailMessage.set(this, format);
        } catch (IllegalAccessException  ignored) {}
    }

    public void setMessage(Throwable e, String message, Object... args) {
        this.setMessage(String.format(message.replaceAll("\\{\\s*}", "%s"), args));
        this.setStackTrace(e.getStackTrace());
    }

    public void setMessage(StackTraceElement[] stackTrace, String message, Object... args) {
        this.setMessage(String.format(message.replaceAll("\\{\\s*}", "%s"), args));
        this.setStackTrace(stackTrace);
    }
}
