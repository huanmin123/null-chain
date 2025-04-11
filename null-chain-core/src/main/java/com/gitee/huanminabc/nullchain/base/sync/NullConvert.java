package com.gitee.huanminabc.nullchain.base.sync;


import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;

import java.util.Collection;
import java.util.Optional;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullConvert<T> extends NullTools<T> {

    //同步转异步
    NullChainAsync<T> async();
    //带线程池的同步转异步
    NullChainAsync<T> async(String threadFactoryName);


    //将object转化为指定的类型 这种一般用于,你通过某种操作,导致推导出来的类型变为了Object, 但是你知道它的具体类型, 那么你可以使用这个方法
    //并不支持将 String 转换为 Integer 这种
    //将Object转换为实际类型， 这样方便后续节点的处理
    <U> NullChain<U> type(Class<U> uClass);

    <U> NullChain<U> type(U uClass);

    //转换为stream，只能处理Collection的子类和数组 ,如果是map返回的是Map.Entry<K, V> , 那么需要<Map.Entry<String,Integer>>toStream()来指定类型
    <V> NullStream<V> toStream(Class<V> type);
    <V> NullStream<V> toStream();
    <V> NullStream<V> toParallelStream(Class<V> type);
    <V> NullStream<V> toParallelStream();

}
