package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.stream.IntStream;

public class NullIntStreamBase  extends NullKernelAbstract<Integer>  implements NullIntStream {
    public NullIntStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public int sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public int min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.min().orElse(0);
    }

    @Override
    public int max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.max().orElse(0);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }

}
