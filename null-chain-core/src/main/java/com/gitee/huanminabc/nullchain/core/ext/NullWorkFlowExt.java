package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullWorkFlow;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;

import java.util.Map;

/**
 * Null工作流扩展接口 - 提供工作流操作的扩展功能
 * 
 * <p>该接口扩展了NullWorkFlow接口，提供了额外的工作流操作功能。
 * 通过默认方法实现，为工作流操作提供更丰富的功能支持。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>工作流操作扩展：提供额外的工作流操作方法</li>
 *   <li>工具调用：支持自定义工具类的调用</li>
 *   <li>任务执行：支持自定义任务的执行</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 * </ul>
 * 
 * @param <T> 工作流处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullWorkFlow 工作流接口
 * @see NullFinalityExt 终结操作扩展接口
 */
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
    default NullChain<Map<String, Object>> task(NullGroupTask nullGroupTask){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(nullGroupTask);
    }

    @Override
    default NullChain<Map<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName){
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
    default NullChain<Map<String, Object>> nfTasks( NullGroupNfTask nullGroupNfTask,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTasks( nullGroupNfTask,threadFactoryName);
    }

}
