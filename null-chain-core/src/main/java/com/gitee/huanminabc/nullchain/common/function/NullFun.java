package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的函数式接口，配合基于 SerializedLambda 的反射工具获取字段名。
 */
@FunctionalInterface
public interface NullFun<T, R> extends Function<T, R>, Serializable {
}


