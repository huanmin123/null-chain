package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-26 14:31
 **/
@FunctionalInterface
public interface NullTaskFun<T> {
    NullTaskList.NullNode<T> nodeTask(T preValue) throws  RuntimeException;
}
