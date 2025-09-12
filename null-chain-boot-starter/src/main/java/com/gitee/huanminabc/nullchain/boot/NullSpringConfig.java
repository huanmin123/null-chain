package com.gitee.huanminabc.nullchain.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
/**
 * Null链Spring配置类 - 自动配置Null链框架
 * 
 * <p>这是Null链框架的Spring Boot自动配置类，用于在Spring环境中自动配置
 * Null链相关的Bean和组件。通过@Configuration和@Import注解实现自动装配。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>自动配置：自动配置Null链相关的Spring Bean</li>
 *   <li>依赖注入：支持Null链组件的依赖注入</li>
 *   <li>上下文管理：管理Null链的执行上下文</li>
 *   <li>生命周期管理：管理Null链组件的生命周期</li>
 * </ul>
 * 
 * <h3>使用方式：</h3>
 * <p>在Spring Boot应用中，只需要添加null-chain-boot-starter依赖，
 * 该配置类会自动生效，无需额外配置。</p>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see InjectNullContext 注入Null上下文类
 * @see Configuration Spring配置注解
 * @see Import Spring导入注解
 */
@Configuration
@Import({InjectNullContext.class})
public class NullSpringConfig {
    // 配置类无需额外实现，通过注解完成自动配置
}
