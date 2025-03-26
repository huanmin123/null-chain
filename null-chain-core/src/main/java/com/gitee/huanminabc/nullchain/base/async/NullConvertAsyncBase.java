package com.gitee.huanminabc.nullchain.base.async;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.stream.NullStreamAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author huanmin
 * @date 2024/2/1
 */
public class NullConvertAsyncBase<T> extends NullToolsAsyncBase<T> implements NullConvertAsync<T> {
    private static final long serialVersionUID = 1L;


    public NullConvertAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullConvertAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }


    //如果序列化的时候发现value是null,那么就不序列化
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isNull) {
            throw new NullChainException("{} 序列化时发现值是空的", this.linkLog.toString());
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    // 反序列化时调用的方法
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        //给linkLog赋值,不然java反序列化后linkLog是null
        this.linkLog = new StringBuilder();
        this.collect = new NullCollect();
    }

    @Override
    public NullChain<T> sync() {
        if (isNull) {
            return NullBuild.empty(linkLog,collect);
        }
        T join = completableFuture.join();
        if (Null.is(join)) {
            return NullBuild.empty(linkLog,collect);
        }
        linkLog.append("sync->");
        return NullBuild.noEmpty(join, linkLog,collect);
    }

    @Override
    public <U> NullChainAsync<U> type(Class<U> uClass) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }

        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (uClass == null) {
                throw new NullPointerException(linkLog.append("type? 转换类型不能为空").toString());
            }
            if (uClass.isInstance(value)) {
                linkLog.append("type->");
                return uClass.cast(value);
            }
            return null;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public <U> NullChainAsync<U> type(U uClass) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        if (uClass == null) {
            completableFuture.completeExceptionally(new NullChainException( linkLog.append("type? ").append("转换类型不能为空").toString()));
            return (NullChainAsync) NullBuild.noEmptyAsync(completableFuture, linkLog, super.currentThreadFactoryName, collect);
        }
        return type((Class<U>) uClass.getClass());
    }


    @Override
    public <V> NullStreamAsync<V> toStream(Class<V> type) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<V> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (type == null) {
                throw new NullChainException(linkLog.append("toStream? ").append("type must not be null").toString());
            }
            if (value instanceof Collection) {
                linkLog.append("toStream->");
                Collection collection = (Collection) value;
                return (V) collection.stream();
            }
            //数组
            if (value instanceof Object[]) {
                linkLog.append("toStream->");
                Object[] array = (Object[]) value;
                return (V) Stream.of(array);
            }
            throw new NullChainException(linkLog.append("toStream? ").append(value.getClass()).append("类型不支持转换为Stream").toString());
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <V> NullStreamAsync<V> toStream() {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<V> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            linkLog.append("toStream->");
            return (V)toStream((Class) value.getClass());
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <V> NullStreamAsync<V> toParallelStream(Class<V> type) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<V> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (type == null) {
                throw new NullChainException(linkLog.append("toParallelStream? ").append("type must not be null").toString());
            }
            if (value instanceof Collection) {
                linkLog.append("toParallelStream->");
                Collection collection = (Collection) value;
                return (V) collection.parallelStream();
            }
            //数组
            if (value instanceof Object[]) {
                linkLog.append("toParallelStream->");
                Object[] array = (Object[]) value;
                return (V) Stream.of(array).parallel();
            }
            throw new NullChainException(linkLog.append("toParallelStream? ").append(value.getClass()).append("类型不支持转换为Stream").toString());
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <V> NullStreamAsync<V> toParallelStream() {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<V> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            linkLog.append("toParallelStream->");
            return (V)toParallelStream((Class) value.getClass());
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }


}

