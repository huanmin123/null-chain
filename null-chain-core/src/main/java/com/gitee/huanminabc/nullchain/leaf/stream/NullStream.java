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
 * Null流操作接口 - 提供空值安全的流式操作
 * 
 * <p>该接口提供了对Java Stream API的空值安全封装，支持各种流操作如map、filter、reduce等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>流转换：map、flatMap等转换操作</li>
 *   <li>流过滤：filter、distinct等过滤操作</li>
 *   <li>流排序：sorted排序操作</li>
 *   <li>流限制：limit、skip等限制操作</li>
 *   <li>流聚合：reduce、collect等聚合操作</li>
 *   <li>流匹配：allMatch、anyMatch、noneMatch等匹配操作</li>
 *   <li>并行流：parallel并行处理支持</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>性能优化：延迟执行和短路操作</li>
 * </ul>
 * 
 * <h3>使用说明：</h3>
 * <p>具体使用教程请参考 {@link Stream} 的官方文档。
 * 该接口提供了与Java Stream API相同的操作，但增加了空值安全处理。</p>
 * 
 * @param <T> 流中元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Stream Java流接口
 * @see NullKernel 内核接口
 */
public interface NullStream<T > extends NullKernel<T> {

    /**
     * 转换为并行流
     * 
     * @return 并行流实例
     */
    NullStream<T> parallel();

    /**
     * 流映射操作
     * 
     * @param <R> 映射后的元素类型
     * @param mapper 映射函数
     * @return 映射后的流
     */
    <R> NullStream<R> map(Function<? super T, ? extends R> mapper);

    /**
     * 映射为整数流
     * 
     * @param mapper 映射函数
     * @return 整数流
     */
    NullIntStream mapToInt(Function<? super T, ? extends Integer> mapper);

    /**
     * 映射为长整型流
     * 
     * @param mapper 映射函数
     * @return 长整型流
     */
    NullLongStream mapToLong(Function<? super T, ? extends Long> mapper);

    /**
     * 映射为双精度流
     * 
     * @param mapper 映射函数
     * @return 双精度流
     */
    NullDoubleStream mapToDouble(Function<? super T, ? extends Double> mapper);

    /**
     * 流过滤操作
     * 
     * @param predicate 过滤条件
     * @return 过滤后的流
     */
    NullStream<T> filter(Predicate<? super T> predicate);
    
    /**
     * 自然排序
     * 
     * @return 排序后的流
     */
    NullStream<T> sorted();
    
    /**
     * 自定义排序
     * 
     * @param comparator 比较器
     * @return 排序后的流
     */
    NullStream<T> sorted(Comparator<? super T> comparator);
    
    /**
     * 去重操作
     * 
     * @return 去重后的流
     */
    NullStream<T> distinct();
    
    /**
     * 限制元素数量
     * 
     * @param maxSize 最大元素数量
     * @return 限制后的流
     */
    NullStream<T> limit(long maxSize);
    
    /**
     * 跳过前n个元素
     * 
     * @param n 跳过的元素数量
     * @return 跳过后的流
     */
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
