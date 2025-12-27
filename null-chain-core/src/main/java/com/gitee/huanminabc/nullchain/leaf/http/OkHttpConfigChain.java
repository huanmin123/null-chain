package com.gitee.huanminabc.nullchain.leaf.http;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;
/**
 * HTTP配置链接口 - 提供HTTP请求的配置功能
 * 
 * <p>该接口定义了HTTP请求的配置方法，包括超时设置、代理配置、请求头添加等。
 * 支持链式调用，可以灵活配置HTTP请求的各种参数。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>超时配置：设置连接、读取、写入超时时间</li>
 *   <li>代理配置：设置HTTP代理</li>
 *   <li>请求头配置：添加自定义请求头</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see OkHttpProtocolChain HTTP协议链接口
 */
public interface OkHttpConfigChain extends  OkHttpProtocolChain{
    /**
     * 设置连接超时时间
     * 
     * <p>该方法用于设置HTTP连接的超时时间，默认值为10秒。
     * 如果目标服务器响应时间过长，可以适当调整此值。</p>
     * 
     * @param time 超时时间长度
     * @param timeUnit 时间单位（如秒：TimeUnit.SECONDS）
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .connectTimeout(30, TimeUnit.SECONDS)  // 设置连接超时为30秒
     *     .get();
     * }</pre>
     */
    OkHttp connectTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置写超时时间
     * 
     * <p>该方法用于设置HTTP请求的写超时时间，默认值为10秒。
     * 写超时是指向服务器发送请求时，如果内容过大导致发送时间过长，
     * 超过了设定的超时时间，则请求会被取消。一般来说，10秒的超时时间是足够的。</p>
     * 
     * @param time 超时时间长度
     * @param timeUnit 时间单位（如秒：TimeUnit.SECONDS）
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .writeTimeout(60, TimeUnit.SECONDS)  // 设置写超时为60秒
     *     .post(OkHttpPostEnum.JSON);
     * }</pre>
     */
    OkHttp writeTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置读超时时间
     * 
     * <p>该方法用于设置HTTP请求的读超时时间，默认值为10秒。
     * 读超时是指服务器响应给我们的数据如果太大，导致在设定的超时时间内没有读取完毕，
     * 则会触发读超时，请求会被取消。通常情况下，10秒的超时时间是合理的。</p>
     * 
     * @param time 超时时间长度
     * @param timeUnit 时间单位（如秒：TimeUnit.SECONDS）
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .readTimeout(120, TimeUnit.SECONDS)  // 设置读超时为120秒
     *     .get();
     * }</pre>
     */
    OkHttp readTimeout(long time, TimeUnit timeUnit);

    /**
     * 设置HTTP代理
     * 
     * <p>该方法用于设置HTTP请求的代理服务器，支持HTTP和SOCKS代理。</p>
     * 
     * @param proxy 代理对象
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
     * OkHttp http = Null.of("https://api.example.com")
     *     .proxy(proxy)  // 设置HTTP代理
     *     .get();
     * }</pre>
     */
    OkHttp proxy(Proxy proxy);

    /**
     * 添加请求头
     * 
     * <p>该方法用于向HTTP请求添加自定义请求头，支持添加多个请求头。</p>
     * 
     * @param key 请求头的键
     * @param value 请求头的值
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .addHeader("Authorization", "Bearer token123")  // 添加认证头
     *     .addHeader("Content-Type", "application/json")  // 添加内容类型头
     *     .get();
     * }</pre>
     */
    OkHttp addHeader(String key, String value);

    /**
     * 设置请求失败时的重试次数
     * 
     * <p>该方法用于设置HTTP请求失败时的重试次数，默认值为3次。
     * 当网络波动或服务器暂时不可用时，自动重试可以提高请求成功率。</p>
     * 
     * @param retryCount 重试次数，必须大于等于0。如果设置为0，则不进行重试
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .retryCount(5)  // 设置重试5次
     *     .get();
     * }</pre>
     */
    OkHttp retryCount(int retryCount);

    /**
     * 设置请求失败时的重试间隔时间
     * 
     * <p>该方法用于设置HTTP请求失败后每次重试之间的间隔时间，默认值为100毫秒。
     * 重试间隔会随着重试次数递增（间隔时间 = 基础间隔 * 当前重试次数），
     * 这种策略可以避免在短时间内对服务器造成过大压力。</p>
     * 
     * @param retryInterval 重试间隔时间（毫秒），必须大于等于0
     * @return OkHttp对象，以便链式调用其他配置方法
     * 
     * @example
     * <pre>{@code
     * OkHttp http = Null.of("https://api.example.com")
     *     .retryCount(3)           // 设置重试3次
     *     .retryInterval(200)      // 设置基础间隔为200毫秒
     *     .get();
     * // 实际重试间隔为：第1次200ms，第2次400ms，第3次600ms
     * }</pre>
     */
    OkHttp retryInterval(long retryInterval);
}
