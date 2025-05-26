package com.gitee.huanminabc.nullchain.base.async.stream;

import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.function.NullPredicate;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * 具体使用教程查看{@link Stream} <br>
 * @description:
 * @author: huanmin
 * @create: 2025-02-21 17:48
 **/
public interface NullStreamAsync<T>   {

    //映射
    <R> NullStreamAsync<R> map(NullFun<? super T, ? extends R> mapper);
    <R> NullStreamAsync<R> map2(NullFun2<NullChain<T>, ? super T, ? extends R> function);

    //过滤
    NullStreamAsync<T> filter(NullPredicate<? super T> predicate);
    //排序
    NullStreamAsync<T> sorted();
    //排序
    NullStreamAsync<T> sorted(Comparator<? super T> comparator);
    //去重
    NullStreamAsync<T> distinct();
    //限制获取的最大条数
    NullStreamAsync<T> limit(long maxSize);
    //跳过前n个元素
    NullStreamAsync<T> skip(long n);

    NullStreamAsync<T> then(Consumer<? super T> action);
    NullStreamAsync<T> then(NullConsumer2<NullChain<T>, ? super T> function);

    //流合并
    <R> NullStreamAsync<R> flatStream(Function<? super T, ? extends NullStreamAsync<? extends R>> mapper);

    //Collectors.xxx 一些常用的收集器
    <R, A> NullChainAsync<R> collect(Collector<? super T, A, R> collector);

    NullChainAsync<T> max(Comparator<? super T> comparator);

    NullChainAsync<T> findFirst();

    NullChainAsync<T> findAny();

    NullChainAsync<T> reduce(BinaryOperator<T> accumulator);

    NullChainAsync<Long> count();

    NullChainAsync<T> min(Comparator<? super T> comparator);

    NullChainAsync<Boolean> allMatch(Predicate<? super T> predicate);

    NullChainAsync<Boolean> anyMatch(Predicate<? super T> predicate);

    NullChainAsync<Boolean> noneMatch(Predicate<? super T> predicate);

    void forEach(Consumer<? super T> action);

}
