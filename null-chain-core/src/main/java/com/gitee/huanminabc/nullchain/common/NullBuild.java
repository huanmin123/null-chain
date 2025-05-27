package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.member.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.member.calculate.NullCalculateBase;
import com.gitee.huanminabc.nullchain.member.stream.NullStream;
import com.gitee.huanminabc.nullchain.member.stream.NullStreamBase;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

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








    public static <T> NullChain<T> empty(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullChainBase<T>(linkLog, true, nullChainCollect,taskList);
    }
    //将NullKernelAbstract转换为 NullChainBase
    public static <T> NullChain<T> busy(NullKernelAbstract o) {
        if (o instanceof NullChainBase) {
            return (NullChain)o;
        }
        NullChainBase<T> tNullChain = (NullChainBase<T>) new NullChainBase<>(o.value, o.linkLog, o.collect, o.taskList);
        tNullChain.setNull(o.isNull);
        return tNullChain;
    }
    public static <T> NullChain<T> busy(NullTaskList taskList) {
        return noEmpty(null,new StringBuilder(),new NullCollect(),taskList);
    }
    //过程中使用
    public static <T> NullChain<T> noEmpty(T object, StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullChainBase<>(object, linkLog, nullChainCollect,taskList);
    }


    public static <T> NullStream<T> emptyStream(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullStreamBase<T>(linkLog, true, nullChainCollect,taskList);
    }
    public static <T> NullStream<T> noEmptyStream(T object, StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullStreamBase<>(object, linkLog, nullChainCollect,taskList);
    }

    public static  NullCalculate<BigDecimal> emptyCalc(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        return new NullCalculateBase<>(linkLog,true, collect,taskList);
    }
    public static NullCalculate<BigDecimal> noEmptyCalc(BigDecimal value, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        return new NullCalculateBase<>( value, linkLog, collect,taskList);
    }




    //因为NULLExt==NullChain 但是 因为NULLExt!=NullChainBase, 在NULLExt转化为NullChainBase的时候会识别导致获取内部的值失败
    //这里兼容一下
    public static <T> T getValue(NullChain<T> nullChain) {
        if (nullChain == null) {
            return null;
        }
        if (nullChain instanceof NullChainBase) {
            return ((NullChainBase<T>) nullChain).value;
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
                nullChains[i] = new NullChainBase<>(new StringBuilder(), true, new NullCollect(),new NullTaskList());
            } else {
                nullChains[i] = new NullChainBase<>(t, new StringBuilder(), new NullCollect(),new NullTaskList());
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
