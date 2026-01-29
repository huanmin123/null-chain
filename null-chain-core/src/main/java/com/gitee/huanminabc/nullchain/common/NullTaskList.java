package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.jcommon.exception.StackTraceUtil;
import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
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
 * Null任务列表管理类
 *
 * @author huanmin
 * @version 1.1.1
 * @since 1.0.0
 */
@Slf4j
public class NullTaskList implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Null任务节点 - 表示任务执行的结果
     *
     * <p>该类封装了任务执行的结果，包括值本身和是否为空的状态。
     * 它用于在任务链中传递执行结果，确保空值安全。</p>
     *
     * @param <T> 节点值的类型
     */
    public static class NullNode<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 是否为空值，true表示值为null，false表示值不为null
         */
        public boolean isNull;
        
        /**
         * 当前任务的值
         */
        public T value;
        
        /**
         * 是否异步执行，true表示开启异步，false表示没有开启（默认）
         */
        public boolean async = false;

        /**
         * 创建一个空的Null节点
         * 
         * <p>构造一个表示空值的节点，isNull为true。</p>
         */
        public NullNode() {
            this.isNull = true;
        }

        /**
         * 创建一个非空的Null节点
         * 
         * <p>根据给定的值创建一个节点。如果值为null，则isNull为true；否则isNull为false。</p>
         * 
         * @param value 节点的值，可以为null
         */
        public NullNode(T value) {
            this.value = value;
            this.isNull = false;
        }

        /**
         * 创建一个Null节点（完整参数）
         * 
         * <p>使用指定的值、空值状态和异步标志创建节点。</p>
         * 
         * @param value 节点的值
         * @param isNull 是否为空值
         * @param async 是否异步执行
         */
        public NullNode(T value, boolean isNull, boolean async) {
            this.value = value;
            this.isNull = isNull;
            this.async = async;
        }
    }

    /**
     * 收集器，用于收集任务执行过程中的中间结果
     */
    @Getter
    @Setter
    private transient NullCollect collect;
    
    /**
     * 任务队列，存储待执行的任务
     */
    private transient LinkedList<NullTaskFunAbs> tasks;
    
    /**
     * 最后一个任务的结果
     */
    protected NullNode lastResult;
    
    /**
     * 最后一个任务的异步结果
     */
    CompletableFuture<NullNode> lastAsyncFuture;

    /**
     * 当前线程工厂名称，用于获取对应的线程池
     */
    @Setter
    protected String currentThreadFactoryName = ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME;

    /**
     * 获取当前线程池
     * 
     * <p>根据线程工厂名称获取对应的线程池。如果是默认线程且允许使用ForkJoinPool，
     * 则返回ForkJoinPool.commonPool()，因为这种线程池是共享任务的，基本不会有切换线程带来的性能损失，
     * 只适合快速且短小的任务。</p>
     * 
     * @param forkJoinPool 是否允许使用ForkJoinPool
     * @return 线程池实例
     */
    protected ExecutorService getCT(boolean forkJoinPool) {
        //如果是默认线程那么使用工作窃取线程 , 因为这种线程池是共享任务的基本不会有切换线程带来的性能损失,只适合快速且短小的任务
        if (forkJoinPool && ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME.equals(currentThreadFactoryName)) {
            return ForkJoinPool.commonPool();
        }
        return ThreadFactoryUtil.getExecutor(currentThreadFactoryName);
    }

    /**
     * 创建一个新的任务列表
     * 
     * <p>初始化任务队列为空列表。</p>
     */
    public NullTaskList() {
        tasks = new LinkedList<>();
    }

    /**
     * 添加任务到任务列表
     * 
     * <p>将任务添加到任务队列中。如果任务已经是NullTaskFunAbs类型，则直接添加；
     * 否则将其包装为NullTaskFunAbs后添加。</p>
     * 
     * @param task 要添加的任务
     */
    public void add(NullTaskFun task) {
        // 优化：使用instanceof替代isAssignableFrom，性能更好
        if (task instanceof NullTaskFunAbs) {
            tasks.add((NullTaskFunAbs) task);
        } else {
            tasks.add(new NullTaskFunAbs() {
                @Override
                public NullNode nodeTask(Object value) throws RuntimeException {
                    return task.nodeTask(value);
                }
            });
        }
    }

    /**
     * 运行所有任务并返回结果（非异步执行）
     * 
     * <p>按顺序执行任务列表中的所有任务，并返回最后一个任务的结果。
     * 如果任务列表为空且存在上次执行的结果，则直接返回上次结果。
     * 如果某个任务返回空值且下一个任务设置了preNullEnd()，则停止执行并返回空值。</p>
     * 
     * <p>执行过程中，如果配置了收集器，会将每个任务的结果添加到收集器中。</p>
     * 
     * @param <T> 返回值的类型
     * @return 最后一个任务的结果，如果没有任何任务执行则返回空节点
     */
    public <T> NullNode<T> runTaskAll() {
        if (tasks == null) {
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
        //优化：如果chain为null，说明没有任何任务执行，返回预定义的空节点，但这里需要创建新对象
        //注意：由于任务链可能为空且没有任务执行，返回新对象是必要的，保持原有逻辑
        return chain != null ? chain : new NullNode<>();
    }

    /**
     * 运行所有任务并返回结果（支持异步执行）
     * 
     * <p>按顺序执行任务列表中的所有任务，支持异步执行。如果调用方支持异步，
     * 那么开启异步之后的节点将脱离主线程执行（例如ifPresent方法）。</p>
     * 
     * <p>执行流程：</p>
     * <ol>
     *   <li>如果任务列表为空且存在上次执行的结果，直接调用supplier返回结果</li>
     *   <li>如果上次结果是异步的，则等待异步完成后再继续</li>
     *   <li>按顺序执行任务，如果任务标记为异步，则切换到异步线程执行</li>
     *   <li>如果某个任务返回空值且下一个任务设置了preNullEnd()，则停止执行</li>
     *   <li>执行完成后通过supplier返回结果，如果发生异常则通过ex处理</li>
     * </ol>
     * 
     * @param <T> 返回值的类型
     * @param supplier 成功回调，接收任务执行结果
     * @param ex 异常回调，接收执行过程中的异常，如果为null则抛出异常
     */
    public <T> void runTaskAll(Consumer<NullNode<T>> supplier, Consumer<Throwable> ex) {
        if (tasks == null) {
            tasks = new LinkedList<>();
        }
        if (lastResult != null && tasks.isEmpty()) {
            //如果最后一个任务不是异步那么直接执行
            if (lastAsyncFuture == null) {
                try {
                    supplier.accept(lastResult);
                } catch (Throwable e) {
                    if (ex != null) {
                        ex.accept(e);
                        return;
                    }else{
                        throw e;
                    }
                }
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
                NullNode task;
                try {
                    task = poll.nodeTask(chain == null ? null : chain.value);
                } catch (Throwable e) {
                    if (ex != null) {
                        ex.accept(e);
                        return;
                    } else {
                        throw e;
                    }
                }
                if (task.isNull) {
                    //向后取一个, 如果后面任务是遇到上一个任务是null那么就停止执行
                    NullTaskFunAbs lastPoll = tasks.peek();
                    if (lastPoll != null && lastPoll.preNullEnd()) {
                        try {
                            supplier.accept(task);
                        } catch (Throwable e) {
                            if (ex != null) {
                                ex.accept(e);
                                return;
                            } else {
                                throw e;
                            }
                        }
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
