package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
/**
 * 可以传入系统的Deque和自定义的Deque
 */
public class NullSuperDeque<T> implements NullDeque<T> {
    private final Deque<T> queue ;

    public NullSuperDeque(Deque<T> queue) {
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
    public T remove() {
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
            linkLog.append("NullSuperDeque.poll?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }

        linkLog.append("NullSuperDeque.poll->");
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
            linkLog.append("NullSuperDeque.peek?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }

        linkLog.append("NullSuperDeque.peek->");
        return NullBuild.noEmpty(peek, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public T pop() {
        return queue.pop();
    }

    @Override
    public void push(T e) {
        if (Null.is(e)){
            return;
        }
        queue.push(e);
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
        queue.addAll(c);
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
        queue.removeAll(c);
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
    public void addFirst(T e) {
        if (Null.is(e)){
            return;
        }
        queue.addFirst(e);
    }

    @Override
    public void addLast(T e) {
        if (Null.is(e)){
            return;
        }
        queue.addLast(e);
    }

    @Override
    public boolean offerFirst(T e) {
        if (Null.is(e)){
            return false;
        }
        return queue.offerFirst(e);
    }

    @Override
    public boolean offerLast(T e) {
        if (Null.is(e)){
            return false;
        }
        return queue.offerLast(e);
    }

    @Override
    public T removeFirst() {
        return queue.removeFirst();
    }

    @Override
    public T removeLast() {
        return queue.removeLast();
    }

    @Override
    public NullChain<T> pollFirst() {
        StringBuilder linkLog = new StringBuilder();
        T pollFirst = queue.pollFirst();
        if (Null.is(pollFirst)) {
            linkLog.append("NullSuperDeque.pollFirst?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperDeque.pollFirst->");
        return NullBuild.noEmpty(pollFirst, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public NullChain<T> pollLast() {
        StringBuilder linkLog = new StringBuilder();
        T pollLast = queue.pollLast();
        if (Null.is(pollLast)) {
            linkLog.append("NullSuperDeque.pollLast?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperDeque.pollLast->");
        return NullBuild.noEmpty(pollLast, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public T getFirst() {
        return queue.getFirst();
    }

    @Override
    public T getLast() {
        return queue.getLast();
    }

    @Override
    public NullChain<T> peekFirst() {
        StringBuilder linkLog = new StringBuilder();
        T peekFirst = queue.peekFirst();
        if (Null.is(peekFirst)) {
            linkLog.append("NullSuperDeque.peekFirst?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperDeque.peekFirst->");
        return NullBuild.noEmpty(peekFirst, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public NullChain<T> peekLast() {
        StringBuilder linkLog = new StringBuilder();
        T peekLast = queue.peekLast();
        if (Null.is(peekLast)) {
            linkLog.append("NullSuperDeque.peekLast?");
            return NullBuild.empty(linkLog, new NullCollect(),new NullTaskList());
        }
        linkLog.append("NullSuperDeque.peekLast->");
        return NullBuild.noEmpty(peekLast, linkLog, new NullCollect(),new NullTaskList());
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
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
        NullSuperDeque<?> that = (NullSuperDeque<?>) object;
        return queue.equals(that.queue);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }
}
