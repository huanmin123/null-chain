package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernel;

public interface NullDoubleStream extends NullKernel<Double> {

    double sum();
    double min();
    double max();
    long count();
    double average();
}
