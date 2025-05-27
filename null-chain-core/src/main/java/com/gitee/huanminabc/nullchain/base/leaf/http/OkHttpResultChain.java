package com.gitee.huanminabc.nullchain.base.leaf.http;

import com.gitee.huanminabc.nullchain.base.NullChain;

import java.io.InputStream;
/**
 * @author huanmin
 * @date 2024/11/30
 */
public interface OkHttpResultChain {


    /**
     * 下载文件到指定路径
     * @param filePath
     * @return
     */
    NullChain<Boolean> downloadFile(String filePath);
    /**
     * 获取返回的字节数组
     * @return
     */
    NullChain<byte[]> toBytes();

    /**
     * 获取返回的输入流
     * @return
     */
    NullChain<InputStream> toInputStream();

    /**
     * 获取返回的字符串
     * @return
     */
    NullChain<String> toStr();

}
