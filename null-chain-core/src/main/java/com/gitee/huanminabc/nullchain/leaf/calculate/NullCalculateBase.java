package com.gitee.huanminabc.nullchain.leaf.calculate;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import java.util.function.Function;
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
    public NullCalculateBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog,taskList);
    }



    @Override
    public <V extends Number> NullCalculate<T> add(V t2) {
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
    public <V extends Number> NullCalculate<T> add(V t2, V defaultValue) {
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
    public <V extends Number> NullCalculate<T> sub(V t2) {
        this.taskList.add((value)->{
            if ( t2 == null) {
                return NullBuild.empty();
            }
            double v1 = t2.doubleValue();
            BigDecimal subtract = ((BigDecimal)value).subtract(BigDecimal.valueOf(v1));
            linkLog.append("subtract->");
            return NullBuild.noEmpty(subtract);
        });
        return  NullBuild.busyCalc(this);
    }

    @Override
    public <V extends Number> NullCalculate<T> sub(V t2, V defaultValue) {
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
            BigDecimal subtract =  ((BigDecimal)value).subtract(BigDecimal.valueOf(v1));
            linkLog.append("subtract->");
            return NullBuild.noEmpty(subtract);
        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public <V extends Number> NullCalculate<T> mul(V t2) {
        this.taskList.add((value)->{
            if ( t2 == null) {
                return NullBuild.empty();
            }
            double v1 = t2.doubleValue();
            BigDecimal multiply =  ((BigDecimal)value).multiply(BigDecimal.valueOf(v1));
            linkLog.append("multiply->");
            return NullBuild.noEmpty(multiply);
        });
        return  NullBuild.busyCalc(this);
    }

    @Override
    public <V extends Number> NullCalculate<T> mul(V t2, V defaultValue) {
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
            BigDecimal multiply = ((BigDecimal)value).multiply(BigDecimal.valueOf(v1));
            linkLog.append("multiply->");
            return NullBuild.noEmpty(multiply);
        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public <V extends Number> NullCalculate<T> div(V t2) {
        this.taskList.add((value)->{
            if (t2 == null) {
                return NullBuild.empty();
            }
            double v1 = t2.doubleValue();
            if (v1 == 0) {
                throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide =  ((BigDecimal)value).divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append("divide->");
            return NullBuild.noEmpty(divide);
        });
        return  NullBuild.busyCalc(this);
    }

    @Override
    public <V extends Number> NullCalculate<T> div(V t2, V defaultValue) {
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
            if (v1 == 0) {
                throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide = ((BigDecimal)value).divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append("divide->");
            return NullBuild.noEmpty(divide);
        });
        return  NullBuild.busyCalc(this);

    }


    @Override
    public NullCalculate<T> negate() {
        this.taskList.add((value)->{
            BigDecimal negate = ((BigDecimal)value).negate();
            linkLog.append("negate->");
            return NullBuild.noEmpty(negate);
        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> abs() {
        this.taskList.add((value)->{
            BigDecimal abs = ((BigDecimal)value).abs();
            linkLog.append("abs->");
            return NullBuild.noEmpty(abs);

        });
        return  NullBuild.busyCalc(this);
    }

    @Override
    public NullCalculate<T> max(BigDecimal t2) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (preValue==null && t2 == null) {
                    return NullBuild.empty();
                }
                if (preValue!=null && t2 == null) {
                    linkLog.append("max->");
                    return NullBuild.noEmpty(preValue);
                }
                if (preValue==null) {
                    linkLog.append("max->");
                    return NullBuild.noEmpty(t2);
                }
                double v1 = t2.doubleValue();
                BigDecimal max = ((BigDecimal)preValue).max(BigDecimal.valueOf(v1));
                linkLog.append("max->");
                return NullBuild.noEmpty(max);
            }

            @Override
            public boolean preNullEnd() {
                return false;
            }
        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> min(BigDecimal t2) {

        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (preValue==null && t2 == null) {
                    return NullBuild.empty();
                }
                if (preValue!=null && t2 == null) {
                    linkLog.append("min->");
                    return NullBuild.noEmpty(preValue);
                }
                if (preValue==null) {
                    linkLog.append("min->");
                    return NullBuild.noEmpty(t2);
                }
                double v1 = t2.doubleValue();
                BigDecimal min = ((BigDecimal)preValue).min(BigDecimal.valueOf(v1));
                linkLog.append("min->");
                return NullBuild.noEmpty(min);
            }

            @Override
            public boolean preNullEnd() {
                return false;
            }
        });
        return  NullBuild.busyCalc(this);
    }

    @Override
    public NullCalculate<T> pow(int n) {
        this.taskList.add((preValue)->{
            BigDecimal pow = ((BigDecimal)preValue).pow(n);
            linkLog.append("pow->");
            return NullBuild.noEmpty(pow);

        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> round(int newScale, RoundingMode roundingMode) {
        this.taskList.add((preValue)->{
            if (roundingMode == null) {
                throw new NullChainException(linkLog.append("round? ").append("roundingMode不能是空").toString());
            }
            BigDecimal round = ((BigDecimal)preValue).setScale(newScale, roundingMode);
            linkLog.append("round->");
            return NullBuild.noEmpty(round);

        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> round() {
        return round(2, RoundingMode.HALF_UP);
    }

    @Override
    public <V extends Number> NullChain<V> map(Function<BigDecimal, V> pickValue) {
        this.taskList.add((preValue)->{
            if (pickValue == null) {
                throw new NullChainException(linkLog.append("result? ").append("pickValue取值器不能是空").toString());
            }
            V v;
            try {
                v = pickValue.apply(((BigDecimal)preValue));
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (v == null) {
                return NullBuild.empty();
            }
            linkLog.append("map->");
            return NullBuild.noEmpty(v);
        });
        return  NullBuild.busy(this);
    }

}
