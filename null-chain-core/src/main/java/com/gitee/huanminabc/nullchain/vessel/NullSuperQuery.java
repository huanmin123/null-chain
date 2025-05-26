package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.*;

/**
 * 可以传入系统的Queue和自定义的Queue
 */
public class NullSuperQuery<T> implements NullQuery<T> {
    private final Queue<T> queue ;

    public NullSuperQuery(Queue<T> queue) {
        Objects.requireNonNull(queue);
        this.queue=queue;
    }

    @Override
    public void add(T t) {
       if (Null.is(t)){
              return;
       }
         queue.add(t);
    }

    @Override
    public boolean offer(T t) {
        if (Null.is(t)){
            return false;
        }
        return queue.offer(t);
    }

    @Override
    public T  remove() {
        return queue.remove();
    }

    @Override
    public void remove(T o) {
        if (Null.is(o)){
            return ;
        }
        queue.remove(o);
    }

    @Override
    public  NullChain<T> poll() {
        StringBuilder linkLog = new StringBuilder();
        T poll = queue.poll();
        if (Null.is(poll)) {
            linkLog.append("NullSuperQuery.poll?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperQuery.poll->");
        return NullBuild.noEmpty(poll, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public T element() {
        return queue.element();
    }

    @Override
    public  NullChain<T> peek() {
        StringBuilder linkLog = new StringBuilder();
        T peek = queue.peek();
        if (Null.is(peek)) {
            linkLog.append("NullSuperQuery.peek?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperQuery.peek->");
        return NullBuild.noEmpty(peek, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public boolean contains(T o) {
        if (Null.is(o)){
            return false;
        }
        return queue.contains(o);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        Null.checkNull(a);
        return queue.toArray(a);
    }

    @Override
    public void addAll(Collection<? extends T> c) {
        if (Null.is(c)){
            return;
        }
        c.forEach(this::add);

    }

    @Override
    public void addAll(NullCollection<? extends T> c) {
        if (Null.is(c)){
            return;
        }
        c.forEach(this::add);
    }

    @Override
    public void removeAll(Collection<T> c) {
        if (Null.is(c)){
            return;
        }
        c.forEach(this::remove);
    }

    @Override
    public void removeAll(NullCollection<T> c) {
        if (Null.is(c)){
            return;
        }
        c.forEach(this::remove);

    }

    @Override
    public int size() {
        return queue.size();
    }


    @Override
    public Iterator<T> iterator() {
        return queue.iterator();
    }

    @Override
    public String toString() {
        return queue.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NullSuperQuery<?> that = (NullSuperQuery<?>) object;
        return queue.equals(that.queue);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }
}
