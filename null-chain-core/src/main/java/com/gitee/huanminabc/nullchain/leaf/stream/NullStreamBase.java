package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.core.NullChain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.*;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-02-21 17:49
 **/
public class NullStreamBase<T> extends NullKernelAbstract<T> implements NullStream<T> {
    public NullStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public NullStream<T> parallel() {
        this.taskList.add((value) -> {
            if (!(value instanceof Stream)) {
                throw new NullChainException(linkLog.append("parallel? ").append("value must be a Stream").toString());
            }
            T parallel = (T) ((Stream) value).parallel();
            return NullBuild.noEmpty(parallel);
        });
        return NullBuild.busyStream(this);

    }

    @Override
    public <R> NullStream<R> map(Function<? super T, ? extends R> mapper) {
        this.taskList.add((value) -> {

            if (mapper == null) {
                throw new NullChainException(linkLog.append("map? ").append("mapper must not be null").toString());
            }
            R stream;
            try {
                stream = (R) ((Stream) value).filter((data) -> {
                    //对mapper进行加强,如果返回的是空那么就丢弃
                    R apply = mapper.apply((T) data);
                    return apply != null;
                }).map(mapper);
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("map->");
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyStream(this);
    }


    @Override
    public NullIntStream mapToInt(Function<? super T, ? extends Integer> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append("mapToInt? ").append("mapper must not be null").toString());
            }
            IntStream stream;
            try {
                //对mapper进行加强,如果返回的是空那么默认0
                stream = ((Stream) value).mapToInt((data) -> {
                    Integer apply = mapper.apply((T) data);
                    return apply == null ? 0 : apply;
                });
            } catch (Exception e) {
                linkLog.append("mapToInt? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("mapToInt->");
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyIntStream(this);
    }

    @Override
    public NullLongStream mapToLong(Function<? super T, ? extends Long> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append("mapToLong? ").append("mapper must not be null").toString());
            }
            LongStream stream;
            try {
                //对mapper进行加强,如果返回的是空那么默认0L
                stream = ((Stream) value).mapToLong((data) -> {
                    Long apply = mapper.apply((T) data);
                    return apply == null ? 0L : apply;
                });
            } catch (Exception e) {
                linkLog.append("mapToLong? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("mapToLong->");
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyLongStream(this);
    }

    @Override
    public NullDoubleStream mapToDouble(Function<? super T, ? extends Double> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append("mapToDouble? ").append("mapper must not be null").toString());
            }
            DoubleStream stream;
            try {
                //对mapper进行加强,如果返回的是空那么默认0.0
                stream = ((Stream) value).mapToDouble((data) -> {
                    Double apply = mapper.apply((T) data);
                    return apply == null ? 0.0 : apply;
                });
            } catch (Exception e) {
                linkLog.append("mapToDouble? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("mapToDouble->");
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyDoubleStream(this);
    }


    @Override
    public NullStream<T> filter(Predicate<? super T> predicate) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> sorted() {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> sorted(Comparator<? super T> comparator) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);

    }

    @Override
    public NullStream<T> distinct() {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> limit(long maxSize) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> skip(long n) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> then(Consumer<? super T> action) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public NullStream<T> then(NullConsumer2<NullChain<T>, ? super T> function) {
        this.taskList.add((value) -> {
            if (function == null) {
                throw new NullChainException(linkLog.append("then? ").append("action must not be null").toString());
            }
            T stream;
            try {
                stream = (T) ((Stream) value).peek((data) -> {
                    function.accept((NullChain) Null.of(data), (T) data);
                });
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("then->");
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyStream(this);
    }

    @Override
    public <R> NullStream<R> flatMap(java.util.function.Function<? super T, ? extends NullStream<? extends R>> mapper) {
        this.taskList.add((value) -> {
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
        return NullBuild.busyStream(this);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        if (collector == null) {
            throw new NullChainException(linkLog.append("collect? ").append("collector must not be null").toString());
        }
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return (R) collector.supplier().get();
        }
        Stream<T> preValue = (Stream<T>) objectNullNode.value;
        R result;
        try {
            result = preValue.collect(collector);
        } catch (Exception e) {
            linkLog.append("collect? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        return result;
    }

    @Override
    public NullChain<T> max(Comparator<? super T> comparator) {
        this.taskList.add((preValue) -> {
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
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> findFirst() {
        this.taskList.add((preValue) -> {
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
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> findAny() {
        this.taskList.add((preValue) -> {
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
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> reduce(BinaryOperator<T> accumulator) {
        this.taskList.add((preValue) -> {
            if (accumulator == null) {
                throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
            }
            Optional reduce;
            try {
                reduce = ((Stream) preValue).reduce((a, b) -> {
                    if (b == null) {
                        //判断a是什么类型的然后给默认值
                        if (a instanceof BigDecimal) {
                            return (T) BigDecimal.ZERO;
                        } else if (a instanceof Integer) {
                            return (T) Integer.valueOf(0);
                        } else if (a instanceof Long) {
                            return (T) Long.valueOf(0);
                        } else if (a instanceof Double) {
                            return (T) Double.valueOf(0.0);
                        } else if (a instanceof Float) {
                            return (T) Float.valueOf(0.0f);
                        } else if (a instanceof Short) {
                            return (T) Short.valueOf((short) 0);
                        } else if (a instanceof Byte) {
                            return (T) Byte.valueOf((byte) 0);
                        } else if (a instanceof Character) {
                            return (T) Character.valueOf((char) 0);
                        } else if (a instanceof String) {
                            return "";
                        } else {
                            //如果是其他类型那么就返回null
                            return null;
                        }

                    }
                    return accumulator.apply((T) a, (T) b);
                });
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
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> reduce(T identity, BinaryOperator<T> accumulator) {
        this.taskList.add((preValue) -> {
            if (identity == null) {
                throw new NullChainException(linkLog.append("reduce? ").append("identity must not be null").toString());
            }
            if (accumulator == null) {
                throw new NullChainException(linkLog.append("reduce? ").append("accumulator must not be null").toString());
            }
            T reduce;
            try {
                reduce = (T) ((Stream) preValue).reduce(identity, (a, b) -> {
                    ;
                    if (b == null) {
                        return identity;
                    }
                    return accumulator.apply((T) a, (T) b);
                });
            } catch (Exception e) {
                linkLog.append("reduce? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("reduce->");
            return NullBuild.noEmpty(reduce);
        });
        return NullBuild.busy(this);
    }

    @Override
    public Long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        Stream<T> preValue = (Stream<T>) objectNullNode.value;
        return preValue.count();

    }

    @Override
    public NullChain<T> min(Comparator<? super T> comparator) {
        this.taskList.add((preValue) -> {
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
        return NullBuild.busy(this);
    }

    @Override
    public Boolean allMatch(Predicate<? super T> predicate) {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return false;
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("allMatch? ").append("predicate must not be null").toString());
        }
        boolean stream;
        try {
            stream = ((Stream) objectNullNode.value).allMatch(predicate);
        } catch (Exception e) {
            linkLog.append("allMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("allMatch->");
        return stream;
    }

    @Override
    public Boolean anyMatch(Predicate<? super T> predicate) {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return false;
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("anyMatch? ").append("predicate must not be null").toString());
        }
        boolean stream;
        try {
            stream = ((Stream) objectNullNode.value).anyMatch(predicate);
        } catch (Exception e) {
            linkLog.append("anyMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("anyMatch->");
        return stream;
    }

    @Override
    public Boolean noneMatch(Predicate<? super T> predicate) {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return true; //如果是空链那么默认返回true
        }
        if (predicate == null) {
            throw new NullChainException(linkLog.append("noneMatch? ").append("predicate must not be null").toString());
        }
        boolean stream;
        try {
            stream = ((Stream) objectNullNode.value).noneMatch(predicate);
        } catch (Exception e) {
            linkLog.append("noneMatch? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        linkLog.append("noneMatch->");
        return stream;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        taskList.runTaskAll((nullChainBase) -> {
            if (action == null) {
                linkLog.append("forEach? ").append("action must not be null");
                throw new NullChainException(linkLog.toString());
            }
            try {
                if (!nullChainBase.isNull) {
                    ((Stream) nullChainBase.value).forEach(action);
                }
            } catch (Exception e) {
                linkLog.append("forEach? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, null);
    }
}