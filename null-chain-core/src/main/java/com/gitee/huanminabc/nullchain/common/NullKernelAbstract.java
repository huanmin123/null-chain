package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
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
 * Null内核抽象基类
 * 
 * @param <T> 内核处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
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
        this.linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
    }



    public NullKernelAbstract(){
          this(new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY),new NullTaskList());
    }

    public NullKernelAbstract(StringBuilder linkLog, NullTaskList taskList) {
        if (taskList==null){
            taskList = new NullTaskList();
        }
        this.taskList = taskList;
        if (linkLog == null) {
            linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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
