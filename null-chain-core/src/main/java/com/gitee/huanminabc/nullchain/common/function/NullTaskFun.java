package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-26 14:31
 **/
@FunctionalInterface
public interface NullTaskFun<T> {
    NullChain<T> nodeTask(T value) throws  RuntimeException;
}
