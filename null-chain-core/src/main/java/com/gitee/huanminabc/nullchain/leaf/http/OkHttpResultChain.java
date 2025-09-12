package com.gitee.huanminabc.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.core.NullChain;

import java.io.InputStream;
/**
 * HTTP结果链接口 - 提供HTTP请求结果处理功能
 * 
 * <p>该接口定义了HTTP请求结果的处理方法，包括字符串、字节数组、输入流、文件下载等。
 * 支持多种结果格式的获取，提供灵活的结果处理方式。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>字符串结果：获取HTTP响应的字符串内容</li>
 *   <li>字节数组结果：获取HTTP响应的字节数组</li>
 *   <li>输入流结果：获取HTTP响应的输入流</li>
 *   <li>文件下载：将HTTP响应下载到指定文件路径</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
public interface OkHttpResultChain {


    /**
     * 下载文件到指定路径
     * 
     * <p>该方法用于将HTTP响应内容下载到指定的文件路径。
     * 适用于下载文件、图片、文档等二进制内容。</p>
     * 
     * @param filePath 文件保存路径
     * @return 包含下载结果的Null链，true表示下载成功
     * 
     * @example
     * <pre>{@code
     * Boolean success = Null.of("https://example.com/file.pdf")
     *     .get()
     *     .downloadFile("/path/to/download/file.pdf")  // 下载文件到指定路径
     *     .orElse(false);
     * }</pre>
     */
    NullChain<Boolean> downloadFile(String filePath);
    
    /**
     * 获取返回的字节数组
     * 
     * <p>该方法用于获取HTTP响应的字节数组内容。
     * 适用于处理二进制数据、图片、文件等。</p>
     * 
     * @return 包含字节数组的Null链
     * 
     * @example
     * <pre>{@code
     * byte[] data = Null.of("https://example.com/image.jpg")
     *     .get()
     *     .toBytes()  // 获取图片的字节数组
     *     .orElse(new byte[0]);
     * }</pre>
     */
    NullChain<byte[]> toBytes();

    /**
     * 获取返回的输入流
     * 
     * <p>该方法用于获取HTTP响应的输入流，适用于处理大文件或流式数据。
     * 调用者需要负责关闭输入流。</p>
     * 
     * @return 包含输入流的Null链
     * 
     * @example
     * <pre>{@code
     * InputStream stream = Null.of("https://example.com/large-file.zip")
     *     .get()
     *     .toInputStream()  // 获取大文件的输入流
     *     .orElse(null);
     * }</pre>
     */
    NullChain<InputStream> toInputStream();

    /**
     * 获取返回的字符串
     * 
     * <p>该方法用于获取HTTP响应的字符串内容，适用于处理文本数据、JSON、XML等。
     * 这是最常用的结果获取方法。</p>
     * 
     * @return 包含字符串的Null链
     * 
     * @example
     * <pre>{@code
     * String response = Null.of("https://api.example.com/users")
     *     .get()
     *     .toStr()  // 获取API响应的字符串
     *     .orElse("请求失败");
     * }</pre>
     */
    NullChain<String> toStr();

}
