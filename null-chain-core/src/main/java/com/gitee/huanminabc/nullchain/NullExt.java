package com.gitee.huanminabc.nullchain;

import com.gitee.huanminabc.nullchain.base.ext.NullChainExt;

/**
 * 使用NULLExt接口,可以在任何对象上直接调用链式操作 ,但是需要保证对象变量不为空
 * 如果使用了Null.createEmpty 方式创建的对象, 那么返回的一定不是null 而是一个空对象,可以通过空链的方式来处理
 * @param <T>
 */
public interface NullExt<T> extends NullChainExt<T>{

}
