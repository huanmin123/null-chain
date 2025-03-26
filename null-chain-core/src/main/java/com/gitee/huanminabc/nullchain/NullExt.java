package com.gitee.huanminabc.nullchain;

import com.gitee.huanminabc.nullchain.base.sync.ext.NullChainExt;

import java.io.Externalizable;

/**
 * 使用NULLExt接口,可以在任何对象上直接调用链式操作 ,但是需要保证对象变量不为空
 * @param <T>
 */
public interface NullExt<T> extends NullChainExt<T>{

}
