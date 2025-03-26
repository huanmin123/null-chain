package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.common.NullChainException;

import java.io.Serializable;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@FunctionalInterface
public interface NullFunEx<T, R> extends  Serializable {
    R apply(T t) throws NullChainException;
}