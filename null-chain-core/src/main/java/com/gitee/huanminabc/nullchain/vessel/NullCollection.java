package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.NullExt;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Null集合接口 - 提供空值安全的集合操作
 * 
 * <p>该接口定义了空值安全的集合操作，扩展了Java的Collection接口，
 * 提供了空值安全的集合操作方法，避免NullPointerException。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>集合操作：add、remove、contains等基本集合操作</li>
 *   <li>空值安全：所有操作都处理空值情况</li>
 *   <li>流式操作：支持Stream API操作</li>
 *   <li>扩展功能：提供Null链的扩展功能</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>可迭代：支持迭代器操作</li>
 *   <li>可扩展：支持Null链的扩展功能</li>
 * </ul>
 * 
 * @param <T> 集合元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Iterable 可迭代接口
 * @see NullExt 扩展接口
 * @see NullCheck 空值检查接口
 */
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
