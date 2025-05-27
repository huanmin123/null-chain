package com.gitee.huanminabc.nullchain.base.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.function.NullPredicate;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-02-21 17:49
 **/
public class NullStreamBase<T> extends NullKernelAbstract<T> implements NullStream<T> {
    public NullStreamBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect,taskList);
    }

    public NullStreamBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect,taskList);
    }

    @Override
    public <R> NullStream<R> map(NullFun<? super T, ? extends R> mapper) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (mapper == null) {
            throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
        }
        R stream;
        try {
            stream = (R) ((Stream) value).map(mapper);
        }  catch (Exception e) {
            linkLog.append("map? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("map->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public <R> NullStream<R> map2(NullFun2<NullChain<T>, ? super T, ? extends R> function) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (function == null) {
            throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
        }

        R stream;
        try {
            stream = (R) ((Stream) value).map((data)-> function.apply((NullChain) Null.of(data), (T)data));
        } catch (Exception e) {
            linkLog.append("map? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("map->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> filter(NullPredicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("filter? ").append("predicate must not be null").toString());
        }
        T stream;
        try {
            stream = (T) ((Stream) value).filter(predicate);
        } catch (Exception e) {
            linkLog.append("filter? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("filter->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> sorted() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        T stream;
        try {
            stream = (T) ((Stream) value).sorted();
        } catch (Exception e) {
            linkLog.append("sorted? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("sorted->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> sorted(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        T stream = null;
        try {
            stream = (T) ((Stream) value).sorted(comparator);
        } catch (Exception e) {
            linkLog.append("sorted? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("sorted->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> distinct() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        T stream;
        try {
            stream = (T) ((Stream) value).distinct();
        } catch (Exception e) {
            linkLog.append("distinct? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("distinct->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> limit(long maxSize) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (maxSize < 0) {
            throw new NullChainException(linkLog.append("limit? ").append("maxSize must be greater than 0").toString());
        }
        T stream;
        try {
            stream = (T) ((Stream) value).limit(maxSize);
        } catch (Exception e) {
            linkLog.append("limit? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("limit->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> skip(long n) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (n < 0) {
            throw new NullChainException(linkLog.append("skip? ").append("n must be greater than 0").toString());
        }
        T stream = null;
        try {
            stream = (T) ((Stream) value).skip(n);
        } catch (Exception e) {
            linkLog.append("skip? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("skip->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> then(Consumer<? super T> action) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (action == null) {
            throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
        }
        T stream = null;
        try {
            stream = (T) ((Stream) value).peek(action);
        } catch (Exception e) {
            linkLog.append("then? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("then->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public NullStream<T> then(NullConsumer2<NullChain<T>, ? super T> function) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (function == null) {
            throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
        }
        T stream;
        try {
            stream = (T) ((Stream) value).peek((data)->{function.accept((NullChain) Null.of(data), (T)data);});
        } catch (Exception e) {
            linkLog.append("then? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("then->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public <R> NullStream<R> flatStream(Function<? super T, ? extends NullStream<? extends R>> mapper) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect,taskList);
        }
        if (mapper == null) {
            throw new NullChainException(linkLog.append("flatMap? ").append("mapper must not be null").toString());
        }
        R stream;
        try {
            stream = (R) ((Stream) value).flatMap(mapper);
        } catch (Exception e) {
            linkLog.append("flatMap? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("flatMap->");
        return NullBuild.noEmptyStream(stream, linkLog, collect,taskList);
    }

    @Override
    public <R, A> NullChain<R> collect(Collector<? super T, A, R> collector) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (collector == null) {
            throw new NullChainException(linkLog.append("collect? ").append("collector must not be null").toString());
        }
        R stream;
        try {
            stream = (R) ((Stream) value).collect(collector);
        } catch (Exception e) {
            linkLog.append("collect? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("collect->");
        return NullBuild.noEmpty(stream, linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> max(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (comparator == null) {
            throw new NullChainException(linkLog.append("max? ").append("comparator must not be null").toString());
        }
        Optional max;
        try {
            max = ((Stream) value).max(comparator);
        } catch (Exception e) {
            linkLog.append("max? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (!max.isPresent()) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        linkLog.append("max->");
        return NullBuild.noEmpty((T) max.get(), linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> findFirst() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }

        Optional first;
        try {
            first = ((Stream) value).findFirst();
        } catch (Exception e) {
            linkLog.append("findFirst? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (!first.isPresent()) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        linkLog.append("findFirst->");
        return NullBuild.noEmpty((T) first.get(), linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> findAny() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }

        Optional any = null;
        try {
            any = ((Stream) value).findAny();
        } catch (Exception e) {
            linkLog.append("findAny? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (!any.isPresent()) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        linkLog.append("findAny->");
        return NullBuild.noEmpty((T) any.get(), linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> reduce(BinaryOperator<T> accumulator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (accumulator == null) {
            throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
        }
        Optional reduce;
        try {
            reduce = ((Stream) value).reduce(accumulator);
        } catch (Exception e) {
            linkLog.append("reduce? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (!reduce.isPresent()) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        linkLog.append("reduce->");
        return NullBuild.noEmpty((T) reduce.get(), linkLog, collect,taskList);
    }

    @Override
    public NullChain<Long> count() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        long stream;
        try {
            stream = ((Stream) value).count();
        } catch (Exception e) {
            linkLog.append("count? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("count->");
        return NullBuild.noEmpty(stream, linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> min(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (comparator == null) {
            throw new NullChainException(linkLog.append("min? ").append("comparator must not be null").toString());
        }
        Optional<T> min;
        try {
            min = ((Stream) value).min(comparator);
        } catch (Exception e) {
            linkLog.append("min? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (!min.isPresent()) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        linkLog.append("min->");
        return NullBuild.noEmpty(min.get(), linkLog, collect,taskList);
    }

    @Override
    public NullChain<Boolean> allMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("allMatch? ").append("predicate must not be null").toString());
        }
        boolean stream = false;
        try {
            stream = ((Stream) value).allMatch(predicate);
        } catch (Exception e) {
            linkLog.append("allMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("allMatch->");
        return NullBuild.noEmpty(stream, linkLog, collect,taskList);
    }

    @Override
    public NullChain<Boolean> anyMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("anyMatch? ").append("predicate must not be null").toString());
        }
        boolean stream;
        try {
            stream = ((Stream) value).anyMatch(predicate);
        } catch (Exception e) {
            linkLog.append("anyMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("anyMatch->");
        return NullBuild.noEmpty(stream, linkLog, collect,taskList);
    }

    @Override
    public NullChain<Boolean> noneMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect, taskList);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("noneMatch? ").append("predicate must not be null").toString());
        }
        boolean stream = false;
        try {
            stream = ((Stream) value).noneMatch(predicate);
        } catch (Exception e) {
            linkLog.append("noneMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("noneMatch->");
        return NullBuild.noEmpty(stream, linkLog, collect,taskList);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (isNull) {
            return;
        }
        if (action == null) {
            throw new NullChainException(linkLog.append("forEach? ").append("action must not be null").toString());
        }
        try {
            ((Stream) value).forEach(action);
        } catch (Exception e) {
            linkLog.append("forEach? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("forEach->");
    }
}