package com.gitee.huanminabc.nullchain.base.sync;


import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;
import com.gitee.huanminabc.nullchain.common.NullCollect;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullConvertBase<T> extends NullToolsBase<T> implements NullConvert<T> {


    public NullConvertBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullConvertBase(T object, StringBuilder linkLog, NullCollect collect) {
        super(object, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> async() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        linkLog.append("async->");
        //开启异步
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.complete(value);
        return NullBuild.noEmptyAsync(completableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> async(String threadFactoryName) throws NullChainException {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        ThreadFactoryUtil.addExecutor(threadFactoryName);
        linkLog.append("async->");
        //开启异步
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.complete(value);
        return NullBuild.noEmptyAsync(completableFuture, linkLog, threadFactoryName, collect);
    }

    @Override
    public <U> NullChain<U> type(Class<U> uClass) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (uClass == null) {
            throw new NullChainException(linkLog.append("type? ").append("转换类型不能为空").toString());
        }
        if (uClass.isInstance(value)) {
            linkLog.append("type->");
            return NullBuild.noEmpty(uClass.cast(value), linkLog, collect);
        } else {
            linkLog.append("type? ").append("类型不匹配 ").append(value.getClass().getName()).append(" vs ").append(uClass.getName());
            return NullBuild.empty(linkLog, collect);
        }
    }

    @Override
    public <U> NullChain<U> type(U uClass) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (uClass == null) {
            throw new NullChainException(linkLog.append("type? ").append("转换类型不能为空").toString());
        }
        return type((Class<U>) uClass.getClass());
    }



    @Override
    public <C> NullStream<C> toStream() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        return toStream((Class) value.getClass());
    }



    @Override
    public <V> NullStream<V> toParallelStream() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        return toParallelStream((Class) value.getClass());
    }


    @Override
    public NullCalculate<BigDecimal> toCalc() {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        if (value instanceof Number) {
            linkLog.append("toCalc->");
            return NullBuild.noEmptyCalc(BigDecimal.valueOf(((Number) value).doubleValue()), linkLog, collect);
        }
        //如果是字符串,判断是否是数字
        if (value instanceof String) {
            try {
                BigDecimal bigDecimal = new BigDecimal((String) value);
                linkLog.append("toCalc->");
                return NullBuild.noEmptyCalc(bigDecimal, linkLog, collect);
            } catch (Exception e) {
                linkLog.append("toCalc? ");
                throw new NullChainException(linkLog.append("toCalc? ").append(value).append(" 不是数字").toString());
            }
        }
        throw new NullChainException(linkLog.append("toCalc? ").append(value.getClass()).append("类型不兼容Number").toString());
    }

    @Override
    public NullCalculate<BigDecimal> toCalc(Number defaultValue) {
        Object newValue = value;
        if (isNull) {
            if (defaultValue == null) {
                linkLog.append("toCalc? ");
                return NullBuild.emptyCalc(linkLog, collect);
            }
            newValue= defaultValue;
        }
        if (newValue instanceof Number) {
            linkLog.append("toCalc->");
            return NullBuild.noEmptyCalc(BigDecimal.valueOf(((Number) newValue).doubleValue()), linkLog, collect);
        }
        //如果是字符串,判断是否是数字
        if (value instanceof String) {
            try {
                BigDecimal newValueStr = new BigDecimal((String) newValue);
                linkLog.append("toCalc->");
                return NullBuild.noEmptyCalc(newValueStr, linkLog, collect);
            } catch (Exception e) {
                linkLog.append("toCalc? ");
                throw new NullChainException(linkLog.append("toCalc? ").append(value).append(" 不是数字").toString());
            }
        }
        throw new NullChainException(linkLog.append("toCalc? ").append(value.getClass()).append("类型不兼容Number").toString());
    }


    private  <V> NullStream<V> toStream(Class<V> type) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (type == null) {
            throw new NullChainException(linkLog.append("toStream? ").append("type must not be null").toString());
        }
        if (value instanceof Collection) {
            linkLog.append("toStream->");
            Collection collection = (Collection) value;
            return NullBuild.noEmptyStream((V) collection.stream(), linkLog, collect);
        }
        //数组
        if (value instanceof Object[]) {
            linkLog.append("toStream->");
            Object[] array = (Object[]) value;
            return NullBuild.noEmptyStream((V) Stream.of(array), linkLog, collect);
        }
        //map
        if (value instanceof java.util.Map) {
            linkLog.append("toStream->");
            java.util.Map map = (java.util.Map) value;
            return NullBuild.noEmptyStream((V) map.entrySet().stream(), linkLog, collect);
        }


        throw new NullChainException(linkLog.append("toStream? ").append(value.getClass()).append("类型不支持转换为Stream").toString());
    }
    private  <V> NullStream<V> toParallelStream(Class<V> type) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (type == null) {
            throw new NullChainException(linkLog.append("toParallelStream? ").append("type must not be null").toString());
        }
        if (value instanceof Collection) {
            linkLog.append("toParallelStream->");
            Collection collection = (Collection) value;
            return NullBuild.noEmptyStream((V) collection.parallelStream(), linkLog, collect);
        }
        //数组
        if (value instanceof Object[]) {
            linkLog.append("toParallelStream->");
            Object[] array = (Object[]) value;
            return NullBuild.noEmptyStream((V) Stream.of(array).parallel(), linkLog, collect);
        }
        //map
        if (value instanceof java.util.Map) {
            linkLog.append("toParallelStream->");
            java.util.Map map = (java.util.Map) value;
            return NullBuild.noEmptyStream((V) map.entrySet().stream().parallel(), linkLog, collect);
        }
        throw new NullChainException(linkLog.append("toParallelStream? ").append(value.getClass()).append("类型不支持转换为Stream").toString());
    }
}
