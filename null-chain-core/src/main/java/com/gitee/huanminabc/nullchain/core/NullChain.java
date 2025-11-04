package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Null链式操作接口 - 空值安全的链式编程API
 * 
 * <p>这是Null链框架的核心接口，提供了丰富的链式操作方法，支持空值安全的编程模式。
 * 该接口支持序列化，可以用于网络传输和持久化存储。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>空值安全的链式操作</li>
 *   <li>类型转换和映射</li>
 *   <li>条件判断和分支处理</li>
 *   <li>集合和流操作</li>
 *   <li>异步执行支持</li>
 * </ul>
 * 
 * <h3>设计原则：</h3>
 * <ul>
 *   <li>空值安全：任何操作遇到null都会优雅地处理</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>可序列化：支持网络传输和持久化</li>
 * </ul>
 * 
 * @param <T> 链中值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChainBase 默认实现类
 * @see NullConvert 转换操作接口
 */
public interface NullChain<T> extends NullConvert<T> {
    /**
     * 条件判断操作 - 如果条件函数返回空值则返回空链
     * 
     * <p>该方法会对当前值应用条件函数，如果函数返回null或空值，
     * 则整个链会变为空链，后续操作都会被跳过。</p>
     * 
     * @param <U> 条件函数返回值的类型
     * @param function 条件判断函数，返回null时链变为空链
     * @return 新的Null链，如果条件函数返回空值则为空链
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .of(User::getName)  // 如果用户名为空，链变为空链
     *     .map(String::toUpperCase)
     *     .orElse("UNKNOWN");
     * }</pre>
     */
    <U> NullChain<T> of(NullFun<? super T, ? extends U> function);

    /**
     * 多条件判断操作 - 任意一个条件为空则返回空链
     * 
     * <p>该方法接受多个条件函数，如果其中任意一个返回null或空值，
     * 则整个链会变为空链。</p>
     * 
     * @param functions 条件函数数组
     * @return 新的Null链，如果任意条件为空则为空链
     */
    <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... functions);


    /**
     * 条件分支操作 - 根据条件决定是否继续执行
     * 
     * <p>该方法会对当前值应用条件判断，如果条件为true则继续执行后续操作，
     * 如果条件为false则返回空链，跳过后续所有操作。等同于Optional的filter方法。</p>
     * 
     * @param function 条件判断函数
     * @return 新的Null链，条件为false时为空链
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .ifGo(u -> u.getAge() > 18)  // 只有成年用户才继续处理
     *     .map(User::getName)
     *     .orElse("未成年用户");
     * }</pre>
     */
    NullChain<T> ifGo(Predicate<? super T> function);

    /**
     * 反向条件分支操作 - 根据条件决定是否继续执行
     * 
     * <p>该方法会对当前值应用条件判断，如果条件为false则继续执行后续操作，
     * 如果条件为true则返回空链，跳过后续所有操作。</p>
     * 
     * @param function 条件判断函数
     * @return 新的Null链，条件为true时为空链
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .ifNeGo(u -> u.getAge() < 18)  // 只有非未成年用户才继续处理
     *     .map(User::getName)
     *     .orElse("未成年用户");
     * }</pre>
     */
    NullChain<T> ifNeGo(Predicate<? super T> function);

    /**
     * 空值检查操作 - 如果检查的值为空则继续执行
     * 
     * <p>该方法用于检查指定字段是否为空，如果为空则继续执行后续操作。
     * 这是一个逻辑补充操作，不会改变当前值，只是用于条件判断。</p>
     * 
     * <p><strong>使用场景：</strong>当需要确保对象的多个字段都不为空时，
     * 可以使用此方法进行空值检查，但不会使用检查的值。</p>
     * 
     * @param <U> 检查字段的类型
     * @param function 字段访问函数
     * @return 新的Null链，如果检查的字段为空则继续执行
     * 
     * @example
     * <pre>{@code
     * // 确保用户的所有关键字段都不为空
     * String result = Null.of(user)
     *     .isNull(User::getName)     // 检查姓名是否为空
     *     .isNull(User::getEmail)    // 检查邮箱是否为空
     *     .map(User::getProfile)     // 只有前面的节点满足条件才继续
     *     .orElse("默认配置");
     * }</pre>
     */
    <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function);


    /**
     * 执行操作但不改变值 - 空白节点操作
     * 
     * <p>该方法用于执行某些操作但不改变当前值，相当于在链中添加一个空白节点。
     * 适用于需要在链中执行副作用操作（如日志记录、缓存更新等）的场景。</p>
     * 
     * @param function 要执行的操作
     * @return 新的Null链，值保持不变
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .then(() -> log.info("处理用户数据"))  // 记录日志但不改变值
     *     .map(User::getName)
     *     .orElse("未知用户");
     * }</pre>
     */
    NullChain<T> then(Runnable function);

    /**
     * 执行操作但不改变值 - 带参数的操作
     * 
     * <p>该方法用于执行某些操作但不改变当前值，操作可以访问当前值。
     * 适用于需要在链中执行副作用操作（如日志记录、缓存更新等）的场景。</p>
     * 
     * @param function 要执行的操作，可以访问当前值
     * @return 新的Null链，值保持不变
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .then(u -> log.info("处理用户: {}", u.getName()))  // 记录用户信息
     *     .map(User::getName)
     *     .orElse("未知用户");
     * }</pre>
     */
    NullChain<T> then(Consumer<? super T> function);

    /**
     * 执行操作但不改变值 - 带链和参数的操作
     * 
     * <p>该方法用于执行某些操作但不改变当前值，操作可以访问当前链和值。
     * 适用于需要在链中执行复杂副作用操作的场景。</p>
     * 
     * @param function 要执行的操作，可以访问当前链和值
     * @return 新的Null链，值保持不变
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .then2((chain, u) -> {
     *         log.info("处理用户: {}", u.getName());
     *         // 可以访问链的上下文信息
     *     })
     *     .map(User::getName)
     *     .orElse("未知用户");
     * }</pre>
     */
    NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function);

    /**
     * 值映射操作 - 将当前值转换为新值
     * 
     * <p>该方法用于将当前值通过映射函数转换为新的值。如果当前值为空，
     * 则返回空链，不会执行映射操作。</p>
     * 
     * @param <U> 映射后的值的类型
     * @param function 映射函数
     * @return 包含映射结果的Null链
     * 
     * @example
     * <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)        // 将User对象映射为String
     *     .map(String::toUpperCase)  // 将字符串转换为大写
     *     .orElse("未知用户");
     * }</pre>
     */
    <U> NullChain<U> map(NullFun<? super T, ? extends U> function);

    /**
     * 值映射操作 - 带链上下文的映射
     * 
     * <p>该方法用于将当前值通过映射函数转换为新的值，映射函数可以访问当前链的上下文。
     * 如果当前值为空，则返回空链，不会执行映射操作。</p>
     * 
     * @param <U> 映射后的值的类型
     * @param function 映射函数，可以访问当前链和值
     * @return 包含映射结果的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(user)
     *     .map2((chain, u) -> {
     *         // 可以访问链的上下文信息
     *         return u.getName() + "_processed";
     *     })
     *     .orElse("未知用户");
     * }</pre>
     */
    <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function);

    /**
     * 扁平化链操作 - 解包嵌套的NullChain
     * 
     * <p>该方法用于处理返回NullChain的映射函数，将嵌套的NullChain解包为单层结构。
     * 如果当前值为空，则返回空链，不会执行映射操作。</p>
     * 
     * @param <U> 解包后的值的类型
     * @param function 返回NullChain的映射函数
     * @return 解包后的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(user)
     *     .flatChain(u -> Null.of(u.getProfile())  // 返回NullChain<String>
     *         .map(Profile::getDescription))
     *     .orElse("无描述");
     * }</pre>
     */
    <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function);

    /**
     * 扁平化Optional操作 - 解包Optional值
     * 
     * <p>该方法用于处理返回Optional的映射函数，将Optional解包为直接值。
     * 如果当前值为空或Optional为空，则返回空链。</p>
     * 
     * @param <U> 解包后的值的类型
     * @param function 返回Optional的映射函数
     * @return 解包后的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(user)
     *     .flatOptional(u -> u.getProfile()
     *         .map(Profile::getDescription))  // 返回Optional<String>
     *     .orElse("无描述");
     * }</pre>
     */
    <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function);

    /**
     * 默认值操作 - 使用Supplier提供默认值
     * 
     * <p>该方法用于在当前值为空时提供默认值。如果当前值不为空，
     * 则返回当前链；如果当前值为空，则执行Supplier获取默认值。</p>
     * 
     * @param supplier 默认值提供者
     * @return 包含当前值或默认值的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(user)
     *     .map(User::getName)
     *     .or(() -> "默认用户名")  // 如果用户名为空，使用默认值
     *     .orElse("未知用户");
     * }</pre>
     */
    NullChain<T> or(Supplier<? extends T> supplier);

    /**
     * 默认值操作 - 使用固定值作为默认值
     * 
     * <p>该方法用于在当前值为空时提供固定的默认值。如果当前值不为空，
     * 则返回当前链；如果当前值为空，则使用提供的默认值。</p>
     * 
     * @param defaultValue 默认值
     * @return 包含当前值或默认值的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(user)
     *     .map(User::getName)
     *     .or("默认用户名")  // 如果用户名为空，使用默认值
     *     .orElse("未知用户");
     * }</pre>
     */
    NullChain<T> or(T defaultValue);


}
