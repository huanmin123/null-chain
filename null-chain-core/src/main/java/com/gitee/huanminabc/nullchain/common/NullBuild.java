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
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBase;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.json.NullJson;
import com.gitee.huanminabc.nullchain.leaf.json.NullJsonBase;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStreamBase;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;

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



    //过程中使用
    public static <T> NullTaskList.NullNode<T> noEmpty(T object) {
        return new NullTaskList.NullNode<>(object);
    }

    public static <T> NullChain<T> noEmpty(StringBuilder linkLog, NullTaskList taskList) {
        return new NullChainBase<>(linkLog, taskList);
    }

    public static <T> NullChain<T> busy(StringBuilder linkLog, NullTaskList taskList) {
        return noEmpty(linkLog, taskList);
    }


    //因为在不同的leaf中转化为NullChain需要 , 这样做统一的兼容
    public static <T> NullChain<T> busy(NullKernelAbstract o) {
        if (o instanceof NullChainBase) {
            return (NullChain) o;
        }
        return new NullChainBase<>(o.linkLog, o.taskList);
    }

    public static <T> NullDate busyDate(NullKernelAbstract o) {
        return (NullDate) o;
    }

    public static NullJson busyJson(NullKernelAbstract o) {
        return (NullJson) o;
    }

    public static NullCopy busyCopy(NullKernelAbstract o) {
        return (NullCopy) o;
    }

    public static NullCalculate busyCalc(NullKernelAbstract o) {
        return (NullCalculate) o;
    }
    public static NullStream busyStream(NullKernelAbstract o) {
        return (NullStream) o;
    }



    public static <T> NullDate<T> busyDate(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullDateBase<>(linkLog, nullTaskList);
    }

    public static <T> NullJson<T> busyJson(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullJsonBase<T>(linkLog, nullTaskList);
    }

    public static NullCalculate busyCalc(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullCalculateBase(linkLog, nullTaskList);
    }

    public static <T> NullCopy<T> busyCopy(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullCopyBase<T>(linkLog, nullTaskList);
    }
    public static <T> NullStream<T> busyStream(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullStreamBase<T>(linkLog, nullTaskList);
    }

    public static OkHttp busyHttp(String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase(url,linkLog, nullTaskList);
    }
    public static OkHttp busyHttp(String httpName, String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase(httpName,url,linkLog, nullTaskList);
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
}
