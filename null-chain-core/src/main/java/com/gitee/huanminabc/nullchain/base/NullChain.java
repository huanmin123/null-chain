package com.gitee.huanminabc.nullchain.base;

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
 * 支持序列化网络传输
 *
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullChain<T> extends NullConvert<T> {
    /**
     * 判断是否为空,如果为空那么就返回空链
     *
     * @param function
     * @param <U>
     * @return
     */
    <U> NullChain<T> of(NullFun<? super T, ? extends U> function);

    /**
     * 任意一个是空那么就返回空链
     */
    <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... function);


    /**
     * 用于决定是否还需要继续执行,或者改变终结节点的结果,   返回true那么就继续执行, 返回false那么就返回空链
     */
     NullChain<T> ifGo(NullFun<? super T, Boolean> function);

    /**
     * 如果前一个节点是空,那么打断执行,抛出异常, 这样可以定制节点的异常,而不是等待到结束统一处理
     * @param exceptionSupplier
     * @return
     * @param <X>
     */
    <X extends RuntimeException> NullChain<T> check(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * 如果是空继续往下走, 但是不会用到这个值 , 也不会出现空指针, 只是一种并且的补充
     * 比如 一个对象内 a b c 都不是空 并且 d是空 那么才满足条件 , 但是在实际处理的时候不会用到d , 只是一种逻辑上的处理
     * 这个是有歧义的和of 是一种互补关系
     */
    <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function);



    /**
     * 在上一个任务不是空的情况下执行,不改变对象类型不改变对象内容, 就是一个空白节点无状态的不影响链路的数据
     */

    NullChain<T> then(Runnable function);

    NullChain<T> then(Consumer<? super T> function);

    NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function);

    /**
     * 获取上一个任务的内容,如果上一个任务为空,那么就返回空链
     */
    <U> NullChain<U> map(NullFun<? super T, ? extends U> function);

    <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function);


    /**
     * 上一个任务的内容返回的是NullChain<?>  那么就返回 ?  这样就可以继续操作了 , 通俗来说就是解包
     */
    <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function);
    <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function);


    /**
     * 如果上一个任务为空,那么就执行supplier返回新的任务,如果不为空,那么就返回当前任务
     */
    NullChain<T> or(Supplier<? extends T> supplier);

    NullChain<T> or(T defaultValue);


    /**
     * @param task 任务的类
     * @param params 任务的参数
     */
    <R> NullChain<R> task(Class<? extends NullTask<T,R>> task, Object... params);



    /**
     * @param classPath 任务的类路径
     * @param params    任务的参数
     */
    NullChain<?> task(String classPath, Object... params);


    /**
     * 多任务并发执行, 任务之间没有关联, 任务执行完毕后,会将结果合并  不支持同类型任务
     * @param nullGroupTask   任务组
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是任务的结果
     */
    NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask);

    NullChain<NullMap<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName);





    /**
     * 执行单个nf脚本, 返回的数据是不能确认类型的所以需要后面自己转换
     * @param nullTaskInfo 任务信息
     * @return 如果内部脚本并发绑定变量然后返回这个变量, 那么这个类型一定是 NullChain<Map<String, NullChain<Object>>> 类型, 否则就是NullChain<Object>
     */

    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo);
    NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName);
    NullChain<?> nfTask(String nfContext, Object... params);

    /**
     * 多脚本同时并发执行
     * @param threadFactoryName 线程池名称
     * @param nullGroupNfTask 脚本组
     * @return 返回的是一个NullMap<String, Object>  key是任务的taskClassName, value是脚本的结果
     */
    NullChain<NullMap<String, Object>> nfTasks(NullGroupNfTask nullGroupNfTask,String threadFactoryName);


}
