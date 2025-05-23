package com.gitee.huanminabc.nullchain.base.async;


import com.gitee.huanminabc.common.exception.StackTraceUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernelAsyncAbstract;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * @author huanmin
 * @date 2024/2/1
 */
@Slf4j
public class NullFinalityAsyncBase<T> extends NullKernelAsyncAbstract<T> implements NullFinalityAsync<T> {


    public NullFinalityAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullFinalityAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }

    @Override
    public boolean is() {
        if (isNull) {
            return true;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return Null.is(join);
    }

    @SafeVarargs
    @Override
    public final boolean isAny(NullFun<? super T, ?>... function) {
        if (isNull || Null.is(function)) {
            return true;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return true;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.is(fun.apply(join))) {
                    return true;
                }
            }
        } catch (Exception e) {
            linkLog.append("...is? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return false;
    }

    @SafeVarargs
    @Override
    public final boolean isAll(NullFun<? super T, ?>... function) {
        if (isNull || function == null) {
            return true;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return true;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.non(fun.apply(join))) {
                    return false;
                }
            }
        } catch (Exception e) {
            linkLog.append("...is? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return true;
    }


    @Override
    public boolean non() {
        if (isNull) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return !Null.is(join);
    }

    @SafeVarargs
    @Override
    public final boolean nonAll(NullFun<? super T, ?>... function) {
        if (isNull || function == null) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return false;
        }
        try {
            for (NullFun<? super T, ?> fun : function) {
                if (Null.is(fun.apply(join))) {
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
    public T getSafe() throws NullChainCheckException {
        if (isNull) {
            throw new NullChainCheckException(linkLog.toString());
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            throw new NullChainCheckException(linkLog.toString());
        }
        return join;
    }

    @Override
    public T get() {
        if (isNull) {
            throw new NullChainException(linkLog.toString());
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            throw new NullChainException(linkLog.toString());
        }
        return join;
    }

    @Override
    public T get(String exceptionMessage, Object... args) {
        if (isNull) {
            if (args == null || args.length == 0 || exceptionMessage == null) {
                linkLog.append(exceptionMessage == null ? "" : exceptionMessage);
            } else {
                String format = String.format(exceptionMessage.replaceAll("\\{\\s*}", "%s"), args);
                linkLog.append(" ").append(format);
            }
            throw new NullChainException(linkLog.toString());
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            throw new NullChainException(linkLog.toString());
        }
        return join;
    }


    @Override
    public <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
        if (isNull) {
            if (exceptionSupplier == null) {
                linkLog.append("...get? 异常处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Throwable e) {
                linkLog.append("...get? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrow(x, linkLog);
        }

        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            if (exceptionSupplier == null) {
                linkLog.append("...get? 异常处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Throwable e) {
                linkLog.append("...get? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrow(x, linkLog);
        }
        return join;
    }

    @Override
    public T orElseNull() {
        if (isNull) {
            return null;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return null;
        }
        return join;
    }


    @Override
    public NullCollect collect() {
        if (isNull) {
            throw new NullChainException(linkLog.toString());
        }
        try {
            completableFuture.join();//阻塞当前线程等待结果
        } catch (Exception e) {
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
        try {
            completableFuture.join();//阻塞当前线程等待结果
        } catch (Exception e) {
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
            } catch (Throwable e) {
                linkLog.append("...collectSafe? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrow(x, linkLog);
        }
        try {
            completableFuture.join();//阻塞当前线程等待结果
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return collect;
    }

    @Override
    public <U extends T> boolean eq(U obj) {
        if (isNull || obj == null) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return Null.eq(obj, join);
    }

    @Override
    public <U extends T> boolean eqAny(U... b) {
        if (isNull || Null.is(b)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return Null.eqAny(join, b);
    }

    @Override
    public <U extends T> boolean notEq(U obj) {
        //如果是空那么就返回true
        if (isNull || obj == null) {
            return true;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return !Null.eq(obj, join);
    }

    @Override
    public <U extends T> boolean notEqAll(U... b) {
        if (isNull || Null.is(b)) {
            return true;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return Null.notEqAll(join, b);
    }

    @SafeVarargs
    @Override
    public final <U extends T> boolean inAny(U... obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return false;
        }
        for (U u : obj) {
            if (join.equals(u)) {
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
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            return true;
        }
        for (U u : obj) {
            if (join.equals(u)) {
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
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (join instanceof Comparable) {
            if (join.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) join).compareTo(obj) <= 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean lt(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (join instanceof Comparable) {
            if (join.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) join).compareTo(obj) < 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean ge(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (join instanceof Comparable) {
            if (join.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) join).compareTo(obj) >= 0;
            }
        }
        return false;
    }

    @Override
    public <C extends Comparable<T>> boolean gt(C obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (join instanceof Comparable) {
            if (join.getClass().isAssignableFrom(obj.getClass())) {
                return ((Comparable) join).compareTo(obj) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean logic(Function<T, Boolean> obj) {
        if (isNull || Null.is(obj)) {
            return false;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return obj.apply(join);
    }

    @Override
    public int length() {
        if (isNull) {
            return 0;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        return NullReflectionKit.getSize(join);
    }

    @Override
    public T orElse(T defaultValue) {
        if (isNull) {
            if (Null.is(defaultValue)) {
                linkLog.append("...orElse? 默认值不能为空");
                throw new NullChainException(linkLog.toString());
            }
            return defaultValue;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
            if (Null.is(defaultValue)) {
                linkLog.append("...orElse? 默认值不能为空");
                throw new NullChainException(linkLog.toString());
            }
            return defaultValue;
        }
        return join;
    }

    @Override
    public T orElse(Supplier<T> defaultValue) {
        if (isNull) {
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
                linkLog.append("...orElse? 默认值不能为空");
                throw new NullChainException(linkLog.toString());
            }
            return t;
        }
        T join;
        try {
            join = completableFuture.join();
        } catch (Exception e) {
            throw new NullChainException(linkLog.toString());
        }
        if (Null.is(join)) {
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
                linkLog.append("...orElse? 默认值不能为空");
                throw new NullChainException(linkLog.toString());
            }
            return t;
        }
        return join;
    }


    @Override
    public void ifPresent(Consumer<? super T> action) {
        if (isNull) {
            return;
        }
        CompletableFuture<Object> objectCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (!Null.is(t)) {
                if (action == null) {
                    linkLog.append("...ifPresent? 参数不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept(t);
                } catch (Exception e) {
                    linkLog.append("...ifPresent? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
            return null;
        }, getCT(true));
        StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(4);
        objectCompletableFuture.exceptionally((e) -> {
            e.addSuppressed(new NullChainException(stackTraceElement.toString()));
            log.error("", e);
            return null;
        });
    }


    @Override
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (isNull) {
            return;
        }
        CompletableFuture<Object> objectCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                if (emptyAction == null) {
                    linkLog.append("...ifPresentOrElse-emptyAction? 参数不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    emptyAction.run();
                } catch (Exception e) {
                    linkLog.append("...ifPresentOrElse-emptyAction? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            } else {
                if (action == null) {
                    linkLog.append("...ifPresentOrElse-action? 参数不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept(t);
                } catch (Exception e) {
                    linkLog.append("...ifPresentOrElse-action? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
            return null;
        }, getCT(true));
        StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(4);
        objectCompletableFuture.exceptionally((e) -> {
            e.addSuppressed(new NullChainException(stackTraceElement.toString()));
            log.error("", e);
            return null;
        });
    }

    @Override
    public void except(Consumer<Throwable> consumer) {
        if (consumer == null) {
            throw new NullChainException(linkLog.append("...capture? 参数不能为空").toString());
        }
        if (isNull) {
            consumer.accept(new NullChainException(linkLog.toString()));
            return;
        }
        CompletableFuture<Object> objectCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                throw new NullChainException(linkLog.toString());
            }
            return null;
        }, getCT(true));
        StackTraceElement stackTraceElement = StackTraceUtil.stackTraceLevel(4);
        objectCompletableFuture.exceptionally((e) -> {
            e.addSuppressed(new NullChainException(stackTraceElement.toString()));
            consumer.accept(e);
            return null;
        });
    }


    @Override
    public String toString() {
        return "NullFinalityAsyncBase{" +
                "currentThreadFactoryName='" + currentThreadFactoryName + '\'' +
                ", isNull=" + isNull +
                ", linkLog=" + linkLog +
                '}';
    }
}
