package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class NullWorkFlowBase<T> extends NullFinalityBase<T> implements NullWorkFlow<T> {

    public NullWorkFlowBase(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super( linkLog, collect, taskList);
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool) {
        return tool(tool, new Object[]{});
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params) {
        this.taskList.add((value) -> {
            NullTool<T, R> tool1 = NullToolFactory.getTool(tool);
            //如果不存在注册器
            if (tool1 == null) {
                throw new NullChainException(linkLog.append("tool? ").append(tool.getName()).append(" 不存在的转换器").toString());
            }
            try {
                R run = NullBuild.toolRun((T) value, tool1, linkLog, params);
                if (Null.is(run)) {
                    linkLog.append("tool? ");
                    return NullBuild.empty();
                }
                linkLog.append("tool->");
                return NullBuild.noEmpty(run);
            } catch (NullChainCheckException e) {
                throw new NullChainException(e);
            } catch (Exception e) {
                linkLog.append("tool? ").append(tool1.getClass().getName()).append(" 失败: ");
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
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
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
                if (Null.is(classPath)) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
                }
                Object o = __task__(preValue,classPath, params);
                return NullBuild.noEmpty(o);
            }
        });
        return NullBuild.busy(this);

    }


    @Override
    public NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask) {

        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupTask, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME)) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
                }
                return __task__(preValue,nullGroupTask, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME);
            }
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupTask, threadFactoryName)) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
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
                    linkLog.append("nfTask? 传参不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                //脚本内容不能是空
                if (Null.is(nullTaskInfo.getNfContext())) {
                    linkLog.append("nfTask? 脚本内容不能为空");
                    throw new NullChainException(linkLog.toString());
                }

                try {
                    Object o = __nfTask__(preValue,nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
                    if (Null.is(o)) {
                        linkLog.append("nfTask? ");
                        return NullBuild.empty();
                    }
                    linkLog.append("nfTask->");
                    return NullBuild.noEmpty(o);
                } catch (Exception e) {
                    linkLog.append("nfTask? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
        });
        return NullBuild.busy(this);

    }


    @Override
    public NullChain<NullMap<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }

            @Override
            public NullTaskList.NullNode  nodeTask(Object preValue) throws RuntimeException {
                if (Null.isAny(nullGroupNfTask, threadFactoryName)) {
                    linkLog.append("nfTasks? 传参不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
                NullGroupNfTask.NullTaskInfo[] list = nullGroupNfTask.getList();
                //脚本内容不能是空
                for (NullGroupNfTask.NullTaskInfo nullTaskInfo : list) {
                    if (Null.is(nullTaskInfo.getNfContext())) {
                        linkLog.append("nfTasks? 脚本内容不能为空");
                        throw new NullChainException(linkLog.toString());
                    }
                }
                NullChainException nullChainException = new NullChainException();
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                List<Future<?>> futures = new ArrayList<>();
                NullMap<String, Object> nullChainMap = NullMap.newConcurrentHashMap();
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
                            log.error("{}task? {}多任务脚本并发执行失败", linkLog, nullTaskInfo.getKey(), e);
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
                linkLog.append("task->");
                return NullBuild.noEmpty(nullChainMap);
            }
        });
        return NullBuild.busy(this);
    }


    private NullTaskList.NullNode <NullMap<String, Object>> __task__(Object preValue,NullGroupTask nullGroupTask, String threadFactoryName) {

        ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
        NullMap<String, Object> nullChainMap = NullMap.newConcurrentHashMap();
        List<Future<?>> futures = new ArrayList<>();
        NullGroupTask.NullTaskInfo[] list = nullGroupTask.getList();
        for (NullGroupTask.NullTaskInfo nullTaskInfo : list) {
            if (Null.is(nullTaskInfo.getTaskName())) {
                linkLog.append("task? 任务名不能为空");
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
                    Object run = NullBuild.taskRun(preValue, nullTask, linkLog, params);
                    if (Null.is(run)) {
                        return;
                    }
                    nullChainMap.put(taskName, run);
                } catch (NullChainCheckException e) {
                    nullChainException.setMessage(stackTrace, linkLog.toString());
                    e.addSuppressed(nullChainException);
                    log.error("", e);
                } catch (Exception e) {
                    nullChainException.setMessage(stackTrace, linkLog.toString());
                    e.addSuppressed(nullChainException);
                    log.error("{}task? {}多任务并发执行失败", linkLog, taskName, e);
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
        linkLog.append("task->");
        return NullBuild.noEmpty(nullChainMap);
    }


    private Object __task__(Object preValue,String classPath, Object... params) {
        NullTask nullTask = getNullTask(classPath);
        try {
            Object run = NullBuild.taskRun(preValue, nullTask, linkLog, params);
            if (Null.is(run)) {
                linkLog.append("task? ");
                return NullBuild.empty();
            }
            linkLog.append("task->");
            return run;
        } catch (NullChainCheckException e) {
            throw new NullChainException(e);
        } catch (Exception e) {
            linkLog.append("task? ");
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
                    throw new NullChainException(linkLog.append("task? ").append(taskName).append(" 不是NullTask的子类").toString());
                }
                //注入到任务工厂
                NullTaskFactory.registerTask((Class<? extends NullTask>) aClass1);
                nullTask = NullTaskFactory.getTask(taskName);
            } catch (ClassNotFoundException e) {
                throw new NullChainException(linkLog.append("task? ").append(taskName).append(" 任务不存在").toString());
            }
        }
        return nullTask;
    }


    private Object __nfTask__(Object preValue,String nfContext, String threadFactoryName, Logger logger, Object[] params) {
        //校验线程池是否存在
        ThreadFactoryUtil.addExecutor(threadFactoryName);
        NullMap<String, Object> mainSystemContext = NullMap.newHashMap();
        mainSystemContext.put("threadFactoryName", threadFactoryName);
        mainSystemContext.put("preValue", preValue);//上一个任务的值
        mainSystemContext.put("params", params == null ? new Object[]{} : params);
        return NfMain.run(nfContext, logger, mainSystemContext);
    }

}
