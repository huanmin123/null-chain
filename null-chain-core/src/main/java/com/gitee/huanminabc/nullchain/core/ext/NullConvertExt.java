package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullConvert;
import com.gitee.huanminabc.nullchain.common.NullChainException;

public interface NullConvertExt<T> extends NullConvert<T>, NullWorkFlowExt<T> {

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
}
