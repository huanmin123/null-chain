package com.gitee.huanminabc.nullchain.common.function;

import java.io.IOException;

@FunctionalInterface
public interface NullHttpSupplierEx<T> {
    T get() throws IOException;

}
