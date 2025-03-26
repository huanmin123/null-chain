package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.utils.ThreadFactoryUtil;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
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
    protected ThreadPoolExecutor getCT() {
        return ThreadFactoryUtil.getExecutor(currentThreadFactoryName);
    }

    public NullKernelAsyncAbstract(){
        this(new StringBuilder(),false,new NullCollect());
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
