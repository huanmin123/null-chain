package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.jcommon.str.StringUtil;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.core.NullChain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static com.gitee.huanminabc.nullchain.common.NullLog.ASYNC_ARROW;
import static com.gitee.huanminabc.nullchain.common.NullLog.SERIALIZE_NULL_VALUE;

/**
 * Null内核抽象基类
 *
 * @param <T> 内核处理的值的类型
 * @author huanmin
 * @version 1.1.1
 * @since 1.0.0
 */
public class NullKernelAsyncAbstract<T> extends NullKernelAbstract implements NullKernel<T>, Serializable, NullCheck {

    public NullKernelAsyncAbstract(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    //同步转异步
    public NullChain<T> async() {
        async(linkLog, taskList);
        return NullBuild.busy(this);
    }

    //带线程池的同步转异步
    public NullChain<T> async(String threadFactoryName) throws NullChainException {
        async(linkLog, taskList, threadFactoryName);
        return NullBuild.busy(this);
    }

    public static void async(StringBuilder linkLog, NullTaskList taskList) {
        async(linkLog, taskList,null);
    }
    public static void async(StringBuilder linkLog, NullTaskList taskList, String threadFactoryName) {
        taskList.add((value) -> {
            if (StringUtil.isNotEmpty(threadFactoryName)) {
                ThreadFactoryUtil.addExecutor(threadFactoryName);
                taskList.setCurrentThreadFactoryName(threadFactoryName);
            }
            linkLog.append(ASYNC_ARROW);
            NullTaskList.NullNode<Object> objectNullNode = NullBuild.noEmpty(value);
            objectNullNode.async = true; //设置为异步
            return objectNullNode;

        });
    }
}
