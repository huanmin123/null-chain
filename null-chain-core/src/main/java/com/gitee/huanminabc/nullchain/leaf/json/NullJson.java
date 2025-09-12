package com.gitee.huanminabc.nullchain.leaf.json;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * Null JSON操作接口 - 提供空值安全的JSON处理功能
 * 
 * <p>该接口提供了对JSON操作的空值安全封装，支持对象与JSON字符串之间的相互转换。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>对象转JSON：将Java对象转换为JSON字符串</li>
 *   <li>JSON转对象：将JSON字符串转换为Java对象</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 * </ul>
 * 
 * @param <T> JSON值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullKernel 内核接口
 */
public interface NullJson <T> extends NullChain<T> , NullKernel<T> {

    /**
     * 对象转JSON操作 - 将Java对象转换为JSON字符串
     * 
     * <p>该方法用于将当前Java对象转换为JSON字符串格式。
     * 支持各种Java对象的序列化，包括POJO、集合、数组等。</p>
     * 
     * @return 包含JSON字符串的Null链
     * 
     * @example
     * <pre>{@code
     * String jsonStr = Null.of(user)
     *     .json()  // 将User对象转换为JSON字符串
     *     .orElse("{}");
     * }</pre>
     */
    NullJson<String> json();

    /**
     * JSON转对象操作 - 将JSON字符串转换为指定类型的对象
     * 
     * <p>该方法用于将当前JSON字符串转换为指定类型的Java对象。
     * 通过Class对象指定目标类型，确保类型安全。</p>
     * 
     * @param <U> 目标对象类型
     * @param uClass 目标类型的Class对象
     * @return 包含转换后对象的Null链
     * 
     * @example
     * <pre>{@code
     * User user = Null.of(jsonString)
     *     .json(User.class)  // 将JSON字符串转换为User对象
     *     .orElse(new User());
     * }</pre>
     */
    <U> NullJson<U> json(Class<U> uClass);

    /**
     * JSON转对象操作 - 将JSON字符串转换为指定类型的对象（通过实例推断类型）
     * 
     * <p>该方法用于将当前JSON字符串转换为指定类型的Java对象。
     * 通过传入目标类型的实例来推断类型，确保类型安全。</p>
     * 
     * @param <U> 目标对象类型
     * @param uClass 目标类型的实例（用于类型推断）
     * @return 包含转换后对象的Null链
     * 
     * @example
     * <pre>{@code
     * User user = Null.of(jsonString)
     *     .json(new User())  // 通过User实例推断类型
     *     .orElse(new User());
     * }</pre>
     */
    <U> NullJson<U> json(U uClass);
}
