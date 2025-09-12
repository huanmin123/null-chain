package com.gitee.huanminabc.nullchain.core;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullFinalityBase<T> extends NullKernelAbstract<T> implements NullFinality<T> {


    public NullFinalityBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }



    @Override
    public boolean is() {
        return taskList.runTaskAll().isNull;
    }

    @Override
    public boolean non() {
        return !is();
    }

    @Override
    public T getSafe() throws NullChainCheckException {
        NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            throw new NullChainCheckException(linkLog.toString());
        }
        return (T) nullChainBase.value;
    }

    @Override
    public T get() {
        NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            throw new NullChainException(linkLog.toString());
        }
        return (T) nullChainBase.value;
    }


    @Override
    public void ifPresent(Consumer<? super T> action) {
        taskList.runTaskAll((nullChainBase) -> {
            if (!nullChainBase.isNull) {
                if (action == null) {
                    linkLog.append(IF_PRESENT_Q);
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    linkLog.append(IF_PRESENT_Q);
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
        },null);
    }


    @Override
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        taskList.runTaskAll((nullChainBase) -> {
            if (!nullChainBase.isNull) {
                if (action == null) {
                    linkLog.append(IF_PRESENT_OR_ELSE_ACTION_Q);
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    linkLog.append(IF_PRESENT_OR_ELSE_ACTION_Q);
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            } else {
                if (emptyAction == null) {
                    linkLog.append(IF_PRESENT_OR_ELSE_EMPTY_ACTION_Q);
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    emptyAction.run();
                } catch (Exception e) {
                    linkLog.append(IF_PRESENT_OR_ELSE_EMPTY_ACTION_Q);
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            }
        },null);
    }

    @Override
    public void except(Consumer<Throwable> consumer) {
        taskList.runTaskAll((nullChainBase) -> {
            if (nullChainBase.isNull) {
                consumer.accept(new NullChainException(linkLog.toString()));
                return;
            }
            if (consumer == null) {
                throw new NullChainException(linkLog.append(CAPTURE_PARAM_NULL).toString());
            }
        },consumer);
    }


    @Override
    public <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
         NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        } else {
            if (exceptionSupplier == null) {
                linkLog.append(GET_SAFE_EXCEPTION_NULL);
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                linkLog.append(GET_SAFE_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            throw NullReflectionKit.orThrowable(x, linkLog);
        }
    }

    @Override
    public T get(String exceptionMessage, Object... args) {
         NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        } else {
            if (args == null || args.length == 0 || exceptionMessage == null) {
                linkLog.append(exceptionMessage == null ? "" : exceptionMessage);
            } else {
                String format = String.format(exceptionMessage.replaceAll("\\{\\s*}", "%s"), args);
                linkLog.append(" ").append(format);
            }
            throw new NullChainException(linkLog.toString());
        }
    }

    @Override
    public T orElseNull() {
         NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        return null;
    }


    @Override
    public NullCollect collect() {
        //设置为可进行收集
        taskList.setCollect(new NullCollect());
        //执行所有的节点进收集
        taskList.runTaskAll();
        return taskList.getCollect();
    }


    @Override
    public T orElse(T defaultValue) {
         NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        //判断defaultValue
        if (Null.is(defaultValue)) {
            linkLog.append(OR_ELSE_DEFAULT_NULL);
            throw new NullChainException(linkLog.toString());
        }
        return defaultValue;
    }

    @Override
    public T orElse(Supplier<T> defaultValue) {
         NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (!nullChainBase.isNull) {
            return (T) nullChainBase.value;
        }
        if (defaultValue == null) {
            linkLog.append(OR_ELSE_SUPPLIER_NULL);
            throw new NullChainException(linkLog.toString());
        }
        T t;
        try {
            t = defaultValue.get();
        } catch (Exception e) {
            linkLog.append(OR_ELSE_Q);
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (Null.is(t)) {
            linkLog.append(OR_ELSE_DEFAULT_NULL);
            throw new NullChainException(linkLog.toString());
        }
        return t;

    }

   @Override
    public int length() {
       NullTaskList.NullNode nullChainBase = taskList.runTaskAll();
        if (nullChainBase.isNull) {
            return 0;
        }
        return NullReflectionKit.getSize(nullChainBase.value);
    }
}
