package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.function.NullPredicate;

import java.math.BigDecimal;
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
    public NullStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super( linkLog, taskList);
    }

    @Override
    public NullStream<T> parallel() {
        this.taskList.add((value)->{
            if (!(value instanceof Stream)) {
                throw new NullChainException(linkLog.append("parallel? ").append("value must be a Stream").toString());
            }
            T parallel = (T)((Stream) value).parallel();
            return  NullBuild.noEmpty(parallel);
        });
        return  NullBuild.busyStream(this);

    }

    @Override
    public <R> NullStream<R> map(NullFun<? super T, ? extends R> mapper) {
        this.taskList.add((value)->{

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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public <R> NullStream<R> map2(NullFun2<NullChain<T>, ? super T, ? extends R> function) {
        this.taskList.add((value)->{
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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> filter(NullPredicate<? super T> predicate) {
        this.taskList.add((value)->{
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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> sorted() {
        this.taskList.add((value)->{
            T stream;
            try {
                stream = (T) ((Stream) value).sorted();
            } catch (Exception e) {
                linkLog.append("sorted? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("sorted->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> sorted(Comparator<? super T> comparator) {
        this.taskList.add((value)->{
            T stream;
            try {
                stream = (T) ((Stream) value).sorted(comparator);
            } catch (Exception e) {
                linkLog.append("sorted? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("sorted->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);

    }

    @Override
    public NullStream<T> distinct() {
        this.taskList.add((value)->{
            T stream;
            try {
                stream = (T) ((Stream) value).distinct();
            } catch (Exception e) {
                linkLog.append("distinct? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("distinct->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> limit(long maxSize) {
        this.taskList.add((value)->{
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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> skip(long n) {
        this.taskList.add((value)->{
            if (n < 0) {
                throw new NullChainException(linkLog.append("skip? ").append("n must be greater than 0").toString());
            }
            T stream;
            try {
                stream = (T) ((Stream) value).skip(n);
            } catch (Exception e) {
                linkLog.append("skip? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("skip->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> then(Consumer<? super T> action) {
        this.taskList.add((value)->{
            if (action == null) {
                throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
            }
            T stream;
            try {
                stream = (T) ((Stream) value).peek(action);
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("then->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> then(NullConsumer2<NullChain<T>, ? super T> function) {
        this.taskList.add((value)->{
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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public <R> NullStream<R> flatStream(Function<? super T, ? extends NullStream<? extends R>> mapper) {
        this.taskList.add((value)->{
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
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busyStream(this);
    }

    @Override
    public <R, A> NullChain<R> collect(Collector<? super T, A, R> collector) {
        this.taskList.add((preValue)->{
            if (collector == null) {
                throw new NullChainException(linkLog.append("collect? ").append("collector must not be null").toString());
            }
            R stream;
            try {
                stream = (R) ((Stream) preValue).collect(collector);
            } catch (Exception e) {
                linkLog.append("collect? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("collect->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> max(Comparator<? super T> comparator) {
        this.taskList.add((preValue)->{
            if (comparator == null) {
                throw new NullChainException(linkLog.append("max? ").append("comparator must not be null").toString());
            }
            Optional max;
            try {
                max = ((Stream) preValue).max(comparator);
            } catch (Exception e) {
                linkLog.append("max? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (!max.isPresent()) {
                return NullBuild.empty();
            }
            linkLog.append("max->");
            return NullBuild.noEmpty((T) max.get());
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> findFirst() {
        this.taskList.add((preValue)->{
            Optional first;
            try {
                first = ((Stream) preValue).findFirst();
            } catch (Exception e) {
                linkLog.append("findFirst? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (!first.isPresent()) {
                return NullBuild.empty();
            }
            linkLog.append("findFirst->");
            return NullBuild.noEmpty((T) first.get());
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> findAny() {
        this.taskList.add((preValue)->{
            Optional any;
            try {
                any = ((Stream) preValue).findAny();
            } catch (Exception e) {
                linkLog.append("findAny? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (!any.isPresent()) {
                return NullBuild.empty();
            }
            linkLog.append("findAny->");
            return NullBuild.noEmpty((T) any.get());
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> reduce(BinaryOperator<T> accumulator) {
        this.taskList.add((preValue)->{
            if (accumulator == null) {
                throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
            }
            Optional reduce;
            try {
                reduce = ((Stream) preValue).reduce(accumulator);
            } catch (Exception e) {
                linkLog.append("reduce? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (!reduce.isPresent()) {
                return NullBuild.empty();
            }
            linkLog.append("reduce->");
            return NullBuild.noEmpty((T) reduce.get());
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<Long> count() {
        this.taskList.add((preValue)->{
            long stream;
            try {
                stream = ((Stream) preValue).count();
            } catch (Exception e) {
                linkLog.append("count? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("count->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> min(Comparator<? super T> comparator) {
        this.taskList.add((preValue)->{
            if (comparator == null) {
                throw new NullChainException(linkLog.append("min? ").append("comparator must not be null").toString());
            }
            Optional<T> min;
            try {
                min = ((Stream) preValue).min(comparator);
            } catch (Exception e) {
                linkLog.append("min? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (!min.isPresent()) {
                return NullBuild.empty();
            }
            linkLog.append("min->");
            return NullBuild.noEmpty(min.get());
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<Boolean> allMatch(Predicate<? super T> predicate) {
        this.taskList.add((preValue)->{
            if (predicate == null) {
                throw new NullChainException(linkLog.append("allMatch? ").append("predicate must not be null").toString());
            }
            boolean stream;
            try {
                stream = ((Stream) preValue).allMatch(predicate);
            } catch (Exception e) {
                linkLog.append("allMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("allMatch->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<Boolean> anyMatch(Predicate<? super T> predicate) {
        this.taskList.add((preValue)->{
            if (predicate == null) {
                throw new NullChainException(linkLog.append("anyMatch? ").append("predicate must not be null").toString());
            }
            boolean stream;
            try {
                stream = ((Stream) preValue).anyMatch(predicate);
            } catch (Exception e) {
                linkLog.append("anyMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("anyMatch->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<Boolean> noneMatch(Predicate<? super T> predicate) {
        this.taskList.add((preValue)->{
            if (predicate == null) {
                throw new NullChainException(linkLog.append("noneMatch? ").append("predicate must not be null").toString());
            }
            boolean stream;
            try {
                stream = ((Stream) preValue).noneMatch(predicate);
            } catch (Exception e) {
                linkLog.append("noneMatch? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("noneMatch->");
            return NullBuild.noEmpty(stream);
        });
        return  NullBuild.busy(this);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        taskList.runTaskAll((nullChainBase) -> {
            if (action == null) {
                linkLog.append("forEach? ").append("action must not be null");
                throw new NullChainException(linkLog.toString());
            }
            try {
                ((Stream) nullChainBase.value).forEach(action);
            } catch (Exception e) {
                linkLog.append("forEach? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        },null);
    }
}