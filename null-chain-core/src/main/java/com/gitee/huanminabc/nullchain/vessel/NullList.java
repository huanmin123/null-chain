package com.gitee.huanminabc.nullchain.vessel;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *  请参考{@link List}
 */
public interface NullList<T> extends NullCollection<T>  {

    static <U>  NullList<U> newArrayList() {
        return new NullSuperList<>(new ArrayList<>());
    }

    static <U>  NullList<U> newLinkedList() {
        return new NullSuperList<>(new LinkedList<>());
    }

    static <U>  NullList<U> newVector() {
        return new NullSuperList<>(new Vector<>());
    }

    static <U>  NullList<U> newStack() {
        return new NullSuperList<>(new Stack<>());
    }

    static <U>  NullList<U> newCopyOnWriteArrayList() {
        return new NullSuperList<>(new CopyOnWriteArrayList<>());
    }


    T get(int index);

    T remove(int index);

    void add(int index, T element);


    void set(int index, T element);


    void addAll(NullList<? extends T> c);


    void removeAll(NullList<T> c);


    void sort(Comparator<? super T> c);


    int indexOf(T o);

    int lastIndexOf(T o);

    List<T> subList(int fromIndex, int toIndex);

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
