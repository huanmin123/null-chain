package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;

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
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        nullTaskList.add((__) -> {
            T poll = queue.poll();
            if (Null.is(poll)) {
                linkLog.append(NULL_SUPER_DEQUE_POLL_Q);
                return NullBuild.empty();
            }

            linkLog.append(NULL_SUPER_DEQUE_POLL_ARROW);
            return NullBuild.noEmpty(poll);
        });
        return NullBuild.busy(linkLog, nullTaskList);

    }

    @Override
    public T element() {
        return queue.element();
    }

    @Override
    public  NullChain<T> peek() {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        nullTaskList.add((__) -> {
            T peek = queue.peek();
            if (Null.is(peek)) {
                linkLog.append(NULL_SUPER_DEQUE_PEEK_Q);
                return NullBuild.empty();
            }

            linkLog.append(NULL_SUPER_DEQUE_PEEK_ARROW);
            return NullBuild.noEmpty(peek);
        });
        return NullBuild.busy(linkLog, nullTaskList);
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
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        nullTaskList.add((__) -> {
            T pollFirst = queue.pollFirst();
            if (Null.is(pollFirst)) {
                linkLog.append(NULL_SUPER_DEQUE_POLL_FIRST_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_DEQUE_POLL_FIRST_ARROW);
            return NullBuild.noEmpty(pollFirst);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<T> pollLast() {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        nullTaskList.add((__) -> {
            T pollLast = queue.pollLast();
            if (Null.is(pollLast)) {
                linkLog.append(NULL_SUPER_DEQUE_POLL_LAST_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_DEQUE_POLL_LAST_ARROW);
            return NullBuild.noEmpty(pollLast);
        });
        return NullBuild.busy(linkLog, nullTaskList);
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
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        nullTaskList.add((__) -> {
            T peekFirst = queue.peekFirst();
            if (Null.is(peekFirst)) {
                linkLog.append(NULL_SUPER_DEQUE_PEEK_FIRST_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_DEQUE_PEEK_FIRST_ARROW);
            return NullBuild.noEmpty(peekFirst);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<T> peekLast() {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            T peekLast = queue.peekLast();
            if (Null.is(peekLast)) {
                linkLog.append(NULL_SUPER_DEQUE_PEEK_LAST_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_DEQUE_PEEK_LAST_ARROW);
            return NullBuild.noEmpty(peekLast);
        });
        return NullBuild.busy(linkLog, nullTaskList);
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
