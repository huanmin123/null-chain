package com.gitee.huanminabc.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.common.NullKernel;

/**
 * Null HTTP操作接口 - 提供空值安全的HTTP请求功能
 * 
 * <p>该接口提供了对HTTP请求操作的空值安全封装，支持GET、POST、PUT、DELETE等HTTP方法。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>HTTP配置：设置超时时间、代理、请求头等</li>
 *   <li>HTTP协议：支持GET、POST、PUT、DELETE请求</li>
 *   <li>结果处理：支持字符串、字节数组、输入流、文件下载等</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 * </ul>
 *
 * <h3>默认重试说明：</h3>
 * <ul>
 *   <li>普通 HTTP 终结方法默认启用重试：{@link OkHttpResultChain#toSTR()}、
 *       {@link OkHttpResultChain#toBytes()}、{@link OkHttpResultChain#toInputStream()}、
 *       {@link OkHttpResultChain#toFromJson(Class)}、{@link OkHttpResultChain#downloadFile(String)}。</li>
 *   <li>默认重试次数为 3 次，默认基础间隔为 100 毫秒；可通过
 *       {@link OkHttpConfigChain#retryCount(int)} 和 {@link OkHttpConfigChain#retryInterval(long)} 调整，
 *       {@code retryCount(0)} 表示不重试，只执行一次。</li>
 *   <li>普通 HTTP 重试捕获的是 OkHttp 请求执行、响应体读取和相关文件读写过程中抛出的所有
 *       {@link java.io.IOException}；参数校验、JSON 解析、监听器回调等非 IO 异常不参与该重试。</li>
 *   <li>SSE 与 WebSocket 使用同一组重试配置，但语义分别是 SSE 连接重试和 WebSocket 自动重连。</li>
 * </ul>
 * 
 * @param <T> HTTP请求值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 * @see OkHttpConfigChain HTTP配置链接口
 */
public interface OkHttp<T> extends  OkHttpConfigChain {

}
