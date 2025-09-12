package com.gitee.huanminabc.nullchain.vessel;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Null集合接口 - 提供空值安全的集合操作功能
 * 
 * <p>该接口提供了对Java Set接口的空值安全封装，支持各种集合操作如添加、删除、包含检查等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>集合操作：add、remove、contains等基本集合操作</li>
 *   <li>集合运算：支持集合的并集、差集等操作</li>
 *   <li>遍历操作：支持迭代器和流式操作</li>
 *   <li>工厂方法：提供多种Set实现的创建方法</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>多种实现：支持HashSet、LinkedHashSet、TreeSet等</li>
 * </ul>
 * 
 * @param <T> 集合元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Set Java集合接口
 * @see NullCollection 空值安全集合接口
 */
public interface NullSet<T> extends NullCollection<T>  {

    /**
     * 创建HashSet实现的NullSet
     * 
     * @param <U> 集合元素的类型
     * @return 基于HashSet的NullSet实例
     */
    static <U> NullSet<U> newHashSet() {
        return new NullSuperSet<>(new HashSet<>());
    }

    /**
     * 创建LinkedHashSet实现的NullSet
     * 
     * @param <U> 集合元素的类型
     * @return 基于LinkedHashSet的NullSet实例
     */
    static <U> NullSet<U> newLinkedHashSet() {
        return new NullSuperSet<>(new LinkedHashSet<>());
    }

    /**
     * 创建TreeSet实现的NullSet
     * 
     * @param <U> 集合元素的类型
     * @return 基于TreeSet的NullSet实例
     */
    static  <U> NullSet<U> newTreeSet() {
        return new NullSuperSet<>(new TreeSet<>());
    }

    /**
     * 创建ConcurrentSkipListSet实现的NullSet
     * 
     * @param <U> 集合元素的类型
     * @return 基于ConcurrentSkipListSet的NullSet实例
     */
    static <U> NullSet<U> newConcurrentSkipListSet() {
        return new NullSuperSet<>(new ConcurrentSkipListSet<>());
    }

    /**
     * 创建CopyOnWriteArraySet实现的NullSet
     * 
     * @param <U> 集合元素的类型
     * @return 基于CopyOnWriteArraySet的NullSet实例
     */
    static <U> NullSet<U> newCopyOnWriteArraySet() {
        return new NullSuperSet<>(new CopyOnWriteArraySet<>());
    }



    /**
     * 添加另一个NullList中的所有元素
     * 
     * @param c 要添加的NullList
     */
    void addAll(NullList<? extends T> c);

    /**
     * 移除另一个NullSet中的所有元素
     * 
     * @param c 要移除的NullSet
     */
    void removeAll(NullSet<T> c);


    @Override
    boolean isEmpty();

    @Override
    void add(T t);

    @Override
    void remove(T o);

    @Override
    int size();

    @Override
    boolean contains(T o);

    @Override
    void clear();

    @Override
    Object[] toArray();

    @Override
    <U> U[] toArray(U[] a);

    @Override
    void addAll(Collection<? extends T> c);

    @Override
    void removeAll(Collection<T> c);

    @Override
    default void forEach(Consumer<? super T> action) {
        NullCollection.super.forEach(action);
    }

    @Override
    default Spliterator<T> spliterator() {
        return NullCollection.super.spliterator();
    }

    @Override
    default Stream<T> stream() {
        return NullCollection.super.stream();
    }

    @Override
    default Stream<T> parallelStream() {
        return NullCollection.super.parallelStream();
    }

    @Override
    Iterator<T> iterator();
}
