package com.gitee.huanminabc.nullchain.leaf.copy;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * Null复制操作接口 - 提供空值安全的对象复制功能
 * 
 * <p>该接口提供了对对象复制操作的空值安全封装，支持浅拷贝、深拷贝和字段提取等操作。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>浅拷贝：复制对象的基本结构，引用类型共享</li>
 *   <li>深拷贝：完全复制对象，包括所有嵌套对象</li>
 *   <li>字段提取：提取对象中的指定字段，返回新对象</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 * </ul>
 * 
 * @param <T> 复制对象的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullKernel 内核接口
 */
public interface NullCopy<T> extends NullChain<T>  , NullKernel<T>{

    /**
     * 浅拷贝操作 - 复制对象的基本结构
     * 
     * <p>该方法用于对当前对象进行浅拷贝，复制对象的基本结构，
     * 但引用类型的字段仍然指向原对象，不会进行深度复制。</p>
     * 
     * @return 包含浅拷贝对象的Null链
     * 
     * @example
     * <pre>{@code
     * User copiedUser = Null.of(user)
     *     .copy()  // 对User对象进行浅拷贝
     *     .orElse(new User());
     * }</pre>
     */
    NullCopy<T> copy();

    /**
     * 深拷贝操作 - 完全复制对象及其所有嵌套对象
     * 
     * <p>该方法用于对当前对象进行深拷贝，完全复制对象及其所有嵌套对象。
     * 通过序列化机制实现深拷贝，确保所有引用都是独立的。</p>
     * 
     * <p><strong>注意：</strong>需要拷贝的类必须实现Serializable接口，
     * 包括内部类，否则会抛出NotSerializableException异常。</p>
     * 
     * @return 包含深拷贝对象的Null链
     * 
     * @example
     * <pre>{@code
     * User deepCopiedUser = Null.of(user)
     *     .deepCopy()  // 对User对象进行深拷贝
     *     .orElse(new User());
     * }</pre>
     */
    NullCopy<T> deepCopy();

    /**
     * 字段提取操作 - 提取对象中的指定字段
     * 
     * <p>该方法用于从当前对象中提取指定的字段，返回包含提取字段的新对象。
     * 支持提取多个字段，通过映射函数指定要提取的字段。</p>
     * 
     * @param <U> 提取字段的类型
     * @param mapper 字段提取函数数组
     * @return 包含提取字段的Null链
     * 
     * @example
     * <pre>{@code
     * String name = Null.of(user)
     *     .pick(User::getName)  // 提取用户姓名
     *     .orElse("未知用户");
     * 
     * // 提取多个字段
     * Object[] fields = Null.of(user)
     *     .pick(User::getName, User::getEmail, User::getAge)
     *     .orElse(new Object[0]);
     * }</pre>
     */
    <U> NullCopy<T> pick(Function<? super T, ? extends U>... mapper);

}
