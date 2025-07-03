package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.exception.StackTraceUtil;
import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.common.function.NullTaskFun;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
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
public class NullTaskList implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class NullNode<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean isNull; //true 为null ,false 不为null
        public T value;//当前任务的值
        //是否异步 true 开始异步 false 没有开启(默认)
        public boolean async = false;

        public NullNode() {
            this.isNull = true;
        }

        public NullNode(T value) {
            this.value = value;
            this.isNull = false;
        }

        public NullNode(T value, boolean isNull, boolean async) {
            this.value = value;
            this.isNull = isNull;
            this.async = async;
        }
    }

    //收集器
    @Getter
    @Setter
    private transient NullCollect collect;
    private transient LinkedList<NullTaskFunAbs> tasks;
    protected NullNode lastResult; //最后一个任务的结果
    CompletableFuture<NullNode> lastAsyncFuture ; //最后一个任务的异步结果

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
                public NullNode nodeTask(Object value) throws RuntimeException {
                    return task.nodeTask(value);
                }
            });
        }

    }


    //运行任务返回结果  (非异步的方法执行)
    public <T> NullNode<T> runTaskAll() {
        if (tasks==null){
            tasks = new LinkedList<>();
        }
        if (lastResult != null && tasks.isEmpty()) {
            return lastResult;
        }
        //需要把lastResult的值放入到任务链的开头, 这相当于续接上一个任务的结果
        if (lastResult != null) {

            NullTaskFunAbs taskFunAbs = new NullTaskFunAbs() {
                @Override
                public NullNode nodeTask(Object value) throws RuntimeException {
                    return NullBuild.noEmpty(lastResult.value);
                }
            };
            tasks.addFirst(taskFunAbs);
        }

        NullNode<T> chain = null;
        while (!tasks.isEmpty()) {
            NullTaskFunAbs poll = tasks.poll();
            NullNode task1 = poll.nodeTask(chain == null ? null : chain.value);
            if (task1.isNull) {
                //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                NullTaskFunAbs lastPoll = tasks.peek();
                if (lastPoll != null && lastPoll.preNullEnd()) {
                    return task1;
                }
            }
            if (collect != null) {
                collect.add(task1.value);
            }
            chain = task1;
        }
        lastResult = chain;
        return chain;
    }

    //运行任务返回结果  如果调用方支持异步那么开启异步之后的节点将脱离主线程  比如ifPresent
    public <T> void runTaskAll(Consumer<NullNode<T>> supplier, Consumer<Throwable> ex) {
        if (tasks==null){
            tasks = new LinkedList<>();
        }
        if (lastResult != null && tasks.isEmpty()) {
            //如果最后一个任务不是异步那么直接执行
            if (lastAsyncFuture == null) {
                supplier.accept(lastResult);
            } else {
                CompletableFuture<NullNode> nullChainBaseCompletableFuture = CompletableFuture.completedFuture(lastResult);
                nullChainBaseCompletableFuture.thenComposeAsync((nullNode) -> {
                    supplier.accept(nullNode);
                    return CompletableFuture.completedFuture(null);
                }, getCT(false));//终结的方法一般都比较重, 不使用窃取线程池
                StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(5);
                nullChainBaseCompletableFuture.exceptionally((e) -> {
                    e.addSuppressed(new NullChainException(stackTraceElement.toString()));
                    if (ex != null) {
                        ex.accept(e);
                    } else {
                        log.error("", e);
                    }
                    return null;
                });
            }
            return;
        }


        NullNode chain = null;
        CompletableFuture<NullNode> completableFuture = null;

        if (lastResult != null) {
            NullNode<Object> objectNullNode = NullBuild.noEmpty(lastResult.value);
            if (lastAsyncFuture != null) {
                completableFuture = lastAsyncFuture;
            } else {
                //需要把lastResult的值放入到任务链的开头, 这相当于续接上一个任务的结果
                NullTaskFunAbs taskFunAbs = new NullTaskFunAbs() {
                    @Override
                    public NullNode nodeTask(Object value) throws RuntimeException {
                        return objectNullNode;
                    }
                };
                tasks.addFirst(taskFunAbs);
            }
        }

        while (!tasks.isEmpty()) {
            NullTaskFunAbs poll = tasks.poll();
            if (completableFuture == null) {
                NullNode task = poll.nodeTask(chain == null ? null : chain.value);
                if (task.isNull) {
                    //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                    NullTaskFunAbs lastPoll = tasks.peek();
                    if (lastPoll != null && lastPoll.preNullEnd()) {
                        supplier.accept(task);
                        return;
                    }
                }
                if (collect != null) {
                    collect.add(task.value);
                }
                if (task.async) {
                    completableFuture = new CompletableFuture();
                    completableFuture.complete(task);
                }
                chain = task;
            } else {
                completableFuture = completableFuture.thenComposeAsync((nullNode) -> {
                    NullNode task1 = poll.nodeTask(nullNode.value);
                    if (task1.isNull) {
                        //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                        NullTaskFunAbs lastPoll = tasks.poll();
                        if (lastPoll != null && lastPoll.preNullEnd()) {
                            supplier.accept(task1);
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    if (collect != null) {
                        collect.add(task1.value);
                    }
                    //继续执行
                    return CompletableFuture.completedFuture(task1);
                }, getCT(!poll.isHeavyTask()));
            }
        }
        lastResult = chain;
        if (completableFuture == null) {
            supplier.accept(chain);
        } else {
            completableFuture.thenComposeAsync((nullNode) -> {
                supplier.accept(nullNode);
                return CompletableFuture.completedFuture(null);
            }, getCT(false));//终结的方法一般都比较重, 不使用窃取线程池
            StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(5);
            completableFuture.exceptionally((e) -> {
                e.addSuppressed(new NullChainException(stackTraceElement.toString()));
                if (ex != null) {
                    ex.accept(e);
                } else {
                    log.error("", e);
                }
                return null;
            });
            lastAsyncFuture = completableFuture; //记录最后一个异步任务
        }

    }

}
