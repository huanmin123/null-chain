package com.gitee.huanminabc.nullchain.leaf.check;

import com.gitee.huanminabc.jcommon.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Null多级判空工具基础实现类
 * 
 * <p>该实现类提供了多级判空功能，会全部判定一遍所有节点，收集所有为空的节点信息，然后统一处理。</p>
 * 
 * @param <T> 检查对象的类型
 * @author huanmin
 * @since 1.1.2
 * @version 1.1.2
 */
@Slf4j
public class NullCheckBase<T> extends NullKernelAsyncAbstract<T> implements NullCheck<T> {

    /**
     * 存储所有节点的检查结果
     * 注意：这个列表在任务执行时会被填充
     */
    private final List<NullCheckNode> checkNodes;

    /**
     * 构造函数
     * 
     * @param linkLog 链路日志
     * @param taskList 任务列表
     */
    public NullCheckBase(StringBuilder linkLog, NullTaskList taskList,  List<NullCheckNode> list) {
        super(linkLog, taskList);
        this.checkNodes= list;
    }

    /**
     * 检查节点信息
     */
    @Data
    public static class NullCheckNode {
        /** 节点路径，如 "user.id" */
        private String path;
        /** 是否为空 */
        private boolean isNull;

        public NullCheckNode(String path, boolean isNull) {
            this.path = path;
            this.isNull = isNull;
        }
        public NullCheckNode() {

        }

    }

    /**
     * 添加字段检查任务（isNull 和 map 的公共逻辑）
     * 
     * @param <U> 字段类型
     * @param function 字段访问函数
     * @param paramNullMessage 参数为null时的错误消息
     * @param logQ 空值时的日志标记
     * @param logArrow 非空值时的日志标记
     * @param returnMappedValue 是否返回映射后的值（true为map，false为isNull）
     */
    private <U> void addFieldCheckTask(
            NullFun<? super T, ? extends U> function,
            String paramNullMessage,
            String logQ,
            String logArrow,
            boolean returnMappedValue) {
        if (function == null) {
            throw new NullChainException(linkLog.append(paramNullMessage).toString());
        }

        // 获取字段名
        String fieldName;
        try {
            fieldName = LambdaUtil.getFieldName(function);
        } catch (Exception e) {
            // 如果无法获取字段名，使用默认名称
            fieldName = "field" + checkNodes.size();
            log.debug("无法从NullFun中提取字段名，使用默认名称: {}", fieldName, e);
        }

        final String finalFieldName = fieldName;

        // 执行检查并记录结果
        // 创建一个自定义的 NullTaskFunAbs，重写 preNullEnd() 返回 false，确保即使值为 null 也继续执行
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            @SuppressWarnings("unchecked")
            public NullTaskList.NullNode<Object> nodeTask(Object value) throws RuntimeException {
                try {
                    // 如果当前值为 null，直接标记该节点为空，不执行 function.apply
                    boolean isCurrentValueNull = Null.is(value);
                    U result = null;
                    if (!isCurrentValueNull) {
                        T typedValue = (T) value;
                        result = function.apply(typedValue);
                    }
                    boolean isNull = isCurrentValueNull || Null.is(result);

                    // 记录检查结果
                    checkNodes.add(new NullCheckNode(finalFieldName, isNull));

                    // 正常记录链路日志（按照标准方式）
                    if (isNull) {
                        linkLog.append(logQ).append(finalFieldName);
                    } else {
                        linkLog.append(logArrow).append(finalFieldName);
                    }

                    // 根据操作类型返回不同的值
                    if (returnMappedValue) {
                        // map 操作：返回映射后的值
                        if (isNull) {
                            // 即使映射后的值为 null，也返回一个空节点，但链继续执行
                            return NullBuild.empty();
                        }
                        return NullBuild.noEmpty(result);
                    } else {
                        // isNull 操作：返回原值
                        return NullBuild.noEmpty(value);
                    }
                } catch (Exception e) {
                    linkLog.append(logQ);
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }

            @Override
            public boolean preNullEnd() {
                // 对于 NullCheck，即使值为 null 也要继续执行所有检查
                return false;
            }
        });
    }

    @Override
    public <U> NullCheck<T> of(NullFun<? super T, ? extends U> function) {
        addFieldCheckTask(function, CHECK_ISNULL_PARAM_NULL, CHECK_ISNULL_Q, CHECK_ISNULL_ARROW, false);
        return NullBuild.busyCheck(this);
    }

    @Override
    public <U> NullCheck<U> map(NullFun<? super T, ? extends U> function) {
        addFieldCheckTask(function, CHECK_MAP_PARAM_NULL, CHECK_MAP_Q, CHECK_MAP_ARROW, true);
        // 返回新的 NullCheck<U>，共享同一个 checkNodes 列表和 linkLog
        return NullBuild.busyCheck(this);
    }




    /**
     * 执行所有任务并收集检查结果
     * 如果执行过程中出现异常，直接抛出
     */
    private void runTasksAndCollectResults() {
        try {
            taskList.runTaskAll();
        } catch (Exception e) {
            // 如果执行过程中出现异常，直接抛出
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    /**
     * 构建异常消息并抛出异常（如果有空节点）
     * 通过 NullCheckNode 构建异常消息：字段名->字段名?->字段名
     * 
     * @param exceptionClass 异常类
     * @param prefixMessage 前缀消息，如果为null则不添加前缀
     */
    private void buildAndThrowException(Class<? extends RuntimeException> exceptionClass, String prefixMessage) {
        if (checkNodes.isEmpty()) {
            return;
        }

        // 检查是否有空节点
        boolean hasNull = false;
        for (NullCheckNode node : checkNodes) {
            if (node.isNull) {
                hasNull = true;
                break;
            }
        }

        if (!hasNull) {
            return;
        }

        // 构建异常消息
        StringBuilder exceptionMessage = new StringBuilder();
        // 如果有前缀消息，先添加前缀
        if (prefixMessage != null) {
            exceptionMessage.append(prefixMessage).append(": ");
        }

        // 遍历所有节点，构建异常消息
        for (NullCheckNode node : checkNodes) {
            if (node.isNull) {
                exceptionMessage.append(node.path).append("?");
            } else {
                exceptionMessage.append(node.path).append("->");
            }
        }

        // 抛出异常，使用构建的异常消息
        throw NullReflectionKit.addRunErrorMessage(
                exceptionClass!=null ? exceptionClass : NullChainException.class,
                Thread.currentThread().getStackTrace(),
                exceptionMessage
        );
    }

    @Override
    public void doThrow() {
        // 执行所有任务，收集检查结果
        runTasksAndCollectResults();
        // 构建异常消息并抛出异常
        buildAndThrowException(null, null);
    }
    @Override
    public void doThrow(String prefixMessage) {
        if (prefixMessage == null) {
            throw new NullChainException(linkLog.append(CHECK_DOTHROW_PARAM_NULL).toString());
        }
        // 执行所有任务，收集检查结果
        runTasksAndCollectResults();
        // 构建异常消息并抛出异常
        buildAndThrowException(null, prefixMessage);
    }
    @Override
    public void doThrow(Class<? extends RuntimeException> exceptionClass) {
        if (exceptionClass == null) {
            throw new NullChainException(linkLog.append(CHECK_DOTHROW_PARAM_NULL).toString());
        }

        // 执行所有任务，收集检查结果
        runTasksAndCollectResults();

        // 构建异常消息并抛出异常
        buildAndThrowException(exceptionClass, null);
    }

    @Override
    public void doThrow(Class<? extends RuntimeException> exceptionClass, String prefixMessage) {
        if (exceptionClass == null) {
            throw new NullChainException(linkLog.append(CHECK_DOTHROW_PARAM_NULL).toString());
        }
        if (prefixMessage == null) {
            throw new NullChainException(linkLog.append(CHECK_DOTHROW_PARAM_NULL).toString());
        }

        // 执行所有任务，收集检查结果
        runTasksAndCollectResults();

        // 构建异常消息（包含前缀）并抛出异常
        buildAndThrowException(exceptionClass, prefixMessage);
    }

    @Override
    public boolean is() {
        // 执行所有任务，收集检查结果
        try {
            taskList.runTaskAll();
        } catch (Exception e) {
            // 如果执行过程中出现异常，直接抛出
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }

        // 检查是否有任何空节点
        for (NullCheckNode node : checkNodes) {
            if (node.isNull) {
                return true;
            }
        }
        return false;
    }
}

