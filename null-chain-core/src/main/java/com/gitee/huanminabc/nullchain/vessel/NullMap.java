package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Null映射接口 - 提供空值安全的映射操作功能
 * 
 * <p>该接口提供了对Java Map接口的空值安全封装，支持各种映射操作如添加、删除、获取、遍历等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>映射操作：put、get、remove等基本映射操作</li>
 *   <li>键值操作：支持键集合、值集合、条目集合的获取</li>
 *   <li>条件操作：支持条件计算和合并操作</li>
 *   <li>遍历操作：支持函数式遍历和替换操作</li>
 *   <li>工厂方法：提供多种Map实现的创建方法</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>多种实现：支持HashMap、ConcurrentHashMap、TreeMap等</li>
 * </ul>
 * 
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Map Java映射接口
 * @see NullExt 扩展接口
 * @see NullCheck 空值检查接口
 */
public interface NullMap<K,V> extends NullExt<NullMap<K,V>>, NullCheck {

    /**
     * 创建HashMap实现的NullMap
     * 
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 基于HashMap的NullMap实例
     */
    static <K, V> NullMap<K, V> newHashMap() {
        return new NullSuperMap<>(new HashMap<>());
    }

    /**
     * 创建ConcurrentHashMap实现的NullMap
     * 
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 基于ConcurrentHashMap的NullMap实例
     */
    static <K, V> NullMap<K, V> newConcurrentHashMap() {
        return new NullSuperMap<>(new ConcurrentHashMap<>());
    }

    /**
     * 创建TreeMap实现的NullMap
     * 
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 基于TreeMap的NullMap实例
     */
    static <K, V> NullMap<K, V> newTreeMap() {
        return new NullSuperMap<>(new TreeMap<>());
    }

    /**
     * 创建Hashtable实现的NullMap
     * 
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 基于Hashtable的NullMap实例
     */
    static <K, V> NullMap<K, V> newHashtable() {
        return new NullSuperMap<>(new Hashtable<>());
    }

    /**
     * 创建LinkedHashMap实现的NullMap
     * 
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 基于LinkedHashMap的NullMap实例
     */
    static <K, V> NullMap<K, V> newLinkedHashMap() {
        return new NullSuperMap<>(new LinkedHashMap<>());
    }


    /**
     * 获取指定键对应的值
     * 
     * @param key 键
     * @return 包含值的Null链，如果键不存在则返回空链
     */
    NullChain<V> get(K key);

    /**
     * 移除指定键的映射
     * 
     * @param key 要移除的键
     */
    void remove(K key);

    /**
     * 添加键值对映射
     * 
     * @param key 键
     * @param value 值
     */
    void put(K key, V value);

    /**
     * 获取所有键值对条目的集合
     * 
     * @return 键值对条目的集合
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * 获取所有值的集合
     * 
     * @return 值的集合
     */
    Collection<V> values();

    /**
     * 获取所有键的集合
     * 
     * @return 键的集合
     */
    Set<K> keySet();

    /**
     * 清空映射中的所有键值对
     */
    void clear();

    /**
     * 添加另一个Map中的所有键值对
     * 
     * @param m 要添加的Map
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * 添加另一个NullMap中的所有键值对
     * 
     * @param m 要添加的NullMap
     */
    void putAll(NullMap<? extends K, ? extends V> m);

    /**
     * 检查是否包含指定的值
     * 
     * @param value 要检查的值
     * @return 如果包含该值则返回true
     */
    boolean containsValue(V value);

    /**
     * 检查是否包含指定的键
     * 
     * @param key 要检查的键
     * @return 如果包含该键则返回true
     */
    boolean containsKey(K key);

    /**
     * 检查映射是否为空
     * 
     * @return 如果映射为空则返回true
     */
    boolean isEmpty();

    /**
     * 获取映射中键值对的数量
     * 
     * @return 键值对的数量
     */
    int size();

    /**
     * 如果键不存在则添加键值对
     * 
     * @param key 键
     * @param value 值
     * @return 包含原值或新值的Null链
     */
    NullChain<V> putIfAbsent(K key, V value);
    
    /**
     * 如果键不存在则通过函数计算值并添加
     * 
     * @param key 键
     * @param mappingFunction 值计算函数
     * @return 包含计算后值的Null链
     */
    NullChain<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);
    
    /**
     * 如果键存在则通过函数重新计算值
     * 
     * @param key 键
     * @param remappingFunction 值重新计算函数
     * @return 包含重新计算后值的Null链
     */
    NullChain<V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    
    /**
     * 通过函数计算键对应的值
     * 
     * @param key 键
     * @param remappingFunction 值计算函数
     * @return 包含计算后值的Null链
     */
    NullChain<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    
    /**
     * 合并键对应的值
     * 
     * @param key 键
     * @param value 要合并的值
     * @param remappingFunction 合并函数
     * @return 包含合并后值的Null链
     */
    NullChain<V> merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);
    
    /**
     * 获取指定键的值，如果不存在则返回默认值
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 键对应的值或默认值
     */
    V getOrDefault (K key, V defaultValue);



    default void forEach(BiConsumer<? super K, ? super V> action) {
        Null.checkNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }


    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Null.checkNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                entry.setValue(v);
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }


}
