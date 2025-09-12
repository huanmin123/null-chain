package com.gitee.huanminabc.nullchain.common.function;

import com.gitee.huanminabc.nullchain.common.NullChainException;

import java.io.Serializable;

/**
 * Null函数异常接口 - 提供可能抛出异常的函数操作
 * 
 * <p>该接口定义了可能抛出NullChainException的函数操作。
 * 用于在Null链中处理可能抛出异常的函数操作。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>函数操作：提供函数式操作</li>
 *   <li>异常处理：支持NullChainException异常处理</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>序列化支持：支持序列化传输</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>可能抛出NullChainException的函数操作</li>
 *   <li>需要异常处理的函数式编程</li>
 *   <li>Null链中的异常安全操作</li>
 * </ul>
 * 
 * @param <T> 输入值的类型
 * @param <R> 返回值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChainException Null链异常类
 * @see Serializable 序列化接口
 */
@FunctionalInterface
public interface NullFunEx<T, R> extends  Serializable {
    /**
     * 应用函数操作
     * 
     * @param t 输入值
     * @return 函数操作的结果
     * @throws NullChainException Null链异常
     */
    R apply(T t) throws NullChainException;
}