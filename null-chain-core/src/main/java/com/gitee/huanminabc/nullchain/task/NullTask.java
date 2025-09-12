package com.gitee.huanminabc.nullchain.task;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
/**
 * Null任务接口 - 定义自定义任务的执行规范
 * 
 * <p>该接口定义了自定义任务的执行规范，包括参数校验、初始化和执行方法。
 * 任务类用于在Null链中执行特定的业务逻辑，提供可复用的功能模块。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>参数校验：验证传入参数的类型和格式</li>
 *   <li>初始化：在任务执行前进行必要的初始化</li>
 *   <li>业务执行：执行具体的业务逻辑</li>
 *   <li>上下文管理：管理任务执行过程中的上下文信息</li>
 * </ul>
 * 
 * <h3>生命周期：</h3>
 * <ol>
 *   <li>参数校验：checkTypeParams() - 验证参数类型</li>
 *   <li>初始化：init() - 执行初始化逻辑</li>
 *   <li>业务执行：run() - 执行主要业务逻辑</li>
 * </ol>
 * 
 * <h3>使用方式：</h3>
 * <p>在Spring Boot应用中，使用@NullLabel注解标记任务类，
 * 系统会自动注册并支持在Null链中调用。</p>
 * 
 * <h3>与NullTool的区别：</h3>
 * <ul>
 *   <li>NullTask：用于执行复杂的业务任务</li>
 *   <li>NullTool：用于执行简单的工具操作</li>
 * </ul>
 * 
 * @param <T> 输入值的类型
 * @param <R> 返回值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTool 工具接口
 * @see NullLabel 标签注解
 */
public interface NullTask<T,R> {

    /**
     * 参数类型校验
     * 
     * <p>该方法用于验证传入参数的类型和格式。如果不依赖params参数，那么返回null表示不进行校验。</p>
     * 
     * @return 参数类型信息，返回null表示不校验
     */
    default NullType checkTypeParams() {
        return null; //返回null表示不校验
    }


    /**
     * 任务初始化方法
     * 
     * <p>该方法在任务执行前进行必要的初始化操作，包括参数验证、资源准备等。
     * 上下文信息不会传递到下一个任务。</p>
     * 
     * @param preValue 上一个任务的返回值
     * @param params 当前任务的参数
     * @param context 当前任务的上下文，不会传递到下一个任务
     * @throws Exception 初始化过程中的异常
     */
    default void init(T preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {

    }


    /**
     * 任务执行方法
     * 
     * <p>该方法执行任务的主要业务逻辑，是任务的核心方法。
     * 返回值会传递到下一个任务，上下文信息不会传递到下一个任务。</p>
     * 
     * @param preValue 上一个任务的返回值
     * @param params 当前任务的参数
     * @param context 当前任务的上下文，不会传递到下一个任务
     * @return 返回值会传递到下一个任务
     * @throws Exception 任务执行过程中的异常
     */
    R run(T preValue, NullChain<?>[] params, NullMap<String,Object> context) throws Exception;


}
