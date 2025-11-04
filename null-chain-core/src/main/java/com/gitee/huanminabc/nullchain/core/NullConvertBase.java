package com.gitee.huanminabc.nullchain.core;


import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullConvertBase<T> extends NullWorkFlowBase<T> implements NullConvert<T> {


    public NullConvertBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog,taskList);
    }

    @Override
    public <U> NullChain<U> type(Class<U> uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append(TYPE_Q).append(TYPE_CLASS_NULL).toString());
            }
            if (uClass.isInstance(value)) {
                linkLog.append(TYPE_ARROW);
                return NullBuild.noEmpty(uClass.cast(value));
            } else {
                linkLog.append(TYPE_Q).append(TYPE_MISMATCH).append(value.getClass().getName()).append(" vs ").append(uClass.getName());
                return NullBuild.empty();
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> type(U uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append(TYPE_Q).append(TYPE_CLASS_NULL).toString());
            }
            Class<?> aClass = uClass.getClass();
            //优化：只调用一次getClass()，避免在else分支中重复调用
            Class<?> valueClass = value != null ? value.getClass() : null;
            if (aClass.isInstance(value)) {
                linkLog.append(TYPE_ARROW);
                return NullBuild.noEmpty(aClass.cast(value));
            } else {
                linkLog.append(TYPE_Q).append(TYPE_MISMATCH).append(valueClass != null ? valueClass.getName() : "null").append(" vs ").append(aClass.getName());
                return NullBuild.empty();
            }
        });
        return  NullBuild.busy(this);
    }

}
