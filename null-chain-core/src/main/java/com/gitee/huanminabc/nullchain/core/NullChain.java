package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 支持序列化网络传输
 *
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullChain<T> extends NullConvert<T> {
    /**
     * 判断是否为空,如果为空那么就返回空链
     *
     * @param function
     * @param <U>
     * @return
     */
    <U> NullChain<T> of(Function<? super T, ? extends U> function);

    /**
     * 任意一个是空那么就返回空链
     */
    <U> NullChain<T> ofAny(Function<? super T, ? extends U>... function);


    /**
     * 用于决定是否还需要继续执行,或者改变终结节点的结果,   返回true那么就继续执行, 返回false那么就返回空链
     * 等同于Optional的filter
     */
    NullChain<T> ifGo(Predicate<? super T> function);

    /**
     * 用于决定是否还需要继续执行,或者改变终结节点的结果,   返回false那么就继续执行, 返回true那么就返回空链
     * @param function
     * @return
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
