package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.utils.ReflectionKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

/**
 * @author huanmin
 * @date 2023/11/21
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
            Field detailMessage = FieldUtils.getField(aClass, "detailMessage", true);
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
