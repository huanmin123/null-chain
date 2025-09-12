package com.gitee.huanminabc.nullchain;

import com.gitee.huanminabc.nullchain.core.ext.NullChainExt;

/**
 * Null扩展接口 - 提供对象上的直接链式操作功能
 * 
 * <p>该接口允许在任何对象上直接调用链式操作，但需要保证对象变量不为空。
 * 如果使用了Null.createEmpty方式创建的对象，那么返回的一定不是null而是一个空对象，
 * 可以通过空链的方式来处理。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>直接链式操作：在对象上直接调用链式方法</li>
 *   <li>空值安全：处理空对象的情况</li>
 *   <li>类型转换：支持类型转换操作</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>当对象已经确定不为空时</li>
 *   <li>需要直接在对象上进行链式操作时</li>
 *   <li>使用Null.createEmpty创建的空对象</li>
 * </ul>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>使用前需要确保对象不为null</li>
 *   <li>空对象会通过空链方式处理</li>
 *   <li>支持所有NullChain的操作</li>
 * </ul>
 * 
 * @param <T> 扩展对象的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChainExt 链式扩展接口
 */
public interface NullExt<T> extends NullChainExt<T>{

}
