package com.gitee.huanminabc.nullchain.boot;

import java.lang.annotation.*;
/**
 * Null标签注解 - 标识Null链组件
 * 
 * <p>该注解用于标识Null链框架中的组件，包括NullTool和NullTask。
 * 被此注解标记的类会在Spring容器启动时自动注册到相应的工厂中。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>标记NullTool实现类，用于工具组件注册</li>
 *   <li>标记NullTask实现类，用于任务组件注册</li>
 *   <li>配合Spring Boot自动配置使用</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Component
 * @NullLabel
 * public class MyNullTool implements NullTool {
 *     // 工具实现
 * }
 * 
 * @Component
 * @NullLabel
 * public class MyNullTask implements NullTask {
 *     // 任务实现
 * }
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>只能用于实现了NullTool或NullTask接口的类</li>
 *   <li>需要配合Spring的@Component等注解使用</li>
 *   <li>类必须被Spring容器管理</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTool Null工具接口
 * @see NullTask Null任务接口
 * @see InjectNullContext 注入上下文类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NullLabel {
    // 标记注解，无需额外属性
}
