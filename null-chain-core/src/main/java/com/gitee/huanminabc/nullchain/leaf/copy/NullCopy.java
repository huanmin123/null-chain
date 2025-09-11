package com.gitee.huanminabc.nullchain.leaf.copy;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.core.NullChain;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-28 13:39
 **/
public interface NullCopy<T> extends NullChain<T>  , NullKernel<T>{

    /**
     * 复制上一个任务的值,返回新的任务(浅拷贝)
     * @return
     */
    NullCopy<T> copy();

    /**
     * 复制上一个任务的值,返回新的任务(复制深拷贝)
     * 注意: 需要拷贝的类必须实现Serializable接口 , 包括内部类 ,否则会报错:NotSerializableException
     * @return
     */
    NullCopy<T> deepCopy();

    //提取自己需要的字段,返回新的对象
    <U> NullCopy<T> pick(Function<? super T, ? extends U>... mapper);

}
