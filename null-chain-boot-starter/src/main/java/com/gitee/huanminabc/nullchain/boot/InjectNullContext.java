package com.gitee.huanminabc.nullchain.boot;


import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * Null上下文注入器 - 自动注册Null链组件到Spring上下文
 * 
 * <p>该类负责在Spring容器启动时自动扫描和注册带有@NullLabel注解的组件。
 * 它监听Spring容器的刷新事件，自动将NullTool和NullTask组件注册到相应的工厂中。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>自动扫描：扫描Spring容器中带有@NullLabel注解的Bean</li>
 *   <li>组件注册：自动注册NullTool和NullTask组件</li>
 *   <li>类型检查：验证组件类型是否符合要求</li>
 *   <li>异常处理：对不符合要求的组件抛出异常</li>
 * </ul>
 * 
 * <h3>支持的类型：</h3>
 * <ul>
 *   <li>NullTool：工具类组件</li>
 *   <li>NullTask：任务类组件</li>
 * </ul>
 * 
 * <h3>使用方式：</h3>
 * <p>在Spring Bean上添加@NullLabel注解，该注入器会自动处理注册过程。</p>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullLabel Null标签注解
 * @see NullTool Null工具接口
 * @see NullTask Null任务接口
 * @see ApplicationListener Spring应用监听器接口
 */
@Slf4j
public class InjectNullContext implements ApplicationListener<ContextRefreshedEvent> {
    private static  boolean init=false;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(init){
            return;
        }
        init=true;
       log.info("NullSpringConfig init");
        ApplicationContext applicationContext = event.getApplicationContext();
        //获取所有的NULL注解
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(NullLabel.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof NullTool) {
                NullToolFactory.__registerTool__((NullTool)bean);
            }else if(bean instanceof NullTask) {
                NullTaskFactory.__registerTask__((NullTask) bean);
            }else{
                throw new NullChainException("NULL注解只能用于NULLTool和NULLTask");
            }
        }
    }
}
