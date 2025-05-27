package com.gitee.huanminabc.nullchain.core;



/**
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullConvert<T> extends NullTools<T> {

    //同步转异步
    NullChain<T> async();
    //带线程池的同步转异步
    NullChain<T> async(String threadFactoryName);


    //将object转化为指定的类型 这种一般用于,你通过某种操作,导致推导出来的类型变为了Object, 但是你知道它的具体类型, 那么你可以使用这个方法
    //并不支持将 String 转换为 Integer 这种
    //将Object转换为实际类型， 这样方便后续节点的处理
    <U> NullChain<U> type(Class<U> uClass);

    <U> NullChain<U> type(U uClass);



}
