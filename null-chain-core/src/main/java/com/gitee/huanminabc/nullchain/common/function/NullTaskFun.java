package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-26 14:31
 **/
@FunctionalInterface
public interface NullTaskFun<T> {
    NullChain<T> task(T value) throws  RuntimeException;
}
