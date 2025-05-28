package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullWorkFlow;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

public interface NullWorkFlowExt<T> extends NullWorkFlow<T>,  NullFinalityExt<T>{

    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool){
         NullChain<T> tNullChain = toNULL();
         return tNullChain.tool(tool);
   }

    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> convert, Object... params) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.tool(convert, params);
    }


    @Override
    default <R> NullChain<R> task(Class<? extends NullTask<T, R>> task, Object... objects) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(task, objects);
    }


    @Override
    default NullChain<?> task(String classPath, Object... objects) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(classPath, objects);
    }

    @Override
    default NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(nullGroupTask);
    }

    @Override
    default NullChain<NullMap<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(nullGroupTask,threadFactoryName );
    }

    @Override
    default  NullChain<?> nfTask(String filePath, Object... params){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(filePath, params);
    }

    @Override
    default NullChain<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(nullTaskInfo);
    }

    @Override
    default NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(nullTaskInfo,threadFactoryName);
    }

    @Override
    default NullChain<NullMap<String, Object>> nfTasks( NullGroupNfTask nullGroupNfTask,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTasks( nullGroupNfTask,threadFactoryName);
    }

}
