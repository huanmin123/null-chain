package com.gitee.huanminabc.nullchain.base.sync;

import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.http.sync.OkHttpChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;


/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 **/
public interface NullTools<T> extends NullFinality<T> {

    /**
     * 将对象转换为json字符串
     *
     * @return
     * @
     */
    NullChain<String> json();

    /**
     * 将json字符串转换为对象
     *
     * @param uClass
     * @param <U>
     * @return
     */
    <U> NullChain<U> json(Class<U> uClass);

    /**
     * 将json字符串转换为对象
     * @param uClass
     * @param <U>
     * @return
     */
    <U> NullChain<U> json(U uClass);


    /**
     * 复制上一个任务的值,返回新的任务(浅拷贝)
     * @return
     */
    NullChain<T> copy();

    /**
     * 复制上一个任务的值,返回新的任务(复制深拷贝)
     * 注意: 需要拷贝的类必须实现Serializable接口 , 包括内部类 ,否则会报错:NotSerializableException
     * @return
     */
    NullChain<T> deepCopy();

    //提取自己需要的字段,返回新的对象
     <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper);


    /**
     * 将时间格式化为指定格式的时间字符串
     * 支持时间类型:
     * 1. Date
     * 2. LocalDate
     * 3. LocalDateTime
     * 4. 10位时间戳（数字或者字符串）
     * 5. 13位时间戳（数字或者字符串）
     * 6. 格式化的时间格式字符串({@link DateFormatEnum})
     *
     * @param dateFormatEnum
     */
    NullChain<String> dateFormat(DateFormatEnum dateFormatEnum);

    /**
     * 将时间进行偏移
     * 支持时间类型:
     * 1. Date
     * 2. LocalDate
     * 3. LocalDateTime
     * 4. 10位时间戳（数字或者字符串）
     * 5. 13位时间戳（数字或者字符串）
     * 6. 格式化的时间格式字符串({@link DateFormatEnum})
     *
     * @param controlEnum 偏移方向
     * @param num         偏移量
     * @param timeEnum    时间单位
     */
    NullChain<T> dateOffset(DateOffsetEnum controlEnum, int num, TimeEnum timeEnum);
    NullChain<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum);

    /**
     * 将时间进行比较, 如果节点的时间大于date,返回1,等于返回0,小于返回-1
     * 支持时间类型:
     * 1. Date
     * 2. LocalDate
     * 3. LocalDateTime
     * 4. 10位时间戳（数字或者字符串）
     * 5. 13位时间戳（数字或者字符串）
     * 6. 格式化的时间格式字符串({@link DateFormatEnum})
     */
    NullChain<Integer> dateCompare(Object date);


    /**
     * 请求http, 参数由上一个任务传递
     * @param url 请求地址
     * @return
     */
    OkHttpChain http(String url);

    /**
     * 请求http
     *
     * @param httpName http的名称 , 不同的业务可以用不同的实例
     * @param url      请求地址
     * @return
     */
    OkHttpChain http(String httpName, String url);



    /**
     * 自定义工具
     * @param tool
     * @param <R>
     * @return
     * @
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool);
    /**
     * @param params 工具的参数
     * @return
     * @param <R>
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params);


}
