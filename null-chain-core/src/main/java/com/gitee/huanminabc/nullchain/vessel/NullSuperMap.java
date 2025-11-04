package com.gitee.huanminabc.nullchain.vessel;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 可以传入系统的Map和自定义的Map
 */
public class NullSuperMap<K,V>  implements NullMap<K,V>{
    private final Map<K,V> map  ;
    public NullSuperMap(Map<K,V> map) {
        Objects.requireNonNull(map);
        this.map=map;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void putAll(NullMap<? extends K, ? extends V> m) {
        m.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void remove(K key) {
        if (Null.is(key)){
            return;
        }
         map.remove(key);
    }

    @Override
    public  void put(K key, V value) {
        if (Null.is(key) || Null.is(value)){
            return ;
        }
        map.put(key, value);
    }

    @Override
    public NullChain<V> get(K key) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        
        nullTaskList.add((__) -> {
            if (Null.is(key)){
                linkLog.append(NULL_SUPER_MAP_GET_Q);
                return NullBuild.empty();
            }
            V v = map.get(key);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_GET_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_GET_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public boolean containsValue(V value) {
        if (Null.is(value)){
            return false;
        }
        return map.containsValue(value);
    }

    @Override
    public boolean containsKey(K key) {
        if (Null.is(key)){
            return false;
        }
        return map.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public NullChain<V> putIfAbsent(K key, V value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            if (Null.is(key) || Null.is(value)){
                linkLog.append(NULL_SUPER_MAP_PUT_IF_ABSENT_Q);
                return NullBuild.empty();
            }
            V v = map.putIfAbsent(key, value);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_PUT_IF_ABSENT_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_PUT_IF_ABSENT_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        
        nullTaskList.add((__) -> {
            if (Null.is(key) || Objects.isNull(mappingFunction)){
                linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_ABSENT_Q);
                return NullBuild.empty();
            }
            V v = map.computeIfAbsent(key, mappingFunction);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_ABSENT_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_ABSENT_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        
        nullTaskList.add((__) -> {
            if (Null.is(key) || Objects.isNull(remappingFunction)){
                linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_PRESENT_Q);
                return NullBuild.empty();
            }
            V v = map.computeIfPresent(key, remappingFunction);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_PRESENT_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_COMPUTE_IF_PRESENT_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        
        nullTaskList.add((__) -> {
            if (Null.is(key) ||Objects.isNull(remappingFunction)){
                linkLog.append(NULL_SUPER_MAP_COMPUTE_Q);
                return NullBuild.empty();
            }
            V v = map.compute(key, remappingFunction);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_COMPUTE_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_COMPUTE_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public NullChain<V> merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        
        nullTaskList.add((__) -> {
            if (Null.is(key) || Null.is(value) ||Objects.isNull(remappingFunction)){
                linkLog.append(NULL_SUPER_MAP_MERGE_Q);
                return NullBuild.empty();
            }
            V v = map.merge(key, value, remappingFunction);
            if (Null.is(v)) {
                linkLog.append(NULL_SUPER_MAP_MERGE_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_SUPER_MAP_MERGE_ARROW);
            return NullBuild.noEmpty(v);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        if (Null.is(key) || Null.is(defaultValue)){
            throw new NullChainException("key or defaultValue is null");
        }
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NullSuperMap<?, ?> that = (NullSuperMap<?, ?>) object;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
