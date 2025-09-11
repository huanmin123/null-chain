package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
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
public interface NullStream<T > extends NullKernel<T> {

    //异步模式
    NullStream<T> parallel();

     //映射
    <R> NullStream<R> map(Function<? super T, ? extends R> mapper);

    //mapToInt
    NullIntStream mapToInt(Function<? super T, ? extends Integer> mapper);

    //mapToLong
    NullLongStream mapToLong(Function<? super T, ? extends Long> mapper);

    //mapToDouble
    NullDoubleStream mapToDouble(Function<? super T, ? extends Double> mapper);

    //过滤
    NullStream<T> filter(Predicate<? super T> predicate);
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

    //流展开 (扁平化)
  <R> NullStream<R> flatMap(java.util.function.Function<? super T, ? extends NullStream<? extends R>> mapper);


    NullChain<T> findFirst();

    NullChain<T> findAny();

    //聚合
    NullChain<T>  reduce(BinaryOperator<T> accumulator);
    NullChain<T>  reduce(T identity, BinaryOperator<T> accumulator);

    NullChain<T> max(Comparator<? super T> comparator);

    NullChain<T> min(Comparator<? super T> comparator);




    void forEach(Consumer<? super T> action);


    Long count();


    Boolean allMatch(Predicate<? super T> predicate);

    Boolean anyMatch(Predicate<? super T> predicate);

    Boolean noneMatch(Predicate<? super T> predicate);

    //Collectors.xxx 一些常用的收集器
    <R, A> R collect(Collector<? super T, A, R> collector);



}
