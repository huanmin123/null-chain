package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Null链基础实现类 - 提供空值安全的链式操作实现
 * 
 * <p>这是NullChain接口的默认实现类，提供了完整的链式操作功能。
 * 该类负责管理任务列表和执行链式操作，确保空值安全。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>条件判断和分支处理</li>
 *   <li>值映射和转换</li>
 *   <li>类型转换和检查</li>
 *   <li>链式操作管理</li>
 *   <li>异常处理和日志记录</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>延迟执行：操作不会立即执行，而是在最终获取结果时执行</li>
 *   <li>空值安全：任何操作遇到null都会优雅处理</li>
 *   <li>链式调用：支持流畅的链式编程风格</li>
 *   <li>日志追踪：完整的操作日志记录</li>
 * </ul>
 * 
 * @param <T> 链中值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullConvertBase 转换操作基类
 */
@Slf4j
public class NullChainBase<T> extends NullConvertBase<T> implements NullChain<T> {

    public NullChainBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog,taskList);
    }

    @Override
    public <U> NullChain<T> of(Function<? super T, ? extends U> function) {
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
    public <U> NullChain<T> isNull(Function<? super T, ? extends U> function) {
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
    public final <U> NullChain<T> ofAny(Function<? super T, ? extends U>... function) {
        this.taskList.add((value)->{
            if (Null.is(function)) {
                throw new NullChainException(linkLog.append(CHAIN_OFANY_PARAM_NULL).toString());
            }
            try {
                for (int i = 0; i < function.length; i++) {
                    Function<? super T, ? extends U> nullFun = function[i];
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
    public <U> NullChain<U> map(Function<? super T, ? extends U> function) {
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
    public <U> NullChain<U> flatChain(Function<? super T, ? extends NullChain<U>> function) {
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
    public <U> NullChain<U> flatOptional(Function<? super T, ? extends Optional<U>> function) {
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
