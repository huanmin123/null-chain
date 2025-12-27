package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.*;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NullIntStreamBase  extends NullKernelAsyncAbstract<Integer> implements NullIntStream {
    public NullIntStreamBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }

    @Override
    public int sum() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.sum();
    }

    @Override
    public int min() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.min().orElse(0);
    }

    @Override
    public int max() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.max().orElse(0);
    }

    @Override
    public long count() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.count();
    }

    @Override
    public double average() {
        NullTaskList.NullNode<Object> objectNullNode = taskList.runTaskAll();
        if (objectNullNode.isNull) {
            return 0.0;
        }
        IntStream value = (IntStream) objectNullNode.value;
        return value.average().orElse(0.0);
    }

    @Override
    public NullIntStream map(IntUnaryOperator mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_INT_MAP_Q).append("mapper must not be null").toString());
            }
            IntStream stream;
            try {
                stream = ((IntStream) value).map(mapper);
            } catch (Exception e) {
                linkLog.append(STREAM_INT_MAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_INT_MAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyIntStream(this);
    }

    @Override
    public NullIntStream flatMap(IntFunction<? extends NullIntStream> mapper) {
        this.taskList.add((value) -> {
            if (mapper == null) {
                throw new NullChainException(linkLog.append(STREAM_INT_FLATMAP_Q).append("mapper must not be null").toString());
            }
            IntStream stream;
            try {
                stream = ((IntStream) value).flatMap((data) -> {
                    NullIntStream nullIntStream = mapper.apply(data);
                    if (nullIntStream == null) {
                        return IntStream.empty();
                    }
                    // 将 NullIntStream 转换为 NullIntStreamBase 以访问 taskList
                    if (nullIntStream instanceof NullIntStreamBase) {
                        NullIntStreamBase base = (NullIntStreamBase) nullIntStream;
                        NullTaskList.NullNode<Object> node = base.taskList.runTaskAll();
                        if (node.isNull) {
                            return IntStream.empty();
                        }
                        return (IntStream) node.value;
                    }
                    return IntStream.empty();
                });
            } catch (Exception e) {
                linkLog.append(STREAM_INT_FLATMAP_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(STREAM_INT_FLATMAP_ARROW);
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyIntStream(this);
    }

    @Override
    public NullStream<Integer> boxed() {
        this.taskList.add((value) -> {
            Stream<Integer> stream;
            try {
                stream = ((IntStream) value).boxed();
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
