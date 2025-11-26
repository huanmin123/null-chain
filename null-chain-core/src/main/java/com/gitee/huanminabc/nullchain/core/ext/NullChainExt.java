package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Null链扩展接口 - 提供链式操作的扩展功能
 * 
 * <p>该接口扩展了NullChain接口，提供了额外的链式操作功能。
 * 通过默认方法实现，为链式操作提供更丰富的功能支持。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>链式操作扩展：提供额外的链式操作方法</li>
 *   <li>类型转换：支持类型转换操作</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 * </ul>
 * 
 * @param <T> 链中值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullConvertExt 转换扩展接口
 */
@SuppressWarnings("unchecked")
public interface NullChainExt<T> extends NullChain<T>, NullConvertExt<T> {
    @Override
    default <U> NullChain<T> of(NullFun<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.of(function);
    }


    @Override
    default <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ofAny(function);
    }

    @Override
    default NullChain<T> ifGo(Predicate<? super T> predicate){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ifGo(predicate);
    }

    @Override
    default NullChain<T> ifNeGo(Predicate<? super T> predicate){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ifNeGo(predicate);
    }

    @Override
    default <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.isNull(function);
    }

    @Override
    default NullChain<T> then(Runnable function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then(function);
    }
    @Override
    default NullChain<T> then(Consumer<? super T> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then(function);
    }

    @Override
    default   NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then2(function);
    }



    @Override
    default <U> NullChain<U> map(NullFun<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map(function);
    }
    @Override
    default <R,V> NullChain<R> map(BiFunction<T, V, R> biFunction, V  key){
        return map(biFunction,key);
    }

    @Override
    default <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map2(function);
    }


    @Override
    default <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.flatChain(function);
    }

    @Override
    default <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.flatOptional(function);
    }

    @Override
    default NullChain<T> or(Supplier<? extends T> supplier) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.or(supplier);
    }

    @Override
    default NullChain<T> or(T defaultValue){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.or(defaultValue);
    }

}
