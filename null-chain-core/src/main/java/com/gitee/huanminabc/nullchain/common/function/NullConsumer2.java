package com.gitee.huanminabc.nullchain.common.function;

import java.io.Serializable;

/**
 * Null双参数消费者接口 - 提供两个参数的消费者操作
 * 
 * <p>该接口定义了接受两个参数但不返回结果的消费者操作。
 * 第一个参数是Null链对象，第二个参数是实际的值，用于在Null链中进行复杂的消费者操作。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>双参数消费者：接受Null链对象和实际值两个参数</li>
 *   <li>无返回值：执行操作但不返回结果</li>
 *   <li>链式集成：与Null链框架无缝集成</li>
 *   <li>序列化支持：支持序列化传输</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>需要在消费者中访问Null链对象时</li>
 *   <li>复杂的副作用操作</li>
 *   <li>需要链式操作的消费者模式</li>
 * </ul>
 * 
 * @param <NULL> Null链对象的类型
 * @param <T> 输入值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Serializable 序列化接口
 */
@FunctionalInterface
public interface NullConsumer2<NULL,T> extends Serializable {
    /**
     * 接受并处理参数
     * 
     * @param n Null链对象
     * @param t 输入值
     */
    void accept(NULL n,T t);
}
