package com.gitee.huanminabc.nullchain.core;


import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernel;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author huanmin
 * @date 2024/1/11
 */
public interface NullFinality<T>  extends NullKernel<T>, Serializable {

    /**
     * 判断上个任务的值是否是空, true表示空, false不是空
     */
    boolean is();

    /**
     * 判断上一个任务的值是否是空. true表示不是空, false是空
     */
    boolean non();

    /**
     * 获取上一个任务的值, 如果上一个任务是空那么就抛出异常,外部调用者需要捕获异常进行处理为空的情况
     * 这个方法是为了解决某些场景下出现空导致阻断性的问题,需要异常拦截单独处理的场景
     * @return T
     */
    T getSafe() throws NullChainCheckException;

    /**
     * 获取上一个任务的值, 如果上一个任务是空那么就抛出运行时异常,并且打印出链路信息
     * @return T
     */
    T get();

    /**
     * 如果上一个任务返回的是null那么执行之定义异常
     *
     * @param exceptionSupplier 自定义异常
     * @return T
     * @throws X 自定义异常
     */
    <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X;

    //异常消息可以自定义
    T get(String exceptionMessage, Object... args) ;


    //如果上一个任务是空那么就返回null
    T orElseNull() ;

    /**
     * @param defaultValue 如果上一个任务是null那么给一个默认值
     */
    T orElse(T defaultValue);

    T orElse(Supplier<T> defaultValue);



    /**
     * 收集器用于保留节点之间不同类型的值
     * 在很多情况需要查询A的值, 然后利用A的值查询B的值,然后在利用B的值查询C的值,之后还需要同时用A,B,C的值,这个时候就需要用到收集器
     * 一般来说A,B,C是连贯的, 在没有收集器的时候需要每一个都判空然后再进行操作,这样会导致代码的冗余,使用收集器可以减少代码的冗余同时保证了空安全
     * 注意: 收集器只会保留最新类型的值,旧的值会被覆盖
     */
    NullCollect collect();

//
//    //le 小于等于 , 上一个任务的值小于等于obj的值
//    <C extends Comparable<T>> boolean le(C obj);
//
//    //lt 小于 , 上一个任务的值小于obj的值
//    <C extends Comparable<T>> boolean lt(C obj);
//
//    //ge 大于等于 , 上一个任务的值大于等于obj的值
//    <C extends Comparable<T>> boolean ge(C obj);
//
//    //gt 大于 , 上一个任务的值大于obj的值
//    <C extends Comparable<T>> boolean gt(C obj);
//
//    /**
//     * 自定义逻辑判断
//     */
//    boolean logic(Function<T ,Boolean> obj);




    /**
     * 获取上一个任务的值, 如果上一个任务不是空那么就执行action,否则不执行
     */
    void ifPresent(Consumer<? super T> action);


    void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction);


    /**
     * 抓取异常
     */
    void except(Consumer<Throwable> consumer);



//    /**
//     * 获取值的长度, 如果值是null那么返回0
//     * 1. 如果8大数据类型那么返回的是toString 的长度
//     * 2. 如果是集合和数组那么返回的是 length 或者 size的长度
//     * 3. 如果是自定义对象内部有length 或者 size方法那么返回的是length 或者 size的长度
//     */
//    int length();




}