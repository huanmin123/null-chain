package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernel;

public interface NullIntStream  extends NullKernel<Integer> {
    int sum();
    int min();
    int max();
    long count();
    double average();
}
