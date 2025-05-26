package com.gitee.huanminabc.nullchain.base.sync.ext;

import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.NullConvert;
import com.gitee.huanminabc.nullchain.common.NullChainException;

public interface NullConvertExt<T> extends NullConvert<T>, NullToolsExt<T> {

    @Override
    default NullChain<T> async() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.async();
    }

    @Override
    default NullChain<T> async(String threadFactoryName) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.async(threadFactoryName);
    }


    @Override
    default <U> NullChain<U> type(Class<U> uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.type(uClass);
    }

    @Override
    default <U> NullChain<U> type(U uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.type(uClass);
    }

//    @Override
//    default <C> NullStream<C> toStream(){
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.toStream();
//    }
//
//    @Override
//    default <V> NullStream<V> toParallelStream(){
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.toParallelStream();
//    }

}
