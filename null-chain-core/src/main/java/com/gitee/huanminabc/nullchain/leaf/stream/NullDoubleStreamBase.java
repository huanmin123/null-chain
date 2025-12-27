package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.*;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class NullDoubleStreamBase extends NullKernelAsyncAbstract<Double> implements NullDoubleStream {
    public NullDoubleStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public double sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public double min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.min().orElse(0.0);
    }

    @Override
    public double max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.max().orElse(0.0);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0L;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        DoubleStream value = (DoubleStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }

    @Override
    public NullDoubleStream map(DoubleUnaryOperator mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_DOUBLE_MAP_Q).append("mapper must not be null").toString());
            }
            DoubleStream stream;
            try {
                stream = ((DoubleStream) value).map(mapper);
            } catch (Exception e) {
                linkLog.append(STREAM_DOUBLE_MAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_DOUBLE_MAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyDoubleStream(this);
    }

    @Override
    public NullDoubleStream flatMap(DoubleFunction<? extends NullDoubleStream> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_DOUBLE_FLATMAP_Q).append("mapper must not be null").toString());
            }
            DoubleStream stream;
            try {
                stream = ((DoubleStream) value).flatMap((data) -> {
                    NullDoubleStream nullDoubleStream = mapper.apply(data);
                    if (nullDoubleStream == null) {
                        return DoubleStream.empty();
                    }
                    // 将 NullDoubleStream 转换为 NullDoubleStreamBase 以访问 taskList
                    if (nullDoubleStream instanceof NullDoubleStreamBase) {
                        NullDoubleStreamBase base = (NullDoubleStreamBase) nullDoubleStream;
                        NullTaskList.NullNode<Object> node = base.taskList.runTaskAll();
                        if (node.isNull) {
                            return DoubleStream.empty();
                        }
                        return (DoubleStream) node.value;
                    }
                    return DoubleStream.empty();
                });
            } catch (Exception e) {
                linkLog.append(STREAM_DOUBLE_FLATMAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_DOUBLE_FLATMAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyDoubleStream(this);
    }

    @Override
    public NullStream<Double> boxed() {
        this.taskList.add((value) -> {
            Stream<Double> stream;
            try {
                stream = ((DoubleStream) value).boxed();
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
