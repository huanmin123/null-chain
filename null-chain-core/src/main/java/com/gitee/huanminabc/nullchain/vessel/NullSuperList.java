package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;

import java.util.*;

/**
 * 可以传入系统的List和自定义的List
 */
public class NullSuperList<T> implements NullList<T> {
    private final List<T> list;
    public NullSuperList(List<T> list) {
        Objects.requireNonNull(list);
        this.list = list;
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(T o) {
        if (Null.is(o)) {
            return false;
        }
        return list.contains(o);
    }

    @Override
    public void add(T t) {
        if (Null.is(t)) {
            return;
        }
        list.add(t);
    }

    @Override
    public void remove(T t) {
        if (Null.is(t)) {
            return;
        }
        list.remove(t);
    }

    @Override
    public void addAll(Collection<? extends T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::add);
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
    public void removeAll(NullList<T> c) {
        if (Null.is(c)) {
            return;
        }
        c.forEach(this::remove);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        if (Null.is(c)) {
            return;
        }
        list.sort(c);
    }

    @Override
    public void clear() {
        list.clear();
    }


    @Override
    public void set(int index, T element) {
        if (Null.is(element)) {
            return;
        }
        list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        if (Null.is(element)) {
            return;
        }
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(T o) {
        if (Null.is(o)) {
            return -1;
        }
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(T o) {
        if (Null.is(o)) {
            return -1;
        }
        return list.lastIndexOf(o);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return  list.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        Null.checkNull(a);
        return list.toArray(a);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }


    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NullSuperList<?> that = (NullSuperList<?>) object;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

}
