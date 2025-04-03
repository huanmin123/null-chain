package com.gitee.huanminabc.nullchain.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *@author dengcs
 *
 *类说明：线程休眠辅助工具类
 */
@Slf4j
public class SleepTools {

    /**
     * 按秒休眠
     * @param seconds 秒数
     */
    public static  void second(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException exception) {
            log.error("",exception);

        }
    }

    /**
     * 按毫秒
     * 数休眠
     * @param ms 毫秒数
     */
    public static  void ms(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException exception) {
            log.error("",exception);
        }
    }


    //随机单位时间休眠
    public static  void rand(TimeUnit timeUnit){
        int time =0;
        try {
            switch (timeUnit){
                case DAYS:
                    time = getNumber(365)*24*60*60*1000;
                    TimeUnit.DAYS.sleep(time);
                    break;
                case HOURS:
                    time = getNumber(24)*60*60*1000;
                    TimeUnit.HOURS.sleep(time);
                    break;
                case MINUTES:
                    time = getNumber(60)*60*1000;
                    TimeUnit.MINUTES.sleep(time);
                    break;
                case SECONDS:
                    time = getNumber(60)*1000;
                    TimeUnit.SECONDS.sleep(time);
                    break;
                case MILLISECONDS: //毫秒
                    time = getNumber(1000);
                    TimeUnit.MILLISECONDS.sleep(time);
                    break;
                case MICROSECONDS: //微秒
                    time = getNumber(1000);
                    TimeUnit.MICROSECONDS.sleep(time);
                    break;
                case NANOSECONDS: //纳秒
                    time = getNumber(1000);
                    TimeUnit.NANOSECONDS.sleep(time);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的时间单位");
            }
        } catch (InterruptedException e) {
            log.error("",e);
        }

    }

    //随机生成指定大小内的数字
    private static int getNumber(int size) {
        return new Random().nextInt(size) + 1;
    }
}

