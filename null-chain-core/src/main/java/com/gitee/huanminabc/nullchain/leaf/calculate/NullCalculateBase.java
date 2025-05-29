package com.gitee.huanminabc.nullchain.leaf.calculate;

import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
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
public class NullCalculateBase<T extends BigDecimal> extends NullKernelAbstract<T> implements NullCalculate<BigDecimal> {
    public NullCalculateBase(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(linkLog, collect,taskList);
    }



    @Override
    public <V extends Number> NullCalculate<BigDecimal> add(V t2) {
        this.taskList.add((value)->{
            if (t2 == null) {
                return NullBuild.empty();
            }
            double v1 = t2.doubleValue();
            BigDecimal add = ((BigDecimal)value).add(BigDecimal.valueOf(v1));
            linkLog.append("add->");
            return NullBuild.noEmpty(add);
        });
        return  NullBuild.busyCalc(this);


    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> add(V t2, V defaultValue) {
        final  V t2f = t2;
        this.taskList.add((value)->{
            V t2Value = t2f;
            if (t2Value == null) {
                if (defaultValue == null) {
                    return NullBuild.empty();
                }
                t2Value = defaultValue;
            }
            double v1 = t2Value.doubleValue();
            BigDecimal add = ((BigDecimal)value).add(BigDecimal.valueOf(v1));
            linkLog.append("add->");
            return NullBuild.noEmpty(add);
        });
        return  NullBuild.busyCalc(this);



    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> sub(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        double v1 = t2.doubleValue();
        BigDecimal subtract = value.subtract(BigDecimal.valueOf(v1));
        linkLog.append("subtract->");
        return NullBuild.noEmptyCalc(subtract, linkLog, collect, taskList);
    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> sub(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalc(linkLog, collect, taskList);
            }
            t2 = defaultValue;
        }
        double v1 = t2.doubleValue();
        BigDecimal subtract = value.subtract(BigDecimal.valueOf(v1));
        linkLog.append("subtract->");
        return NullBuild.noEmptyCalc(subtract, linkLog, collect, taskList);
    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> mul(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        double v1 = t2.doubleValue();
        BigDecimal multiply = value.multiply(BigDecimal.valueOf(v1));
        linkLog.append("multiply->");
        return NullBuild.noEmptyCalc(multiply, linkLog, collect, taskList);
    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> mul(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalc(linkLog, collect, taskList);
            }
            t2 = defaultValue;
        }
        double v1 = t2.doubleValue();
        BigDecimal multiply = value.multiply(BigDecimal.valueOf(v1));
        linkLog.append("multiply->");
        return NullBuild.noEmptyCalc(multiply, linkLog, collect, taskList);
    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> div(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        double v1 = t2.doubleValue();
        if (v1 == 0) {
            throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
        }
        //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
        BigDecimal divide = value.divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
        linkLog.append("divide->");
        return NullBuild.noEmptyCalc(divide, linkLog, collect, taskList);
    }

    @Override
    public <V extends Number> NullCalculate<BigDecimal> div(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalc(linkLog, collect, taskList);
            }
            t2 = defaultValue;
        }
        double v1 = t2.doubleValue();
        if (v1 == 0) {
            throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
        }
        //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
        BigDecimal divide = value.divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
        linkLog.append("divide->");
        return NullBuild.noEmptyCalc(divide, linkLog, collect, taskList);
    }


    @Override
    public NullCalculate<BigDecimal> negate() {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        BigDecimal negate = value.negate();
        linkLog.append("negate->");
        return NullBuild.noEmptyCalc(negate, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> abs() {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        BigDecimal abs = value.abs();
        linkLog.append("abs->");
        return NullBuild.noEmptyCalc(abs, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> max(BigDecimal t2) {
        if (isNull && t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (!isNull && t2 == null) {
            linkLog.append("max->");
            return NullBuild.noEmptyCalc(value, linkLog, collect, taskList);
        }
        if (isNull) {
            linkLog.append("max->");
            return NullBuild.noEmptyCalc(t2, linkLog, collect, taskList);
        }
        double v1 = t2.doubleValue();
        BigDecimal max = value.max(BigDecimal.valueOf(v1));
        linkLog.append("max->");
        return NullBuild.noEmptyCalc(max, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> min(BigDecimal t2) {
        if (isNull && t2 == null) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (!isNull && t2 == null) {
            linkLog.append("min->");
            return NullBuild.noEmptyCalc(value, linkLog, collect, taskList);
        }
        if (isNull) {
            linkLog.append("min->");
            return NullBuild.noEmptyCalc(t2, linkLog, collect, taskList);
        }
        double v1 = t2.doubleValue();
        BigDecimal min = value.min(BigDecimal.valueOf(v1));
        linkLog.append("min->");
        return NullBuild.noEmptyCalc(min, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> pow(int n) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        BigDecimal pow = value.pow(n);
        linkLog.append("pow->");
        return NullBuild.noEmptyCalc(pow, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> round(int newScale, RoundingMode roundingMode) {
        if (isNull) {
            return NullBuild.emptyCalc(linkLog, collect, taskList);
        }
        if (roundingMode == null) {
            throw new NullChainException(linkLog.append("round? ").append("roundingMode不能是空").toString());
        }
        BigDecimal round = value.setScale(0, roundingMode);
        linkLog.append("round->");
        return NullBuild.noEmptyCalc(round, linkLog, collect, taskList);
    }

    @Override
    public NullCalculate<BigDecimal> round() {
        return round(2, RoundingMode.HALF_UP);
    }

    @Override
    public <V extends Number> NullChain<V> map(NullFun<BigDecimal, V> pickValue) {
        this.taskList.add((__)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (pickValue == null) {
                throw new NullChainException(linkLog.append("result? ").append("pickValue取值器不能是空").toString());
            }
            V v;
            try {
                v = pickValue.apply(value);
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (v == null) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            linkLog.append("map->");
            return NullBuild.noEmpty(v, linkLog, collect, taskList);
        });
        return  NullBuild.busy(this);
    }

}
