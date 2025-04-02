package com.gitee.huanminabc.nullchain.utils;


import lombok.extern.slf4j.Slf4j;

/**
 * 代码执行时间监控(毫秒)
 */
@Slf4j
public class CodeTimeUtil {
    // 1000毫秒=1秒
    public  static void  creator(Runnable runnable ) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        log.info("执行时长：{} 毫秒.\n", (endTime - startTime) );
    }

    public  static void  creator(String message,Runnable runnable ) throws Exception {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        log.info("{}：{} 毫秒.\n", message,(endTime - startTime) );
    }
    //微妙  (1000微妙=1毫秒)
    public  static void  creatorSubtle(Runnable runnable ) throws Exception {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        log.info("执行时长：{} 微妙.\n", (endTime - startTime) );
    }

}
