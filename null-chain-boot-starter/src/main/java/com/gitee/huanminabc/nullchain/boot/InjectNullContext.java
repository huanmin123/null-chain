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
 * @author huanmin
 * @date 2024/11/22
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
