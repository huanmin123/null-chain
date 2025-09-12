package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Null队列接口 - 提供空值安全的队列操作功能
 * 
 * <p>该接口提供了对Java Queue接口的空值安全封装，支持队列的各种操作如添加、删除、获取等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>队列操作：支持队列的先进先出操作</li>
 *   <li>阻塞操作：支持阻塞队列的阻塞操作</li>
 *   <li>优先级操作：支持优先级队列的优先级操作</li>
 *   <li>工厂方法：提供多种Queue实现的创建方法</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>多种实现：支持PriorityQueue、ConcurrentLinkedQueue、BlockingQueue等</li>
 * </ul>
 * 
 * @param <T> 队列元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Queue Java队列接口
 * @see NullCollection 空值安全集合接口
 */
public interface NullQuery<T> extends NullCollection<T> {

    static <U> NullQuery<U> newPriorityQueue() {
        return new NullSuperQuery<>(new PriorityQueue<>());
    }
    static <U> NullQuery<U> newConcurrentLinkedQueue() {
        return new NullSuperQuery<>(new ConcurrentLinkedQueue<>());
    }
    static <U> NullQuery<U> newBlockingQueue(int capacity) {
        return new NullSuperQuery<>(new ArrayBlockingQueue<>(capacity));
    }
    //常用的BlockingQueue实现类包括ArrayBlockingQueue、LinkedBlockingQueue和PriorityBlockingQueue。
    static <U> NullQuery<U> newArrayBlockingQueue(int capacity) {
        return new NullSuperQuery<>(new ArrayBlockingQueue<>(capacity));
    }
    static <U> NullQuery<U> newPriorityBlockingQueue(int capacity) {
        return new NullSuperQuery<>(new PriorityBlockingQueue<>(capacity));
    }
    static <U> NullQuery<U> newLinkedBlockingQueue() {
        return new NullSuperQuery<>(new LinkedBlockingQueue<>());
    }
    static <U> NullQuery<U> newSynchronousQueue() {
        return new NullSuperQuery<>(new SynchronousQueue<>());
    }



    boolean offer(T e);

    T remove();


    NullChain<T> poll();

    T element();

    NullChain<T> peek();


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
