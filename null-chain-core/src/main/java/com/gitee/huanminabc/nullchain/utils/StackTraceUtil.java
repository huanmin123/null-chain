package com.gitee.huanminabc.nullchain.utils;

public class StackTraceUtil {
    //获取调用此方法的 类名 方法名,行号
    public static StackTraceElement currentStackTrace(int num) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= num+ 1) {
            return stackTrace[num];
        }
        return new StackTraceElement("null", "null", "null", 0);
    }

}
