package com.gitee.huanminabc.nullchain.base.sync.calculate;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-22 10:12
 **/
@Slf4j
public class NullCalculateBase<T extends BigDecimal> extends NullKernelAbstract<T> implements NullCalculate<T> {
    public NullCalculateBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullCalculateBase(T object, StringBuilder linkLog, NullCollect collect) {
        super(object, linkLog, collect);
    }


    @Override
    public <V extends Number> NullCalculate<T> add(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal add = value.add(BigDecimal.valueOf(v1));
        linkLog.append("add->");
        return NullBuild.noEmptyCalc(add, linkLog, collect);
    }

    @Override
    public <V extends Number> NullCalculate<T> subtract(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal subtract = value.subtract(BigDecimal.valueOf(v1));
        linkLog.append("subtract->");
        return NullBuild.noEmptyCalc(subtract, linkLog, collect);
    }

    @Override
    public <V extends Number> NullCalculate<T> multiply(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal multiply = value.multiply(BigDecimal.valueOf(v1));
        linkLog.append("multiply->");
        return NullBuild.noEmptyCalc(multiply, linkLog, collect);
    }

    @Override
    public <V extends Number> NullCalculate<T> divide(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        if (v1 == 0) {
            throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
        }
        //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
        BigDecimal divide = value.divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
        linkLog.append("divide->");
        return NullBuild.noEmptyCalc(divide, linkLog, collect);
    }

    @Override
    public NullCalculate<T> remainder(T t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal remainder = value.remainder(BigDecimal.valueOf(v1));
        linkLog.append("remainder->");
        return NullBuild.noEmptyCalc(remainder, linkLog, collect);
    }

    @Override
    public NullCalculate<T> negate() {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        BigDecimal negate = value.negate();
        linkLog.append("negate->");
        return NullBuild.noEmptyCalc(negate, linkLog, collect);
    }

    @Override
    public NullCalculate<T> abs() {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        BigDecimal abs = value.abs();
        linkLog.append("abs->");
        return NullBuild.noEmptyCalc(abs, linkLog, collect);
    }

    @Override
    public NullCalculate<T> max(T t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal max = value.max(BigDecimal.valueOf(v1));
        linkLog.append("max->");
        return NullBuild.noEmptyCalc(max, linkLog, collect);
    }

    @Override
    public NullCalculate<T> min(T t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        double v1 = t2.doubleValue();
        BigDecimal min = value.min(BigDecimal.valueOf(v1));
        linkLog.append("min->");
        return NullBuild.noEmptyCalc(min, linkLog, collect);
    }

    @Override
    public NullCalculate<T> round(int newScale, RoundingMode roundingMode) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect);
        }
        if (roundingMode == null) {
            throw new NullChainException(linkLog.append("round? ").append("roundingMode不能是空").toString());
        }
        BigDecimal round = value.setScale(0, roundingMode);
        linkLog.append("round->");
        return NullBuild.noEmptyCalc(round, linkLog, collect);
    }

    @Override
    public NullCalculate<T> round() {
        return round(2, RoundingMode.HALF_UP);
    }

    @Override
    public <V extends Number> NullChain<V> result(NullFun<BigDecimal, V> pickValue) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (pickValue == null) {
            throw new NullChainException(linkLog.append("result? ").append("pickValue取值器不能是空").toString());
        }
        V v = pickValue.apply(value);
        if (v == null) {
            return NullBuild.empty(linkLog, collect);
        }
        linkLog.append("result->");
        return NullBuild.noEmpty(v, linkLog, collect);
    }

}
