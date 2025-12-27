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
 * @param <T> HTTP请求值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 * @see OkHttpConfigChain HTTP配置链接口
 */
public interface OkHttp<T> extends  OkHttpConfigChain {

}
