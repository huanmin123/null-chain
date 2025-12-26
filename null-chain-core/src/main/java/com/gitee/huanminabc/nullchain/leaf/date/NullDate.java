package com.gitee.huanminabc.nullchain.leaf.date;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;

/**
 * Null日期操作接口 - 提供空值安全的日期处理功能
 * 
 * <p>该接口提供了对日期时间操作的空值安全封装，支持各种日期格式的转换、偏移和比较操作。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>支持的时间类型：</h3>
 * <ul>
 *   <li>Date - Java标准日期类型</li>
 *   <li>LocalDate - 本地日期</li>
 *   <li>LocalDateTime - 本地日期时间</li>
 *   <li>10位时间戳（数字或字符串）</li>
 *   <li>13位时间戳（数字或字符串）</li>
 *   <li>格式化的时间格式字符串</li>
 * </ul>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>日期格式化：将日期转换为指定格式的字符串</li>
 *   <li>日期偏移：对日期进行时间偏移操作</li>
 *   <li>日期比较：比较两个日期的大小关系</li>
 * </ul>
 * 
 * @param <T> 日期值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullKernel 内核接口
 */
public interface NullDate<T> extends NullChain<T>, NullKernel<T> {

    /**
     * 日期格式化操作 - 将日期转换为指定格式的字符串
     * 
     * <p>该方法用于将当前日期值格式化为指定格式的时间字符串。
     * 支持多种日期类型的格式化，包括Date、LocalDate、LocalDateTime、时间戳等。</p>
     * 
     * @param dateFormatEnum 日期格式枚举
     * @return 格式化后的日期字符串
     * 
     * @example
     * <pre>{@code
     * String dateStr = Null.of(new Date())
     *     .dateFormat(DateFormatEnum.YYYY_MM_DD_HH_MM_SS)  // 格式化为 yyyy-MM-dd HH:mm:ss
     *     .orElse("格式化失败");
     * }</pre>
     */
    NullDate<String> dateFormat(DateFormatEnum dateFormatEnum);

    /**
     * 日期偏移操作 - 对日期进行时间偏移
     * 
     * <p>该方法用于对当前日期值进行时间偏移操作，支持向前或向后偏移指定的时间单位。
     * 支持多种日期类型的时间偏移，包括Date、LocalDate、LocalDateTime、时间戳等。</p>
     * 
     * @param controlEnum 偏移方向（向前或向后）
     * @param num 偏移量
     * @param timeEnum 时间单位（年、月、日、时、分、秒等）
     * @return 偏移后的日期
     * 
     * @example
     * <pre>{@code
     * Date futureDate = Null.of(new Date())
     *     .dateOffset(DateOffsetEnum.ADD, 7, TimeEnum.DAY)  // 向前偏移7天
     *     .orElse(new Date());
     * }</pre>
     */
    NullDate<T> dateOffset(DateOffsetEnum controlEnum, int num, TimeEnum timeEnum);
    
    /**
     * 日期偏移操作 - 对日期进行时间偏移（默认偏移量为1）
     * 
     * <p>该方法用于对当前日期值进行时间偏移操作，偏移量默认为1。
     * 支持多种日期类型的时间偏移，包括Date、LocalDate、LocalDateTime、时间戳等。</p>
     * 
     * @param controlEnum 偏移方向（向前或向后）
     * @param timeEnum 时间单位（年、月、日、时、分、秒等）
     * @return 偏移后的日期
     * 
     * @example
     * <pre>{@code
     * Date tomorrow = Null.of(new Date())
     *     .dateOffset(DateOffsetEnum.ADD, TimeEnum.DAY)  // 向前偏移1天
     *     .orElse(new Date());
     * }</pre>
     */
    NullDate<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum);

    /**
     * 日期比较操作 - 比较两个日期的大小关系
     * 
     * <p>该方法用于比较当前日期值与指定日期的大小关系。
     * 支持多种日期类型的比较，包括Date、LocalDate、LocalDateTime、时间戳等。</p>
     * 如果需要比较到日期后的时间那么请保证单位一致性: 比如都是yyyy-MM-dd HH:mm:ss
     * 不能有一个是yyyy-MM-dd 另一个是yyyy-MM-dd HH:mm:ss
     * 这样就会导致因为单位不一致，比较的结果不准确
     * <p><strong>返回值说明：</strong></p>
     * <ul>
     *   <li>1：当前日期大于指定日期</li>
     *   <li>0：当前日期等于指定日期</li>
     *   <li>-1：当前日期小于指定日期</li>
     * </ul>
     * 
     * @param date 要比较的日期对象
     * @return 比较结果（1、0或-1）
     * 
     * @example
     * <pre>{@code
     * Integer result = Null.of(new Date())
     *     .dateCompare(new Date(System.currentTimeMillis() + 86400000))  // 比较与明天的关系
     *     .orElse(0);
     * 
     * if (result > 0) {
     *     System.out.println("当前日期大于比较日期");
     * } else if (result < 0) {
     *     System.out.println("当前日期小于比较日期");
     * } else {
     *     System.out.println("两个日期相等");
     * }
     * }</pre>
     */
    NullDate<Integer> dateCompare(Object date);

    /**
     * 日期间隔操作 - 计算两个日期之间的间隔
     * 1. 不包括开始时间, 包括结束时间
     * 2. 日的比如21~25之间相差4天,如果需要算开始时间那么需要+1
     * 3. 如果计算周期比如01~25之间经过了多少周那么就是3 因为第四周没有走完还差2天
     * 4. 所以在有些时候需要完整的周期忽略未走完的需要+1
     *
     * 注意时间单位的精确度(建议对比的时候时间格式是一致的避免导致对比结果出问题)：
     * 传入含时间的对象（LocalDateTime/ZonedDateTime）：
     *      会精确对比年、月、日、时、分、秒、毫秒等所有维度，计算的是 “完整单位间隔”—— 比如要凑够 24 小时才算 1 天，凑够 60 分钟才算 1 小时。
     * 传入仅日期的对象（LocalDate）：
     *      只对比年、月、日，忽略时间，只要日期跨 1 天，天数差就为 1（比如 12-01 到 12-02，不管时间是几点，天数差都是 1）。
     * 传入仅时间的对象（LocalTime）：
     *      只对比时、分、秒、毫秒，无法对比日期相关单位（如天、周、月）。
     *
     * <p>该方法用于计算当前日期值与指定日期之间的间隔，根据指定的时间单位返回间隔数。
     * 支持多种日期类型的计算，包括Date、LocalDate、LocalDateTime、时间戳等。</p>
     * 
     * <p><strong>返回值说明：</strong></p>
     * <ul>
     *   <li>返回两个日期之间的间隔数（绝对值）</li>
     *   <li>例如，如果 timeEnum 是 DAYS，则返回间隔多少天</li>
     *   <li>如果 timeEnum 是 HOURS，则返回间隔多少小时</li>
     * </ul>
     * 
     * @param date 要比较的日期对象
     * @param timeEnum 时间单位（年、月、日、时、分、秒等）
     * @return 两个日期之间的间隔数
     * 
     * @example
     * <pre>{@code
     * Long days = Null.of(new Date())
     *     .dateBetween(new Date(System.currentTimeMillis() + 86400000 * 7), TimeEnum.DAYS)  // 计算与7天后的间隔
     *     .orElse(0L);
     * System.out.println("间隔天数: " + days);  // 输出: 间隔天数: 7
     * 
     * Long hours = Null.of(new Date())
     *     .dateBetween(new Date(System.currentTimeMillis() + 3600000 * 24), TimeEnum.HOURS)  // 计算与24小时后的间隔
     *     .orElse(0L);
     * System.out.println("间隔小时数: " + hours);  // 输出: 间隔小时数: 24
     * }</pre>
     */
    NullDate<Long> dateBetween(Object date, TimeEnum timeEnum);
}
