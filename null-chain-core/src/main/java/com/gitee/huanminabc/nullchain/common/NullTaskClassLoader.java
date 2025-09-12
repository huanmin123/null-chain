package com.gitee.huanminabc.nullchain.common;

/**
 * Null任务类加载器 - 提供任务类的动态加载功能
 * 
 * <p>该类提供了任务类的动态加载功能，继承自ClassLoader，支持从字节码数据动态加载类定义。
 * 通过动态类加载机制，为Null链操作提供灵活的任务执行能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>动态类加载：从字节码数据动态加载类</li>
 *   <li>任务类管理：管理任务类的加载和定义</li>
 *   <li>字节码处理：处理字节码数据</li>
 *   <li>类定义：动态定义类结构</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>动态加载：支持运行时动态加载</li>
 *   <li>字节码支持：支持字节码级别的类定义</li>
 *   <li>任务专用：专门用于任务类的加载</li>
 *   <li>安全性：通过类加载器保证安全性</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see ClassLoader 类加载器
 */
public class NullTaskClassLoader extends ClassLoader {
    public Class<?> findClass(String name,byte[] data) throws ClassNotFoundException {
        return defineClass(name, data, 0, data.length);
    }
}
