package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Null内核抽象基类 - 提供Null链框架的核心功能
 * 
 * <p>这是所有Null链实现类的抽象基类，提供了核心的序列化、任务管理和异步执行功能。
 * 该类实现了Serializable接口，支持网络传输和持久化存储。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>序列化支持：自定义序列化逻辑，避免空值传递</li>
 *   <li>任务管理：管理链式操作的任务队列</li>
 *   <li>异步执行：支持异步操作和线程池管理</li>
 *   <li>日志追踪：维护操作日志链</li>
 *   <li>空值检查：提供空值检查功能</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>序列化安全：transient字段在序列化时会被正确处理</li>
 *   <li>线程安全：支持多线程环境下的异步操作</li>
 *   <li>内存优化：避免不必要的空值序列化</li>
 *   <li>扩展性：为子类提供完整的扩展基础</li>
 * </ul>
 * 
 * @param <T> 内核处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 * @see NullCheck 空值检查接口
 * @see Serializable 序列化接口
 */
public class NullKernelAbstract<T> implements NullKernel<T>, Serializable, NullCheck {
    private static final long serialVersionUID = 1L;
    protected transient StringBuilder linkLog;

    //任务队列
    protected  NullTaskList taskList;

    //如果序列化的时候发现value是null,那么就不序列化,避免不必要的无用空值传递
    private void writeObject(ObjectOutputStream out) throws IOException {
       NullTaskList.NullNode nullChainBase = this.taskList.runTaskAll();
        if (nullChainBase.isNull){
            throw new NullChainException(SERIALIZE_NULL_VALUE, this.linkLog.toString());
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    // 反序列化时调用的方法
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        // 初始化反序列化后的临时状态
        if (this.taskList == null) {
            this.taskList = new NullTaskList();
        }
        this.linkLog = new StringBuilder();
    }



    public NullKernelAbstract(){
          this(new StringBuilder(),new NullTaskList());
    }

    public NullKernelAbstract(StringBuilder linkLog, NullTaskList taskList) {
        if (taskList==null){
            taskList = new NullTaskList();
        }
        this.taskList = taskList;
        if (linkLog == null) {
            linkLog = new StringBuilder();
        }
        if (linkLog.length() > 0 && this.linkLog != null) {
            this.linkLog.append(linkLog);
        } else {
            this.linkLog = linkLog;
        }
    }


    @Override
    public boolean isEmpty() {
        return taskList.runTaskAll().isNull;
    }


    //同步转异步
    public NullChain<T> async() {
        this.taskList.add((value)->{
            linkLog.append(ASYNC_ARROW);
            NullTaskList.NullNode<Object> objectNullNode = NullBuild.noEmpty(value);
            objectNullNode.async= true; //设置为异步
            return objectNullNode;
        });
        return  NullBuild.busy(this);
    }
    //带线程池的同步转异步
    public NullChain<T> async(String threadFactoryName) throws NullChainException {
        this.taskList.add((value)->{
            ThreadFactoryUtil.addExecutor(threadFactoryName);
            taskList.setCurrentThreadFactoryName(threadFactoryName);
            linkLog.append(ASYNC_ARROW);
            NullTaskList.NullNode<Object> objectNullNode = NullBuild.noEmpty(value);
            objectNullNode.async = true; //设置为异步
            return objectNullNode;

        });
        return  NullBuild.busy(this);
    }
}
