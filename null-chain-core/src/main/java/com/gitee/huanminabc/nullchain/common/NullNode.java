package com.gitee.huanminabc.nullchain.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
/**
 * @Author huanmin
 * @Date 2024/11/15
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
