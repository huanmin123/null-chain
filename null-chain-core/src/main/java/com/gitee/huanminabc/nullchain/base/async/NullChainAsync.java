package com.gitee.huanminabc.nullchain.base.async;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NullChainAsync 不支持序列化
 *
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullChainAsync<T> extends NullConvertAsync<T> {

    /**
     * 判断是否为空,如果为空那么就返回空链 ,包括空字符串
     *
     * @param function
     * @param <U>
     * @return
     */
    <U> NullChainAsync<T> of(NullFun<? super T, ? extends U> function);

    <U> NullChainAsync<T> ofAny(NullFun<? super T, ? extends U>... function);


    /**
     * 用于决定是否还需要继续执行,或者改变终结节点的结果,   返回true那么就继续执行, 返回false那么就返回空链
     */
    NullChainAsync<T> ifGo(NullFun<? super T, Boolean> function);

    /**
     * 如果前一个节点是空,那么打断执行,抛出异常, 这样可以定制节点的异常,而不是等待到结束统一处理
     * @param exceptionSupplier
     * @return
     * @param <X>
     */
    <X extends RuntimeException> NullChainAsync<T> check(Supplier<? extends X> exceptionSupplier) throws X;


    /**
     * 在上一个任务不是空的情况下执行,不改变对象类型不改变对象内容, 就是一个空白节点无状态的不影响链路的数据
     *
     * @param function
     */
    NullChainAsync<T> then(Runnable function);

    NullChainAsync<T> then(Consumer<? super T> function);

    NullChainAsync<T> then2(NullConsumer2<NullChain<T>, ? super T> function);


    /**
     * 获取上一个任务的内容,如果上一个任务为空,那么就返回空链,不会执行下一个任务也不会抛出异常
     *
     * @param function
     * @param <U>
     * @return
     */
    <U> NullChainAsync<U> map(NullFun<? super T, ? extends U> function);

    <U> NullChainAsync<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function);


    /**
     * 上一个任务的内容返回的是NullChain<?> 那么就返回 ?  这样就可以继续操作了  , 通俗来说就是解包
     * @param function
     * @param <U>
     * @return
     */
    <U> NullChainAsync<U> unChain(NullFun<? super T, ? extends NullChain<U>> function);
    <U> NullChainAsync<U> unOptional(NullFun<? super T, ? extends Optional<U>> function);


    /**
     * 如果上一个任务为空,那么就执行supplier,如果不为空,那么就返回当前任务
     *
     * @param supplier
     * @return
     */
    NullChainAsync<T> or(Supplier<T> supplier);

    NullChainAsync<T> or(T defaultValue);

    /**
     * @param task
     * @param params 任务的参数
     * @return
     */
    <R> NullChainAsync<R> task(Class<? extends NullTask<T,R>> task, Object... params);


    /**
     * @param classPath
     * @param params    任务的参数
     * @return
     */
    NullChainAsync<?> task(String classPath, Object... params);

    /**
     * 多任务并发执行, 任务之间没有关联, 任务执行完毕后,会将结果合并  不支持同类型任务
     * @param nullGroupTask   任务组
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是任务的结果
     */
    NullChainAsync<NullMap<String, Object>> task(NullGroupTask nullGroupTask);

    NullChainAsync<NullMap<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName);





    /**
     * 执行单个nf脚本, 返回的数据是不能确认类型的所以需要后面自己转换
     * @param nullTaskInfo 任务信息
     * @return 如果内部脚本并发绑定变量然后返回这个变量, 那么这个类型一定是 NullChain<Map<String, NullChain<Object>>> 类型, 否则就是NullChain<Object>
     */

    NullChainAsync<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo);
    NullChainAsync<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName);
    NullChainAsync<?> nfTask(String nfContext, Object... params);
    /**
     * 多脚本同时并发执行
     * @param threadFactoryName
     * @param nullGroupNfTask
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是脚本的结果
     */
    NullChainAsync<NullMap<String, Object>> nfTasks( NullGroupNfTask nullGroupNfTask,String threadFactoryName);


}
