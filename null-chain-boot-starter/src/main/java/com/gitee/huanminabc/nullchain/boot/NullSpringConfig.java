package com.gitee.huanminabc.nullchain.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
/**
 * @author huanmin
 * @date 2024/11/22
 */
@Configuration
@Import({InjectNullContext.class})
public class NullSpringConfig {


}
