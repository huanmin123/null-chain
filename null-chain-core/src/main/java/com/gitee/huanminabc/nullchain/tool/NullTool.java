package com.gitee.huanminabc.nullchain.tool;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public interface NullTool<T,R> {
    //参数类型校验, 如果不依賴params参数,那么返回null
    default NullType checkTypeParams() {
        return null; //返回null表示不校验
    }

    /**
     * @param preValue 上一个任务的返回值
     * @param params 当前任务的参数
     * @param context 当前任务的上下文,不会传单到下一个任务
     * @throws Exception
     */
    default void init(T preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {

    }

    /**
     * 任务执行方法
     * @param preValue 上一个任务的返回值
     * @param params 当前任务的参数
     * @param context 当前任务的上下文,不会传单到下一个任务
     * @return 返回值会传递到下一个任务
     */
    R run(T preValue, NullChain<?>[] params, NullMap<String,Object> context) throws Exception;


}
