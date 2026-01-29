package com.gitee.huanminabc.nullchain.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    /**
     * 链式操作的日志记录器，用于记录操作过程
     */
    protected transient StringBuilder linkLog;

    /**
     * 任务队列，存储要执行的任务链
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public NullTaskList taskList;

    /**
     * 序列化时调用的方法
     * 
     * <p>在序列化之前，先执行任务链获取最终值。如果最终值为null，
     * 则抛出异常，避免不必要的无用空值传递。</p>
     * 
     * @param out 对象输出流
     * @throws IOException 如果序列化过程中发生IO错误
     * @throws NullChainException 如果最终值为null
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
       NullTaskList.NullNode nullChainBase = this.taskList.runTaskAll();
        if (nullChainBase.isNull){
            throw new NullChainException(SERIALIZE_NULL_VALUE, this.linkLog.toString());
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    /**
     * 反序列化时调用的方法
     * 
     * <p>在反序列化后，初始化transient字段，包括任务列表和日志记录器。</p>
     * 
     * @param in 对象输入流
     * @throws IOException 如果反序列化过程中发生IO错误
     * @throws ClassNotFoundException 如果找不到类
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        // 初始化反序列化后的临时状态
        if (this.taskList == null) {
            this.taskList = new NullTaskList();
        }
        this.linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
    }

    /**
     * 创建一个新的Null内核实例
     * 
     * <p>使用默认的日志记录器和任务列表初始化。</p>
     */
    public NullKernelAbstract(){
          this(new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY),new NullTaskList());
    }

    /**
     * 创建一个新的Null内核实例
     * 
     * <p>使用指定的日志记录器和任务列表初始化。如果参数为null，则使用默认值。
     * 因为是链式操作，需要续上日志记录器。</p>
     * 
     * @param linkLog 链式操作的日志记录器，如果为null则创建新的
     * @param taskList 任务列表，如果为null则创建新的
     */
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

    /**
     * 检查当前值是否为空
     * 
     * <p>执行任务链获取最终值，判断是否为空。</p>
     * 
     * @return 如果值为null返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return taskList.runTaskAll().isNull;
    }


}
