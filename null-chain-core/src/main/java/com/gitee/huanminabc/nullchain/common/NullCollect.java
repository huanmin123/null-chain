package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;

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
    protected void add(Object o) {
        //如果存在就覆盖, 按照经验来说,在一个链路中只有最后一个是有效的有价值的
        nullMap.put(o.getClass(), o);
    }

    //获取内容
    public <T> NullChain<T> get(Class<T> t) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        Object o = nullMap.get(t);
        nullTaskList.add((__) -> {
            if (Null.is(o)) {
                linkLog.append(NULL_COLLECT_GET_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_COLLECT_GET_ARROW);
            return NullBuild.noEmpty(o);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }

    public boolean isEmpty() {
        return nullMap.isEmpty();
    }
}
