package com.gitee.huanminabc.nullchain.base.async;

import com.gitee.huanminabc.nullchain.base.async.stream.NullStreamAsync;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.http.async.OkHttpAsyncChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;

public interface NullToolsAsync<T> extends NullFinalityAsync<T> {

    /**
     * 将对象转换为json字符串
     * @return
     * @
     */
    NullChainAsync<String> json();

    /**
     * 将json字符串转换为对象
     * @param uClass
     * @return
     * @param <U>
     */
    <U>NullChainAsync<U> json(Class<U> uClass);

    /**
     * 将json字符串转换为对象
     * @param uClass
     * @param <U>
     * @return
     */
    <U>NullChainAsync<U> json(U uClass);


    /**
     * 复制上一个任务的值,返回新的任务(浅拷贝)
     *
     * @return
     */
    NullChainAsync<T> copy();

    /**
     * 复制上一个任务的值,返回新的任务(复制深拷贝)
     * 注意: 需要拷贝的类必须实现Serializable接口 , 包括内部类 ,否则会报错:NotSerializableException
     * @return
     */
    NullChainAsync<T> deepCopy();


    /**
     * 将部分值提取然后产生一个新值, 意思就是我有一个UserEntity,我想要取出UserEntity中的name,age,返回一个新的UserEntity只有name,age
     *
     * @param mapper
     * @param <U>
     * @return
     */
    <U> NullChainAsync<T> pick(NullFun<? super T, ? extends U>... mapper);

    /**
     * 将时间格式化为指定格式的时间字符串
     * 支持时间类型:
     * 1. Date
     * 2. LocalDate
     * 3. LocalDateTime
     * 4. 10位时间戳（数字或者字符串）
     * 5. 13位时间戳（数字或者字符串）
     * 6. 格式化的时间格式字符串({@link DateFormatEnum})
     * @param dateFormatEnum
     */
    NullChainAsync<String> dateFormat(DateFormatEnum dateFormatEnum) ;

    /**
     * 将时间进行偏移
     * 支持时间类型:
     * 1. Date
     * 2. LocalDate
     * 3. LocalDateTime
     * 4. 10位时间戳（数字或者字符串）
     * 5. 13位时间戳（数字或者字符串）
     * 6. 格式化的时间格式字符串({@link DateFormatEnum})
     * @param offsetEnum 偏移方向
     * @param num 偏移量
     * @param timeEnum 时间单位
     */
    NullChainAsync<T> dateOffset(DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum);
    NullChainAsync<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum);

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
    NullChainAsync<Integer> dateCompare(Object date);



    /**
     * 请求http
     * @param url 请求地址
     * @return
     */
    OkHttpAsyncChain http(String url);

    /**
     * 请求http
     * @param httpName http的名称 , 不同的业务可以用不同的实例
     * @param url 请求地址
     * @return
     */
    OkHttpAsyncChain http(String httpName, String url);


    /**
     * 自定义工具 , 入参的类必须在实现里面把泛型的T和R写为具体的类
     * @param tool
     * @return
     * @param <R>
     * @
     */
    <R> NullChainAsync<R> tool(Class<? extends NullTool<T,R>>  tool) ;
    <R> NullChainAsync<R> tool(Class<? extends NullTool<T,R>>  tool, Object... params) ;

}
