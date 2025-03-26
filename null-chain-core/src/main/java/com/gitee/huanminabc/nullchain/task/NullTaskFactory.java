package com.gitee.huanminabc.nullchain.task;

import com.gitee.huanminabc.nullchain.common.NullTaskClassLoader;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NullTaskFactory {
    private static final Map<String, NullTask> classList = new HashMap<>();
    private static final Map<String, NullTask> taskMap = new HashMap<>();
    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Lock readLock = readWriteLock.readLock();
    private static final Lock writeLock = readWriteLock.writeLock();

    //获取转换器
    public static NullTask getTask(String clazzName) {
        try {
            readLock.lock();
            //去加载器中找找
            NullTask o = classList.get(clazzName);
            if (o != null) {
                return o;
            }
            //如果没有找到就去注册的任务中找
            return taskMap.get(clazzName);
        } finally {
            readLock.unlock();
        }
    }

    //注册任务
    public static void registerTask(Class<? extends NullTask> task) {
        try {
            writeLock.lock();
            String name = task.getName();
            //判断是否存在如果存在就异常
            if (taskMap.containsKey(name)) {
                throw new NullChainException("已经存在任务:" + name);
            }
            //获取转换器的类型
            NullTask trnullTask = task.newInstance();
            taskMap.put(name, trnullTask);
        } catch (Exception e) {
            log.error("注册任务失败 :", e);
        } finally {
            writeLock.unlock();
        }
    }




    /**
     * @param className 类的全限定名 com.xxx.xxx.ClassName
     * @param data      类的字节码
     */
    public static void loadTaskClass(String className, byte[] data) {
        try {
            writeLock.lock();
            //判断data是否为空
            if (data == null || data.length == 0) {
                throw new NullChainException(className + "字节码为空");
            }
            Class<?> aClass = new NullTaskClassLoader().findClass(className, data);
            //判断类型是NULLTask的子类
            if (!NullTask.class.isAssignableFrom(aClass)) {
                throw new NullChainException(className + "不是任务,没有继承NULLTask");
            }
            Object o = aClass.newInstance();
            classList.put(className, (NullTask) o);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("加载新任务{}失败:", className, e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 通过文件路径加载任务
     *
     * @param className 类的全限定名 com.xxx.xxx.ClassName
     * @param filePath  类的路径
     */
    public static void loadTaskClass(String className, String filePath) {
        try {
            writeLock.lock();
            File read = new File(filePath);
            //判断文件是否存在
            if (!read.exists()) {
                throw new NullChainException("文件不存在:" + filePath);
            }
            byte[] data = new byte[(int) read.length()];
            try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(read.toPath()))) {
                int read1 = fis.read(data);
                if (read1 != data.length) {
                    throw new NullChainException("读取文件失败:" + filePath);
                }
            }
            loadTaskClass(className, data);
        } catch (Exception e) {
            log.error("加载新任务{}失败:", className, e);
        } finally {
            writeLock.unlock();
        }
    }





    //注册任务 ,内部使用
    public static void __registerTask__(NullTask task) {
        String name = task.getClass().getName();
        //判断是否存在如果存在就异常
        if (taskMap.containsKey(name)) {
            throw new NullChainException("已经存在任务:" + name);
        }
        taskMap.put(name, task);
    }

}
