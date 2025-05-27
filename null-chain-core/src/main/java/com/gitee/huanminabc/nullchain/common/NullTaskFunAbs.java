package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.common.function.NullTaskFun;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-27 10:00
 **/
@Slf4j
public abstract class NullTaskFunAbs implements NullTaskFun {
    //是否是重任务 ，重任务不会被工作窃取线程池执行而是自定义的线程池
    public abstract   boolean isHeavyTask();
}
