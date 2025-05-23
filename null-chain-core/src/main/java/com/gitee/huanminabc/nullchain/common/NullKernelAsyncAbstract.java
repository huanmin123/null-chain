package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.NullCheck;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-03-21 12:45
 **/
public abstract class NullKernelAsyncAbstract<T> implements Serializable, NullCheck {
    private static final long serialVersionUID = 1L;

    @Setter
    protected transient StringBuilder linkLog;
    //收集器
    @Setter
    protected transient NullCollect collect;

    protected CompletableFuture<T> completableFuture;
    @Setter
    protected boolean isNull; //true 为空
    @Setter
    protected String currentThreadFactoryName = ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME;

    //获取当前线程池
    protected ExecutorService getCT(boolean forkJoinPool) {
        //如果是默认线程那么使用工作窃取线程 , 因为这种线程池是共享任务的基本不会有切换线程带来的性能损失,只适合快速且短小的任务
        if (forkJoinPool && ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME.equals(currentThreadFactoryName)) {
            return ForkJoinPool.commonPool();
        }
        return ThreadFactoryUtil.getExecutor(currentThreadFactoryName);
    }

    public NullKernelAsyncAbstract() {
        this(new StringBuilder(), false, new NullCollect());
    }

    public NullKernelAsyncAbstract(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        this.isNull = isNull;

        if (collect == null) {
            collect = new NullCollect();
        }
        this.collect = collect;
        if (linkLog == null) {
            linkLog = new StringBuilder();
        }
        if (linkLog.length() > 0 && this.linkLog != null) {
            this.linkLog.append(linkLog);
        } else {
            this.linkLog = linkLog;
        }
    }


    public NullKernelAsyncAbstract(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        this(linkLog, false, collect);
        this.completableFuture = completableFuture;
        this.currentThreadFactoryName = threadFactoryName;
    }

    @Override
    public boolean isEmpty() {
        return isNull;
    }
}
