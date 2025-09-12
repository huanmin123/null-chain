package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * Null内核接口 - 提供异步执行功能
 * 
 * <p>这是Null链框架的核心接口，定义了异步执行的基本功能。
 * 所有Null链组件都继承此接口，提供统一的异步执行能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>异步执行：将同步操作转换为异步执行</li>
 *   <li>线程池管理：支持自定义线程池执行</li>
 *   <li>并发控制：提供并发执行能力</li>
 * </ul>
 * 
 * <h3>设计原则：</h3>
 * <ul>
 *   <li>统一接口：所有Null链组件都支持异步执行</li>
 *   <li>灵活配置：支持默认和自定义线程池</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 * </ul>
 * 
 * @param <T> 内核处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 */
public interface NullKernel<T> {
    
    /**
     * 将当前操作转换为异步执行
     * 
     * <p>该方法会将当前Null链的操作转换为异步执行，使用默认的线程池。
     * 异步执行不会阻塞当前线程，适合处理耗时操作。</p>
     * 
     * @return 异步执行的Null链
     * 
     * @example
     * <pre>{@code
     * Null.of(data)
     *     .async()  // 异步执行后续操作
     *     .map(heavyOperation)
     *     .orElse(defaultValue);
     * }</pre>
     */
    NullChain<T> async();
    
    /**
     * 使用指定线程池将当前操作转换为异步执行
     * 
     * <p>该方法会将当前Null链的操作转换为异步执行，使用指定的线程池。
     * 可以通过线程池名称来指定特定的执行环境。</p>
     * 
     * @param threadFactoryName 线程池工厂名称
     * @return 异步执行的Null链
     * 
     * @example
     * <pre>{@code
     * Null.of(data)
     *     .async("customThreadPool")  // 使用自定义线程池异步执行
     *     .map(heavyOperation)
     *     .orElse(defaultValue);
     * }</pre>
     */
    NullChain<T> async(String threadFactoryName);
}
