package com.gitee.huanminabc.nullchain.language;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfCheckException extends Exception {


    public NfCheckException(Throwable e) {
        super(e);
    }
    //format
    public NfCheckException(String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*\\}", "%s"), args));
    }
    //有些错误是通过e.getMessage()获取不到的,所以需要传入e拿到堆栈信息
    //但是直接打印堆栈信息是加锁的导致有性能问题,所以我们需要通过让printStackTrace接收一个输出流来避免加锁并且按照日志的方式打印堆栈信息
    public NfCheckException(Throwable e, String message, Object... args) {
        //将{}替换为%s
        super(String.format(message.replaceAll("\\{\\s*\\}", "%s"), args),e);
    }
}
