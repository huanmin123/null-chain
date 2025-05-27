package com.gitee.huanminabc.nullchain.member.calculate;

import com.gitee.huanminabc.nullchain.core.NullChain;
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
public interface NullCalculate<T extends BigDecimal> {

    //加
    <V extends Number> NullCalculate<T> add(V t2);
    <V extends Number> NullCalculate<T> add(V t2,V defaultValue);

    //减
    <V extends Number> NullCalculate<T> sub(V t2);
    <V extends Number> NullCalculate<T> sub(V t2, V defaultValue);

    //乘
    <V extends Number> NullCalculate<T> mul(V t2);

    <V extends Number> NullCalculate<T> mul(V t2, V defaultValue);

    //除
    <V extends Number> NullCalculate<T> div(V t2);
    <V extends Number> NullCalculate<T> div(V t2, V defaultValue);


    //取反 比如-1.0变成1.0  而1.0变成-1.0
    NullCalculate<T> negate();

    //取绝对值 比如-1.0变成1.0
    NullCalculate<T> abs();

    //取最大值
    NullCalculate<T> max(T t2);

    //取最小值
    NullCalculate<T> min(T t2);


    //取平方
    NullCalculate<T> pow(int n);


    //舍入
    NullCalculate<T> round(int newScale, RoundingMode roundingMode);

    //默认保留2位 并且 四舍五入
    NullCalculate<T> round();

    <V extends Number> NullChain<V> map(NullFun<BigDecimal, V> pickValue);
}
