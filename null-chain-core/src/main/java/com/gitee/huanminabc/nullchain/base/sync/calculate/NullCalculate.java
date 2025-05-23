package com.gitee.huanminabc.nullchain.base.sync.calculate;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.function.NullFun;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @description:  底层采用的是BigDecimal
 * 方法入参取值都是doubleValue 避免了精度丢失 ,计算的过程都是BigDecimal
 * @author: huanmin
 * @create: 2025-05-22 10:12
 **/
public interface NullCalculate<T extends Number> {

    //加
    <V extends Number> NullCalculate<T> add(V t2);

    //减
    <V extends Number> NullCalculate<T> subtract(V t2);

    //乘
    <V extends Number>NullCalculate<T> multiply(V t2) ;

    //除
    <V extends Number>NullCalculate<T> divide(V t2) ;

    //取余  比如 5.0 % 2=1.0
    NullCalculate<T> remainder(T t2);

    //取反 比如-1.0变成1.0  而1.0变成-1.0
    NullCalculate<T> negate();

    //取绝对值 比如-1.0变成1.0
    NullCalculate<T> abs();

    //取最大值
    NullCalculate<T> max(T t2);

    //取最小值
    NullCalculate<T> min(T t2);


    //舍入
    NullCalculate<T> round(int newScale,RoundingMode roundingMode);
    //默认保留2位 并且 四舍五入
    NullCalculate<T> round();

    <V extends Number> NullChain<V> result(NullFun<BigDecimal,V> pickValue );

}
