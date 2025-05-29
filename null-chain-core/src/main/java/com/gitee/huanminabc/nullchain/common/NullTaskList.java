package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.exception.StackTraceUtil;
import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.common.function.NullTaskFun;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-26 14:22
 **/
@Slf4j
public class NullTaskList {
    private Queue<NullTaskFunAbs> tasks;
    private NullChainBase lastResult; //最后一个任务的结果
    private boolean lastAsync = false; //最后一个任务是否异步

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

    public NullTaskList() {
        tasks = new LinkedList<>();
    }

    public void add(NullTaskFun task) {
        if (NullTaskFunAbs.class.isAssignableFrom(task.getClass())) {
            NullTaskFunAbs taskAbs = (NullTaskFunAbs) task;
            tasks.add(taskAbs);
        } else {
            tasks.add(new NullTaskFunAbs() {
                @Override
                public NullChainBase nodeTask(Object value) throws RuntimeException {
                    return (NullChainBase) task.nodeTask(value);
                }
            });
        }

    }


    //运行任务返回结果  (非异步的方法执行)
    public <T> NullChainBase<T> runTaskAll() {
        if (lastResult != null) {
            return lastResult;
        }
        NullChainBase<T> chain = null;
        while (!tasks.isEmpty()) {
            NullTaskFunAbs poll = tasks.poll();
            NullChainBase task1 = (NullChainBase) poll.nodeTask(chain == null ? null : chain.value);
            if (task1.isNull ) {
                //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                NullTaskFunAbs lastPoll  = tasks.peek();
                if (lastPoll != null&&lastPoll.preNullEnd()) {
                    return task1;
                }
            }
            chain = task1;
        }
        lastResult = chain;
        tasks = null;//避免被重复调用
        return chain;
    }

    //运行任务返回结果  如果调用方支持异步那么开启异步之后的节点将脱离主线程  比如ifPresent
    public <T> void runTaskAll(Consumer<NullChainBase<T>> supplier, Consumer<Throwable> consumer) {
        if (lastResult != null) {
            if (!lastAsync) {
                supplier.accept(lastResult);
            } else {
                CompletableFuture<NullChainBase> nullChainBaseCompletableFuture = CompletableFuture.completedFuture(lastResult);
                nullChainBaseCompletableFuture.thenComposeAsync((nullChainBase) -> {
                    supplier.accept(nullChainBase);
                    return CompletableFuture.completedFuture(null);
                }, getCT(false));//终结的方法一般都比较重, 不使用窃取线程池
                StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(5);
                nullChainBaseCompletableFuture.exceptionally((e) -> {
                    e.addSuppressed(new NullChainException(stackTraceElement.toString()));
                    if (consumer != null) {
                        consumer.accept(e);
                    } else {
                        log.error("", e);
                    }
                    return null;
                });
            }
            return;
        }
        NullChainBase chain = null;
        CompletableFuture<NullChainBase> completableFuture = null;
        while (!tasks.isEmpty()) {
            NullTaskFunAbs poll = tasks.poll();
            if (completableFuture == null) {
                NullChainBase task = (NullChainBase) poll.nodeTask(chain == null ? null : chain.value);
                if (task.isNull) {
                    //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                    NullTaskFunAbs lastPoll = tasks.peek();
                    if (lastPoll != null&&lastPoll.preNullEnd()) {
                        supplier.accept(task);
                        return;
                    }
                }
                if (task.async) {
                    completableFuture = new CompletableFuture();
                    completableFuture.complete(task);
                }
                chain = task;
            } else {
                completableFuture = completableFuture.thenComposeAsync((taskFut) -> {
                    NullChainBase task1 = (NullChainBase) poll.nodeTask(taskFut.value);
                    if (task1.isNull) {
                        //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                        NullTaskFunAbs lastPoll = tasks.poll();
                        if (lastPoll != null&&lastPoll.preNullEnd()) {
                            supplier.accept(task1);
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    //继续执行
                    return CompletableFuture.completedFuture(task1);
                }, getCT(!poll.isHeavyTask()));
            }
        }
        lastResult = chain;
        tasks = null;
        if (completableFuture == null) {
            lastAsync = false; //没有异步任务
            supplier.accept(chain);
        } else {
            lastAsync = true; //有异步任务
            completableFuture.thenComposeAsync((nullChainBase) -> {
                supplier.accept(nullChainBase);
                return CompletableFuture.completedFuture(null);
            }, getCT(false));//终结的方法一般都比较重, 不使用窃取线程池
            StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(5);
            completableFuture.exceptionally((e) -> {
                e.addSuppressed(new NullChainException(stackTraceElement.toString()));
                if (consumer != null) {
                    consumer.accept(e);
                } else {
                    log.error("", e);
                }

                return null;
            });
        }

    }

}
