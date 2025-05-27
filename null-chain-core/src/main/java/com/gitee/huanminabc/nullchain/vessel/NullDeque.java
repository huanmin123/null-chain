package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.base.NullChain;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 请参考{@link Deque}
 * @param <T>
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
