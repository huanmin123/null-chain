package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;


/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 **/
public interface NullWorkFlow<T> extends NullFinality<T> {
    
    /**
     * 自定义工具
     * @param tool
     * @param <R>
     * @return
     * @
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool);
    /**
     * @param params 工具的参数
     * @return
     * @param <R>
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params);



    /**
     * @param task 任务的类
     * @param params 任务的参数
     */
    <R> NullChain<R> task(Class<? extends NullTask<T,R>> task, Object... params);



    /**
     * @param classPath 任务的类路径
     * @param params    任务的参数
     */
    NullChain<?> task(String classPath, Object... params);


    /**
     * 多任务并发执行, 任务之间没有关联, 任务执行完毕后,会将结果合并  不支持同类型任务
     * @param nullGroupTask   任务组
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是任务的结果
     */
    NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask);

    NullChain<NullMap<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName);





    /**
     * 执行单个nf脚本, 返回的数据是不能确认类型的所以需要后面自己转换
     * @param nullTaskInfo 任务信息
     * @return 如果内部脚本并发绑定变量然后返回这个变量, 那么这个类型一定是 NullChain<Map<String, NullChain<Object>>> 类型, 否则就是NullChain<Object>
     */

    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo);
    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName);
    NullChain<?> nfTask(String nfContext, Object... params);

    /**
     * 多脚本同时并发执行
     * @param threadFactoryName 线程池名称
     * @param nullGroupNfTask 脚本组
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是脚本的结果
     */
    NullChain<NullMap<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask,String threadFactoryName);
}
