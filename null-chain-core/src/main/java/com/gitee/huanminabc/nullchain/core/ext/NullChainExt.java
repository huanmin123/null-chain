package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
@SuppressWarnings("unchecked")
public interface NullChainExt<T> extends NullChain<T>, NullConvertExt<T> {
    @Override
    default <U> NullChain<T> of(Function<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.of(function);
    }


    @Override
    default <U> NullChain<T> ofAny(Function<? super T, ? extends U>... function) {
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
    default <U> NullChain<T> isNull(Function<? super T, ? extends U> function){
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
    default <U> NullChain<U> map(Function<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map(function);
    }

    @Override
    default <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map2(function);
    }


    @Override
    default <U> NullChain<U> flatChain(Function<? super T, ? extends NullChain<U>> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.flatChain(function);
    }

    @Override
    default <U> NullChain<U> flatOptional(Function<? super T, ? extends Optional<U>> function){
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
