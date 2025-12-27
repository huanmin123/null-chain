package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.*;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NullLongStreamBase extends NullKernelAsyncAbstract<Long> implements NullLongStream {
    public NullLongStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public long sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public long min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.min().orElse(0L);
    }

    @Override
    public long max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.max().orElse(0L);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        LongStream value = (LongStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }

    @Override
    public NullLongStream map(LongUnaryOperator mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_LONG_MAP_Q).append("mapper must not be null").toString());
            }
            LongStream stream;
            try {
                stream = ((LongStream) value).map(mapper);
            } catch (Exception e) {
                linkLog.append(STREAM_LONG_MAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_LONG_MAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyLongStream(this);
    }

    @Override
    public NullLongStream flatMap(LongFunction<? extends NullLongStream> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_LONG_FLATMAP_Q).append("mapper must not be null").toString());
            }
            LongStream stream;
            try {
                stream = ((LongStream) value).flatMap((data) -> {
                    NullLongStream nullLongStream = mapper.apply(data);
                    if (nullLongStream == null) {
                        return LongStream.empty();
                    }
                    // 将 NullLongStream 转换为 NullLongStreamBase 以访问 taskList
                    if (nullLongStream instanceof NullLongStreamBase) {
                        NullLongStreamBase base = (NullLongStreamBase) nullLongStream;
                        NullTaskList.NullNode<Object> node = base.taskList.runTaskAll();
                        if (node.isNull) {
                            return LongStream.empty();
                        }
                        return (LongStream) node.value;
                    }
                    return LongStream.empty();
                });
            } catch (Exception e) {
                linkLog.append(STREAM_LONG_FLATMAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_LONG_FLATMAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyLongStream(this);
    }

    @Override
    public NullStream<Long> boxed() {
        this.taskList.add((value) -> {
            Stream<Long> stream;
            try {
                stream = ((LongStream) value).boxed();
            } catch (Exception e) {
                linkLog.append(STREAM_BOXED_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_BOXED_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyStream(linkLog, taskList);
    }
}
