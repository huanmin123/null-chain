package com.gitee.huanminabc.nullchain.base;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullFinalityBase<T> extends NullKernelAbstract<T> implements NullFinality<T> {


    public NullFinalityBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect, taskList);
    }

    public NullFinalityBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect, taskList);
    }


    @Override
    public boolean is() {
        return taskList.runTaskAll().isNull;
    }

    @Override
    public boolean non() {
        return !is();
    }

    @Override
    public T getSafe() throws NullChainCheckException {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            throw new NullChainCheckException(nullChainBase.linkLog.toString());
        }
        return (T) nullChainBase.value;
    }

    @Override
    public T get() {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
        return (T) nullChainBase.value;
    }


    @Override
    public void ifPresent(Consumer<? super T> action) {
        taskList.runTaskAll((nullChainBase) -> {
            if (!nullChainBase.isNull) {
                if (action == null) {
                    nullChainBase.linkLog.append("...ifPresent? ");
                    throw new NullChainException(nullChainBase.linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    nullChainBase.linkLog.append("...ifPresent? ");
                    throw NullReflectionKit.addRunErrorMessage(e, nullChainBase.linkLog);
                }
            }
        },null);
    }


    @Override
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        taskList.runTaskAll((nullChainBase) -> {
            if (!nullChainBase.isNull) {
                if (action == null) {
                    nullChainBase.linkLog.append("...ifPresentOrElse-action? ");
                    throw new NullChainException(nullChainBase.linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    nullChainBase.linkLog.append("...ifPresentOrElse-action? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            } else {
                if (emptyAction == null) {
                    nullChainBase.linkLog.append("...ifPresentOrElse-emptyAction? ");
                    throw new NullChainException(nullChainBase.linkLog.toString());
                }
                try {
                    emptyAction.run();
                } catch (Exception e) {
                    nullChainBase.linkLog.append("...ifPresentOrElse-emptyAction? ");
                    throw NullReflectionKit.addRunErrorMessage(e, nullChainBase.linkLog);
                }
            }
        },null);
    }

    @Override
    public void except(Consumer<Throwable> consumer) {
        taskList.runTaskAll((nullChainBase) -> {
            if (nullChainBase.isNull) {
                consumer.accept(new NullChainException(nullChainBase.linkLog.toString()));
                return;
            }
            if (consumer == null) {
                throw new NullChainException(linkLog.append("...capture? 参数不能为空").toString());
            }
        },consumer);
    }


    @Override
    public <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        } else {
            if (exceptionSupplier == null) {
                nullChainBase.linkLog.append("...getSafe? 异常处理器不能为空");
                throw new NullChainException(nullChainBase.linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                nullChainBase.linkLog.append("...getSafe? ");
                throw NullReflectionKit.addRunErrorMessage(e, nullChainBase.linkLog);
            }
            throw NullReflectionKit.orThrowable(x, nullChainBase.linkLog);
        }
    }

    @Override
    public T get(String exceptionMessage, Object... args) {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        } else {
            if (args == null || args.length == 0 || exceptionMessage == null) {
                nullChainBase.linkLog.append(exceptionMessage == null ? "" : exceptionMessage);
            } else {
                String format = String.format(exceptionMessage.replaceAll("\\{\\s*}", "%s"), args);
                nullChainBase.linkLog.append(" ").append(format);
            }
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
    }

    @Override
    public T orElseNull() {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        return null;
    }


    @Override
    public NullCollect collect() {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
        return nullChainBase.collect;
    }

    @Override
    public NullCollect collect(String exceptionMessage, Object... args) {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
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
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            if (exceptionSupplier == null) {
                nullChainBase.linkLog.append("...collectSafe? 异常处理器不能为空");
                throw new NullChainException(nullChainBase.linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                nullChainBase.linkLog.append("...collectSafe? ");
                throw NullReflectionKit.addRunErrorMessage(e, nullChainBase.linkLog);
            }
            throw NullReflectionKit.orThrowable(x, nullChainBase.linkLog);
        }
        return collect;
    }


    @Override
    public T orElse(T defaultValue) {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        //判断defaultValue
        if (Null.is(defaultValue)) {
            nullChainBase.linkLog.append("...orElse? 默认值不能是空的");
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
        return defaultValue;
    }

    @Override
    public T orElse(Supplier<T> defaultValue) {
        NullChainBase<Object> nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        if (defaultValue == null) {
            linkLog.append("...orElse? 默认值不能为空");
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
        T t;
        try {
            t = defaultValue.get();
        } catch (Exception e) {
            nullChainBase.linkLog.append("...orElse? ");
            throw NullReflectionKit.addRunErrorMessage(e, nullChainBase.linkLog);
        }
        if (Null.is(t)) {
            nullChainBase.linkLog.append("orElse? 默认值不能是空的");
            throw new NullChainException(nullChainBase.linkLog.toString());
        }
        return t;

    }

    //    @Override
//    public int length() {
//        if (isNull) {
//            return 0;
//        }
//        return NullReflectionKit.getSize(value);
//    }


    //
//    @Override
//    public <U extends T> boolean eq(U obj) {
//        return Null.eq(obj, value);
//    }
//
//    @Override
//    public <U extends T> boolean eqAny(U... b) {
//        if (isNull || Null.is(b)) {
//            return false;
//        }
//        return Null.eqAny(value, b);
//    }
//
//    @Override
//    public <U extends T> boolean notEq(U obj) {
//        return !Null.eq(obj, value);
//    }
//
//    @Override
//    public <U extends T> boolean notEqAll(U... b) {
//        if (isNull || Null.is(b)) {
//            return true;
//        }
//        return Null.notEqAll(value, b);
//    }
//
//    @Override
//    public boolean logic(Function<T, Boolean> obj) {
//        if (isNull || obj == null) {
//            return false;
//        }
//        try {
//            return obj.apply(value);
//        } catch (Exception e) {
//            linkLog.append("...notEq? ");
//            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
//        }
//    }
//
//    @SafeVarargs
//    @Override
//    public final <U extends T> boolean inAny(U... obj) {
//        if (isNull || Null.is(obj)) {
//            return false;
//        }
//        for (U u : obj) {
//            if (value.equals(u)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//    @SafeVarargs
//    @Override
//    public final <U extends T> boolean notIn(U... obj) {
//        if (isNull || Null.is(obj)) {
//            return true;
//        }
//        for (U u : obj) {
//            if (value.equals(u)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public <C extends Comparable<T>> boolean le(C obj) {
//        if (isNull || Null.is(obj)) {
//            return false;
//        }
//        if (value instanceof Comparable) {
//            //判断obj是否是value的父类或者实现类
//            if (value.getClass().isAssignableFrom(obj.getClass())) {
//                return ((Comparable) value).compareTo(obj) <= 0;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public <C extends Comparable<T>> boolean lt(C obj) {
//        if (isNull || Null.is(obj)) {
//            return false;
//        }
//        if (value instanceof Comparable) {
//            //判断obj是否是value的父类或者实现类
//            if (value.getClass().isAssignableFrom(obj.getClass())) {
//                return ((Comparable) value).compareTo(obj) < 0;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public <C extends Comparable<T>> boolean ge(C obj) {
//        if (isNull || Null.is(obj)) {
//            return false;
//        }
//        if (value instanceof Comparable) {
//            //判断obj是否是value的父类或者实现类
//            if (value.getClass().isAssignableFrom(obj.getClass())) {
//                return ((Comparable) value).compareTo(obj) >= 0;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public <C extends Comparable<T>> boolean gt(C obj) {
//        if (isNull || Null.is(obj)) {
//            return false;
//        }
//        if (value instanceof Comparable) {
//            //判断obj是否是value的父类或者实现类
//            if (value.getClass().isAssignableFrom(obj.getClass())) {
//                return ((Comparable) value).compareTo(obj) > 0;
//            }
//        }
//        return false;
//    }
    @Override
    public String toString() {
        return "NullFinalityBase{" +
                "isNull=" + isNull +
                ", value=" + value +
                ", linkLog=" + linkLog +
                '}';
    }
}
