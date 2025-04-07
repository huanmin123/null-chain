package com.gitee.huanminabc.nullchain.base.async.stream;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullKernelAsyncAbstract;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.function.NullPredicate;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class NullStreamAsyncBase<T> extends NullKernelAsyncAbstract<T> implements NullStreamAsync<T>{


    public NullStreamAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullStreamAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }

    @Override
    public <R> NullStreamAsync<R> map(NullFun<? super T, ? extends R> mapper) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value) ) {
                return null;
            }
            if (mapper == null) {
                throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
            }
            try {
                R r = (R) ((Stream) value).map(mapper);
                linkLog.append("map->");
                return r;
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <R> NullStreamAsync<R> map2(NullFun2<NullChain<T>, ? super T, ? extends R> function) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("map? ").append("function must not be null").toString());
            }
            try {
                R stream = (R) ((Stream) value).map((data)->{return function.apply((NullChain)Null.of(data), (T)data);});
                linkLog.append("map->");
                return stream;
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> filter(NullPredicate<? super T> predicate) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value) ) {
                return null;
            }
            if (predicate == null) {
                throw new NullChainException(linkLog.append("filter? ").append("predicate must not be null").toString());
            }
            try {
                T t = (T) ((Stream) value).filter(predicate);
                linkLog.append("filter->");
                return t;
            } catch (Exception e) {
                linkLog.append("filter? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> sorted() {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                T sorted = (T) ((Stream) value).sorted();
                linkLog.append("sorted->");
                return sorted;
            } catch (Exception e) {
                linkLog.append("sorted? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> sorted(Comparator<? super T> comparator) {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (comparator == null) {
                throw new NullChainException(linkLog.append("sorted? ").append("comparator must not be null").toString());
            }
            try {
                T sorted = (T) ((Stream) value).sorted(comparator);
                linkLog.append("sorted->");
                return sorted;
            } catch (Exception e) {
                linkLog.append("sorted? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> distinct() {
        if (isNull) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                T distinct = (T) ((Stream) value).distinct();
                linkLog.append("distinct->");
                return distinct;
            } catch (Exception e) {
                linkLog.append("distinct? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> limit(long maxSize) {
        if (isNull ) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (maxSize < 0){
                throw new NullChainException(linkLog.append("limit? ").append("maxSize must be greater than 0").toString());
            }
            try {
                T limit = (T) ((Stream) value).limit(maxSize);
                linkLog.append("limit->");
                return limit;
            } catch (Exception e) {
                linkLog.append("limit? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> skip(long n) {
        if (isNull ) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (n < 0){
                throw new NullChainException(linkLog.append("skip? ").append("n must be greater than 0").toString());
            }
            try {
                T skip = (T) ((Stream) value).skip(n);
                linkLog.append("skip->");
                return skip;
            } catch (Exception e) {
                linkLog.append("skip? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> then(Consumer<? super T> action) {
        if (isNull ) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (action == null) {
                throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
            }
            try {
                T stream =  (T) ((Stream) value).peek(action);
                linkLog.append("then->");
                return stream;
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullStreamAsync<T> then(NullConsumer2<NullChain<T>, ? super T> function) {
        if (isNull ) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? ").append("function must not be null").toString());
            }
            try {
                T stream =  (T) ((Stream) value).peek((data)->{function.accept((NullChain)Null.of(data), (T)data);});
                linkLog.append("then->");
                return stream;
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <R> NullStreamAsync<R> flatStream(Function<? super T, ? extends NullStreamAsync<? extends R>> mapper) {
        if (isNull ) {
            return NullBuild.emptyStreamAsync(linkLog,collect);
        }
        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (mapper == null) {
                throw new NullChainException(linkLog.append("flatStream? ").append("mapper must not be null").toString());
            }
            try {
                R stream = (R) ((Stream) value).flatMap(mapper);
                linkLog.append("flatStream->");
                return stream;
            } catch (Exception e) {
                linkLog.append("flatStream? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyStreamAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public <R, A> NullChainAsync<R> collect(Collector<? super T, A, R> collector) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }

        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (collector == null) {
                throw new NullChainException(linkLog.append("collect? ").append("collector must not be null").toString());
            }
            try {
                R collect1 = (R) ((Stream) value).collect(collector);
                linkLog.append("collect->");
                return collect1;
            } catch (Exception e) {
                linkLog.append("collect? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> max(Comparator<? super T> comparator) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (comparator == null) {
                throw new NullChainException(linkLog.append("max? ").append("comparator must not be null").toString());
            }
            try {
                Optional max = ((Stream) value).max(comparator);
                if (!max.isPresent()){
                    return null;
                }
                linkLog.append("max->");
                return (T)max.get();
            } catch (Exception e) {
                linkLog.append("max? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }
        , getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> findFirst() {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                Optional first = ((Stream) value).findFirst();
                if (!first.isPresent()) {
                    return null;
                }
                linkLog.append("findFirst->");
                return (T)first.get();
            } catch (Exception e) {
                linkLog.append("findFirst? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> findAny() {
         if ( isNull ) {
             return NullBuild.emptyAsync(linkLog, collect);
         }
            CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
                if (Null.is(value)) {
                    return null;
                }
                try {
                    Optional any = ((Stream) value).findAny();
                    if (!any.isPresent()) {
                        return null;
                    }
                    linkLog.append("findAny->");
                    return (T)any.get();
                } catch (Exception e) {
                    linkLog.append("findAny? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }, getCT());
            return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> reduce(BinaryOperator<T> accumulator) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (accumulator == null) {
                throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
            }
            try {
                T reduce = (T) ((Stream) value).reduce(accumulator);
                linkLog.append("reduce->");
                return reduce;
            } catch (Exception e) {
                linkLog.append("reduce? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<Long> count() {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<Long> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                long count = ((Stream) value).count();
                linkLog.append("count->");
                return count;
            } catch (Exception e) {
                linkLog.append("count? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<T> min(Comparator<? super T> comparator) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (comparator == null) {
                throw new NullChainException(linkLog.append("min? ").append("comparator must not be null").toString());
            }
            try {
                Optional min = ((Stream) value).min(comparator);
                if (!min.isPresent()){
                    return null;
                }
                linkLog.append("min->");
                return (T)min;
            } catch (Exception e) {
                linkLog.append("min? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<Boolean> allMatch(Predicate<? super T> predicate) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<Boolean> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (predicate == null) {
                throw new NullChainException(linkLog.append("allMatch? ").append("predicate must not be null").toString());
            }
            try {
                boolean allMatch = ((Stream) value).allMatch(predicate);
                linkLog.append("allMatch->");
                return allMatch;
            } catch (Exception e) {
                linkLog.append("allMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<Boolean> anyMatch(Predicate<? super T> predicate) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<Boolean> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (predicate == null) {
                throw new NullChainException(linkLog.append("anyMatch? ").append("predicate must not be null").toString());
            }
            try {
                boolean anyMatch = ((Stream) value).anyMatch(predicate);
                linkLog.append("anyMatch->");
                return anyMatch;
            } catch (Exception e) {
                linkLog.append("anyMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public NullChainAsync<Boolean> noneMatch(Predicate<? super T> predicate) {
        if (isNull ) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<Boolean> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (predicate == null) {
                throw new NullChainException(linkLog.append("noneMatch? ").append("predicate must not be null").toString());
            }
            try {
                boolean noneMatch = ((Stream) value).noneMatch(predicate);
                linkLog.append("noneMatch->");
                return noneMatch;
            } catch (Exception e) {
                linkLog.append("noneMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, collect);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (isNull ) {
            return;
        }
        completableFuture.thenAcceptAsync((value) -> {
            if (Null.is(value)) {
                return;
            }
            if (action == null) {
                throw new NullChainException(linkLog.append("forEach? ").append("action must not be null").toString());
            }
            try {
                ((Stream) value).forEach(action);
                linkLog.append("forEach->");
            } catch (Exception e) {
                linkLog.append("forEach? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
    }
}
