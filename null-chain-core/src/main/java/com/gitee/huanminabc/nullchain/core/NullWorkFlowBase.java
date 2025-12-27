package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.jcommon.str.StringUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Null工作流基础实现类
 * 
 * @param <T> 工作流处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
@Slf4j
public class NullWorkFlowBase<T> extends NullFinalityBase<T> implements NullWorkFlow<T> {

    public NullWorkFlowBase(StringBuilder linkLog, NullTaskList taskList) {
        super( linkLog, taskList);
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool) {
        return tool(tool, NullConstants.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params) {
        this.taskList.add((value) -> {
            NullTool<T, R> tool1 = NullToolFactory.getTool(tool);
            //如果不存在注册器
            if (tool1 == null) {
                throw new NullChainException(linkLog.append(TOOL_Q).append(tool.getName()).append(" 不存在的转换器").toString());
            }
            try {
                R run = toolRun((T) value, tool1, linkLog, params);
                if (Null.is(run)) {
                    linkLog.append(TOOL_Q);
                    return NullBuild.empty();
                }
                linkLog.append(TOOL_ARROW);
                return NullBuild.noEmpty(run);
            } catch (NullChainCheckException e) {
                throw new NullChainException(e);
            } catch (Exception e) {
                linkLog.append(TOOL_Q).append(tool1.getClass().getName()).append(" 失败: ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }


    @Override
    public <R> NullChain<R> task(Class<? extends NullTask<T, R>> task, Object... params) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (task == null) {
                    throw new NullChainException(linkLog.append(TASK_Q).append("传参不能为空").toString());
                }
                Object o = __task__(preValue,task.getName(), params);
                return NullBuild.noEmpty((R) o);
            }
        });
        return NullBuild.busy(this);

    }


    @Override
    public NullChain<?> task(String classPath, Object... params) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (StringUtil.isEmpty(classPath)) {
                    throw new NullChainException(linkLog.append(TASK_Q).append("传参不能为空").toString());
                }
                Object o = __task__(preValue,classPath, params);
                return NullBuild.noEmpty(o);
            }
        });
        return NullBuild.busy(this);

    }


    @Override
    public NullChain<Map<String, Object>> task(NullGroupTask nullGroupTask) {

        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupTask, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME)) {
                    throw new NullChainException(linkLog.append(TASK_Q).append("传参不能为空").toString());
                }
                return __task__(preValue,nullGroupTask, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME);
            }
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<Map<String, Object>> task(NullGroupTask nullGroupTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupTask, threadFactoryName)) {
                    throw new NullChainException(linkLog.append(TASK_Q).append("传参不能为空").toString());
                }
                return __task__(preValue,nullGroupTask, threadFactoryName);
            }
        });
        return NullBuild.busy(this);

    }

    @Override
    public NullChain<?> nfTask(String nfContext, Object... params) {
        NullGroupNfTask.NullTaskInfo nullTaskInfo = NullGroupNfTask.task(nfContext, params);
        return nfTask(nullTaskInfo);
    }

    @Override
    public NullChain<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo) {
        return nfTask(nullTaskInfo, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME);
    }


    @Override
    public NullChain<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode  nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullTaskInfo, threadFactoryName)) {
                    linkLog.append(NFTASK_Q).append("传参不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                //脚本内容不能是空
                if (Null.is(nullTaskInfo.getNfContext())) {
                    linkLog.append(NFTASK_Q).append("脚本内容不能为空");
                    throw new NullChainException(linkLog.toString());
                }

                try {
                    Object o = __nfTask__(preValue,nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
                    if (Null.is(o)) {
                        linkLog.append(NFTASK_Q);
                        return NullBuild.empty();
                    }
                    linkLog.append(NFTASK_ARROW);
                    return NullBuild.noEmpty(o);
                } catch (Exception e) {
                    linkLog.append(NFTASK_Q);
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
        });
        return NullBuild.busy(this);

    }


    @Override
    public NullChain<Map<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode  nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupNfTask, threadFactoryName)) {
                    linkLog.append(NFTASKS_Q).append("传参不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
                NullGroupNfTask.NullTaskInfo[] list = nullGroupNfTask.getList();
                //脚本内容不能是空
                for (NullGroupNfTask.NullTaskInfo nullTaskInfo : list) {
                    if (Null.is(nullTaskInfo.getNfContext())) {
                        linkLog.append(NFTASKS_Q).append("脚本内容不能为空");
                        throw new NullChainException(linkLog.toString());
                    }
                }
                NullChainException nullChainException = new NullChainException();
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                List<Future<?>> futures = new ArrayList<>();
                Map<String, Object> nullChainMap = new ConcurrentHashMap<>();
                for (NullGroupNfTask.NullTaskInfo nullTaskInfo : list) {
                    Future<?> submit = executor.submit(() -> {
                        try {
                            Object run = __nfTask__(preValue,nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
                            if (Null.is(run)) {
                                return;
                            }
                            nullChainMap.put(nullTaskInfo.getKey(), run);
                        } catch (Exception e) {
                            nullChainException.setMessage(stackTrace, linkLog.toString());
                            e.addSuppressed(nullChainException);
                            log.error("{}{}{}多任务脚本并发执行失败", linkLog, TASK_Q, nullTaskInfo.getKey(), e);
                        }
                    });
                    futures.add(submit);
                }
                //等待所有任务执行完毕
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception ignored) {

                    }
                }
                linkLog.append(TASK_ARROW);
                return NullBuild.noEmpty(nullChainMap);
            }
        });
        return NullBuild.busy(this);
    }


    private NullTaskList.NullNode <Map<String, Object>> __task__(Object preValue,NullGroupTask nullGroupTask, String threadFactoryName) {

        ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
        Map<String, Object> nullChainMap = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();
        NullGroupTask.NullTaskInfo[] list = nullGroupTask.getList();
        for (NullGroupTask.NullTaskInfo nullTaskInfo : list) {
            if (StringUtil.isEmpty(nullTaskInfo.getTaskName())) {
                linkLog.append(TASK_Q).append("任务名不能为空");
                throw new NullChainException(linkLog.toString());
            }
        }
        NullChainException nullChainException = new NullChainException();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (NullGroupTask.NullTaskInfo nullTaskInfo : list) {
            String taskName = nullTaskInfo.getTaskName();
            Object[] params = nullTaskInfo.getParams();
            NullTask nullTask = getNullTask(taskName);
            Future<?> submit = executor.submit(() -> {
                try {
                    Object run = taskRun((T)preValue, nullTask, linkLog, params);
                    if (Null.is(run)) {
                        return;
                    }
                    nullChainMap.put(taskName, run);
                }catch (Exception e) {
                    nullChainException.setMessage(stackTrace, "{}{}", linkLog, TASK_Q);
                    e.addSuppressed(nullChainException);
                    log.error("{}{}多任务并发执行失败:{}", linkLog, TASK_Q, taskName, e);
                }
            });
            futures.add(submit);
        }
        //等待所有任务执行完毕
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception ignored) {

            }
        }
        linkLog.append(TASK_ARROW);
        return NullBuild.noEmpty(nullChainMap);
    }


    private Object __task__(Object preValue,String classPath, Object... params) {
        NullTask nullTask = getNullTask(classPath);
        try {
            Object run = taskRun((T)preValue, nullTask, linkLog, params);
            if (Null.is(run)) {
                linkLog.append(TASK_Q);
                return NullBuild.empty();
            }
            linkLog.append(TASK_ARROW);
            return run;
        }  catch (Exception e) {
            linkLog.append(TASK_Q).append(TASK_IN);
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    private NullTask getNullTask(String taskName) {
        NullTask nullTask = NullTaskFactory.getTask(taskName);
        if (nullTask == null) {
            //如果没有找到那么尝试从系统的classLoader中加载
            try {
                Class<?> aClass1 = Class.forName(taskName);
                //判断是否是NULLTask的子类
                if (!NullTask.class.isAssignableFrom(aClass1)) {
                    throw new NullChainException(linkLog.append(TASK_Q).append(taskName).append(" 不是NullTask的子类").toString());
                }
                //注入到任务工厂
                NullTaskFactory.registerTask((Class<? extends NullTask>) aClass1);
                nullTask = NullTaskFactory.getTask(taskName);
            } catch (ClassNotFoundException e) {
                throw new NullChainException(linkLog.append(TASK_Q).append(taskName).append(" 任务不存在").toString());
            }
        }
        return nullTask;
    }


    private Object __nfTask__(Object preValue,String nfContext, String threadFactoryName, Logger logger, Object[] params) {
        //校验线程池是否存在
        ThreadFactoryUtil.addExecutor(threadFactoryName);
        Map<String, Object> mainSystemContext = new HashMap<>();
        mainSystemContext.put("threadFactoryName", threadFactoryName);
        mainSystemContext.put("preValue", preValue);//上一个任务的值
        mainSystemContext.put("params", params == null ? NullConstants.EMPTY_OBJECT_ARRAY : params);
        return NfMain.run(nfContext, logger, mainSystemContext);
    }


    private  < R> R taskRun(T value, NullTask<T, R> nullTask, StringBuilder linkLog, Object... params) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Object[] objects = params == null ? NullConstants.EMPTY_OBJECT_ARRAY : params;
        //校验参数类型和长度
        NullType nullType = nullTask.checkTypeParams();
        try {
            if (nullType != null) {
                nullType.checkType(objects, map);
            }
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append(TASK_Q).append(nullTask.getClass().getName()).append(TASK_PARAM_VALIDATION_FAILED).toString());
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(objects);
        nullTask.init(value, nullChains, map);
        return nullTask.run(value, nullChains, map);
    }

    private  <R> R toolRun(T value, NullTool<T, R> nullTool, StringBuilder linkLog, Object... params) throws NullChainCheckException {
        Map<String, Object> map = new HashMap<>();
        Object[] objects = params == null ? NullConstants.EMPTY_OBJECT_ARRAY : params;
        //校验参数类型和长度
        NullType nullType = nullTool.checkTypeParams();
        try {
            if (nullType != null) {
                nullType.checkType(objects, map);
            }
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append(TOOL_Q).append(nullTool.getClass().getName()).append(TOOL_PARAM_VALIDATION_FAILED).toString());
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(objects);
        try {
            nullTool.init(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append(TOOL_Q).append(nullTool.getClass().getName()).append(TOOL_INIT_FAILED).toString());
        }
        try {
            return nullTool.run(value, nullChains, map);
        } catch (Exception e) {
            throw new NullChainCheckException(e, linkLog.append(TOOL_Q).append(nullTool.getClass().getName()).append(TOOL_RUN_FAILED).toString());
        }

    }

}
