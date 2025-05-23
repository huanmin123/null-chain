package com.gitee.huanminabc.nullchain.base.async.calculate;

import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.function.NullFun;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @description: 底层采用的是BigDecimal
 * 方法入参取值都是doubleValue 避免了精度丢失 ,计算的过程都是BigDecimal
 * 要求实现Number的类必须都实现doubleValue方法并且返回正确的值
 * @author: huanmin
 * @create: 2025-05-22 10:12
 **/
public interface NullCalculateAsync<T extends Number> {

    //加
    <V extends Number> NullCalculateAsync<T> add(V t2);
    <V extends Number> NullCalculateAsync<T> add(V t2, V defaultValue);

    //减
    <V extends Number> NullCalculateAsync<T> subtract(V t2);
    <V extends Number> NullCalculateAsync<T> subtract(V t2, V defaultValue);

    //乘
    <V extends Number> NullCalculateAsync<T> multiply(V t2);

    <V extends Number> NullCalculateAsync<T> multiply(V t2, V defaultValue);

    //除
    <V extends Number> NullCalculateAsync<T> divide(V t2);
    <V extends Number> NullCalculateAsync<T> divide(V t2, V defaultValue);


    //取反 比如-1.0变成1.0  而1.0变成-1.0
    NullCalculateAsync<T> negate();

    //取绝对值 比如-1.0变成1.0
    NullCalculateAsync<T> abs();

    //取最大值
    NullCalculateAsync<T> max(T t2);

    //取最小值
    NullCalculateAsync<T> min(T t2);


    //取平方
    NullCalculateAsync<T> pow(int n);


    //舍入
    NullCalculateAsync<T> round(int newScale, RoundingMode roundingMode);

    //默认保留2位 并且 四舍五入
    NullCalculateAsync<T> round();

    <V extends Number> NullChainAsync<V> map(NullFun<BigDecimal, V> pickValue);

}
