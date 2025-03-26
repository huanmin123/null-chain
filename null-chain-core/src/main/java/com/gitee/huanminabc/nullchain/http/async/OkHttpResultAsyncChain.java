package com.gitee.huanminabc.nullchain.http.async;

import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;

import java.io.InputStream;

public interface OkHttpResultAsyncChain {
    /**
     * 下载文件到指定路径(异步)
     * @param filePath
     */
    NullChainAsync<Boolean> downloadFile(String filePath);

    /**
     * 获取返回的字节数组(异步)
     * @return
     */
    NullChainAsync<String> toStr();

    /**
     * 获取返回的输入流 (异步)
     * @return
     */
    NullChainAsync<InputStream> toInputStream();

    /**
     * 获取返回的字节数组(异步)
     * @return
     */
    NullChainAsync<byte[]> toBytes();
}
