package com.gitee.huanminabc.nullchain.tool;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
/**
 * Null工具接口 - 定义自定义工具的执行规范
 * 
 * <p>该接口定义了自定义工具的执行规范，包括参数校验、初始化和执行方法。
 * 工具类用于在Null链中执行特定的业务逻辑，提供可复用的功能模块。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>参数校验：验证传入参数的类型和格式</li>
 *   <li>初始化：在工具执行前进行必要的初始化</li>
 *   <li>业务执行：执行具体的业务逻辑</li>
 *   <li>上下文管理：管理工具执行过程中的上下文信息</li>
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
 * <p>在Spring Boot应用中，使用@NullLabel注解标记工具类，
 * 系统会自动注册并支持在Null链中调用。</p>
 * 
 * @param <T> 输入值的类型
 * @param <R> 返回值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTask 任务接口
 * @see NullLabel 标签注解
 */
public interface NullTool<T,R> {
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
     * 工具初始化方法
     * 
     * <p>该方法在工具执行前进行必要的初始化操作，包括参数验证、资源准备等。
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
     * 工具执行方法
     * 
     * <p>该方法执行工具的主要业务逻辑，是工具的核心方法。
     * 返回值会传递到下一个任务，上下文信息不会传递到下一个任务。</p>
     * 
     * @param preValue 上一个任务的返回值
     * @param params 当前任务的参数
     * @param context 当前任务的上下文，不会传递到下一个任务
     * @return 返回值会传递到下一个任务
     * @throws Exception 工具执行过程中的异常
     */
    R run(T preValue, NullChain<?>[] params, NullMap<String,Object> context) throws Exception;


}
