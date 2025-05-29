package com.gitee.huanminabc.nullchain.leaf.json;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-28 09:48
 **/
public interface NullJson <T> extends NullChain<T> , NullKernel<T> {

    /**
     * 将对象转换为json字符串
     *
     * @return
     * @
     */
    NullJson<String> json();

    /**
     * 将json字符串转换为对象
     *
     * @param uClass
     * @param <U>
     * @return
     */
    <U> NullJson<U> json(Class<U> uClass);

    /**
     * 将json字符串转换为对象
     * @param uClass
     * @param <U>
     * @return
     */
    <U> NullJson<U> json(U uClass);
}
