package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * 简要描述
 * @Author: mac
 * @Date: 2025/5/29 19:31
 * @Version: 1.0
 * @Description: 文件作用详细描述....
 */
public interface NullKernel<T> {
    //同步转异步
    NullChain<T> async();
    //带线程池的同步转异步
    NullChain<T> async(String threadFactoryName);

}
