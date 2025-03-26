package com.gitee.huanminabc.nullchain.base.sync.stream;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.function.NullPredicate;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
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
    public NullStreamBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullStreamBase(T object, StringBuilder linkLog, NullCollect collect) {
        super(object, linkLog, collect);
    }

    @Override
    public <R> NullStream<R> map(NullFun<? super T, ? extends R> mapper) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (mapper == null) {
            throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
        }
        linkLog.append("map->");
        R stream = (R) ((Stream) value).map(mapper);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public <R> NullStream<R> map(NullFun2<NullChain<T>, ? super T, ? extends R> function) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (function == null) {
            throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
        }
        linkLog.append("map->");
        R stream = (R) ((Stream) value).map((data)->{return function.apply((NullChain)Null.of(data), (T)data);});
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> filter(NullPredicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("filter? ").append("predicate must not be null").toString());
        }
        linkLog.append("filter->");
        T stream = (T) ((Stream) value).filter(predicate);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> sorted() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        linkLog.append("sorted->");
        T stream = (T) ((Stream) value).sorted();
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> sorted(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        linkLog.append("sorted->");
        T stream = (T) ((Stream) value).sorted(comparator);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> distinct() {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        linkLog.append("distinct->");
        T stream = (T) ((Stream) value).distinct();
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> limit(long maxSize) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (maxSize < 0) {
            throw new NullChainException(linkLog.append("limit? ").append("maxSize must be greater than 0").toString());
        }
        linkLog.append("limit->");
        T stream = (T) ((Stream) value).limit(maxSize);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> skip(long n) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (n < 0) {
            throw new NullChainException(linkLog.append("skip? ").append("n must be greater than 0").toString());
        }
        linkLog.append("skip->");
        T stream = (T) ((Stream) value).skip(n);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> then(Consumer<? super T> action) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (action == null) {
            throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
        }
        linkLog.append("then->");
        T stream = (T) ((Stream) value).peek(action);
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public NullStream<T> then(NullConsumer2<NullChain<T>, ? super T> function) {
        if (isNull) {
            return NullBuild.emptyStream(linkLog, collect);
        }
        if (function == null) {
            throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
        }
        linkLog.append("then->");
        T stream = (T) ((Stream) value).peek((data)->{function.accept((NullChain)Null.of(data), (T)data);});
        return NullBuild.noEmptyStream(stream, linkLog, collect);
    }

    @Override
    public <R, A> NullChain<R> collect(Collector<? super T, A, R> collector) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (collector == null) {
            throw new NullChainException(linkLog.append("collect? ").append("collector must not be null").toString());
        }
        linkLog.append("collect->");
        R stream = (R) ((Stream) value).collect(collector);
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public NullChain<T> max(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (comparator == null) {
            throw new NullChainException(linkLog.append("max? ").append("comparator must not be null").toString());
        }
        linkLog.append("max->");
        Optional max = ((Stream) value).max(comparator);
        if (!max.isPresent()) {
            return NullBuild.empty(linkLog, collect);
        }
        return NullBuild.noEmpty((T) max.get(), linkLog, collect);
    }

    @Override
    public NullChain<T> findFirst() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        linkLog.append("findFirst->");
        Optional first = ((Stream) value).findFirst();
        if (!first.isPresent()) {
            return NullBuild.empty(linkLog, collect);
        }
        return NullBuild.noEmpty((T) first.get(), linkLog, collect);
    }

    @Override
    public NullChain<T> findAny() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        linkLog.append("findAny->");
        Optional any = ((Stream) value).findAny();
        if (!any.isPresent()) {
            return NullBuild.empty(linkLog, collect);
        }
        return NullBuild.noEmpty((T) any.get(), linkLog, collect);
    }

    @Override
    public NullChain<T> reduce(BinaryOperator<T> accumulator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (accumulator == null) {
            throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
        }
        linkLog.append("reduce->");
        Optional reduce = ((Stream) value).reduce(accumulator);
        if (!reduce.isPresent()) {
            return NullBuild.empty(linkLog, collect);
        }
        return NullBuild.noEmpty((T) reduce.get(), linkLog, collect);
    }

    @Override
    public NullChain<Long> count() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        linkLog.append("count->");
        long stream = ((Stream) value).count();
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public NullChain<T> min(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (comparator == null) {
            throw new NullChainException(linkLog.append("min? ").append("comparator must not be null").toString());
        }
        linkLog.append("min->");
        T stream = (T) ((Stream) value).min(comparator).get();
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public NullChain<Boolean> allMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("allMatch? ").append("predicate must not be null").toString());
        }
        linkLog.append("allMatch->");
        boolean stream = ((Stream) value).allMatch(predicate);
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public NullChain<Boolean> anyMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("anyMatch? ").append("predicate must not be null").toString());
        }
        linkLog.append("anyMatch->");
        boolean stream = ((Stream) value).anyMatch(predicate);
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public NullChain<Boolean> noneMatch(Predicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("noneMatch? ").append("predicate must not be null").toString());
        }
        linkLog.append("noneMatch->");
        boolean stream = ((Stream) value).noneMatch(predicate);
        return NullBuild.noEmpty(stream, linkLog, collect);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (isNull) {
            return;
        }
        if (action == null) {
            throw new NullChainException(linkLog.append("forEach? ").append("action must not be null").toString());
        }
        linkLog.append("forEach->");
        ((Stream) value).forEach(action);
    }
}