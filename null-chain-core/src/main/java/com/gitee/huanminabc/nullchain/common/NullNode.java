package com.gitee.huanminabc.nullchain.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Null节点类 - 提供键值对的节点封装功能
 * 
 * <p>该类提供了键值对的节点封装功能，实现了Map.Entry接口，支持序列化传输。
 * 通过节点化的数据结构，为Null链操作提供灵活的数据组织能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>键值对封装：封装键值对数据</li>
 *   <li>Map.Entry实现：实现Map.Entry接口</li>
 *   <li>序列化支持：支持序列化传输</li>
 *   <li>节点管理：提供节点数据管理</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>序列化支持：支持序列化传输</li>
 *   <li>接口实现：实现Map.Entry接口</li>
 *   <li>节点化设计：提供节点化的数据结构</li>
 * </ul>
 * 
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Map.Entry Map条目接口
 * @see Serializable 序列化接口
 */
@Data
public class NullNode<K,V> implements Map.Entry<K,V>, Serializable {
    private static final long serialVersionUID = 1L;
    private  K key;
    private V value;
    public NullNode(K key, V value) {
        this.key = key;
        this.value = value;
    }
    public NullNode() {
    }
    public static <K,V> NullNode<K,V> NEW(K key, V value) {
        return new NullNode<>(key, value);
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }
    @Override
    public V setValue(V value) {
        this.value = value;
        return value;
    }
}
