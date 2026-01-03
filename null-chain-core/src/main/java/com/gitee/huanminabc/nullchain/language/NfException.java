package com.gitee.huanminabc.nullchain.language;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfException extends RuntimeException {


    public NfException(Throwable e) {
        super(e);
    }
    //format
    public NfException(String message, Object... args) {
        //将{}替换为%s，但如果没有提供参数，则直接使用原始消息
        super((args == null || args.length == 0) ? message : String.format(message.replaceAll("\\{\\s*\\}", "%s"), args));
    }
    //有些错误是通过e.getMessage()获取不到的,所以需要传入e拿到堆栈信息
    //但是直接打印堆栈信息是加锁的导致有性能问题,所以我们需要通过让printStackTrace接收一个输出流来避免加锁并且按照日志的方式打印堆栈信息
    public NfException(Throwable e, String message, Object... args) {
        //将{}替换为%s，但如果没有提供参数，则直接使用原始消息
        super(((args == null || args.length == 0) ? message : String.format(message.replaceAll("\\{\\s*\\}", "%s"), args)), e);
    }
}
