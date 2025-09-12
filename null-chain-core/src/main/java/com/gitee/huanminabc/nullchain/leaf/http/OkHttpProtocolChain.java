package com.gitee.huanminabc.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;

/**
 * HTTP协议链接口 - 提供HTTP请求协议和参数设置功能
 * 
 * <p>该接口定义了HTTP请求的协议方法，包括GET、POST、PUT、DELETE等HTTP方法。
 * 支持各种请求类型和参数处理，提供灵活的HTTP请求配置。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>GET请求：支持URL参数拼接</li>
 *   <li>POST请求：支持JSON、表单、文件上传</li>
 *   <li>PUT请求：支持JSON、表单数据</li>
 *   <li>DELETE请求：支持URL参数拼接</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see OkHttpResultChain HTTP结果链接口
 */
public interface OkHttpProtocolChain  extends OkHttpResultChain {
    /**
     * 设置GET请求协议和参数
     * 
     * <p>该方法用于设置HTTP GET请求，根据HTTP规范，GET请求的参数是拼接在URL后面的，
     * 不支持请求体。参数来源可以是对象、Map或String，值为null的参数会被忽略。</p>
     * 
     * <p>数据拼接会自动识别URL中是否已有参数，如果有会自动与节点参数拼接。</p>
     * 
     * @return OkHttp对象，以便链式调用其他方法
     * 
     * @example
     * <pre>{@code
     * String result = Null.of("https://api.example.com/users")
     *     .get()  // 设置GET请求
     *     .toStr()
     *     .orElse("请求失败");
     * }</pre>
     */
    OkHttp get();

    /**
     * 设置POST请求协议和参数
     * 
     * <p>该方法用于设置HTTP POST请求，支持多种请求类型：</p>
     * <ul>
     *   <li>JSON：节点参数必须是对象或Map，会转化为JSON格式</li>
     *   <li>FORM：节点参数必须是对象或Map，会转化为表单格式</li>
     *   <li>FILE：节点参数必须是对象或Map，会转化为表单，自动识别文件类型</li>
     * </ul>
     * 
     * <p><strong>文件上传说明：</strong></p>
     * <ul>
     *   <li>支持File、File[]、byte[]、byte[][]类型</li>
     *   <li>文件的key是Map的key或对象字段名称</li>
     *   <li>可通过@JSONField(name="file")指定key</li>
     *   <li>字节上传必须指定fileName，可通过@JSONField(name="fileName")指定</li>
     * </ul>
     * 
     * @param type 请求类型枚举
     * @return OkHttp对象，以便链式调用其他方法
     * 
     * @example
     * <pre>{@code
     * String result = Null.of("https://api.example.com/users")
     *     .post(OkHttpPostEnum.JSON)  // 设置POST请求，JSON格式
     *     .toStr()
     *     .orElse("请求失败");
     * }</pre>
     */
    OkHttp post(OkHttpPostEnum type);

    /**
     * 设置PUT请求协议和参数
     * 
     * <p>该方法用于设置HTTP PUT请求，与POST请求类似，只是请求类型不同。
     * 支持JSON、FORM、FILE等请求类型，参数处理方式与POST相同。</p>
     * 
     * @param type 请求类型枚举
     * @return OkHttp对象，以便链式调用其他方法
     * 
     * @example
     * <pre>{@code
     * String result = Null.of("https://api.example.com/users/1")
     *     .put(OkHttpPostEnum.JSON)  // 设置PUT请求，JSON格式
     *     .toStr()
     *     .orElse("请求失败");
     * }</pre>
     */
    OkHttp put(OkHttpPostEnum type);

    /**
     * 设置DELETE请求协议和参数
     * 
     * <p>该方法用于设置HTTP DELETE请求，根据HTTP规范，DELETE请求一般用于删除数据，
     * 所以参数是拼接在URL后面的，不支持请求体。</p>
     * 
     * <p>参数来源可以是对象、Map或String，值为null的参数会被忽略。
     * 数据拼接会自动识别URL中是否已有参数，如果有会自动与节点参数拼接。</p>
     * 
     * @return OkHttp对象，以便链式调用其他方法
     * 
     * @example
     * <pre>{@code
     * String result = Null.of("https://api.example.com/users/1")
     *     .del()  // 设置DELETE请求
     *     .toStr()
     *     .orElse("请求失败");
     * }</pre>
     */
    OkHttp del();
}
