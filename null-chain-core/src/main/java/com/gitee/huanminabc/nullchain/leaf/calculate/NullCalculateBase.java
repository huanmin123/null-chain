package com.gitee.huanminabc.nullchain.leaf.calculate;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Null计算操作基础类 - 提供空值安全的数值计算功能
 * 
 * <p>该类提供了对数值计算操作的空值安全封装，支持各种数学运算如加减乘除、取绝对值、取最大值等。
 * 所有计算操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>基本运算：add、subtract、multiply、divide等基本数学运算</li>
 *   <li>数值处理：negate取反、abs取绝对值等</li>
 *   <li>比较运算：max、min等比较操作</li>
 *   <li>幂运算：pow幂运算</li>
 *   <li>舍入运算：round四舍五入</li>
 *   <li>映射运算：map数值映射</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>精度保证：使用BigDecimal确保计算精度</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>异常处理：完善的异常处理机制</li>
 * </ul>
 * 
 * @param <T> 计算值的类型，必须继承自BigDecimal
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullCalculate 计算操作接口
 * @see NullKernelAbstract 内核抽象基类
 * @see BigDecimal 高精度数值类型
 */
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
            linkLog.append(CALC_ADD_ARROW);
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
            linkLog.append(CALC_ADD_ARROW);
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
            linkLog.append(CALC_SUB_ARROW);
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
            linkLog.append(CALC_SUB_ARROW);
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
            linkLog.append(CALC_MUL_ARROW);
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
            linkLog.append(CALC_MUL_ARROW);
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
                throw new NullChainException(linkLog.append(CALC_DIV_Q).append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide =  ((BigDecimal)value).divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append(CALC_DIV_ARROW);
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
                throw new NullChainException(linkLog.append(CALC_DIV_Q).append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide = ((BigDecimal)value).divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append(CALC_DIV_ARROW);
            return NullBuild.noEmpty(divide);
        });
        return  NullBuild.busyCalc(this);

    }


    @Override
    public NullCalculate<T> negate() {
        this.taskList.add((value)->{
            BigDecimal negate = ((BigDecimal)value).negate();
            linkLog.append(CALC_NEGATE_ARROW);
            return NullBuild.noEmpty(negate);
        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> abs() {
        this.taskList.add((value)->{
            BigDecimal abs = ((BigDecimal)value).abs();
            linkLog.append(CALC_ABS_ARROW);
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
                    linkLog.append(CALC_MAX_ARROW);
                    return NullBuild.noEmpty(preValue);
                }
                if (preValue==null) {
                    linkLog.append(CALC_MAX_ARROW);
                    return NullBuild.noEmpty(t2);
                }
                double v1 = t2.doubleValue();
                BigDecimal max = ((BigDecimal)preValue).max(BigDecimal.valueOf(v1));
                linkLog.append(CALC_MAX_ARROW);
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
                    linkLog.append(CALC_MIN_ARROW);
                    return NullBuild.noEmpty(preValue);
                }
                if (preValue==null) {
                    linkLog.append(CALC_MIN_ARROW);
                    return NullBuild.noEmpty(t2);
                }
                double v1 = t2.doubleValue();
                BigDecimal min = ((BigDecimal)preValue).min(BigDecimal.valueOf(v1));
                linkLog.append(CALC_MIN_ARROW);
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
            linkLog.append(CALC_POW_ARROW);
            return NullBuild.noEmpty(pow);

        });
        return  NullBuild.busyCalc(this);

    }

    @Override
    public NullCalculate<T> round(int newScale, RoundingMode roundingMode) {
        this.taskList.add((preValue)->{
            if (roundingMode == null) {
                throw new NullChainException(linkLog.append(CALC_ROUND_Q).append("roundingMode不能是空").toString());
            }
            BigDecimal round = ((BigDecimal)preValue).setScale(newScale, roundingMode);
            linkLog.append(CALC_ROUND_ARROW);
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
                throw new NullChainException(linkLog.append(CALC_RESULT_PICK_VALUE_NULL).toString());
            }
            V v;
            try {
                v = pickValue.apply(((BigDecimal)preValue));
            } catch (Exception e) {
                linkLog.append(CALC_MAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (v == null) {
                return NullBuild.empty();
            }
            linkLog.append(CALC_MAP_ARROW);
            return NullBuild.noEmpty(v);
        });
        return  NullBuild.busy(this);
    }

}
