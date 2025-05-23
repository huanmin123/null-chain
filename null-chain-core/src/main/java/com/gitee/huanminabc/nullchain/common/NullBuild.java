package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsyncBase;
import com.gitee.huanminabc.nullchain.base.async.calculate.NullCalculateAsync;
import com.gitee.huanminabc.nullchain.base.async.calculate.NullCalculateAsyncBase;
import com.gitee.huanminabc.nullchain.base.async.stream.NullStreamAsync;
import com.gitee.huanminabc.nullchain.base.async.stream.NullStreamAsyncBase;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.NullChainBase;
import com.gitee.huanminabc.nullchain.base.sync.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.base.sync.calculate.NullCalculateBase;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStreamBase;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@Slf4j
public class NullBuild {
    static {
        //注册内置的转换器
        try {
            Class.forName("com.gitee.huanminabc.nullchain.register.RegisterTool");
            Class.forName("com.gitee.huanminabc.nullchain.register.RegisterTask");
        } catch (ClassNotFoundException e) {
            log.debug("空链 NullBuild 注册任务和工具失败, 应该是没有导入相关的依赖,如果不需要可以忽略");
        }
    }








    public static <T> NullChain<T> empty(StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullChainBase<T>(linkLog, true, nullChainCollect);
    }

    public static <T> NullChainAsyncBase<T> emptyAsync(StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullChainAsyncBase<T>(linkLog, true, nullChainCollect);
    }

    //过程中使用
    public static <T> NullChain<T> noEmpty(T object, StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullChainBase<>(object, linkLog, nullChainCollect);
    }



    //过程中使用
    public static <T> NullChainAsyncBase<T> noEmptyAsync(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        //添加到收集器里面
        completableFuture.thenApplyAsync((T t) -> {
            collect.add(t);
            return t;
        });
        return new NullChainAsyncBase<T>(completableFuture, linkLog, threadFactoryName, collect);
    }






    public static <T> NullStream<T> emptyStream(StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullStreamBase<T>(linkLog, true, nullChainCollect);
    }
    public static <T> NullStream<T> noEmptyStream(T object, StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullStreamBase<>(object, linkLog, nullChainCollect);
    }

    public static <T> NullStreamAsync<T> emptyStreamAsync(StringBuilder linkLog, NullCollect nullChainCollect) {
        return new NullStreamAsyncBase<T>(linkLog, true, nullChainCollect);
    }

    public static <T> NullStreamAsync<T> noEmptyStreamAsync(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        return new NullStreamAsyncBase<T>(completableFuture, linkLog, threadFactoryName, collect);
    }


    public static <V extends Number> NullCalculate<V> emptyCalc(StringBuilder linkLog, NullCollect collect) {
        return new NullCalculateBase(linkLog,true, collect);
    }
    public static <V extends Number> NullCalculate<V> noEmptyCalc(BigDecimal value, StringBuilder linkLog, NullCollect collect) {
        return new NullCalculateBase( value, linkLog, collect);
    }
    public static <V extends Number> NullCalculateAsync<V> emptyCalcAsync(StringBuilder linkLog, NullCollect collect) {
        return new NullCalculateAsyncBase(linkLog,true, collect);
    }
    public static <V extends Number> NullCalculateAsync<V> noEmptyCalcAsync(CompletableFuture<BigDecimal> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        return new NullCalculateAsyncBase( completableFuture, linkLog,threadFactoryName, collect);
    }



    //因为NULLExt==NullChain 但是 因为NULLExt!=NullChainBase, 在NULLExt转化为NullChainBase的时候会识别导致获取内部的值失败
    //这里兼容一下
    public static <T> T getValue(NullChain<T> nullChain) {
        if (nullChain == null) {
            return null;
        }
        if (nullChain instanceof NullChainBase) {
            return ((NullChainBase<T>) nullChain).getValue();
        }
        //这种情况就是继承,无法直接利用NullChainBase的getValue方法需要自己拦截
        //类一旦继承NULLExt,走到这里那么本身肯定不是空,所以必然能获取到值,不会报错
        return nullChain.get();
    }

    //将数组转换为空链
    public static <T> NullChain<T>[] arrayToNullChain(T[] ts) {
        NullChain[] nullChains = new NullChain[ts.length];
        for (int i = 0; i < ts.length; i++) {
            T t = ts[i];
            if (Null.is(t)) {
                nullChains[i] = new NullChainBase<>(new StringBuilder(), true, new NullCollect());
            } else {
                nullChains[i] = new NullChainBase<>(t, new StringBuilder(), new NullCollect());
            }
        }
        return nullChains;
    }

    public static<T,R> R taskRun(T value, NullTask<T,R> nullTask, StringBuilder linkLog, Object... params) throws NullChainCheckException {
        NullMap<String, Object> map = NullMap.newHashMap();
        Object[] objects = params == null ? new Object[]{} : params;
        //校验参数类型和长度
        NullType nullType = nullTask.checkTypeParams();
        try {
            if (nullType != null ) {
                nullType.checkType(objects, map);
            }
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("task? ").append(nullTask.getClass().getName()).append(" 任务参数校验失败").toString());
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(objects);
        try {
            nullTask.init(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("task? ").append(nullTask.getClass().getName()).append(" 初始化失败: ").toString());
        }
        try {
            return nullTask.run(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("task? ").append(nullTask.getClass().getName()).append(" 运行失败: ").toString());
        }
    }

    public static <T,R> R toolRun(T value, NullTool<T,R> nullTool, StringBuilder linkLog, Object... params) throws NullChainCheckException {
        NullMap<String, Object> map = NullMap.newHashMap();
        Object[] objects = params == null ? new Object[]{} : params;
        //校验参数类型和长度
        NullType nullType = nullTool.checkTypeParams();
        try {
            if (nullType != null  ) {
                nullType.checkType(objects, map);
            }
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 工具参数校验失败").toString());
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(objects);
        try {
            nullTool.init(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e,linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 初始化失败: ").toString());
        }
        try {
            return nullTool.run(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e,linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 运行失败: ").toString());
        }

    }



}
