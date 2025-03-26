package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.NullCheck;
import lombok.Getter;
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
    @Setter
    protected boolean isNull; //true 为null ,false 不为null
    @Getter
    protected T value;//当前任务的值
    @Setter
    protected transient StringBuilder linkLog;
    //收集器
    @Setter
    protected transient NullCollect collect;

    //如果序列化的时候发现value是null,那么就不序列化,避免不必要的无用空值传递
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isNull) {
            throw new NullChainException("{} 序列化时发现值是空的", this.linkLog.toString());
        }
        out.defaultWriteObject(); // 序列化非transient字段
    }

    // 反序列化时调用的方法
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // 反序列化非transient字段
        //给linkLog赋值,不然java反序列化后linkLog是null
        this.linkLog = new StringBuilder();
        this.collect = new NullCollect();
    }



    public NullKernelAbstract(){
          this(new StringBuilder(),false,new NullCollect());
    }

    public NullKernelAbstract(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        this.isNull = isNull;
        if (collect == null) {
            collect = new NullCollect();
        }
        this.collect = collect;
        if (linkLog == null) {
            linkLog = new StringBuilder();
        }
        if (linkLog.length() > 0 && this.linkLog != null) {
            this.linkLog.append(linkLog);
        } else {
            this.linkLog = linkLog;
        }
    }

    public NullKernelAbstract(T object, StringBuilder linkLog, NullCollect collect) {
        this(linkLog, false, collect);
        this.value = object;
        collect.add(object);//收集任务的值
    }

    @Override
    public boolean isEmpty() {
        return isNull;
    }
}
