package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.NullCheck;
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
public class NullKernelAbstract<T> implements Serializable, NullCheck {
    private static final long serialVersionUID = 1L;
    protected boolean isNull; //true 为null ,false 不为null
    protected T value;//当前任务的值
    //是否异步 true 开始异步 false 没有开启(默认)
    protected boolean async=false;

    protected transient StringBuilder linkLog;
    //收集器
    protected transient NullCollect collect;
    //任务队列
    protected transient NullTaskList taskList;

    //如果序列化的时候发现value是null,那么就不序列化,避免不必要的无用空值传递
    private void writeObject(ObjectOutputStream out) throws IOException {
        NullChainBase<Object> nullChainBase = this.taskList.runTaskAll();
        if (nullChainBase.isNull){
            throw new NullChainException("{} 序列化时发现值是空的", this.linkLog.toString());
        }else{
            this.value = (T) nullChainBase.value;
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    // 反序列化时调用的方法
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        //给linkLog赋值,不然java反序列化后linkLog是null
        this.taskList = new NullTaskList();
        this.taskList.add((__)-> NullBuild.noEmpty(this.value,new StringBuilder(),new NullCollect(),new NullTaskList()));
        this.linkLog = new StringBuilder();
        this.collect = new NullCollect();
    }



    public NullKernelAbstract(){
          this(new StringBuilder(),false,new NullCollect(),new NullTaskList());
    }

    public NullKernelAbstract(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        this.isNull = isNull;
        if (collect == null) {
            collect = new NullCollect();
        }
        this.collect = collect;
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

    public NullKernelAbstract(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        this(linkLog, false, collect,taskList);
        this.value = object;
        collect.add(object);//收集任务的值
    }

    @Override
    public boolean isEmpty() {
        return isNull;
    }
}
