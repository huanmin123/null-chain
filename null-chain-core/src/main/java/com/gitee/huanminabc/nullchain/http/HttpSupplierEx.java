package com.gitee.huanminabc.nullchain.http;

import java.io.IOException;

@FunctionalInterface
public interface HttpSupplierEx<T> {
    T get() throws IOException;

}
