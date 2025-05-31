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

    public NullChainBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog,taskList);
    }

    @Override
    public <U> NullChain<T> of(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append("of? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append("of?");
                    return NullBuild.empty();
                }
                linkLog.append("of->");
                return NullBuild.noEmpty(value);
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
            if (function == null) {
                throw new NullChainException(linkLog.append("ifGo? 传参不能为空").toString());
            }
            try {
                Boolean apply = function.apply((T)value);
                if (!apply) {
                    linkLog.append("ifGo?");
                    return NullBuild.empty();
                }
                linkLog.append("ifGo->");
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append("ifGo? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }





    @Override
    public <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value) -> {
            if (value == null) {
                return NullBuild.empty();
            }
            if (function == null) {
                throw new NullChainException(linkLog.append("isNull? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T) value);
                if (Null.non(apply)) {
                    linkLog.append("isNull?");
                    return NullBuild.empty();
                }
                linkLog.append("isNull->");
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append("isNull? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }



    @SafeVarargs
    @Override
    public final <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... function) {
        this.taskList.add((value)->{
            if (Null.is(function)) {
                throw new NullChainException(linkLog.append("ofAny? 传参不能为空").toString());
            }
            try {
                for (int i = 0; i < function.length; i++) {
                    NullFun<? super T, ? extends U> nullFun = function[i];
                    U apply = nullFun.apply((T)value);
                    if (Null.is(apply)) {
                        linkLog.append("ofAny? 第").append(i + 1).append("个");
                        return NullBuild.empty();
                    }
                }
            } catch (Exception e) {
                linkLog.append("ofAny? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append("ofAny->");
            return NullBuild.noEmpty(value);

        });
        return  NullBuild.busy(this);
    }


    @Override
    public NullChain<T> then(Runnable function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 传参不能为空").toString());
            }
            try {
                function.run();
                linkLog.append("then->");
                return NullBuild.noEmpty(value);
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
            if (function == null) {
                throw new NullChainException(linkLog.append("then? 传参不能为空").toString());
            }
            try {
                function.accept((T)value);
                linkLog.append("then->");
                return NullBuild.noEmpty(value);
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
            if (function == null) {
                throw new NullChainException(linkLog.append("then2? 传参不能为空").toString());
            }
            try {
                function.accept(Null.of(valueT), valueT);
                linkLog.append("then2->");
                return NullBuild.noEmpty(value);
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
            if (function == null) {
                throw new NullChainException(linkLog.append("map? 传参不能为空").toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append("map?");
                    return NullBuild.empty();
                }
                linkLog.append("map->");
                return NullBuild.noEmpty(apply);
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
            if (function == null) {
                throw new NullChainException(linkLog.append("map2? 传参不能为空").toString());
            }
            try {
                U apply = function.apply(Null.of(value1), value1);
                if (Null.is(apply)) {
                    linkLog.append("map2?");
                    return NullBuild.empty();
                }
                linkLog.append("map2->");
                return NullBuild.noEmpty(apply);
            } catch (Exception e) {
                linkLog.append("map2? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  (NullChain<U>)this;

    }


    @Override
    public <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append("flatChain? 传参不能为空").toString());
            }
            try {
                NullChain<U> apply = function.apply((T)value);
                if (apply.is()) {
                    linkLog.append("flatChain?");
                    return NullBuild.empty();
                }
                linkLog.append("flatChain->");
                return NullBuild.noEmpty(apply.get());
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
            if (function == null) {
                throw new NullChainException(linkLog.append("flatOptional? 传参不能为空").toString());
            }
            try {
                Optional<U> apply = function.apply((T)value);
                if (!apply.isPresent()) {
                    linkLog.append("flatOptional?");
                    return NullBuild.empty();
                }
                linkLog.append("flatOptional->");
                return NullBuild.noEmpty(apply.get());
            } catch (Exception e) {
                linkLog.append("flatOptional? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public NullChain<T> or(Supplier<? extends T> supplier) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public NullTaskList.NullNode nodeTask(Object value) throws RuntimeException {
                if (value==null) {
                    if (supplier == null) {
                        throw new NullChainException(linkLog.append("or? 传参不能为空").toString());
                    }
                    try {
                        T t = supplier.get();
                        if (Null.is(t)) {
                            linkLog.append("or?");
                            return NullBuild.empty();
                        }
                        linkLog.append("or->");
                        return NullBuild.noEmpty(t);
                    } catch (Exception e) {
                        linkLog.append("or? ");
                        throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                    }
                }
                linkLog.append("or->");
                return NullBuild.noEmpty(value);
            }

            @Override
            public boolean preNullEnd() {
                return false;
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public NullChain<T> or(T defaultValue) {
        this.taskList.add(new NullTaskFunAbs() {
            @Override
            public NullTaskList.NullNode nodeTask(Object value) throws RuntimeException {
                if (value==null) {
                    if (Null.is(defaultValue)) {
                        linkLog.append("or? 传参不能为空");
                        return NullBuild.empty();
                    }
                    linkLog.append("or->");
                    return NullBuild.noEmpty(defaultValue);
                }
                linkLog.append("or->");
                return NullBuild.noEmpty(value);
            }

            @Override
            public boolean preNullEnd() {
                return false;
            }
        });
        return  NullBuild.busy(this);
    }


}
