package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 空链收集器
 */
public class NullCollect implements Serializable {
    private final Map<Class<?>, Object> nullMap;

    public NullCollect() {
        this.nullMap = new HashMap<>();
    }

    //添加内容
    public void add(Object o) {
        if (Null.is(o)) {
            return;
        }
        //如果存在就覆盖, 按照经验来说,在一个链路中只有最后一个是有效的有价值的
        nullMap.put(o.getClass(), o);
    }

    //获取内容
    public <T> NullChain<T> get(Class<T> t) {
        StringBuilder linkLog = new StringBuilder();
        Object o = nullMap.get(t);
        if (Null.is(o)) {
            linkLog.append("NullCollect.get?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append("NullCollect.get->");
        return NullBuild.noEmpty((T)o, linkLog,new NullCollect());
    }

    public boolean isEmpty() {
        return nullMap.isEmpty();
    }

    //多个类型都不能为空
    public boolean notEmpty(Class<?>... classes) {
        for (Class<?> aClass : classes) {
            Object o = nullMap.get(aClass);
            if (Null.is(o)) {
                return false;
            }
        }
        return true;
    }
    //只要有一个为空就返回true
    public boolean anyEmpty(Class<?>... classes) {
        for (Class<?> aClass : classes) {
            Object o = nullMap.get(aClass);
            if (Null.is(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NullChainCollect"+ nullMap;
    }
}
