package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Null日期格式化工具类 - 提供日期格式化的工具功能
 *
 * <p>该类提供了日期格式化的工具功能，支持Date、LocalDate、LocalDateTime、10或13位时间戳(数值或字符串)等格式。
 * 通过统一的日期格式化接口，为Null链操作提供便捷的日期处理能力。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>日期格式化：格式化各种日期类型</li>
 *   <li>时间戳处理：处理10位和13位时间戳</li>
 *   <li>类型转换：在不同日期类型间转换</li>
 *   <li>格式验证：验证日期格式的正确性</li>
 *   <li>时区处理：处理时区相关的日期操作</li>
 * </ul>
 *
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>多格式支持：支持多种日期格式</li>
 *   <li>类型安全：通过类型检查保证安全性</li>
 *   <li>异常处理：完善的异常处理机制</li>
 *   <li>性能优化：提供高效的日期处理</li>
 * </ul>
 *
 * @author huanmin
 * @version 1.1.1
 * @see Date 日期类
 * @see LocalDate 本地日期类
 * @see LocalDateTime 本地日期时间类
 * @see DateFormatEnum 日期格式枚举
 * @see DateOffsetEnum 日期偏移枚举
 * @see TimeEnum 时间枚举
 * @since 1.0.0
 */
public class NullDateFormat {


    public static final ZoneId ZONE = ZoneId.systemDefault();

    /**
     * 对 Temporal 进行时间偏移
     * 
     * @param temporal 源 Temporal 对象
     * @param controlEnum 偏移方向（加、减、开始、结束等）
     * @param num 偏移量
     * @param timeEnum 时间单位
     * @return 偏移后的 Temporal 对象
     */
    private static Temporal temporalOffset(Temporal temporal, DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) {
        if (temporal == null) {
            return null;
        }

        // 转换为 LocalDateTime（统一使用 LocalDateTime 作为中间类型）
        LocalDateTime localDateTime;
        if (temporal instanceof LocalDateTime) {
            localDateTime = (LocalDateTime) temporal;
        } else if (temporal instanceof LocalDate) {
            localDateTime = ((LocalDate) temporal).atStartOfDay();
        } else {
            try {
                localDateTime = LocalDateTime.from(temporal);
            } catch (Exception e) {
                return null;
            }
        }

        // 转换为毫秒时间戳进行处理
        long epochMilli = localDateTime.atZone(ZONE).toInstant().toEpochMilli();
        epochMilli = compatibilityDateOffset(epochMilli, controlEnum, num, timeEnum);
        
        // 转换回 LocalDateTime
        return Instant.ofEpochMilli(epochMilli).atZone(ZONE).toLocalDateTime();
    }

    /**
     * 将时间类型(Date,LocalDate,LocalDateTime), 10或13位时间戳(数值或字符串) ,进行时间偏移
     * 统一使用 Temporal 作为中间类型进行处理
     *
     * @param controlEnum 偏移方向（加、减、开始、结束等）
     * @param num         偏移量
     * @param timeEnum    时间单位
     */
    public static <T> T dateOffset(T date, DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) throws ParseException {
        if (date == null) {
            return null;
        }

        // 统一转换为 Temporal
        Temporal temporal = toTemporal(date);
        if (temporal == null) {
            return null;
        }

        // 进行时间偏移
        Temporal offsetTemporal = temporalOffset(temporal, controlEnum, num, timeEnum);
        if (offsetTemporal == null) {
            return null;
        }

        // 转换回目标类型
        Class<?> targetType = date.getClass();
        return fromTemporal(offsetTemporal, targetType, date);
    }


    private static Long compatibilityDateOffset(long epochMilli, DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) {
        long offset = timeEnum.toMillis(num);
        if (controlEnum == DateOffsetEnum.SUB) {
            epochMilli -= offset;
        } else if (controlEnum == DateOffsetEnum.ADD) {
            epochMilli += offset;
        } else if (controlEnum == DateOffsetEnum.START) {
            epochMilli = getStartTimeOfUnit(epochMilli, timeEnum);
        } else if (controlEnum == DateOffsetEnum.END) {
            epochMilli = getEndTimeOfUnit(epochMilli, timeEnum);
        } else if (controlEnum == DateOffsetEnum.START_ADD) {
            long startTimeOfUnit = getStartTimeOfUnit(epochMilli, timeEnum);
            epochMilli = startTimeOfUnit + offset;
        } else if (controlEnum == DateOffsetEnum.START_SUB) {
            long startTimeOfUnit = getStartTimeOfUnit(epochMilli, timeEnum);
            epochMilli = startTimeOfUnit - offset;
        } else if (controlEnum == DateOffsetEnum.END_ADD) {
            long endTimeOfUnit = getEndTimeOfUnit(epochMilli, timeEnum);
            epochMilli = endTimeOfUnit + offset;
        } else if (controlEnum == DateOffsetEnum.END_SUB) {
            long endTimeOfUnit = getEndTimeOfUnit(epochMilli, timeEnum);
            epochMilli = endTimeOfUnit - offset;
        } else {
            throw new NfException(controlEnum + "不支持的时间控制类型");
        }
        return epochMilli;
    }


    /**
     * 将日期对象格式化为指定格式的字符串
     * 统一使用 Temporal 作为中间类型进行处理
     * 
     * @param value 日期对象
     * @param dateFormatEnum 日期格式枚举
     * @return 格式化后的日期字符串
     */
    public static <T> String toString(T value, DateFormatEnum dateFormatEnum) {
        if (value == null || dateFormatEnum == null) {
            return null;
        }

        // 统一转换为 Temporal
        Temporal temporal = toTemporal(value);
        if (temporal == null) {
            return null;
        }

        // 转换为 LocalDateTime 进行格式化
        LocalDateTime localDateTime;
        if (temporal instanceof LocalDateTime) {
            localDateTime = (LocalDateTime) temporal;
        } else if (temporal instanceof LocalDate) {
            localDateTime = ((LocalDate) temporal).atStartOfDay();
        } else {
            try {
                localDateTime = LocalDateTime.from(temporal);
            } catch (Exception e) {
                return null;
            }
        }

        // 格式化为字符串
        try {
            Date date = Date.from(localDateTime.atZone(ZONE).toInstant());
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param date1
     * @param date2
     * @return -1 date1<date2 0 date1=date2 1 date1>date2  ,null 无法比较
     */
    public static Integer dateCompare(Object date1, Object date2) {
        Long time1 = toLong(date1);
        Long time2 = toLong(date2);
        if (time1 == null || time2 == null) {
            return null;
        }
        return Long.compare(time1, time2);

    }

    /**
     * 将 Temporal 转换为指定目标类型
     * 
     * @param temporal 源 Temporal 对象
     * @param targetType 目标类型的 Class 对象（用于确定返回类型）
     * @param originalValue 原始值（用于保持类型一致性，如 String 格式、Integer 等）
     * @return 转换后的目标类型对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T fromTemporal(Temporal temporal, Class<?> targetType, Object originalValue) {
        if (temporal == null) {
            return null;
        }

        // 转换为 LocalDateTime（统一使用 LocalDateTime 作为中间类型）
        LocalDateTime localDateTime;
        if (temporal instanceof LocalDateTime) {
            localDateTime = (LocalDateTime) temporal;
        } else if (temporal instanceof LocalDate) {
            localDateTime = ((LocalDate) temporal).atStartOfDay();
        } else {
            // 其他 Temporal 类型，尝试转换为 LocalDateTime
            try {
                localDateTime = LocalDateTime.from(temporal);
            } catch (Exception e) {
                return null;
            }
        }

        // 根据目标类型进行转换
        if (targetType == Date.class || Date.class.isAssignableFrom(targetType)) {
            return (T) Date.from(localDateTime.atZone(ZONE).toInstant());
        }
        
        if (targetType == LocalDateTime.class || LocalDateTime.class.isAssignableFrom(targetType)) {
            return (T) localDateTime;
        }
        
        if (targetType == LocalDate.class || LocalDate.class.isAssignableFrom(targetType)) {
            return (T) localDateTime.toLocalDate();
        }
        
        if (targetType == Long.class || Long.class.isAssignableFrom(targetType)) {
            return (T) Long.valueOf(localDateTime.atZone(ZONE).toInstant().toEpochMilli());
        }
        
        if (targetType == Integer.class || Integer.class.isAssignableFrom(targetType)) {
            long epochMilli = localDateTime.atZone(ZONE).toInstant().toEpochMilli();
            // 转换为10位时间戳
            String timeStr = String.valueOf(epochMilli);
            if (timeStr.length() > 10) {
                timeStr = timeStr.substring(0, 10);
            }
            return (T) Integer.valueOf(timeStr);
        }
        
        if (targetType == String.class || String.class.isAssignableFrom(targetType)) {
            // 如果是字符串，需要根据原始值的格式进行格式化
            if (originalValue instanceof String) {
                String originalStr = originalValue.toString();
                // 尝试识别原始字符串的格式
                DateFormatEnum dateFormatEnum = DateFormatEnum.parseDateToEnum(originalStr);
                if (dateFormatEnum != null) {
                    // 使用原始格式进行格式化
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                    Date date = Date.from(localDateTime.atZone(ZONE).toInstant());
                    return (T) sdf.format(date);
                }
                // 如果是时间戳字符串，返回时间戳字符串
                if (DateFormatEnum.isPositiveNumeric(originalStr) && 
                    (originalStr.length() == 10 || originalStr.length() == 13)) {
                    long epochMilli = localDateTime.atZone(ZONE).toInstant().toEpochMilli();
                    // 保持原始长度
                    if (originalStr.length() == 10) {
                        String timeStr = String.valueOf(epochMilli);
                        if (timeStr.length() > 10) {
                            timeStr = timeStr.substring(0, 10);
                        }
                        return (T) timeStr;
                    } else {
                        return (T) String.valueOf(epochMilli);
                    }
                }
            }
            // 默认格式化为标准格式
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormatEnum.DATETIME_PATTERN.getValue());
            Date date = Date.from(localDateTime.atZone(ZONE).toInstant());
            return (T) sdf.format(date);
        }
        
        return null;
    }

    /**
     * 将日期对象转换为 Temporal（统一入口）
     * 统一处理所有日期类型，参考 toString 方法的处理逻辑
     */
    private static Temporal toTemporal(Object date) {
        if (date == null) {
            return null;
        }
        
        // Date 类型
        if (date instanceof Date) {
            Date date1 = (Date) date;
            return date1.toInstant().atZone(ZONE).toLocalDateTime();
        }
        
        // LocalDateTime 类型
        if (date instanceof LocalDateTime) {
            return (LocalDateTime) date;
        }
        
        // LocalDate 类型
        if (date instanceof LocalDate) {
            return (LocalDate) date;
        }
        
        // Long、Integer、String 类型，参考 toString 的处理逻辑
        if (date instanceof Long || date instanceof Integer || date instanceof String) {
            // 如果是字符串，先尝试识别时间字符串格式
            if (date instanceof String) {
                Date parsedDate = DateFormatEnum.parseDate(date.toString());
                if (parsedDate != null) {
                    return parsedDate.toInstant().atZone(ZONE).toLocalDateTime();
                }
            }
            
            // 处理时间戳（10位或13位数字）
            String timeString = date.toString();
            if (DateFormatEnum.isPositiveNumeric(timeString)) {
                if (timeString.length() >= 10 && timeString.length() <= 13) {
                    // 长度不为13位的话，添加3个0
                    if (timeString.length() != 13) {
                        // 先截取前10位
                        timeString = timeString.substring(0, 10);
                        timeString = timeString + "000";
                    }
                    long time = Long.parseLong(timeString);
                    return Instant.ofEpochMilli(time).atZone(ZONE).toLocalDateTime();
                } else {
                    // 尝试识别其他数字格式的日期（如 20241122）
                    DateFormatEnum dateFormatEnum = DateFormatEnum.parseDateToEnum(timeString);
                    if (dateFormatEnum != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                            Date parsedDate = sdf.parse(timeString);
                            return parsedDate.toInstant().atZone(ZONE).toLocalDateTime();
                        } catch (ParseException ignored) {
                            // 解析失败，返回 null
                        }
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * 将 TimeEnum 映射到 ChronoUnit
     */
    private static ChronoUnit toChronoUnit(TimeEnum timeEnum) {
        if (timeEnum == null) {
            return null;
        }
        switch (timeEnum) {
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            case WEEKS:
                return ChronoUnit.WEEKS;
            case MONTHS:
                return ChronoUnit.MONTHS;
            case YEARS:
                return ChronoUnit.YEARS;
            default:
                return null;
        }
    }

    /**
     * 计算两个日期之间的间隔
     * 
     * <p>该方法用于计算两个日期之间的间隔，根据指定的时间单位返回间隔数。
     * 例如，如果 timeEnum 是 DAYS，则返回间隔多少天。</p>
     * 
     * <p>统一使用 ChronoUnit.between 进行精确计算，这样可以正确处理时区、夏令时等因素。
     * 所有日期类型（Date、LocalDate、LocalDateTime、时间戳、字符串等）都会统一转换为 Temporal 进行处理。</p>
     * 
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @param timeEnum 时间单位（年、月、日、时、分、秒等）
     * @return 两个日期之间的间隔数（绝对值），如果无法计算则返回 null
     */
    public static Long dateBetween(Object date1, Object date2, TimeEnum timeEnum) {
        if (date1 == null || date2 == null || timeEnum == null) {
            return null;
        }

        ChronoUnit chronoUnit = toChronoUnit(timeEnum);
        if (chronoUnit == null) {
            return null;
        }

        // 统一转换为 Temporal 进行处理
        Temporal temporal1 = toTemporal(date1);
        Temporal temporal2 = toTemporal(date2);
        if (temporal1 == null || temporal2 == null) {
            return null;
        }

        try {
            // 使用 ChronoUnit.between 计算间隔，取绝对值
            return Math.abs(chronoUnit.between(temporal1, temporal2));
        } catch (Exception e) {
            return null;
        }
    }

    //自动识别全部时间类型,都转换为13位的时间戳
    private static Long toLong(Object date) {
        if (date instanceof Date) {
            Date date1 = (Date) date;
            return date1.getTime();
        }
        if (date instanceof LocalDateTime) {
            LocalDateTime localDate = (LocalDateTime) date;
            return localDate.toInstant(ZONE.getRules().getOffset(localDate)).toEpochMilli();
        }
        if (date instanceof LocalDate) {
            LocalDate localDate = (LocalDate) date;
            return localDate.atStartOfDay(ZONE).toInstant().toEpochMilli();
        }
        if (date instanceof Long || date instanceof Integer || date instanceof String) {
            if (date instanceof String) {
                //尝试识别时间字符串
                Date date1 = DateFormatEnum.parseDate(date.toString());
                if (date1 != null) {
                    return date1.getTime();
                }
            }

            String timeString = date.toString();
            //判断是否是纯数字,并且>=10<=13位
            if (DateFormatEnum.isPositiveNumeric(timeString)) {
                if (timeString.length() >= 10 && timeString.length() <= 13) {
                    //长度不为13位的话,添加3个0
                    if (timeString.length() != 13) {
                        //先截取前10位
                        timeString = timeString.substring(0, 10);
                        timeString = timeString + "000";
                    }
                    return Long.parseLong(timeString);
                } else {
                    DateFormatEnum dateFormatEnum = DateFormatEnum.parseDateToEnum(timeString);
                    if (dateFormatEnum != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                            return sdf.parse(timeString).getTime();
                        } catch (ParseException ignored) {
                        }
                    }
                }
            }
            return null;
        }
        return null;
    }


    private static long getStartTimeOfUnit(long timestamp, TimeEnum timeEnum) {
        //timestamp的单位默认是毫秒 ,先转换为时间
        LocalDateTime localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZONE).toLocalDateTime();
        switch (timeEnum) {
            case YEARS:
                return localDateTime.withDayOfYear(1).with(LocalTime.MIN).atZone(ZONE).toInstant().toEpochMilli();
            case MONTHS:
                return localDateTime.withDayOfMonth(1).with(LocalTime.MIN).atZone(ZONE).toInstant().toEpochMilli();
            case WEEKS:
                return localDateTime.with(DayOfWeek.MONDAY).with(LocalTime.MIN).atZone(ZONE).toInstant().toEpochMilli();
            case DAYS:
                return localDateTime.with(LocalTime.MIN).atZone(ZONE).toInstant().toEpochMilli();
            case HOURS:
                return localDateTime.withMinute(0).withSecond(0).withNano(0).atZone(ZONE).toInstant().toEpochMilli();
            case MINUTES:
                return localDateTime.withSecond(0).withNano(0).atZone(ZONE).toInstant().toEpochMilli();
            case SECONDS:
                return localDateTime.withNano(0).atZone(ZONE).toInstant().toEpochMilli();
            default:
                return timestamp;
        }
    }

    private static long getEndTimeOfUnit(long timestamp, TimeEnum timeEnum) {
        //timestamp的单位默认是毫秒 ,先转换为时间
        LocalDateTime localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZONE).toLocalDateTime();
        switch (timeEnum) {
            case YEARS:
                return localDateTime.withDayOfYear(localDateTime.toLocalDate().lengthOfYear()).with(LocalTime.MAX).atZone(ZONE).toInstant().toEpochMilli();
            case MONTHS:
                return localDateTime.withDayOfMonth(localDateTime.toLocalDate().lengthOfMonth()).with(LocalTime.MAX).atZone(ZONE).toInstant().toEpochMilli();
            case WEEKS:
                return localDateTime.with(DayOfWeek.SUNDAY).with(LocalTime.MAX).atZone(ZONE).toInstant().toEpochMilli();
            case DAYS:
                return localDateTime.with(LocalTime.MAX).atZone(ZONE).toInstant().toEpochMilli();
            case HOURS:
                return localDateTime.withMinute(59).withSecond(59).withNano(999999999).atZone(ZONE).toInstant().toEpochMilli();
            case MINUTES:
                return localDateTime.withSecond(59).withNano(999999999).atZone(ZONE).toInstant().toEpochMilli();
            case SECONDS:
                return localDateTime.withNano(999999999).atZone(ZONE).toInstant().toEpochMilli();
            default:
                return timestamp;
        }
    }

}
