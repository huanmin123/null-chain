package com.gitee.huanminabc.nullchain.leaf.http;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;
/**
 * @author huanmin
 * @date 2024/11/30
 */
public interface OkHttpConfigChain extends  OkHttpProtocolChain{
    /**
     * 设置超时时间,默认10s , 如果目标服务器响应时间过长,可以适当调整
     * @param time
     * @param timeUnit
     * @return
     */
    OkHttp connectTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置写超时时间。默认值为10秒，一般不需要手动设置。
     * 写超时是指我们向服务器发送请求时，如果内容过大导致发送时间过长，
     * 超过了设定的超时时间，则请求会被取消。一般来说，10秒的超时时间是足够的。
     *
     * @param time 超时时间长度
     * @param timeUnit 时间单位（如秒：TimeUnit.SECONDS）
     * @return OkHttpChain对象，以便链式调用其他配置方法
     */
    OkHttp writeTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置读超时时间。默认值为10秒，一般不需要手动设置。
     * 读超时是指服务器响应给我们的数据如果太大，导致在设定的超时时间内没有读取完毕，
     * 则会触发读超时，请求会被取消。通常情况下，10秒的超时时间是合理的。
     *
     * @param time 超时时间长度
     * @param timeUnit 时间单位（如秒：TimeUnit.SECONDS）
     * @return OkHttpChain对象，以便链式调用其他配置方法
     */
    OkHttp readTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置代理
     * @param proxy
     * @return
     */
    OkHttp proxy(Proxy proxy);



    /**
     * 添加请求头
     * @param key
     * @param value
     * @return
     */
    OkHttp addHeader(String key, String value);
}
