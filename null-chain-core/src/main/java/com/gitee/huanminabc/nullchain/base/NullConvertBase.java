package com.gitee.huanminabc.nullchain.base;


import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullTaskList;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public class NullConvertBase<T> extends NullToolsBase<T> implements NullConvert<T> {


    public NullConvertBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect,taskList);
    }

    public NullConvertBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect,taskList);
    }

    @Override
    public NullChain<T> async() {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect,taskList);
            }
            linkLog.append("async->");
            NullChainBase<T> chain = (NullChainBase)NullBuild.noEmpty(value, linkLog, collect, taskList);
            chain.async=true;
            return chain;
        });
        return  NullBuild.busy(this);
        
    }

    @Override
    public NullChain<T> async(String threadFactoryName) throws NullChainException {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect,taskList);
            }
            ThreadFactoryUtil.addExecutor(threadFactoryName);
            linkLog.append("async->");
            NullChainBase<T> chain = (NullChainBase)NullBuild.noEmpty(value, linkLog, collect, taskList);
            chain.async=true;
            taskList.setCurrentThreadFactoryName(threadFactoryName);
            return chain;
        });
        return  NullBuild.busy(this);
    }

    @Override
    public <U> NullChain<U> type(Class<U> uClass) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (uClass == null) {
                throw new NullChainException(linkLog.append("type? ").append("转换类型不能为空").toString());
            }
            if (uClass.isInstance(value)) {
                linkLog.append("type->");
                return NullBuild.noEmpty(uClass.cast(value), linkLog, collect,taskList);
            } else {
                linkLog.append("type? ").append("类型不匹配 ").append(value.getClass().getName()).append(" vs ").append(uClass.getName());
                return NullBuild.empty(linkLog, collect, taskList);
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> type(U uClass) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            if (uClass == null) {
                throw new NullChainException(linkLog.append("type? ").append("转换类型不能为空").toString());
            }
            return type((Class<U>) uClass.getClass());
        });
        return  NullBuild.busy(this);
    }

}
