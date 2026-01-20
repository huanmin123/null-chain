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

    /**
     * 创建一个空的Null节点
     * 
     * <p>返回一个表示空值的Null节点，用于初始化空链或表示null值。</p>
     * 
     * @param <T> 节点值的类型
     * @return 空的Null节点，isNull为true
     */
    public static <T> NullTaskList.NullNode<T> empty() {
        return new NullTaskList.NullNode<>();
    }

    /**
     * 创建一个非空的Null节点
     * 
     * <p>根据给定的对象创建一个Null节点。如果对象为null，则创建空节点；否则创建非空节点。
     * 此方法在内部使用，用于构建任务链中的节点。</p>
     * 
     * @param <T> 节点值的类型
     * @param object 要包装的对象，可以为null
     * @return Null节点，如果object为null则isNull为true，否则为false
     */
    public static <T> NullTaskList.NullNode<T> noEmpty(T object) {
        return new NullTaskList.NullNode<>(object);
    }

    /**
     * 创建一个非空的Null链实例
     * 
     * <p>根据给定的日志记录器和任务列表创建一个新的Null链实例。
     * 此方法用于内部构建，将任务列表和日志记录器组合成可用的链式对象。</p>
     * 
     * @param <T> 链中值的类型
     * @param linkLog 链式操作的日志记录器，用于记录操作过程
     * @param taskList 任务列表，包含要执行的任务链
     * @return 新的Null链实例
     */
    public static <T> NullChain<T> noEmpty(StringBuilder linkLog, NullTaskList taskList) {
        return new NullChainBase<>(linkLog, taskList);
    }

    /**
     * 创建一个忙碌的Null链实例（别名方法）
     * 
     * <p>此方法是noEmpty方法的别名，用于语义化表达"忙碌"状态。
     * 实际功能与noEmpty相同。</p>
     * 
     * @param <T> 链中值的类型
     * @param linkLog 链式操作的日志记录器
     * @param taskList 任务列表
     * @return 新的Null链实例
     * @see #noEmpty(StringBuilder, NullTaskList)
     */
    public static <T> NullChain<T> busy(StringBuilder linkLog, NullTaskList taskList) {
        return noEmpty(linkLog, taskList);
    }

    /**
     * 将NullKernelAbstract对象转换为NullChain
     * 
     * <p>在不同的leaf实现中，需要将NullKernelAbstract转换为NullChain时使用此方法。
     * 如果对象已经是NullChainBase实例，则直接返回；否则创建新的NullChainBase实例。</p>
     * 
     * @param <X> 链中值的类型
     * @param o Null内核抽象对象
     * @return NullChain实例
     */
    @SuppressWarnings("unchecked")
    public static <X> NullChain<X> busy(NullKernelAbstract o) {
        if (o instanceof NullChainBase) {
            return (NullChain<X>) o;
        }
        return new NullChainBase<>(o.linkLog, o.taskList);
    }

    /**
     * 将NullKernelAbstract对象转换为NullCalculate
     * 
     * <p>将Null内核抽象对象转换为计算链对象，用于数值计算操作。</p>
     * 
     * @param <X> 计算值的类型，必须是BigDecimal或其子类
     * @param o Null内核抽象对象
     * @return NullCalculate实例
     */
    @SuppressWarnings("unchecked")
    public static <X extends java.math.BigDecimal> NullCalculate<X> busyCalc(NullKernelAbstract o) {
        return (NullCalculate<X>) o;
    }

    /**
     * 将NullKernelAbstract对象转换为NullStream
     * 
     * <p>将Null内核抽象对象转换为流链对象，用于流式操作。</p>
     * 
     * @param <X> 流中元素的类型
     * @param o Null内核抽象对象
     * @return NullStream实例
     */
    @SuppressWarnings("unchecked")
    public static <X> NullStream<X> busyStream(NullKernelAbstract o) {
        return (NullStream<X>) o;
    }

    /**
     * 将NullKernelAbstract对象转换为NullCheck
     * 
     * <p>将Null内核抽象对象转换为检查链对象，用于多级判空操作。</p>
     * 
     * @param <X> 检查值的类型
     * @param o Null内核抽象对象
     * @return NullCheck实例
     */
    @SuppressWarnings("unchecked")
    public static <X> NullCheck<X> busyCheck(NullKernelAbstract o) {
        return (NullCheck<X>) o;
    }

    /**
     * 创建Int流链实例
     * 
     * <p>根据Null内核抽象对象创建Int流链，用于处理整数流操作。</p>
     * 
     * @param <T> 泛型占位符（未使用）
     * @param o Null内核抽象对象
     * @return NullIntStream实例
     */
    public static <T> NullIntStream busyIntStream(NullKernelAbstract o) {
        return  new NullIntStreamBase( o.linkLog, o.taskList);
    }

    /**
     * 创建Long流链实例
     * 
     * <p>根据Null内核抽象对象创建Long流链，用于处理长整数流操作。</p>
     * 
     * @param <T> 泛型占位符（未使用）
     * @param o Null内核抽象对象
     * @return NullLongStream实例
     */
    public static <T> NullLongStream busyLongStream(NullKernelAbstract o) {
        return new NullLongStreamBase(o.linkLog, o.taskList);
    }

    /**
     * 创建Double流链实例
     * 
     * <p>根据Null内核抽象对象创建Double流链，用于处理双精度浮点数流操作。</p>
     * 
     * @param <T> 泛型占位符（未使用）
     * @param o Null内核抽象对象
     * @return NullDoubleStream实例
     */
    public static <T> NullDoubleStream busyDoubleStream(NullKernelAbstract o) {
        return new NullDoubleStreamBase(o.linkLog, o.taskList);
    }

    /**
     * 创建计算链实例
     * 
     * <p>根据日志记录器和任务列表创建计算链实例，用于BigDecimal数值计算操作。</p>
     * 
     * @param <T> 计算值的类型，必须是BigDecimal或其子类
     * @param linkLog 链式操作的日志记录器
     * @param nullTaskList 任务列表
     * @return NullCalculate实例
     */
    public static <T extends java.math.BigDecimal> NullCalculate<T> busyCalc(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullCalculateBase<T>(linkLog, nullTaskList);
    }

    /**
     * 创建流链实例
     * 
     * <p>根据日志记录器和任务列表创建流链实例，用于集合和流的操作。</p>
     * 
     * @param <T> 流中元素的类型
     * @param linkLog 链式操作的日志记录器
     * @param nullTaskList 任务列表
     * @return NullStream实例
     */
    public static <T> NullStream<T> busyStream(StringBuilder linkLog, NullTaskList nullTaskList) {
        return new NullStreamBase<T>(linkLog, nullTaskList);
    }

    /**
     * 创建HTTP链实例
     * 
     * <p>根据URL、日志记录器和任务列表创建HTTP链实例，用于HTTP请求操作。</p>
     * 
     * @param <T> HTTP响应值的类型
     * @param url HTTP请求的URL地址
     * @param linkLog 链式操作的日志记录器
     * @param nullTaskList 任务列表
     * @return OkHttp实例
     */
    public static <T> OkHttp<T> busyHttp(String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase<T>(url,linkLog, nullTaskList);
    }

    /**
     * 创建HTTP链实例（带名称）
     * 
     * <p>根据HTTP名称、URL、日志记录器和任务列表创建HTTP链实例。
     * 此方法允许为HTTP请求指定一个名称，用于区分不同的HTTP客户端配置。</p>
     * 
     * @param <T> HTTP响应值的类型
     * @param httpName HTTP客户端的名称，用于标识不同的HTTP配置
     * @param url HTTP请求的URL地址
     * @param linkLog 链式操作的日志记录器
     * @param nullTaskList 任务列表
     * @return OkHttp实例
     */
    public static <T> OkHttp<T> busyHttp(String httpName, String url, StringBuilder linkLog, NullTaskList nullTaskList) {
        return new OkHttpBase<T>(httpName,url,linkLog, nullTaskList);
    }

    /**
     * 创建检查链实例
     * 
     * <p>根据日志记录器、任务列表和检查节点列表创建检查链实例，用于多级判空操作。</p>
     * 
     * @param <T> 检查值的类型
     * @param linkLog 链式操作的日志记录器
     * @param nullTaskList 任务列表
     * @param list 检查节点列表，定义多级判空的路径
     * @return NullCheck实例
     */
    public static <T> NullCheck<T> busyCheck(StringBuilder linkLog, NullTaskList nullTaskList, List<NullCheckBase.NullCheckNode> list) {
        return new NullCheckBase<T>(linkLog, nullTaskList, list);
    }

    /**
     * 将数组转换为NullChain数组
     * 
     * <p>将普通对象数组转换为NullChain数组。对于数组中的每个元素：
     * - 如果元素为null，则创建空的Null链（Null.empty()）
     * - 如果元素不为null，则创建包含该元素的Null链（Null.of(element)）</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * String[] arr = {"a", null, "b"};
     * NullChain<String>[] chains = NullBuild.arrayToNullChain(arr);
     * // chains[0] 包含 "a"
     * // chains[1] 是空链
     * // chains[2] 包含 "b"
     * }</pre>
     * 
     * @param <T> 数组元素的类型
     * @param ts 要转换的对象数组
     * @return NullChain数组，长度与输入数组相同
     */
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
