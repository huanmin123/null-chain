package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.stream.LongStream;

public class NullLongStreamBase extends NullKernelAbstract<Long> implements NullLongStream {
    public NullLongStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public long sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public long min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.min().orElse(0L);
    }

    @Override
    public long max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.max().orElse(0L);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }
}
