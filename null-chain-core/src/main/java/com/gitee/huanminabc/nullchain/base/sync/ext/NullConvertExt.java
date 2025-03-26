package com.gitee.huanminabc.nullchain.base.sync.ext;

import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.NullConvert;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.http.sync.OkHttpChain;

import java.util.concurrent.TimeUnit;

public interface NullConvertExt<T> extends NullConvert<T>, NullToolsExt<T> {

    @Override
    default NullChainAsync<T> async() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.async();
    }

    @Override
    default NullChainAsync<T> async(String threadFactoryName) throws NullChainException {
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

    @Override
    default <V> NullStream<V> toParallelStream(Class<V> type){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.toParallelStream(type);
    }

    @Override
    default <V> NullStream<V> toStream(Class<V> type){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.toStream(type);
    }

    @Override
    default <C> NullStream<C> toStream(){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.toStream();
    }

    @Override
    default <V> NullStream<V> toParallelStream(){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.toParallelStream();
    }
}
