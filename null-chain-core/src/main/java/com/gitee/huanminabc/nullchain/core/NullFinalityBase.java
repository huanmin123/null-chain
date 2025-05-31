package com.gitee.huanminabc.nullchain.core;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
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
                    linkLog.append("...ifPresent? ");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    linkLog.append("...ifPresent? ");
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
                    linkLog.append("...ifPresentOrElse-action? ");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    action.accept((T) nullChainBase.value);
                } catch (Exception e) {
                    linkLog.append("...ifPresentOrElse-action? ");
                    throw NullReflectionKit.addRunErrorMessage(e, linkLog);
                }
            } else {
                if (emptyAction == null) {
                    linkLog.append("...ifPresentOrElse-emptyAction? ");
                    throw new NullChainException(linkLog.toString());
                }
                try {
                    emptyAction.run();
                } catch (Exception e) {
                    linkLog.append("...ifPresentOrElse-emptyAction? ");
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
                throw new NullChainException(linkLog.append("...capture? 参数不能为空").toString());
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
                linkLog.append("...getSafe? 异常处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            X x;
            try {
                x = exceptionSupplier.get();
            } catch (Exception e) {
                linkLog.append("...getSafe? ");
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
            linkLog.append("...orElse? 默认值不能是空的");
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
            linkLog.append("...orElse? 默认值不能为空");
            throw new NullChainException(linkLog.toString());
        }
        T t;
        try {
            t = defaultValue.get();
        } catch (Exception e) {
            linkLog.append("...orElse? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (Null.is(t)) {
            linkLog.append("orElse? 默认值不能是空的");
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
