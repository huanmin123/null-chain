package com.gitee.huanminabc.nullchain.core;



/**
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullConvert<T> extends NullWorkFlow<T> {
    //将object转化为指定的类型 这种一般用于,你通过某种操作,导致推导出来的类型变为了Object, 但是你知道它的具体类型, 那么你可以使用这个方法
    //并不支持将 String 转换为 Integer 这种
    //等效于 User user=(User)obj;
    <U> NullChain<U> type(Class<U> uClass);

    <U> NullChain<U> type(U uClass);
}
