package com.gitee.huanminabc.nullchain.base.leaf.stream;
import com.gitee.huanminabc.nullchain.base.NullChain;
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
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-02-21 17:48
 **/
public interface NullStream<T >  {

     //映射
    <R> NullStream<R> map(NullFun<? super T, ? extends R> mapper);

    <R> NullStream<R> map2(NullFun2<NullChain<T>, ? super T, ? extends R> function);

    //过滤
    NullStream<T> filter(NullPredicate<? super T> predicate);
    //排序
    NullStream<T> sorted();
    //排序
    NullStream<T> sorted(Comparator<? super T> comparator);
    //去重
    NullStream<T> distinct();
    //限制获取的最大条数
    NullStream<T> limit(long maxSize);
    //跳过前n个元素
    NullStream<T> skip(long n);

    NullStream<T> then(Consumer<? super T> action);

    NullStream<T> then(NullConsumer2<NullChain<T>, ? super T> function);

    //流合并
  <R> NullStream<R> flatStream(Function<? super T, ? extends NullStream<? extends R>> mapper);

    //Collectors.xxx 一些常用的收集器
    <R, A> NullChain<R> collect(Collector<? super T, A, R> collector);

    NullChain<T> max(Comparator<? super T> comparator);

    NullChain<T> findFirst();

    NullChain<T> findAny();

    //聚合
    NullChain<T> reduce(BinaryOperator<T> accumulator);

    NullChain<Long> count();

    NullChain<T> min(Comparator<? super T> comparator);

    NullChain<Boolean> allMatch(Predicate<? super T> predicate);

    NullChain<Boolean> anyMatch(Predicate<? super T> predicate);

    NullChain<Boolean> noneMatch(Predicate<? super T> predicate);

    void forEach(Consumer<? super T> action);





}
