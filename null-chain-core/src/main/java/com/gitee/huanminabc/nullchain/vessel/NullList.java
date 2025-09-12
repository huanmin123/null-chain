package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Null列表接口 - 提供空值安全的列表操作功能
 * 
 * <p>该接口提供了对Java List接口的空值安全封装，支持各种列表操作如添加、删除、获取、排序等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>列表操作：add、remove、get、set等基本列表操作</li>
 *   <li>索引操作：支持基于索引的访问和修改</li>
 *   <li>排序操作：支持自定义比较器排序</li>
 *   <li>子列表：支持获取子列表</li>
 *   <li>工厂方法：提供多种List实现的创建方法</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>多种实现：支持ArrayList、LinkedList、Vector、Stack等</li>
 * </ul>
 * 
 * @param <T> 列表元素的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see List Java列表接口
 * @see NullCollection 空值安全集合接口
 */
public interface NullList<T> extends NullCollection<T>  {

    /**
     * 创建ArrayList实现的NullList
     * 
     * @param <U> 列表元素的类型
     * @return 基于ArrayList的NullList实例
     */
    static <U>  NullList<U> newArrayList() {
        return new NullSuperList<>(new ArrayList<>());
    }

    /**
     * 创建LinkedList实现的NullList
     * 
     * @param <U> 列表元素的类型
     * @return 基于LinkedList的NullList实例
     */
    static <U>  NullList<U> newLinkedList() {
        return new NullSuperList<>(new LinkedList<>());
    }

    /**
     * 创建Vector实现的NullList
     * 
     * @param <U> 列表元素的类型
     * @return 基于Vector的NullList实例
     */
    static <U>  NullList<U> newVector() {
        return new NullSuperList<>(new Vector<>());
    }

    /**
     * 创建Stack实现的NullList
     * 
     * @param <U> 列表元素的类型
     * @return 基于Stack的NullList实例
     */
    static <U>  NullList<U> newStack() {
        return new NullSuperList<>(new Stack<>());
    }

    /**
     * 创建CopyOnWriteArrayList实现的NullList
     * 
     * @param <U> 列表元素的类型
     * @return 基于CopyOnWriteArrayList的NullList实例
     */
    static <U>  NullList<U> newCopyOnWriteArrayList() {
        return new NullSuperList<>(new CopyOnWriteArrayList<>());
    }


    /**
     * 获取指定索引位置的元素
     * 
     * @param index 索引位置
     * @return 包含元素的Null链，如果索引越界则返回空链
     */
    NullChain<T> get(int index);

    /**
     * 移除指定索引位置的元素
     * 
     * @param index 索引位置
     * @return 包含被移除元素的Null链，如果索引越界则返回空链
     */
    NullChain<T> remove(int index);

    /**
     * 在指定索引位置插入元素
     * 
     * @param index 索引位置
     * @param element 要插入的元素
     */
    void add(int index, T element);

    /**
     * 设置指定索引位置的元素
     * 
     * @param index 索引位置
     * @param element 要设置的元素
     */
    void set(int index, T element);

    /**
     * 添加另一个NullList中的所有元素
     * 
     * @param c 要添加的NullList
     */
    void addAll(NullList<? extends T> c);

    /**
     * 移除另一个NullList中的所有元素
     * 
     * @param c 要移除的NullList
     */
    void removeAll(NullList<T> c);

    /**
     * 根据比较器对列表进行排序
     * 
     * @param c 比较器
     */
    void sort(Comparator<? super T> c);

    /**
     * 获取指定元素第一次出现的索引
     * 
     * @param o 要查找的元素
     * @return 元素第一次出现的索引，如果不存在则返回-1
     */
    int indexOf(T o);

    /**
     * 获取指定元素最后一次出现的索引
     * 
     * @param o 要查找的元素
     * @return 元素最后一次出现的索引，如果不存在则返回-1
     */
    int lastIndexOf(T o);

    /**
     * 获取指定范围的子列表
     * 
     * @param fromIndex 起始索引（包含）
     * @param toIndex 结束索引（不包含）
     * @return 子列表
     */
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
