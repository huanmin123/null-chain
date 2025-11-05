package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;

import java.util.Map;


/**
 * Null工作流接口 - 提供工具和任务执行功能
 * 
 * <p>该接口定义了Null链的工作流操作，包括自定义工具调用、任务执行等功能。
 * 它扩展了终结操作接口，提供了更丰富的工作流处理能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>工具调用：支持自定义工具类的调用</li>
 *   <li>任务执行：支持自定义任务的执行</li>
 *   <li>参数传递：支持向工具和任务传递参数</li>
 *   <li>工作流管理：提供完整的工作流处理能力</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>扩展性强：支持自定义工具和任务</li>
 *   <li>参数灵活：支持多种参数传递方式</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值安全：所有操作都处理空值情况</li>
 * </ul>
 * 
 * @param <T> 工作流处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullFinality 终结操作接口
 * @see NullTool 工具接口
 * @see NullTask 任务接口
 */
public interface NullWorkFlow<T> extends NullFinality<T> {
    
    /**
     * 调用自定义工具
     * 
     * <p>该方法用于调用自定义的工具类来处理当前值。
     * 工具类必须实现NullTool接口，并且会被自动实例化调用。</p>
     * 
     * @param <R> 工具返回值的类型
     * @param tool 工具类的Class对象
     * @return 包含工具处理结果的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(data)
     *     .tool(MyCustomTool.class)  // 调用自定义工具
     *     .orElse("default");
     * }</pre>
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool);
    
    /**
     * 调用自定义工具并传递参数
     * 
     * <p>该方法用于调用自定义的工具类来处理当前值，并可以向工具传递额外的参数。
     * 工具类必须实现NullTool接口，参数会传递给工具的构造函数。</p>
     * 
     * @param <R> 工具返回值的类型
     * @param tool 工具类的Class对象
     * @param params 传递给工具构造函数的参数
     * @return 包含工具处理结果的Null链
     * 
     * @example
     * <pre>{@code
     * String result = Null.of(data)
     *     .tool(MyCustomTool.class, "param1", 123)  // 调用工具并传递参数
     *     .orElse("default");
     * }</pre>
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
     * @return 返回的是一个Map<String, Object>  key是任务的taskClassName, value是任务的结果
     */
    NullChain<Map<String, Object>> task(NullGroupTask nullGroupTask);

    NullChain<Map<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName);





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
     * @return 返回的是一个Map<String, Object>  key是任务的taskClassName, value是脚本的结果
     */
    NullChain<Map<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask,String threadFactoryName);
}
