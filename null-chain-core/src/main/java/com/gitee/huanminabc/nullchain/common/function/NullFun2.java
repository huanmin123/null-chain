package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@FunctionalInterface
public interface NullFun2<NULL,T, R> extends Serializable {
    R apply(NULL n,T t);
}