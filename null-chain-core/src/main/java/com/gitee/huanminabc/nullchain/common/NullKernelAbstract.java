package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-03-21 12:53
 **/
public class NullKernelAbstract<T> implements NullKernel<T>, Serializable, NullCheck {
    private static final long serialVersionUID = 1L;
    protected transient StringBuilder linkLog;

    //任务队列
    protected  NullTaskList taskList;

    //如果序列化的时候发现value是null,那么就不序列化,避免不必要的无用空值传递
    private void writeObject(ObjectOutputStream out) throws IOException {
       NullTaskList.NullNode nullChainBase = this.taskList.runTaskAll();
        if (nullChainBase.isNull){
            throw new NullChainException("{} 序列化时发现值是空的", this.linkLog.toString());
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    // 反序列化时调用的方法
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        //给linkLog赋值,不然java反序列化后linkLog是null
        if (this.taskList.lastResult==null){
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
            linkLog.append("async->");
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
            linkLog.append("async->");
            NullTaskList.NullNode<Object> objectNullNode = NullBuild.noEmpty(value);
            objectNullNode.async = true; //设置为异步
            return objectNullNode;

        });
        return  NullBuild.busy(this);
    }
}
