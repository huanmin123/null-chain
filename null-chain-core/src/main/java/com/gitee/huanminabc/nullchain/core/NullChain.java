package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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
    <U> NullChain<T> of(Function<? super T, ? extends U> function);

    /**
     * 多条件判断操作 - 任意一个条件为空则返回空链
     * 
     * <p>该方法接受多个条件函数，如果其中任意一个返回null或空值，
     * 则整个链会变为空链。</p>
     * 
     * @param functions 条件函数数组
     * @return 新的Null链，如果任意条件为空则为空链
     */
    <U> NullChain<T> ofAny(Function<? super T, ? extends U>... functions);


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
     * 如果是空继续往下走, 但是不会用到这个值 , 也不会出现空指针, 只是一种并且的补充
     * 比如 一个对象内 a b c 都不是空 并且 d是空 那么才满足条件 , 但是在实际处理的时候不会用到d , 只是一种逻辑上的处理
     * 这个是有歧义的和of 是一种互补关系
     */
    <U> NullChain<T> isNull(Function<? super T, ? extends U> function);


    /**
     * 在上一个任务不是空的情况下执行,不改变对象类型不改变对象内容, 就是一个空白节点无状态的不影响链路的数据
     */

    NullChain<T> then(Runnable function);

    NullChain<T> then(Consumer<? super T> function);

    NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function);

    /**
     * 获取上一个任务的内容,如果上一个任务为空,那么就返回空链
     */
    <U> NullChain<U> map(Function<? super T, ? extends U> function);

    <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function);


    /**
     * 上一个任务的内容返回的是NullChain<?>  那么就返回 ?  这样就可以继续操作了 , 通俗来说就是解包
     */
    <U> NullChain<U> flatChain(Function<? super T, ? extends NullChain<U>> function);

    <U> NullChain<U> flatOptional(Function<? super T, ? extends Optional<U>> function);


    /**
     * 如果上一个任务为空,那么就执行supplier返回新的任务,如果不为空,那么就返回当前任务
     */
    NullChain<T> or(Supplier<? extends T> supplier);

    NullChain<T> or(T defaultValue);


}
