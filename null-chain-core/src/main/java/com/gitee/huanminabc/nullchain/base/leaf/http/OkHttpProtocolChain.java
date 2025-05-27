package com.gitee.huanminabc.nullchain.base.leaf.http;

import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;

/**
 * @author huanmin
 * @date 2024/11/30
 */
public interface OkHttpProtocolChain  extends OkHttpResultChain {
    /**
     * 设置get请求协议和参数
     * 根据规范如果是get请求,那么参数是拼接在url后面的, 不支持请求体这些
     * 数据的来源: 可以是一个对象或者Map或者String , 值为null的参数会被忽略
     * 数据拼接:  会自动识别url中是否有参数,如果有会自动和节点参数拼接
     * @return
     */
    OkHttpChain get();

    /**
     * 设置post请求协议和参数
     * @param type 请求类型
     *         如果类型是JSON,那么节点参数必须是一个对象或者Map,会转化为json
     *         如果类型是FORM,那么节点参数必须是一个对象或者Map,会转化为表单
     *         如果类型是FILE,那么节点参数必须是一个对象或者Map,会转化为表单,自动识别File或者File[]或者byte[]或者byte[][] 这几种类型然后上传
     *              文件的key 就是map的key, 对象字段的名称,  如果用的是对象,可以通过@JSONField(name="file")指定key
     *              如果是字节上传,那么必须指定文件名称fileName, 这个是固定的, 也可以通过@JSONField(name="fileName")指定
     * 数据的来源: 可以是一个对象或者Map  , 值为null的参数会被忽略
     * @return
     */
    OkHttpChain post(OkHttpPostEnum type);

    /**
     * 设置put请求协议和参数, 和post一样,只是请求类型不一样
     * @return
     */
    OkHttpChain put(OkHttpPostEnum type);

    /**
     * 设置为delete请求协议和参数
     * 根据规范,delete请求一般用于删除数据,所以参数是拼接在url后面的, 不支持请求体这些
     * 数据的来源: 可以是一个对象或者Map或者String  , 对象或者Map值为null的参数会被忽略
     * 数据拼接:  会自动识别url中是否有参数,如果有会自动和节点参数拼接
     * @return
     */
    OkHttpChain del();
}
