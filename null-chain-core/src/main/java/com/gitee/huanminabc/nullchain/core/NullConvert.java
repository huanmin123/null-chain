package com.gitee.huanminabc.nullchain.core;



/**
 * Null类型转换接口 - 提供类型转换功能
 * 
 * <p>该接口定义了Null链的类型转换操作，主要用于将Object类型转换为具体的类型。
 * 它扩展了工作流接口，提供了类型安全的转换能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>类型转换：将Object类型转换为具体类型</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值处理：处理转换过程中的空值情况</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>当某个操作导致类型推导为Object时</li>
 *   <li>需要将Object类型转换为具体类型时</li>
 *   <li>类型擦除后的类型恢复</li>
 * </ul>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>不支持跨类型转换（如String转Integer）</li>
 *   <li>主要用于类型恢复和类型断言</li>
 *   <li>等效于强制类型转换：User user = (User)obj</li>
 * </ul>
 * 
 * @param <T> 转换前的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullWorkFlow 工作流接口
 */
public interface NullConvert<T> extends NullWorkFlow<T> {
    
    /**
     * 将当前值转换为指定类型
     * 
     * <p>该方法用于将当前值转换为指定的类型。主要用于类型恢复场景，
     * 当某个操作导致类型推导为Object时，可以使用此方法恢复具体类型。</p>
     * 
     * <p><strong>注意：</strong>此方法不支持跨类型转换（如String转Integer），
     * 主要用于类型恢复和类型断言，等效于强制类型转换。</p>
     * 
     * @param <U> 目标类型
     * @param uClass 目标类型的Class对象
     * @return 转换后的Null链
     * 
     * @example
     * <pre>{@code
     * // 类型恢复示例
     * User user = Null.of(someObject)
     *     .type(User.class)  // 将Object转换为User类型
     *     .orElse(new User());  // 如果转换失败，返回默认用户对象
     * }</pre>
     */
    <U> NullChain<U> type(Class<U> uClass);

    /**
     * 将当前值转换为指定类型（通过实例）
     * 
     * <p>该方法通过传入目标类型的实例来确定转换类型。
     * 主要用于类型恢复场景，当某个操作导致类型推导为Object时使用。</p>
     * 
     * @param <U> 目标类型
     * @param uClass 目标类型的实例（用于类型推断）
     * @return 转换后的Null链
     * 
     * @example
     * <pre>{@code
     * // 通过实例进行类型转换
     * User user = Null.of(someObject)
     *     .type(new User())  // 通过User实例确定类型
     *     .orElse(new User());  // 如果转换失败，返回默认用户对象
     * }</pre>
     */
    <U> NullChain<U> type(U uClass);
}
