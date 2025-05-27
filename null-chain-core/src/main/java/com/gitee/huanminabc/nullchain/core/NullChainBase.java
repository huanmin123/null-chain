package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
@Slf4j
public class NullChainBase<T> extends NullConvertBase<T> implements NullChain<T> {

    public NullChainBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect,taskList);
    }

    public NullChainBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect, taskList);
    }
    public NullChainBase( boolean isNull,T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect, taskList);
        this.isNull = isNull;
    }


    @Override
    public <U> NullChain<T> of(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("of? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append("of?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("of->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("of? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> ifGo(NullFun<? super T, Boolean> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("ifGo? 传参不能为空").toString());
            }
            try {
                Boolean apply = function.apply((T)value);
                if (!apply) {
                    linkLog.append("ifGo?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("ifGo->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("ifGo? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public <X extends RuntimeException> NullChain<T> check(Supplier<? extends X> exceptionSupplier) throws X {
        this.taskList.add((value)->{
            if (isNull) {
                if (exceptionSupplier == null) {
                    linkLog.append("check? 异常处理器不能为空");
                    throw new NullChainException(linkLog.toString());
                }
                X x;
                try {
                    x = exceptionSupplier.get();
                } catch (Exception e) {
                    linkLog.append("check? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
                throw NullReflectionKit.orRuntimeException(x, linkLog);
            }
            return NullBuild.noEmpty(value, linkLog, collect, taskList);
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("isNull? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.non(apply)) {
                    linkLog.append("isNull?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("isNull->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("isNull? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }


    @SafeVarargs
    @Override
    public final <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (Null.is(function)) {
                throw new NullChainException(linkLog.append("ofAny? 传参不能为空").toString());
            }
            try {
                for (int i = 0; i < function.length; i++) {
                    NullFun<? super T, ? extends U> nullFun = function[i];
                    U apply = nullFun.apply((T)value);
                    if (Null.is(apply)) {
                        linkLog.append("ofAny? 第").append(i + 1).append("个");
                        return NullBuild.empty(linkLog, collect, taskList);
                    }
                }
            } catch (Exception e) {
                linkLog.append("ofAny? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("ofAny->");
            return NullBuild.noEmpty(value, linkLog, collect, taskList);

        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> then(Runnable function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 传参不能为空").toString());
            }
            try {
                function.run();
                linkLog.append("then->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public NullChain<T> then(Consumer<? super T> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 传参不能为空").toString());
            }
            try {
                function.accept((T)value);
                linkLog.append("then->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("then? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function) {
        this.taskList.add((value)->{
            T valueT = (T) value;
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("then2? 传参不能为空").toString());
            }
            try {
                function.accept(Null.of(valueT), valueT);
                linkLog.append("then2->");
                return NullBuild.noEmpty(value, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("then2? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public <U> NullChain<U> map(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("map? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append("map?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("map->");
                return NullBuild.noEmpty(apply, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("map? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function) {
        this.taskList.add((value)->{
            T value1 = (T) value;
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("map2? 传参不能为空").toString());
            }
            try {
                U apply = function.apply(Null.of(value1), value1);
                if (Null.is(apply)) {
                    linkLog.append("map2?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("map2->");
                return NullBuild.noEmpty(apply, linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("map2? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("flatChain? 传参不能为空").toString());
            }
            try {
                NullChain<U> apply = function.apply((T)value);
                if (apply.is()) {
                    linkLog.append("flatChain?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("flatChain->");
                return NullBuild.noEmpty(apply.get(), linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("flatChain? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("flatOptional? 传参不能为空").toString());
            }
            try {
                Optional<U> apply = function.apply((T)value);
                if (!apply.isPresent()) {
                    linkLog.append("flatOptional?");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("flatOptional->");
                return NullBuild.noEmpty(apply.get(), linkLog, collect, taskList);
            } catch (Exception e) {
                linkLog.append("flatOptional? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public NullChain<T> or(Supplier<? extends T> supplier) {
        this.taskList.add((value)->{
            if (isNull) {
                if (supplier == null) {
                    throw new NullChainException(linkLog.append("or? 传参不能为空").toString());
                }
                try {
                    T t = supplier.get();
                    if (Null.is(t)) {
                        linkLog.append("or?");
                        return NullBuild.empty(linkLog, collect, taskList);
                    }
                    linkLog.append("or->");
                    return NullBuild.noEmpty(t, linkLog, collect, taskList);
                } catch (Exception e) {
                    linkLog.append("or? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
            linkLog.append("or->");
            return NullBuild.noEmpty(value, linkLog, collect, taskList);
        });
        return  NullBuild.busy(this);

    }

    @Override
    public NullChain<T> or(T defaultValue) {
        this.taskList.add((value)->{
            if (isNull) {
                if (Null.is(defaultValue)) {
                    linkLog.append("or? 传参不能为空");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("or->");
                return NullBuild.noEmpty(defaultValue, linkLog, collect,taskList);
            }
            linkLog.append("or->");
            return NullBuild.noEmpty(value, linkLog, collect, taskList);
        });
        return  NullBuild.busy(this);

    }


    @Override
    public <R> NullChain<R> task(Class<? extends NullTask<T, R>> task, Object... params) {

        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }
            @Override
            public NullChain nodeTask(Object value) throws RuntimeException {
                if (isNull) {
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                if (task == null) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
                }
                Object o = __task__(task.getName(), params);
                return NullBuild.noEmpty((R) o, linkLog, collect, taskList);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public NullChain<?> task(String classPath, Object... params) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }
            @Override
            public NullChain nodeTask(Object value) throws RuntimeException {
                if (isNull) {
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                if (Null.is(classPath)) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
                }
                Object o = __task__(classPath, params);
                return NullBuild.noEmpty(o, linkLog, collect, taskList);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask) {
        return task(nullGroupTask, ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME);
    }

    @Override
    public NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }
            @Override
            public NullChain nodeTask(Object value) throws RuntimeException {
                if (isNull) {
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                if (Null.isAny(nullGroupTask, threadFactoryName)) {
                    throw new NullChainException(linkLog.append("task? 传参不能为空").toString());
                }
                return __task__(nullGroupTask, threadFactoryName);
            }
        });
        return  NullBuild.busy(this);

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
            public NullChain nodeTask(Object value) throws RuntimeException {
                if (isNull) {
                    return NullBuild.empty(linkLog, collect, taskList);
                }
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
                    Object o = __nfTask__(nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
                    if (Null.is(o)) {
                        linkLog.append("nfTask? ");
                        return NullBuild.empty(linkLog, collect, taskList);
                    }
                    linkLog.append("nfTask->");
                    return NullBuild.noEmpty(o, linkLog, collect,taskList);
                } catch (Exception e) {
                    linkLog.append("nfTask? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
        });
        return  NullBuild.busy(this);

    }



    @Override
    public NullChain<NullMap<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask, String threadFactoryName) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public boolean isHeavyTask() {
                return true;
            }
            @Override
            public NullChain nodeTask(Object value) throws RuntimeException {
                if (isNull) {
                    return NullBuild.empty(linkLog, collect, taskList);
                }
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
                            Object run = __nfTask__(nullTaskInfo.getNfContext(), threadFactoryName, nullTaskInfo.getLogger(), nullTaskInfo.getParams());
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
                return NullBuild.noEmpty(nullChainMap, linkLog, collect,taskList);
            }
        });
        return  NullBuild.busy(this);
    }



    private NullChain<NullMap<String, Object>> __task__(NullGroupTask nullGroupTask, String threadFactoryName) {

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
        return NullBuild.noEmpty(nullChainMap, linkLog, collect,taskList);
    }


    private Object __task__(String classPath, Object... params) {
        NullTask nullTask = getNullTask(classPath);
        try {
            Object run = NullBuild.taskRun(value, nullTask, linkLog, params);
            if (Null.is(run)) {
                linkLog.append("task? ");
                return NullBuild.empty(linkLog, collect, taskList);
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


    private Object __nfTask__(String nfContext, String threadFactoryName, Logger logger, Object[] params) {
        //校验线程池是否存在
        ThreadFactoryUtil.addExecutor(threadFactoryName);
        NullMap<String, Object> mainSystemContext = NullMap.newHashMap();
        mainSystemContext.put("threadFactoryName", threadFactoryName);
        mainSystemContext.put("preValue", value);//上一个任务的值
        mainSystemContext.put("params", params == null ? new Object[]{} : params);
        return NfMain.run(nfContext, logger, mainSystemContext);
    }


}
