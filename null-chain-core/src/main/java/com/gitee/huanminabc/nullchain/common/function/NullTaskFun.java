package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * Null任务函数式接口 - 提供任务执行的函数式操作
 * 
 * <p>该接口定义了任务执行的函数式操作，用于在Null链中执行任务。
 * 通过函数式编程的方式，提供灵活的任务执行能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>任务执行：提供任务执行的函数式操作</li>
 *   <li>节点创建：创建任务节点</li>
 *   <li>异常处理：支持运行时异常处理</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>在Null链中执行自定义任务</li>
 *   <li>创建任务节点</li>
 *   <li>函数式编程的任务处理</li>
 * </ul>
 * 
 * @param <T> 任务处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTaskList.NullNode 任务节点类
 */
@FunctionalInterface
public interface NullTaskFun<T> {
    /**
     * 创建任务节点
     * 
     * @param preValue 上一个任务的值
     * @return 任务节点
     * @throws RuntimeException 任务执行过程中的运行时异常
     */
    NullTaskList.NullNode<T> nodeTask(T preValue) throws  RuntimeException;
}
