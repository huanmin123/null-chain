package com.gitee.huanminabc.nullchain.base.async;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.utils.ReflectionKit;
import com.gitee.huanminabc.nullchain.utils.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@Slf4j
public class NullChainAsyncBase<T> extends NullConvertAsyncBase<T> implements NullChainAsync<T> {

    public NullChainAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullChainAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }


    @Override
    public <U> NullChainAsync<T> of(NullFun<? super T, ? extends U> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            try {
                if (Null.is(t)) {
                    return null;
                }
                if (function == null) {
                    linkLog.append("of? 方法参数不能是空");
                    return null;
                }
                U apply = function.apply(t);
                if (Null.is(apply)) {
                    linkLog.append("of?");
                    return null;
                }
                linkLog.append("of->");
                return t;
            } catch (Exception e) {
                linkLog.append("of? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @SafeVarargs
    @Override
    public final <U> NullChainAsync<T> ofAny(NullFun<? super T, ? extends U>... function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (Null.is(function)) {
                throw new NullChainException(linkLog.append("ofAny? 方法参数不能是空").toString());
            }
            try {
                for (int i = 0; i < function.length; i++) {
                    NullFun<? super T, ? extends U> nullFun = function[i];
                    U apply = nullFun.apply(t);
                    if (Null.is(apply)) {
                        linkLog.append("ofAny? 第").append(i + 1).append("个");
                        return null;
                    }
                }
            } catch (Exception e) {
                linkLog.append("ofAny? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("ofAny->");
            return t;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> then(Runnable function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 方法参数不能是空").toString());
            }
            try {
                function.run();
                linkLog.append("then->");
                return t;
            } catch (Exception e) {
                linkLog.append("then? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> then(Consumer<? super T> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 方法参数不能是空").toString());
            }
            try {
                function.accept(t);
                linkLog.append("then->");
                return t;
            } catch (Exception e) {
                linkLog.append("then? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> then2(NullConsumer2<NullChain<T>, ? super T> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 方法参数不能是空").toString());
            }
            try {
                function.accept(Null.of(t), t);
                linkLog.append("then->");
                return t;
            } catch (Exception e) {
                linkLog.append("then? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @Override
    public <U> NullChainAsync<U> map(NullFun<? super T, ? extends U> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("map? 方法参数不能是空").toString());
            }
            try {
                U invoke = function.apply(t);
                if (Null.is(invoke)) {
                    linkLog.append("map?");
                    return null;
                }
                linkLog.append("map->");
                return invoke;
            } catch (Exception e) {
                linkLog.append("map? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public <U> NullChainAsync<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("map? 方法参数不能是空").toString());
            }
            try {
                U invoke = function.apply(Null.of(t), t);
                if (Null.is(invoke)) {
                    linkLog.append("map?");
                    return null;
                }
                linkLog.append("map->");
                return invoke;
            } catch (Exception e) {
                linkLog.append("map? ");
                throw new NullChainException(e, linkLog.toString());
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @Override
    public <U> NullChainAsync<U> unChain(NullFun<? super T, ? extends NullChain<U>> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("unChain? 方法参数不能是空").toString());
            }
            try {
                NullChain<U> invoke = function.apply(t);
                if (Null.is(invoke)) {
                    linkLog.append("unChain?");
                    return null;
                }
                linkLog.append("unChain->");
                return invoke.get();
            } catch (Exception e) {
                linkLog.append("unChain? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public <U> NullChainAsync<U> unOptional(NullFun<? super T, ? extends Optional<U>> function) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("unOptional? 方法参数不能是空").toString());
            }
            try {
                Optional<U> invoke = function.apply(t);
                if (!invoke.isPresent()) {
                    linkLog.append("unOptional?");
                    return null;
                }
                linkLog.append("unOptional->");
                return invoke.get();
            } catch (Exception e) {
                linkLog.append("unOptional? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @Override
    public NullChainAsync<T> or(Supplier<T> supplier) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                if (supplier == null) {
                    throw new NullChainException(linkLog.append("or? 方法参数不能是空").toString());
                }
                try {
                    T t1 = supplier.get();
                    if (Null.is(t1)) {
                        linkLog.append("or?");
                        return null;
                    }
                    linkLog.append("or->");
                    return t1;
                } catch (Exception e) {
                    linkLog.append("or? ");
                    throw ReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
            linkLog.append("or->");
            return t;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> or(T defaultValue) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            linkLog.append("or->");
            if (Null.is(t)) {
                if (Null.is(defaultValue)) {
                    throw new NullChainException(linkLog.append("or? 方法参数不能是空").toString());
                }
                return defaultValue;
            }
            return t;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @Override
    public <R> NullChainAsync<R> task(Class<? extends NullTask<T, R>> task, Object... params) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        if (task == null) {
            completableFuture.completeExceptionally(new NullChainException(linkLog.append("task? 方法参数不能是空").toString()));
            return (NullChainAsync) NullBuild.noEmptyAsync(completableFuture, linkLog, super.currentThreadFactoryName, collect);
        }
        return __task__(task.getName(), params);
    }


    @Override
    public NullChainAsync<?> task(String classPath, Object... params) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        if (Null.is(classPath)) {
            completableFuture.completeExceptionally(new NullChainException(linkLog.append("task? 方法参数不能是空").toString()));
            return NullBuild.noEmptyAsync(completableFuture, linkLog, super.currentThreadFactoryName, collect);
        }
        return __task__(classPath, params);
    }

    @Override
    public NullChainAsync<NullMap<String, Object>> task(NullGroupTask nullGroupTask) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        return task(nullGroupTask, super.currentThreadFactoryName);
    }

    @Override
    public NullChainAsync<NullMap<String, Object>> task(NullGroupTask nullGroupTask, String threadFactoryName) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<NullMap<String, Object>> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.isAny(nullGroupTask, threadFactoryName)) {
                throw new NullChainException(linkLog.append("task? 方法参数不能是空").toString());
            }
            //任务名不能是空
            NullGroupTask.NullTaskInfo[] list = nullGroupTask.getList();
            for (NullGroupTask.NullTaskInfo nullTaskInfo : list) {
                if (Null.is(nullTaskInfo.getTaskName())) {
                    throw new NullChainException(linkLog.append("task? 任务名不能是空").toString());
                }
            }
            return __task__(value, threadFactoryName, nullGroupTask);
        }, getCT());

        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);

    }

    @Override
    public NullChainAsync<?> nfTask(String nfContext, Object... params) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        if (Null.is(nfContext)) {
            completableFuture.completeExceptionally(new NullChainException(linkLog.append("nfTask? 脚本文件内容不能是空").toString()));
            return NullBuild.noEmptyAsync(completableFuture, linkLog, super.currentThreadFactoryName, collect);
        }
        NullGroupNfTask.NullTaskInfo nullTaskInfo = NullGroupNfTask.task(nfContext, params);
        return nfTask(nullTaskInfo);
    }

    @Override
    public NullChainAsync<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        return nfTask(nullTaskInfo, super.currentThreadFactoryName);
    }

    @Override
    public NullChainAsync<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo, String threadFactoryName) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<?> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.isAny(nullTaskInfo, threadFactoryName)) {
                throw new NullChainException(linkLog.append("nfTask? 方法参数不能是空").toString());
            }
            //脚步内容不能是空
            if (Null.is(nullTaskInfo.getNfContext())) {
                throw new NullChainException(linkLog.append("nfTask? 脚本文件内容不能是空").toString());
            }
            try {
                Object run = __nfTask__(value, nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
                if (Null.is(run)) {
                    linkLog.append("nfTask? ");
                    return null;
                }
                linkLog.append("nfTask->");
                return run;
            } catch (Exception e) {
                linkLog.append("nfTask? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<NullMap<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask, String threadFactoryName) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }

        CompletableFuture<NullMap<String, Object>> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.isAny(nullGroupNfTask, threadFactoryName)) {
                throw new NullChainException(linkLog.append("nfTasks? 方法参数不能是空").toString());
            }
            ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
            NullGroupNfTask.NullTaskInfo[] list = nullGroupNfTask.getList();
            for (NullGroupNfTask.NullTaskInfo nullTaskInfo : list) {
                if (Null.is(nullTaskInfo.getNfContext())) {
                    throw new NullChainException(linkLog.append("nfTasks? 脚本文件内容不能是空").toString());
                }
            }
            NullChainException nullChainException = new NullChainException();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            List<Future<?>> futures = new ArrayList<>();
            NullMap<String, Object> nullChainMap = NullMap.newConcurrentHashMap();
            for (NullGroupNfTask.NullTaskInfo nullTaskInfo : list) {
                Future<?> submit = executor.submit(() -> {
                    try {
                        Object run = __nfTask__(value, nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
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
            linkLog.append("nfTasks->");
            return nullChainMap;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    private NullMap<String, Object> __task__(Object value, String threadFactoryName, NullGroupTask nullGroupTask) {

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
                    Object run = NullBuild.taskRun(value, nullTask, linkLog, params);
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
        return nullChainMap;
    }


    private <R> NullChainAsync<R> __task__(String task, Object... params) {
        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.is(task)) {
                throw new NullChainException(linkLog.append("task? 任务名称不能是空").toString());
            }
            NullTask<T, R> nulltask = getNullTask(task);
            try {
                R run = NullBuild.taskRun(value, nulltask, linkLog, params);
                if (Null.is(run)) {
                    linkLog.append("task? ");
                    return null;
                }
                linkLog.append("task->");
                return run;
            } catch (NullChainCheckException e) {
                throw new NullChainException(e);
            } catch (Exception e) {
                linkLog.append("task? ");
                throw ReflectionKit.addRunErrorMessage(e, linkLog);
            }

        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
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

    private Object __nfTask__(Object value, String nfContext, String threadFactoryName, Logger logger, Object[] params) {
        //校验线程池是否存在
        ThreadFactoryUtil.addExecutor(threadFactoryName);
        NullMap<String, Object> mainSystemContext = NullMap.newHashMap();
        mainSystemContext.put("threadFactoryName", threadFactoryName);
        mainSystemContext.put("preValue", value);//上一个任务的值
        mainSystemContext.put("params", params == null ? new Object[]{} : params);
        return NfMain.run(nfContext, logger, mainSystemContext);
    }

}
