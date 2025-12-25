package com.gitee.huanminabc.nullchain.core.ext;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullFinality;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Null终结操作扩展接口 - 提供终结操作的扩展功能
 * 
 * <p>该接口扩展了NullFinality接口，提供了额外的终结操作功能。
 * 通过默认方法实现，为终结操作提供更丰富的功能支持。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>终结操作扩展：提供额外的终结操作方法</li>
 *   <li>值获取：支持多种值获取方式</li>
 *   <li>异常处理：提供异常安全的获取方式</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 * </ul>
 * 
 * @param <T> 终结操作的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullFinality 终结操作接口
 * @see NullKernelExt 内核扩展接口
 */
public interface NullFinalityExt<T> extends NullFinality<T>,NullKernelExt<T> {


    @Override
    default boolean is() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.is();
    }


    @Override
    default boolean non() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.non();
    }

    @JSONField(serialize = false)
    @JsonIgnore
    @Override
    default T getSafe() throws NullChainCheckException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.getSafe();
    }

    @Override
    default T get() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get();
    }

    @Override
    default <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get(exceptionSupplier);
    }

    @Override
    default T get(String exceptionMessage, Object... args) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get(exceptionMessage, args);
    }


    @Override
    default NullCollect collect() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.collect();
    }

    @Override
    default void ifPresent(Consumer<? super T> action) {
        NullChain<T> tNullChain = toNULL();
        tNullChain.ifPresent(action);
    }


    @Override
    default void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        NullChain<T> tNullChain = toNULL();
        tNullChain.ifPresentOrElse(action, emptyAction);
    }

    @Override
    default void capture(Consumer<Throwable> consumer) {
        NullChain<T> tNullChain = toNULL();
        tNullChain.capture(consumer);
    }
    @Override
    default void doThrow(Class<? extends RuntimeException> exceptionClass, String exceptionMessage, Object... args) {
        NullChain<T> tNullChain = toNULL();
        tNullChain.doThrow(exceptionClass, exceptionMessage, args);
    }

    @Override
    default T orElseNull() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElseNull();
    }

    @Override
    default T orElse(T defaultValue) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElse(defaultValue);
    }

    @Override
    default T orElse(Supplier<T> defaultValue) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElse(defaultValue);
    }

    @Override
    default   int length(){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.length();
    }


}
