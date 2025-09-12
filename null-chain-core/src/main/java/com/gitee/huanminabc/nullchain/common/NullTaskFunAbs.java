package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.common.function.NullTaskFun;
import lombok.extern.slf4j.Slf4j;

/**
 * Null任务函数抽象类 - 提供任务函数的基础实现
 * 
 * <p>该类提供了任务函数的基础实现，实现了NullTaskFun接口，为任务函数提供通用的基础功能。
 * 通过抽象类的方式，为任务函数的实现提供便利。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>任务函数基础：提供任务函数的基础实现</li>
 *   <li>重任务标识：标识是否为重任务</li>
 *   <li>任务管理：管理任务的基本信息</li>
 *   <li>线程池选择：根据任务类型选择线程池</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>抽象实现：提供抽象的基础实现</li>
 *   <li>重任务支持：支持重任务标识</li>
 *   <li>线程池优化：根据任务类型优化线程池选择</li>
 *   <li>扩展性：提供良好的扩展性</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTaskFun 任务函数接口
 */
@Slf4j
public abstract class NullTaskFunAbs implements NullTaskFun {
    //是否是重任务 ，重任务不会被工作窃取线程池执行而是自定义的线程池
    public    boolean isHeavyTask(){
        return  false;
    }
    //上一个节点是空那么就停止执行   默认是true  否则继续
    public    boolean preNullEnd(){
        return  true;
    }
}
