package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-02-08 19:11
 **/
@FunctionalInterface
public interface NullConsumer2<NULL,T> extends Serializable {
    void accept(NULL n,T t);
}
