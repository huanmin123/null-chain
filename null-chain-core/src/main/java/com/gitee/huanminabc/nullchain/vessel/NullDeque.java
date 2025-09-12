package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Null双端队列接口 - 提供空值安全的双端队列操作功能
 * 
 * <p>该接口提供了对Java Deque接口的空值安全封装，支持双端队列的各种操作如头部和尾部的添加、删除、获取等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>双端操作：支持头部和尾部的添加、删除、获取操作</li>
 *   <li>队列操作：支持队列的先进先出操作</li>
 *   <li>栈操作：支持栈的后进先出操作</li>
 *   <li>工厂方法：提供多种Deque实现的创建方法</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>多种实现：支持LinkedList、ArrayDeque、ConcurrentLinkedDeque等</li>
 * </ul>
 * 
 * @param <T> 双端队列元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Deque Java双端队列接口
 * @see NullCollection 空值安全集合接口
 */
public interface NullDeque<T> extends NullCollection<T> {

    static <U> NullDeque<U> newLinkedList() {
        return new NullSuperDeque<>(new LinkedList<>());
    }

    static <U> NullDeque<U> newArrayDeque() {
        return new NullSuperDeque<>(new ArrayDeque<>());
    }

    static <U> NullDeque<U> newConcurrentLinkedDeque() {
        return new NullSuperDeque<>(new ConcurrentLinkedDeque<>());
    }

    static <U> NullDeque<U> newLinkedBlockingDeque() {
        return new NullSuperDeque<>(new LinkedBlockingDeque<>());
    }



    boolean offer(T e);

    T remove();

    NullChain<T> poll();

    T element();

    NullChain<T> peek();

    T pop();

    void push(T e);

    void addFirst(T e);

    void addLast(T e);

    boolean offerFirst(T e);

    boolean offerLast(T e);

    T removeFirst();

    T removeLast();

    NullChain<T> pollFirst();

    NullChain<T> pollLast();

    T getFirst();

    T getLast();

    NullChain<T> peekFirst();

    NullChain<T> peekLast();


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
