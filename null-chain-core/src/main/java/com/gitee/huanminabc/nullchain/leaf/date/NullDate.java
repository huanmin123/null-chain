package com.gitee.huanminabc.nullchain.leaf.date;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;

/**
 * 简要描述
 *
 * @Author: huanmin
 * @Date: 2025/5/28 00:25
 * @Version: 1.0
 * @Description: 文件作用详细描述....
 */
public interface NullDate<T> extends NullChain<T> {

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
    NullDateBase<String> dateFormat(DateFormatEnum dateFormatEnum);

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
    NullDateBase<T> dateOffset(DateOffsetEnum controlEnum, int num, TimeEnum timeEnum);
    NullDateBase<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum);

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
    NullDateBase<Integer> dateCompare(Object date);
}
