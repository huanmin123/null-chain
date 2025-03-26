package com.gitee.huanminabc.nullchain.vessel;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 请参考{@link Set}
 * @param <T>
 */
public interface NullSet<T> extends NullCollection<T>  {

    static <U> NullSet<U> newHashSet() {
        return new NullSuperSet<>(new HashSet<>());
    }

    static <U> NullSet<U> newLinkedHashSet() {
        return new NullSuperSet<>(new LinkedHashSet<>());
    }
    static  <U> NullSet<U> newTreeSet() {
        return new NullSuperSet<>(new TreeSet<>());
    }
    static <U> NullSet<U> newConcurrentSkipListSet() {
        return new NullSuperSet<>(new ConcurrentSkipListSet<>());
    }

    static <U> NullSet<U> newCopyOnWriteArraySet() {
        return new NullSuperSet<>(new CopyOnWriteArraySet<>());
    }



    void addAll(NullList<? extends T> c);


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
