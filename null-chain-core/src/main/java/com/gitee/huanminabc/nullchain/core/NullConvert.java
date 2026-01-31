package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.jcommon.enums.DateOffsetEnum;
import com.gitee.huanminabc.jcommon.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.jcommon.enums.DateFormatEnum;

/**
 * Null类型转换接口 - 提供类型转换功能
 * 
 * <p>该接口定义了Null链的类型转换操作，主要用于将Object类型转换为具体的类型。
 * 它扩展了工作流接口，提供了类型安全的转换能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>类型转换：将Object类型转换为具体类型</li>
 *   <li>JSON操作：对象与JSON字符串之间的相互转换</li>
 *   <li>日期操作：日期格式化、偏移、比较和间隔计算</li>
 *   <li>对象复制：浅拷贝、深拷贝和字段提取</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值处理：处理转换过程中的空值情况</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>当某个操作导致类型推导为Object时</li>
 *   <li>需要将Object类型转换为具体类型时</li>
 *   <li>类型擦除后的类型恢复</li>
 *   <li>JSON序列化和反序列化</li>
 *   <li>日期时间处理和格式化</li>
 *   <li>对象复制和字段提取</li>
 * </ul>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>类型转换不支持跨类型转换（如String转Integer）</li>
 *   <li>主要用于类型恢复和类型断言</li>
 *   <li>等效于强制类型转换：User user = (User)obj</li>
 * </ul>
 * 
 * @param <T> 转换前的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.2
 * @see NullWorkFlow 工作流接口
 */
public interface NullConvert<T> extends NullWorkFlow<T> {
    
    /**
     * 将当前值转换为指定类型
     * 
     * <p>该方法用于将当前值转换为指定的类型。主要用于类型恢复场景，
     * 当某个操作导致类型推导为Object时，可以使用此方法恢复具体类型。</p>
     * 
     * <p><strong>注意：</strong>此方法不支持跨类型转换（如String转Integer），
     * 主要用于类型恢复和类型断言，等效于强制类型转换。</p>
     * 
     * @param <U> 目标类型
     * @param uClass 目标类型的Class对象
     * @return 转换后的Null链
     * 
     * @example
     * <pre>{@code
     * // 类型恢复示例
     * User user = Null.of(someObject)
     *     .type(User.class)  // 将Object转换为User类型
     *     .orElse(new User());  // 如果转换失败，返回默认用户对象
     * }</pre>
     */
    <U> NullChain<U> type(Class<U> uClass);

    /**
     * 将当前值转换为指定类型（通过实例）
     * 
     * <p>该方法通过传入目标类型的实例来确定转换类型。
     * 主要用于类型恢复场景，当某个操作导致类型推导为Object时使用。</p>
     * 
     * @param <U> 目标类型
     * @param uClass 目标类型的实例（用于类型推断）
     * @return 转换后的Null链
     * 
     * @example
     * <pre>{@code
     * // 通过实例进行类型转换
     * User user = Null.of(someObject)
     *     .type(new User())  // 通过User实例确定类型
     *     .orElse(new User());  // 如果转换失败，返回默认用户对象
     * }</pre>
     */
    <U> NullChain<U> type(U uClass);

    /**
     * 对象转JSON操作 - 将Java对象转换为JSON字符串
     * 
     * <p>该方法用于将当前Java对象转换为JSON字符串格式。
     * 支持各种Java对象的序列化，包括POJO、集合、数组等。</p>
     * 
     * @return 包含JSON字符串的Null链
     * 
     * @example
     * <pre>{@code
     * String jsonStr = Null.of(user)
     *     .json()  // 将User对象转换为JSON字符串
     *     .orElse("{}");
     * }</pre>
     */
    NullChain<String> json();

    /**
     * JSON转对象操作 - 将JSON字符串转换为指定类型的对象
     * 
     * <p>该方法用于将当前JSON字符串转换为指定类型的Java对象。
     * 通过Class对象指定目标类型，确保类型安全。</p>
     * 
     * @param <U> 目标对象类型
     * @param uClass 目标类型的Class对象
     * @return 包含转换后对象的Null链
     * 
     * @example
     * <pre>{@code
     * User user = Null.of(jsonString)
     *     .fromJson(User.class)  // 将JSON字符串转换为User对象
     *     .orElse(new User());
     * }</pre>
     */
    <U> NullChain<U> fromJson(Class<U> uClass);

    /**
     * JSON转对象操作 - 将JSON字符串转换为指定类型的对象（通过实例推断类型）
     * 
     * <p>该方法用于将当前JSON字符串转换为指定类型的Java对象。
     * 通过传入目标类型的实例来推断类型，确保类型安全。</p>
     * 
     * @param <U> 目标对象类型
     * @param uClass 目标类型的实例（用于类型推断）
     * @return 包含转换后对象的Null链
     * 
     * @example
     * <pre>{@code
     * User user = Null.of(jsonString)
     *     .fromJson(new User())  // 通过User实例推断类型
     *     .orElse(new User());
     * }</pre>
     */
    <U> NullChain<U> fromJson(U uClass);

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
    NullChain<String> dateFormat(DateFormatEnum dateFormatEnum);

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
    NullChain<T> dateOffset(DateOffsetEnum controlEnum, int num, TimeEnum timeEnum);
    
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
    NullChain<T> dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum controlEnum, TimeEnum timeEnum);

    /**
     * 日期比较操作 - 比较两个日期的大小关系
     * 
     * <p>该方法用于比较当前日期值与指定日期的大小关系。
     * 支持多种日期类型的比较，包括Date、LocalDate、LocalDateTime、时间戳等。
     * 如果需要比较到日期后的时间那么请保证单位一致性: 比如都是yyyy-MM-dd HH:mm:ss
     * 不能有一个是yyyy-MM-dd 另一个是yyyy-MM-dd HH:mm:ss
     * 这样就会导致因为单位不一致，比较的结果不准确</p>
     * 
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
    NullChain<Integer> dateCompare(Object date);

    /**
     * 日期间隔操作 - 计算两个日期之间的间隔
     * 1. 不包括开始时间, 包括结束时间
     * 2. 日的比如21~25之间相差4天,如果需要算开始时间那么需要+1
     * 3. 如果计算周期比如01~25之间经过了多少周那么就是3 因为第四周没有走完还差2天
     * 4. 所以在有些时候需要完整的周期忽略未走完的需要+1
     *
     * 注意时间单位的精确度(建议对比的时候时间格式是一致的避免导致对比结果出问题)：
     * 传入含时间的对象（LocalDateTime/ZonedDateTime）：
     *      会精确对比年、月、日、时、分、秒、毫秒等所有维度，计算的是 "完整单位间隔"—— 比如要凑够 24 小时才算 1 天，凑够 60 分钟才算 1 小时。
     * 传入仅日期的对象（LocalDate）：
     *      只对比年、月、日，忽略时间，只要日期跨 1 天，天数差就为 1（比如 12-01 到 12-02，不管时间是几点，天数差都是 1）。
     * 传入仅时间的对象（LocalTime）：
     *      只对比时、分、秒、毫秒，无法对比日期相关单位（如天、周、月）。
     *
     * <p>该方法用于计算当前日期值与指定日期之间的间隔，根据指定的时间单位返回间隔数。</p>
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
    NullChain<Long> dateBetween(Object date, TimeEnum timeEnum);

    /**
     * 浅拷贝操作 - 复制对象的基本结构
     * 
     * <p>该方法用于对当前对象进行浅拷贝，复制对象的基本结构，
     * 但引用类型的字段仍然指向原对象，不会进行深度复制。</p>
     * 
     * @return 包含浅拷贝对象的Null链
     * 
     * @example
     * <pre>{@code
     * User copiedUser = Null.of(user)
     *     .copy()  // 对User对象进行浅拷贝
     *     .orElse(new User());
     * }</pre>
     */
    NullChain<T> copy();

    /**
     * 深拷贝操作 - 完全复制对象及其所有嵌套对象
     * 
     * <p>该方法用于对当前对象进行深拷贝，完全复制对象及其所有嵌套对象。
     * 通过序列化机制实现深拷贝，确保所有引用都是独立的。</p>
     * 
     * <p><strong>注意：</strong>需要拷贝的类必须实现Serializable接口，
     * 包括内部类，否则会抛出NotSerializableException异常。</p>
     * 
     * @return 包含深拷贝对象的Null链
     * 
     * @example
     * <pre>{@code
     * User deepCopiedUser = Null.of(user)
     *     .deepCopy()  // 对User对象进行深拷贝
     *     .orElse(new User());
     * }</pre>
     */
    NullChain<T> deepCopy();

    /**
     * 字段提取操作 - 提取对象中的指定字段
     * 
     * <p>该方法用于从当前对象中提取指定的字段，返回包含提取字段的新对象。
     * 支持提取多个字段，通过映射函数指定要提取的字段。</p>
     * 
     * @param <U> 提取字段的类型
     * @param mapper 字段提取函数数组
     * @return 包含提取字段的Null链
     * 
     * @example
     * <pre>{@code
     * String name = Null.of(user)
     *     .pick(User::getName)  // 提取用户姓名
     *     .orElse("未知用户");
     * 
     * // 提取多个字段
     * Object[] fields = Null.of(user)
     *     .pick(User::getName, User::getEmail, User::getAge)
     *     .orElse(new Object[0]);
     * }</pre>
     */
    <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper);
}
