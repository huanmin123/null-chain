package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;

import java.util.*;

/**
 * 可以传入系统的List和自定义的List
 */
public class NullSuperSet<T> implements NullSet<T> {
    private final Set<T> set;

    public NullSuperSet(Set<T> set) {
        Objects.requireNonNull(set);
        this.set = set;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(T o) {
        if (Null.is(o)) {
            return false;
        }
        return set.contains(o);
    }

    @Override
    public void add(T t) {
        if (Null.is(t)) {
            return;
        }
        set.add(t);
    }

    @Override
    public void remove(T t) {
        if (Null.is(t)) {
            return;
        }
        set.remove(t);
    }

    @Override
    public void addAll(Collection<? extends T> c) {
        if (Null.is(c)) {
            return;
        }
        set.addAll(c);
    }

    @Override
    public void addAll(NullCollection<? extends T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::add);
    }

    @Override
    public void addAll(NullList<? extends T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::add);
    }

    @Override
    public void removeAll(Collection<T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::remove);
    }

    @Override
    public void removeAll(NullCollection<T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::remove);

    }

    @Override
    public void removeAll(NullSet<T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::remove);
    }



    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        Null.checkNull(a);
        return set.toArray(a);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }
    @Override
    public String toString() {
        return set.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NullSuperSet<?> that = (NullSuperSet<?>) object;
        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
