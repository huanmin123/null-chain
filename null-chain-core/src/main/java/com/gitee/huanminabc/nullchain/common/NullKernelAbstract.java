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
public class NullKernelAbstract implements  Serializable, NullCheck {
    private static final long serialVersionUID = 1L;
    protected transient StringBuilder linkLog;

    //任务队列
    public   NullTaskList taskList;

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
        //因为是链式需要续上日志
        this.linkLog = linkLog;
    }

    @Override
    public boolean isEmpty() {
        return taskList.runTaskAll().isNull;
    }


}
