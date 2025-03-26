package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@FunctionalInterface
public interface NullFun<T, R> extends Function<T, R>, Serializable {
}