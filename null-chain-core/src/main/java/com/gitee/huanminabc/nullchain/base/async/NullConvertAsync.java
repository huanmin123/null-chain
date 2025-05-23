package com.gitee.huanminabc.nullchain.base.async;


import com.gitee.huanminabc.nullchain.base.async.calculate.NullCalculateAsync;
import com.gitee.huanminabc.nullchain.base.async.stream.NullStreamAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.http.async.OkHttpAsyncChain;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullConvertAsync<T> extends NullToolsAsync<T> {


    /**
     * 异步转同步
     */
    NullChain<T> sync();

    //=====================个性化处理, 针对日常最常用的几种类型转换=====================

    /**
     * //将object转化为指定的类型 这种一般用于,你通过某种操作,导致推导出来的类型变为了Object, 但是你知道它的具体类型, 那么你可以使用这个方法
     * @param uClass
     * @return
     * @param <U>
     *  如果类型转换失败, 会抛出异常
     */
    <U> NullChainAsync<U> type(Class<U> uClass) ;

    <U> NullChainAsync<U> type(U type) ;


    //转换为stream，只能处理Collection的子类和数组
    //xxx.<AgentFeeRatio>toStream()
    <V> NullStreamAsync<V> toStream();
    <V> NullStreamAsync<V> toParallelStream();

    //转计算 , 支持Number 和 String(数字)
    NullCalculateAsync<BigDecimal> toCalc();

}
