package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculateBase;
import com.gitee.huanminabc.nullchain.leaf.copy.NullCopy;
import com.gitee.huanminabc.nullchain.leaf.copy.NullCopyBase;
import com.gitee.huanminabc.nullchain.leaf.date.NullDate;
import com.gitee.huanminabc.nullchain.leaf.date.NullDateBase;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpChain;
import com.gitee.huanminabc.nullchain.leaf.json.NullJson;
import com.gitee.huanminabc.nullchain.leaf.json.NullJsonBase;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStreamBase;
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


    public static <T> NullTaskList.NullNode<T> empty() {
        return new NullTaskList.NullNode<>();
    }


    public static <T> NullChain<T> empty(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullChainBase<>(linkLog, nullChainCollect, taskList);
    }


    //过程中使用
    public static <T> NullTaskList.NullNode<T> noEmpty(T object) {
        return new NullTaskList.NullNode<>(object);
    }

    public static <T> NullChain<T> noEmpty(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullChainBase<>(linkLog, nullChainCollect, taskList);
    }

    public static <T> NullChain<T> busy(StringBuilder linkLog, NullCollect nullCollect, NullTaskList taskList) {
        return noEmpty(linkLog, nullCollect, taskList);
    }


    public static <T> NullStream<T> emptyStream(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullStreamBase<T>(linkLog, nullChainCollect, taskList);
    }

    public static <T> NullStream<T> noEmptyStream(StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new NullStreamBase<>(linkLog, nullChainCollect, taskList);
    }

    public static NullCalculate<BigDecimal> emptyCalc(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        return new NullCalculateBase<>(linkLog, collect, taskList);
    }

    public static NullCalculate<BigDecimal> noEmptyCalc(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        return new NullCalculateBase<>( linkLog, collect, taskList);
    }

    //创建一个空的OkHttpUtil
    public static <T> OkHttpChain emptyHttp(StringBuilder linkLog) {
        return new OkHttp<>(true, linkLog);
    }

    public static <T> OkHttpChain notEmptyHttp(String url, T value, StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return notEmptyHttp(OkHttp.DEFAULT_THREAD_FACTORY_NAME, url, value, linkLog, nullChainCollect, taskList);
    }

    public static <T> OkHttpChain notEmptyHttp(String httpName, String url, T value, StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        return new OkHttp<>(httpName, url, value, linkLog, nullChainCollect, taskList);
    }





    //将NullKernelAbstract转换为 NullChainBase
    public static <T> NullChain<T> busy(NullKernelAbstract o) {
        if (o instanceof NullChainBase) {
            return (NullChain) o;
        }
        return new NullChainBase<>(o.linkLog, o.collect, o.taskList);
    }


    public static <T> NullDate busyDate(NullKernelAbstract o) {
        if (o instanceof NullDateBase) {
            return (NullDate) o;
        }
        return new NullDateBase<>(o.linkLog, o.collect, o.taskList);
    }

    public static <T> NullDate<T> busyDate(StringBuilder linkLog, NullCollect nullCollect, NullTaskList nullTaskList) {
        return new NullDateBase<>(linkLog, nullCollect, nullTaskList);
    }


    public static NullJson busyJson(NullKernelAbstract o) {
        if (o instanceof NullJsonBase) {
            return (NullJson) o;
        }
        return new NullJsonBase<>(o.linkLog, o.collect, o.taskList);
    }

    public static <T> NullJson<T> busyJson(StringBuilder linkLog, NullCollect nullCollect, NullTaskList nullTaskList) {
        return new NullJsonBase<T>(linkLog, nullCollect, nullTaskList);
    }


    public static NullCopy busyCopy(NullKernelAbstract o) {
        if (o instanceof NullCopyBase) {
            return (NullCopy) o;
        }
        return new NullCopyBase<>(o.linkLog, o.collect, o.taskList);
    }

    public static NullCalculate busyCalc(NullKernelAbstract o) {
        if (o instanceof NullCalculateBase) {
            return (NullCalculate) o;
        }
        return new NullCalculateBase<>(o.linkLog, o.collect, o.taskList);
    }
    public static NullStream busyStream(NullKernelAbstract o) {
        if (o instanceof NullStreamBase) {
            return (NullStream) o;
        }
        return new NullStreamBase<>(o.linkLog, o.collect, o.taskList);
    }
    public static NullCalculate busyCalc(StringBuilder linkLog, NullCollect nullCollect, NullTaskList nullTaskList) {
        return new NullCalculateBase(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullCopy<T> busyCopy(StringBuilder linkLog, NullCollect nullCollect, NullTaskList nullTaskList) {
        return new NullCopyBase<T>(linkLog, nullCollect, nullTaskList);
    }
    public static <T> NullStream<T> busyStream(StringBuilder linkLog, NullCollect nullCollect, NullTaskList nullTaskList) {
        return new NullStreamBase<T>(linkLog, nullCollect, nullTaskList);
    }


    //将数组转换为空链
    public static <T> NullChain<T>[] arrayToNullChain(T[] ts) {
        NullChain[] nullChains = new NullChain[ts.length];
        for (int i = 0; i < ts.length; i++) {
            T t = ts[i];
            if (Null.is(t)) {
                nullChains[i] = Null.empty();
            } else {
                nullChains[i] = Null.of(t);
            }
        }
        return nullChains;
    }

    public static <T, R> R taskRun(T value, NullTask<T, R> nullTask, StringBuilder linkLog, Object... params) throws NullChainCheckException {
        NullMap<String, Object> map = NullMap.newHashMap();
        Object[] objects = params == null ? new Object[]{} : params;
        //校验参数类型和长度
        NullType nullType = nullTask.checkTypeParams();
        try {
            if (nullType != null) {
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

    public static <T, R> R toolRun(T value, NullTool<T, R> nullTool, StringBuilder linkLog, Object... params) throws NullChainCheckException {
        NullMap<String, Object> map = NullMap.newHashMap();
        Object[] objects = params == null ? new Object[]{} : params;
        //校验参数类型和长度
        NullType nullType = nullTool.checkTypeParams();
        try {
            if (nullType != null) {
                nullType.checkType(objects, map);
            }
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 工具参数校验失败").toString());
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(objects);
        try {
            nullTool.init(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 初始化失败: ").toString());
        }
        try {
            return nullTool.run(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append("tool? ").append(nullTool.getClass().getName()).append(" 运行失败: ").toString());
        }

    }


}
