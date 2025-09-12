package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;

/**
 * Null双参数函数式接口 - 提供两个参数的函数操作
 * 
 * <p>该接口定义了接受两个参数并返回结果的函数式操作。
 * 第一个参数是Null链对象，第二个参数是实际的值，用于在Null链中进行复杂的函数操作。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>双参数函数：接受Null链对象和实际值两个参数</li>
 *   <li>链式集成：与Null链框架无缝集成</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>序列化支持：支持序列化传输</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>需要在函数中访问Null链对象时</li>
 *   <li>复杂的业务逻辑处理</li>
 *   <li>需要链式操作的函数式编程</li>
 * </ul>
 * 
 * @param <NULL> Null链对象的类型
 * @param <T> 输入值的类型
 * @param <R> 返回值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Serializable 序列化接口
 */
@FunctionalInterface
public interface NullFun2<NULL,T, R> extends Serializable {
    /**
     * 应用函数操作
     * 
     * @param n Null链对象
     * @param t 输入值
     * @return 函数操作的结果
     */
    R apply(NULL n,T t);
}