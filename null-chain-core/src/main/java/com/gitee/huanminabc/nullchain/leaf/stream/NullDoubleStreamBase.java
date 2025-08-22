package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.stream.DoubleStream;

public class NullDoubleStreamBase extends NullKernelAbstract<Double> implements NullDoubleStream {
    public NullDoubleStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public double sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public double min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.min().orElse(0.0);
    }

    @Override
    public double max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.max().orElse(0.0);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }
}
