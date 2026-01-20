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
     * 执行自定义任务
     *
     * <p>根据任务类执行任务逻辑，当前链值会作为任务的输入参数之一。</p>
     *
     * @param <R>    任务返回值类型
     * @param task   任务类，必须实现NullTask
     * @param params 任务执行时的额外参数
     * @return 包含任务结果的Null链
     * 
     * @example
     * <pre>{@code
     * // 执行用户处理任务，传递额外参数
     * String result = Null.of(user)
     *     .task(UserProcessTask.class, "param1", 123)
     *     .orElse("处理失败");
     * 
     * // 执行数据转换任务
     * DataResult result = Null.of(rawData)
     *     .task(DataTransformTask.class)
     *     .orElse(new DataResult());
     * }</pre>
     */
    <R> NullChain<R> task(Class<? extends NullTask<T,R>> task, Object... params);



    /**
     * 执行自定义任务（通过类路径）
     *
     * <p>通过任务的全限定类名执行任务，适用于动态指定任务实现。</p>
     *
     * @param classPath 任务类的全限定名
     * @param params    任务执行时的额外参数
     * @return 包含任务结果的Null链
     * 
     * @example
     * <pre>{@code
     * // 通过类路径动态执行任务
     * Object result = Null.of(data)
     *     .task("com.example.tasks.MyTask", "param1", "param2")
     *     .orElse(null);
     * 
     * // 根据配置动态选择任务
     * String taskClass = config.getTaskClass();
     * Object result = Null.of(input)
     *     .task(taskClass, input)
     *     .orElse(null);
     * }</pre>
     */
    NullChain<?> task(String classPath, Object... params);


    /**
     * 多任务并发执行
     * 
     * <p>任务之间没有关联，任务执行完毕后会将结果合并。不支持同类型任务。</p>
     *
     * @param nullGroupTask   任务组
     * @return 返回的是一个Map<String, Object>，key是任务的taskClassName，value是任务的结果
     * 
     * @example
     * <pre>{@code
     * // 创建任务组并并发执行
     * NullGroupTask taskGroup = NullGroupTask.buildGroup(
     *     NullGroupTask.task(UserQueryTask.class.getName(), userId),
     *     NullGroupTask.task(OrderQueryTask.class.getName(), userId),
     *     NullGroupTask.task(AddressQueryTask.class.getName(), userId)
     * );
     * 
     * Map<String, Object> results = Null.of(userId)
     *     .task(taskGroup)
     *     .orElse(new HashMap<>());
     * 
     * // 获取各个任务的结果
     * User user = (User) results.get(UserQueryTask.class.getName());
     * List<Order> orders = (List<Order>) results.get(OrderQueryTask.class.getName());
     * Address address = (Address) results.get(AddressQueryTask.class.getName());
     * }</pre>
     */
    NullChain<Map<String, Object>> task(NullGroupTask nullGroupTask);

    /**
     * 多任务并发执行（指定线程池）
     * 
     * <p>任务之间没有关联，任务执行完毕后会将结果合并。不支持同类型任务。
     * 使用指定的线程池名称来执行任务。</p>
     *
     * @param nullGroupTask   任务组
     * @param threadFactoryName 线程池名称
     * @return 返回的是一个Map<String, Object>，key是任务的taskClassName，value是任务的结果
     * 
     * @example
     * <pre>{@code
     * // 使用指定的线程池执行任务组
     * NullGroupTask taskGroup = NullGroupTask.buildGroup(
     *     NullGroupTask.task(Task1.class.getName()),
     *     NullGroupTask.task(Task2.class.getName())
     * );
     * 
     * Map<String, Object> results = Null.of(input)
     *     .task(taskGroup, "custom-thread-pool")
     *     .orElse(new HashMap<>());
     * }</pre>
     */
    NullChain<Map<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName);





    /**
     * 执行单个NF脚本
     * 
     * <p>执行NF脚本并返回结果。返回的数据类型不能确认，需要后面自己转换。
     * 如果内部脚本并发绑定变量然后返回这个变量，那么这个类型一定是 NullChain<Map<String, NullChain<Object>>> 类型，
     * 否则就是NullChain<Object>。</p>
     *
     * @param nullTaskInfo 任务信息
     * @return 脚本执行结果，类型不确定，需要后续转换
     * 
     * @example
     * <pre>{@code
     * // 执行NF脚本任务
     * NullGroupNfTask.NullTaskInfo taskInfo = new NullGroupNfTask.NullTaskInfo();
     * taskInfo.setNfContext("var name = 'test'; return name;");
     * 
     * Object result = Null.of(input)
     *     .nfTask(taskInfo)
     *     .orElse(null);
     * 
     * // 转换结果类型
     * String strResult = Null.of(result)
     *     .type(String.class)
     *     .orElse("");
     * }</pre>
     */
    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo);

    /**
     * 执行单个NF脚本（指定线程池）
     * 
     * <p>使用指定的线程池执行NF脚本。</p>
     *
     * @param nullTaskInfo 任务信息
     * @param threadFactoryName 线程池名称
     * @return 脚本执行结果，类型不确定，需要后续转换
     * 
     * @example
     * <pre>{@code
     * NullGroupNfTask.NullTaskInfo taskInfo = new NullGroupNfTask.NullTaskInfo();
     * taskInfo.setNfContext("return input + 1;");
     * 
     * Object result = Null.of(10)
     *     .nfTask(taskInfo, "nf-thread-pool")
     *     .orElse(null);
     * }</pre>
     */
    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName);

    /**
     * 执行单个NF脚本（直接传入脚本内容）
     * 
     * <p>直接传入NF脚本内容执行，适用于简单的脚本执行场景。</p>
     *
     * @param nfContext NF脚本内容
     * @param params    脚本参数
     * @return 脚本执行结果，类型不确定，需要后续转换
     * 
     * @example
     * <pre>{@code
     * // 执行简单的NF脚本
     * Object result = Null.of(10)
     *     .nfTask("return input * 2;")
     *     .orElse(null);
     * 
     * // 执行带参数的NF脚本
     * Object result = Null.of(user)
     *     .nfTask("return name + '_' + age;", "prefix")
     *     .orElse(null);
     * }</pre>
     */
    NullChain<?> nfTask(String nfContext, Object... params);

    /**
     * 多脚本同时并发执行
     * 
     * <p>多个NF脚本并发执行，执行完毕后将结果合并。</p>
     *
     * @param threadFactoryName 线程池名称
     * @param nullGroupNfTask 脚本组
     * @return 返回的是一个Map<String, Object>，key是任务的taskClassName，value是脚本的结果
     * 
     * @example
     * <pre>{@code
     * // 创建脚本组
     * NullGroupNfTask scriptGroup = NullGroupNfTask.buildGroup(
     *     NullGroupNfTask.task("script1", "return input + 1;"),
     *     NullGroupNfTask.task("script2", "return input * 2;"),
     *     NullGroupNfTask.task("script3", "return input - 1;")
     * );
     * 
     * // 并发执行所有脚本
     * Map<String, Object> results = Null.of(10)
     *     .nfTasks(scriptGroup, "nf-thread-pool")
     *     .orElse(new HashMap<>());
     * 
     * // 获取各个脚本的结果
     * Object result1 = results.get("script1");
     * Object result2 = results.get("script2");
     * Object result3 = results.get("script3");
     * }</pre>
     */
    NullChain<Map<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask,String threadFactoryName);
}
