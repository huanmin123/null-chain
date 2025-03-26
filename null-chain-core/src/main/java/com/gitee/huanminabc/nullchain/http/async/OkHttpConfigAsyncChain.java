package com.gitee.huanminabc.nullchain.http.async;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public interface OkHttpConfigAsyncChain extends OkHttpProtocolAsyncChain{
    /**
     * 设置超时时间
     * @param time
     * @param timeUnit
     * @return
     */
    OkHttpAsync connectTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置写超时时间
     * @param time
     * @param timeUnit
     * @return
     */
    OkHttpAsync writeTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置读超时时间
     * @param time
     * @param timeUnit
     * @return
     */
    OkHttpAsync readTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置代理
     * @param proxy
     * @return
     */
    OkHttpAsync proxy(Proxy proxy);


    /**
     * 添加请求头
     * @param key
     * @param value
     * @return
     */
    OkHttpAsync addHeader(String key, String value);
}
