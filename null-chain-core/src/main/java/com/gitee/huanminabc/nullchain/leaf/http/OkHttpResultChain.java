package com.gitee.huanminabc.nullchain.leaf.http;

import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;

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
     * @return true表示下载成功，false表示下载失败
     * 
     * @example
     * <pre>{@code
     * boolean success = Null.ofHttp("https://example.com/file.pdf")
     *     .get()
     *     .downloadFile("/path/to/download/file.pdf");  // 下载文件到指定路径
     * }</pre>
     */
    NullChain<Boolean> downloadFile(String filePath);
    
    /**
     * 获取返回的字节数组
     * 
     * <p>该方法用于获取HTTP响应的字节数组内容。
     * 适用于处理二进制数据、图片、文件等。</p>
     * 
     * @return 包含字节数组的Null链，如果响应为空则返回空链
     * 
     * @example
     * <pre>{@code
     * byte[] data = Null.ofHttp("https://example.com/image.jpg")
     *     .get()
     *     .toBytes()  // 获取图片的字节数组
     *     .orElse(new byte[0]);  // 如果为空则返回空数组
     * }</pre>
     */
    NullChain<byte[]> toBytes();

    /**
     * 获取返回的输入流
     * 
     * <p>该方法用于获取HTTP响应的输入流，适用于处理大文件或流式数据。
     * 调用者需要负责关闭输入流。</p>
     * 
     * @return 包含输入流的Null链，如果响应为空则返回空链
     * 
     * @example
     * <pre>{@code
     * InputStream stream = Null.ofHttp("https://example.com/large-file.zip")
     *     .get()
     *     .toInputStream()  // 获取大文件的输入流
     *     .orElseNull();
     * if (stream != null) {
     *     try (stream) {
     *         // 处理流数据
     *     }
     * }
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
     * String response = Null.ofHttp("https://api.example.com/users")
     *     .get()
     *     .toStr()  // 获取API响应的字符串
     *     .orElse("请求失败");
     * }</pre>
     */
    NullChain<String> toSTR();

    /**
     * 获取返回的字符串并转换为指定类型的对象
     * 
     * <p>该方法用于获取HTTP响应的字符串内容，并通过JSON反序列化转换为指定类型的对象。
     * 适用于处理JSON格式的API响应。</p>
     * 
     * @param <R> 目标对象类型
     * @param clazz 目标类型的Class对象
     * @return 包含目标类型对象的Null链
     * 
     * @example
     * <pre>{@code
     * User user = Null.ofHttp("https://api.example.com/user/1")
     *     .get()
     *     .toStr(User.class)  // 将JSON响应转换为User对象
     *     .orElseNull();
     * }</pre>
     */
    <R> NullChain<R> toFromJson(Class<R> clazz);


    /**
     * 处理 SSE (Server-Sent Events) 流式响应（自定义解码器）
     * 
     * <p>该方法用于处理 HTTP 响应为 SSE 流的情况。如果响应 Content-Type 为 text/event-stream，
     * 则按照 SSE 协议解析并触发相应的事件回调；如果响应不是 SSE 格式，则触发非 SSE 响应回调。</p>
     * 
     * <h3>使用说明：</h3>
     * <ul>
     *   <li>SSE 流：会调用 {@link SSEEventListener#onOpen()}、{@link SSEEventListener#onEvent(EventMessage)}、
     *       {@link SSEEventListener#onComplete()} 等方法</li>
     *   <li>非 SSE 响应：会调用 {@link SSEEventListener#onNonSseResponse(String, String)} 方法，
     *       避免与 SSE 事件混淆</li>
     *   <li>错误处理：通过 {@link SSEEventListener#onError(int, Integer, String, Throwable)} 回调</li>
     *   <li>主动终止：用户可以通过 {@link EventMessage#terminate()} 方法主动终止流，
     *       终止后会触发 {@link SSEEventListener#onInterrupt()} 回调</li>
     * </ul>
     * 
     * <h3>主动终止功能：</h3>
     * <p>在处理事件时，如果发现结果不符合预期，可以通过 {@link EventMessage#terminate()} 方法主动终止流。
     * 终止后会在读取完当前行后停止，并触发 {@link SSEEventListener#onInterrupt()} 回调。</p>
     * 
     * @param <T> SSE 数据解码后的类型
     * @param listener SSE 事件监听器，处理各种 SSE 事件
     * @param decoder 数据解码器，将原始字符串解码为泛型对象
     * 
     * @example
     * <pre>{@code
     * // 使用 JSON 解码器
     * import com.alibaba.fastjson.JSONObject;
     * import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
     * 
     * Null.ofHttp("https://api.example.com/sse")
     *     .get()
     *     .toSSE(new SSEEventListener<JSONObject>() {
     *         public void onEvent(EventMessage<JSONObject> msg) {
     *             JSONObject data = msg.getData();
     *             if (data != null) {
     *                 System.out.println("收到事件: " + data.getString("content"));
     *                 
     *                 // 如果发现结果不对，可以主动终止
     *                 if (shouldStop(data)) {
     *                     msg.terminate();  // 终止流
     *                 }
     *             }
     *         }
     *         
     *         public void onInterrupt() {
     *             // 流被用户主动终止时的回调
     *             System.out.println("SSE 流已被用户终止");
     *         }
     *         // ... 其他回调方法
     *     }, DataDecoder.jsonDecoder());  // 使用 JSON 解码器
     * }</pre>
     */
    <T> void toSSE(SSEEventListener<T> listener, DataDecoder<T> decoder);
    void toSSEText(SSEEventListener<String> listener);
    void toSSEJson(SSEEventListener<JSONObject> listener);

}
