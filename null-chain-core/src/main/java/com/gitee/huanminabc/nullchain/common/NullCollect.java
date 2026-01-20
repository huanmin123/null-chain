package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Null链收集器 - 提供链式操作的收集功能
 *
 * <p>该类提供了收集和管理Null链操作结果的功能，支持将不同类型的链操作结果收集到统一的容器中。
 * 通过类型安全的收集机制，为复杂的链式操作提供结果管理能力。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>结果收集：收集各种类型的链操作结果</li>
 *   <li>类型管理：通过Class类型管理收集的结果</li>
 *   <li>结果获取：提供类型安全的结果获取方法</li>
 *   <li>序列化支持：支持序列化传输</li>
 * </ul>
 *
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>序列化支持：支持序列化传输</li>
 *   <li>结果管理：提供统一的结果管理机制</li>
 * </ul>
 *
 * @author huanmin
 * @version 1.1.1
 * @see Serializable 序列化接口
 * @since 1.0.0
 */
public class NullCollect implements Serializable {
    /**
     * 存储收集的结果，key为Class类型，value为对应的对象
     */
    private final Map<Class<?>, Object> nullMap;

    /**
     * 创建一个新的收集器
     * 
     * <p>初始化内部的Map用于存储收集的结果。</p>
     */
    public NullCollect() {
        this.nullMap = new HashMap<>();
    }

    /**
     * 添加内容到收集器
     * 
     * <p>将对象添加到收集器中，以对象的Class类型作为key。
     * 如果对象为null则忽略。如果已存在相同类型的对象，则覆盖原有对象。
     * 按照经验来说，在一个链路中只有最后一个是有效的有价值的。</p>
     * 
     * @param o 要添加的对象，如果为null则忽略
     */
    protected void add(Object o) {
        if (o == null) {
            return;
        }
        //如果存在就覆盖, 按照经验来说,在一个链路中只有最后一个是有效的有价值的
        nullMap.put(o.getClass(), o);
    }

    /**
     * 根据类型获取收集的内容
     * 
     * <p>根据指定的Class类型从收集器中获取对应的对象，并返回NullChain包装的结果。
     * 如果找不到对应类型的对象，则返回空的NullChain。</p>
     * 
     * @param <T> 要获取的对象类型
     * @param t 对象的Class类型
     * @return NullChain包装的对象，如果不存在则返回空链
     */
    public <T> NullChain<T> get(Class<T> t) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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

    /**
     * 判断收集器是否为空
     * 
     * @return 如果收集器为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return nullMap.isEmpty();
    }
}
