package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Null链基础实现类
 * 
 * @param <T> 链中值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
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
                throw new NullChainException(linkLog.append(CHAIN_OF_PARAM_NULL).toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append(CHAIN_OF_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_OF_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_OF_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> ifGo(Predicate<? super T> predicate) {
        this.taskList.add((value)->{
            if (predicate == null) {
                throw new NullChainException(linkLog.append(CHAIN_IFGO_PARAM_NULL).toString());
            }
            try {
                boolean apply = predicate.test((T)value);
                if (!apply) {
                    linkLog.append(CHAIN_IFGO_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_IFGO_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_IFGO_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> ifNeGo(Predicate<? super T> predicate) {
        this.taskList.add((value)->{
            if (predicate == null) {
                throw new NullChainException(linkLog.append(CHAIN_IFNEGO_PARAM_NULL).toString());
            }
            try {
                boolean apply = predicate.test((T)value);
                if (apply) {
                    linkLog.append(CHAIN_IFNEGO_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_IFNEGO_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_IFNEGO_Q);
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
                throw new NullChainException(linkLog.append(CHAIN_ISNULL_PARAM_NULL).toString());
            }
            try {
                U apply = function.apply((T) value);
                if (Null.non(apply)) {
                    linkLog.append(CHAIN_ISNULL_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_ISNULL_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_ISNULL_Q);
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
                throw new NullChainException(linkLog.append(CHAIN_OFANY_PARAM_NULL).toString());
            }
            try {
                for (int i = 0; i < function.length; i++) {
                    NullFun<? super T, ? extends U> nullFun = function[i];
                    U apply = nullFun.apply((T)value);
                    if (Null.is(apply)) {
                        linkLog.append(CHAIN_OFANY_INDEX).append(i + 1).append("个");
                        return NullBuild.empty();
                    }
                }
            } catch (Exception e) {
                linkLog.append(CHAIN_OFANY_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            linkLog.append(CHAIN_OFANY_ARROW);
            return NullBuild.noEmpty(value);

        });
        return  NullBuild.busy(this);
    }


    @Override
    public NullChain<T> then(Runnable function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append(CHAIN_THEN_PARAM_NULL).toString());
            }
            try {
                function.run();
                linkLog.append(CHAIN_THEN_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_THEN_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<T> then(Consumer<? super T> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append(CHAIN_THEN_PARAM_NULL).toString());
            }
            try {
                function.accept((T)value);
                linkLog.append(CHAIN_THEN_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_THEN_Q);
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
                throw new NullChainException(linkLog.append(CHAIN_THEN2_PARAM_NULL).toString());
            }
            try {
                function.accept(Null.of(valueT), valueT);
                linkLog.append(CHAIN_THEN2_ARROW);
                return NullBuild.noEmpty(value);
            } catch (Exception e) {
                linkLog.append(CHAIN_THEN2_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public <U> NullChain<U> map(NullFun<? super T, ? extends U> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append(CHAIN_MAP_PARAM_NULL).toString());
            }
            try {
                U apply = function.apply((T)value);
                if (Null.is(apply)) {
                    linkLog.append(CHAIN_MAP_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_MAP_ARROW);
                return NullBuild.noEmpty(apply);
            } catch (Exception e) {
                linkLog.append(CHAIN_MAP_Q);
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
                throw new NullChainException(linkLog.append(CHAIN_MAP2_PARAM_NULL).toString());
            }
            try {
                U apply = function.apply(Null.of(value1), value1);
                if (Null.is(apply)) {
                    linkLog.append(CHAIN_MAP2_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_MAP2_ARROW);
                return NullBuild.noEmpty(apply);
            } catch (Exception e) {
                linkLog.append(CHAIN_MAP2_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }


    @Override
    public <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append(CHAIN_FLATCHAIN_PARAM_NULL).toString());
            }
            try {
                NullChain<U> apply = function.apply((T)value);
                if (apply.is()) {
                    linkLog.append(CHAIN_FLATCHAIN_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_FLATCHAIN_ARROW);
                return NullBuild.noEmpty(apply.get());
            } catch (Exception e) {
                linkLog.append(CHAIN_FLATCHAIN_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function) {
        this.taskList.add((value)->{
            if (function == null) {
                throw new NullChainException(linkLog.append(CHAIN_FLATOPTIONAL_PARAM_NULL).toString());
            }
            try {
                Optional<U> apply = function.apply((T)value);
                if (!apply.isPresent()) {
                    linkLog.append(CHAIN_FLATOPTIONAL_Q);
                    return NullBuild.empty();
                }
                linkLog.append(CHAIN_FLATOPTIONAL_ARROW);
                return NullBuild.noEmpty(apply.get());
            } catch (Exception e) {
                linkLog.append(CHAIN_FLATOPTIONAL_Q);
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
                        throw new NullChainException(linkLog.append(CHAIN_OR_PARAM_NULL).toString());
                    }
                    try {
                        T t = supplier.get();
                        if (Null.is(t)) {
                            linkLog.append(CHAIN_OR_Q);
                            return NullBuild.empty();
                        }
                        linkLog.append(CHAIN_OR_ARROW);
                        return NullBuild.noEmpty(t);
                    } catch (Exception e) {
                        linkLog.append(CHAIN_OR_Q);
                        throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                    }
                }
                linkLog.append(CHAIN_OR_ARROW);
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
                        linkLog.append(CHAIN_OR_PARAM_NULL);
                        return NullBuild.empty();
                    }
                    linkLog.append(CHAIN_OR_ARROW);
                    return NullBuild.noEmpty(defaultValue);
                }
                linkLog.append(CHAIN_OR_ARROW);
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
