package com.gitee.huanminabc.nullchain.common;

/**
 * 任务加载器
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NullTaskClassLoader extends ClassLoader {
    public Class<?> findClass(String name,byte[] data) throws ClassNotFoundException {
        return defineClass(name, data, 0, data.length);
    }
}
