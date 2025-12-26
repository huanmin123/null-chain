package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculateBase;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBase;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.stream.*;
import com.gitee.huanminabc.nullchain.leaf.check.NullCheck;
import com.gitee.huanminabc.nullchain.leaf.check.NullCheckBase;
// import cleanup: removed unused imports
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Null构建器 - 提供Null链的构建功能
 * 
 * <p>该类提供了构建各种Null链实例的工厂方法，包括核心链、计算链、复制链、日期链、HTTP链、JSON链和流链等。
 * 通过统一的构建接口，为开发者提供便捷的链式操作入口。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>核心链构建：构建基础的Null链实例</li>
 *   <li>计算链构建：构建数值计算链实例</li>
 *   <li>HTTP链构建：构建HTTP请求链实例</li>
 *   <li>流链构建：构建流处理链实例</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>工厂模式：提供统一的构建接口</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值安全：所有构建的链都支持空值安全操作</li>
 *   <li>链式调用：支持链式编程风格</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 核心链接口
 * @see NullCalculate 计算链接口
 * @see OkHttp HTTP链接口
 * @see NullStream 流链接口
 */

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
    @SuppressWarnings("unchecked")
    public static <X> NullChain<X> busy(NullKernelAbstract<?> o) {
        if (o instanceof NullChainBase) {
            return (NullChain<X>) o;
        }
        return new NullChainBase<>(o.linkLog, o.taskList);
    }


    @SuppressWarnings("unchecked")
    public static <X extends java.math.BigDecimal> NullCalculate<X> busyCalc(NullKernelAbstract<?> o) {
        return (NullCalculate<X>) o;
    }

    @SuppressWarnings("unchecked")
    public static <X> NullStream<X> busyStream(NullKernelAbstract<?> o) {
        return (NullStream<X>) o;
    }

    @SuppressWarnings("unchecked")
    public static <X> NullCheck<X> busyCheck(NullKernelAbstract<?> o) {
        return (NullCheck<X>) o;
    }



    public static <T> NullIntStream busyIntStream(NullKernelAbstract<T> o) {
        return  new NullIntStreamBase( o.linkLog, o.taskList);
    }

    public static <T> NullLongStream busyLongStream(NullKernelAbstract<T> o) {
        return new NullLongStreamBase(o.linkLog, o.taskList);
    }


    public static <T> NullDoubleStream busyDoubleStream(NullKernelAbstract<T> o) {
        return new NullDoubleStreamBase(o.linkLog, o.taskList);
    }


    public static <T extends java.math.BigDecimal> NullCalculate<T> busyCalc(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullCalculateBase<T>(linkLog, nullTaskList);
    }

    public static <T> NullStream<T> busyStream(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullStreamBase<T>(linkLog, nullTaskList);
    }

    public static <T> OkHttp<T> busyHttp(String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase<T>(url,linkLog, nullTaskList);
    }
    public static <T> OkHttp<T> busyHttp(String httpName, String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase<T>(httpName,url,linkLog, nullTaskList);
    }

    public static <T> NullCheck<T> busyCheck(StringBuilder linkLog, NullTaskList nullTaskList, List<NullCheckBase.NullCheckNode> list) {
        return new NullCheckBase<T>(linkLog, nullTaskList, list);
    }



    //将数组转换为空链
    @SuppressWarnings("unchecked")
    public static <T> NullChain<T>[] arrayToNullChain(T[] ts) {
        NullChain<T>[] nullChains = new NullChain[ts.length];
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
