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
 *  请参考{@link Map}
 */
public interface NullMap<K,V> extends NullExt<NullMap<K,V>>, NullCheck {

    static <K, V> NullMap<K, V> newHashMap() {
        return new NullSuperMap<>(new HashMap<>());
    }

    static <K, V> NullMap<K, V> newConcurrentHashMap() {
        return new NullSuperMap<>(new ConcurrentHashMap<>());
    }
    //treeMap
    static <K, V> NullMap<K, V> newTreeMap() {
        return new NullSuperMap<>(new TreeMap<>());
    }
    //Hashtable
    static <K, V> NullMap<K, V> newHashtable() {
        return new NullSuperMap<>(new Hashtable<>());
    }
    //LinkedHashMap
    static <K, V> NullMap<K, V> newLinkedHashMap() {
        return new NullSuperMap<>(new LinkedHashMap<>());
    }


    NullChain<V> get(K key);

    void remove(K key);

    void put(K key, V value);

    Set<Map.Entry<K, V>> entrySet();

    Collection<V> values();

    Set<K> keySet();

    void clear();

    void putAll(Map<? extends K, ? extends V> m);

    void putAll(NullMap<? extends K, ? extends V> m);

    boolean containsValue(V value);

    boolean containsKey(K key);

    boolean isEmpty();

    int size();

    NullChain<V> putIfAbsent(K key, V value);
    NullChain<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);
    NullChain<V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    NullChain<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    NullChain<V> merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);
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
