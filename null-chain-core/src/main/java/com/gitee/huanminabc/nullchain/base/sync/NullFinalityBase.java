package com.gitee.huanminabc.nullchain.base.sync;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullFinalityBase<T> extends NullKernelAbstract<T> implements NullFinality<T> {


    public NullFinalityBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullFinalityBase(T object, StringBuilder linkLog, NullCollect collect) {
        super(object, linkLog, collect);
    }

    @SafeVarargs
    @Override
    public final boolean isAny(NullFun<? super T, ?>... function) {
        if (isNull || function == null) {
            return true;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.is(fun.apply(value))) {
                    return true;
                }
            }
        } catch (Exception e) {
            linkLog.append("...is? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return isNull;
    }

    @SafeVarargs
    @Override
    public final boolean isAll(NullFun<? super T, ?>... function) {
        if (isNull || function == null) {
            return false;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.non(fun.apply(value))) {
                    return false;
                }
            }
        } catch (Exception e) {
            linkLog.append("...isAll? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return true;
    }


    @Override
    public boolean is() {
        return isNull;
    }

    @Override
    public boolean non() {
        return !isNull;
    }

    @SafeVarargs
    @Override
    public final boolean nonAll(NullFun<? super T, ?>... function) {
        if (isNull || function == null) {
            return false;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.is(fun.apply(value))) {
                    return false;
                }
            }
        } catch (Exception e) {
            linkLog.append("...non? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return true;
    }


    @Override
    public <U extends T> boolean eq(U obj) {
        return !isNull && value.equals(obj);
    }

    @Override
    public <U extends T> boolean notEq(U obj) {
        //如果是空那么就返回true
        if (isNull || Null.is(obj)) {
            return true;
        }
        return !value.equals(obj);
    }
    @Override
    public  boolean logic(Function<T,Boolean> obj) {
        if (isNull || obj == null) {
            return false;
        }
        try {
            return obj.apply(value);
        } catch (Exception e) {
            linkLog.append("...notEq? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @SafeVarargs
    @Override
    public final <U extends T> boolean inAny(U... obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        for (U u : obj) {
            if (value.equals(u)) {
                return true;
            }
        }
        return false;
    }





    @SafeVarargs
    @Override
    public final <U extends T> boolean notIn(U... obj) {
        if (isNull || Null.is(obj)) {
            return true;
        }
        for (U u : obj) {
            if (value.equals(u)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <C extends Comparable<T>> boolean le(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        if (value instanceof Comparable) {
            //判断obj是否是value的父类或者实现类
            if (value.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) value).compareTo(obj) <= 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean lt(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        if (value instanceof Comparable) {
            //判断obj是否是value的父类或者实现类
            if (value.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) value).compareTo(obj) < 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean ge(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        if (value instanceof Comparable) {
            //判断obj是否是value的父类或者实现类
            if (value.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) value).compareTo(obj) >= 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean gt(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        if (value instanceof Comparable) {
            //判断obj是否是value的父类或者实现类
            if (value.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) value).compareTo(obj) > 0;
            }
        }
        return false;
    }


    @Override
    public void ifPresent(Consumer<? super T> action) {
        if (action == null) {
            linkLog.append("...ifPresent? ");
            throw new NullChainException(linkLog.toString());
        }
        if (!isNull ) {
            try {
                action.accept(value);
            } catch (Exception e) {
                linkLog.append("...ifPresent? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }
    }


    @Override
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (!isNull) {
            if (action == null) {
                linkLog.append("...ifPresentOrElse-action? ");
                throw new NullChainException(linkLog.toString());
            }
            try {
                action.accept(value);
            } catch (Exception e) {
                linkLog.append("...ifPresentOrElse-action? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        } else {
            if (emptyAction == null) {
                linkLog.append("...ifPresentOrElse-emptyAction? ");
                throw new NullChainException(linkLog.toString());
            }
            try {
                emptyAction.run();
            } catch (Exception e) {
                linkLog.append("...ifPresentOrElse-emptyAction? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }
    }

    @Override
    public int length() {
        if (isNull) {
            return 0;
        }
        return NullReflectionKit.getSize(value);
    }

    @Override
    public T getSafe() throws NullChainCheckException {
        if (isNull) {
            throw new NullChainCheckException(linkLog.toString());
        }
        return value;
    }

    @Override
    public T get() {
        if (isNull) {
            throw new NullChainException(linkLog.toString());
        }
        return value;
    }


    @Override
    public <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
        if (!isNull) {
            return value;
        } else {
            if (exceptionSupplier == null) {
                linkLog.append("...getSafe? 异常处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                linkLog.append("...getSafe? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrow(x, linkLog);
        }
    }

    @Override
    public T get(String exceptionMessage, Object... args) {
        if (!isNull) {
            return value;
        } else {
            if (args == null || args.length == 0 || exceptionMessage == null) {
                linkLog.append(exceptionMessage == null ? "" : exceptionMessage);
            } else {
                String format = String.format(exceptionMessage.replaceAll("\\{\\s*}", "%s"), args);
                linkLog.append(" ").append(format);
            }
            throw new NullChainException(linkLog.toString());
        }
    }


    @Override
    public NullCollect collect() {
        if (isNull) {
            throw new NullChainException(linkLog.toString());
        }
        return collect;
    }

    @Override
    public NullCollect collect(String exceptionMessage, Object... args) {
        if (isNull) {
            if (args == null || args.length == 0 || exceptionMessage == null) {
                linkLog.append(exceptionMessage == null ? "" : exceptionMessage);
            } else {
                String format = String.format(exceptionMessage.replaceAll("\\{\\s*}", "%s"), args);
                linkLog.append(" ").append(format);
            }
            throw new NullChainException(linkLog.toString());
        }
        return collect;
    }

    @Override
    public <X extends Throwable> NullCollect collect(Supplier<? extends X> exceptionSupplier) throws X {
        if (isNull) {
            if (exceptionSupplier == null) {
                linkLog.append("...collectSafe? 异常处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                linkLog.append("...collectSafe? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrow(x, linkLog);
        }
        return collect;
    }


    @Override
    public T orElse(T defaultValue) {
        if (!isNull) {
            return value;
        }
        //判断defaultValue
        if (Null.is(defaultValue)) {
            linkLog.append("...orElse? 默认值不能是空的");
            throw new NullChainException(linkLog.toString());
        }
        return defaultValue;
    }

    @Override
    public T orElse(Supplier<T> defaultValue) {
        if (!isNull) {
            return value;
        }
        if (defaultValue == null) {
            linkLog.append("...orElse? 默认值不能为空");
            throw new NullChainException(linkLog.toString());
        }
        T t;
        try {
            t = defaultValue.get();
        } catch (Exception e) {
            linkLog.append("...orElse? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (Null.is(t)) {
            linkLog.append("orElse? 默认值不能是空的");
            throw new NullChainException(linkLog.toString());
        }
        return t;

    }


    @Override
    public String toString() {
        return "NullFinalityBase{" +
                "isNull=" + isNull +
                ", value=" + value +
                ", linkLog=" + linkLog +
                '}';
    }
}
