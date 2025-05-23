package com.gitee.huanminabc.nullchain.base.async.calculate;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-22 10:12
 **/
@Slf4j
public class NullCalculateAsyncBase<T extends BigDecimal> extends NullKernelAsyncAbstract<T> implements NullCalculateAsync<T> {
    public NullCalculateAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullCalculateAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }


    @Override
    public <V extends Number> NullCalculateAsync<T> add(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = t2.doubleValue();
            BigDecimal add = value.add(BigDecimal.valueOf(v1));
            linkLog.append("add->");
            return add;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> add(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalcAsync(linkLog, collect);
            }
            t2 = defaultValue;
        }
        V finalT = t2;
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = finalT.doubleValue();
            BigDecimal add = value.add(BigDecimal.valueOf(v1));
            linkLog.append("add->");
            return add;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> subtract(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = t2.doubleValue();
            BigDecimal subtract = value.subtract(BigDecimal.valueOf(v1));
            linkLog.append("subtract->");
            return subtract;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> subtract(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalcAsync(linkLog, collect);
            }
            t2 = defaultValue;
        }
        V finalT = t2;
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = finalT.doubleValue();
            BigDecimal subtract = value.subtract(BigDecimal.valueOf(v1));
            linkLog.append("subtract->");
            return subtract;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> multiply(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = t2.doubleValue();
            BigDecimal multiply = value.multiply(BigDecimal.valueOf(v1));
            linkLog.append("multiply->");
            return multiply;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> multiply(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalcAsync(linkLog, collect);
            }
            t2 = defaultValue;
        }
        V finalT = t2;
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = finalT.doubleValue();
            BigDecimal multiply = value.multiply(BigDecimal.valueOf(v1));
            linkLog.append("multiply->");
            return multiply;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> divide(V t2) {
        if (isNull || t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            double v1 = t2.doubleValue();
            if (v1 == 0) {
                throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide = value.divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append("divide->");
            return divide;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public <V extends Number> NullCalculateAsync<T> divide(V t2, V defaultValue) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        if (t2 == null) {
            if (defaultValue == null) {
                return NullBuild.emptyCalcAsync(linkLog, collect);
            }
            t2 = defaultValue;
        }
        double v1 = t2.doubleValue();
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (v1 == 0) {
                throw new NullChainException(linkLog.append("divide? ").append("除数不能为0").toString());
            }
            //在进行除法运算时，可能会出现除不尽的情况，从而导致无限循环小数。当有溢出的的时候会进行截取到16位然后四舍五入到15位
            BigDecimal divide = value.divide(BigDecimal.valueOf(v1), 15, RoundingMode.HALF_UP);
            linkLog.append("divide->");
            return divide;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }


    @Override
    public NullCalculateAsync<T> negate() {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }

        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            BigDecimal negate = value.negate();
            linkLog.append("negate->");
            return negate;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> abs() {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            BigDecimal abs = value.abs();
            linkLog.append("abs->");
            return abs;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> max(T t2) {
        if (isNull && t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.non(value)&& t2 == null) {
                linkLog.append("max->");
                return value;
            }
            if (Null.is(value)) {
                linkLog.append("max->");
                return t2;
            }
            double v1 = t2.doubleValue();
            BigDecimal max = value.max(BigDecimal.valueOf(v1));
            linkLog.append("max->");
            return max;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> min(T t2) {
        if (isNull && t2 == null) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.non(value) && t2 == null) {
                linkLog.append("min->");
                return value;
            }
            if (Null.is(value)) {
                linkLog.append("min->");
                return t2;
            }
            double v1 = t2.doubleValue();
            BigDecimal min = value.min(BigDecimal.valueOf(v1));
            linkLog.append("min->");
            return min;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> pow(int n) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            BigDecimal pow = value.pow(n);
            linkLog.append("pow->");
            return pow;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> round(int newScale, RoundingMode roundingMode) {
        if (isNull) {
            return NullBuild.emptyCalcAsync(linkLog, collect);
        }
        CompletableFuture<BigDecimal> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (roundingMode == null) {
                throw new NullChainException(linkLog.append("round? ").append("roundingMode不能是空").toString());
            }
            BigDecimal round = value.setScale(0, roundingMode);
            linkLog.append("round->");
            return round;
        },getCT(true));
        return NullBuild.noEmptyCalcAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

    @Override
    public NullCalculateAsync<T> round() {
        return round(2, RoundingMode.HALF_UP);
    }

    @Override
    public <V extends Number> NullChainAsync<V> map(NullFun<BigDecimal, V> pickValue) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<V> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (pickValue == null) {
                throw new NullChainException(linkLog.append("map? ").append("pickValue取值器不能是空").toString());
            }
            V v;
            try {
                v = pickValue.apply(value);
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (v == null) {
                return null;
            }
            linkLog.append("map->");
            return v;
        },getCT(true));
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog,this.currentThreadFactoryName, collect);
    }

}
