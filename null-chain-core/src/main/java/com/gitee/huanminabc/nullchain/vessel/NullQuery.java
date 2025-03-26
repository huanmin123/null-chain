package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 请参考{@link Queue}
 * @param <T>
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
