package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.NullExt;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface NullCollection<T> extends Iterable<T>, NullExt<NullCollection<T>>, NullCheck {

    boolean isEmpty();

    void add(T t);

    void remove(T o);

    int size();

    boolean contains(T o);

    void clear();

    Object[] toArray();

    <U> U[] toArray(U[] a);

    void addAll(Collection<? extends T> c);

    void addAll(NullCollection<? extends T> c);

    void removeAll(Collection<T> c);
    void removeAll(NullCollection<T> c);

    @Override
    default void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }

    @Override
    default Spliterator<T> spliterator() {
        return Iterable.super.spliterator();
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
