package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/**
 * 只兼容Date,LocalDate,LocalDateTime,10或13位时间戳(数值或字符串)
 *
 * @author huanmin
 * @date 2024/11/22
 */
public class NullDateFormat {


    /**
     * 将时间类型(Date,LocalDate,LocalDateTime), 10或13位时间戳(数值或字符串) ,进行时间偏移
     *
     * @param controlEnum 1:加, -1:减
     * @param num         偏移量
     * @param timeEnum    时间单位
     */
    public static <T> T dateOffset(T date, DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) throws ParseException {
        if (date instanceof Date) {
            Date date1 = (Date) date;
            long time = date1.getTime();
            time = compatibilityDateOffset(time, controlEnum, num, timeEnum);
            return (T) new Date(time);
        }
        if (date instanceof LocalDateTime) {
            LocalDateTime localDate = (LocalDateTime) date;
            ZoneId defaultZoneId = ZoneId.systemDefault();
            long epochMilli = localDate.toInstant(defaultZoneId.getRules().getOffset(localDate)).toEpochMilli();
            epochMilli = compatibilityDateOffset(epochMilli, controlEnum, num, timeEnum);
            Date parse = new Date(epochMilli);
            return (T) parse.toInstant().atZone(defaultZoneId).toLocalDateTime();
        }

        if (date instanceof LocalDate) {
            LocalDate localDate = (LocalDate) date;
            ZoneId defaultZoneId = ZoneId.systemDefault();
            long epochMilli = localDate.atStartOfDay(defaultZoneId).toInstant().toEpochMilli(); //毫秒
            epochMilli = compatibilityDateOffset(epochMilli, controlEnum, num, timeEnum);
            Date parse = new Date(epochMilli);
            return (T) parse.toInstant().atZone(defaultZoneId).toLocalDate();
        }

        if (date instanceof Long || date instanceof Integer || date instanceof String) {

            //如果类型是字符串,那么先识别看看符合转换条件吗? 如果不符合,那么就走下面的数字判断
            if (date instanceof String) {
                DateFormatEnum dateFormatEnum = DateFormatEnum.parseDateToEnum(date.toString());
                if (dateFormatEnum != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                    long epochMilli = sdf.parse(date.toString()).getTime();
                    epochMilli = compatibilityDateOffset(epochMilli, controlEnum, num, timeEnum);
                    Date parse = new Date(epochMilli);
                    SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormatEnum.getValue());
                    return (T) sdf1.format(parse);
                }
            }

            String timeString = date.toString();
            //判断是否是纯数字,并且10或13位
            if (DateFormatEnum.isPositiveNumeric(timeString) ) {
                if (timeString.length() == 10 || timeString.length() == 13){
                    Long aLong = toLong(date);
                    Long aLong1 = compatibilityDateOffset(aLong, controlEnum, num, timeEnum);
                    if (date instanceof Long){
                        return  (T) aLong1;
                    }else if (date instanceof Integer) {
                        return (T) Integer.valueOf(aLong1.toString().substring(0, 10));
                    }
                    return (T) aLong1.toString();
                }else{//能走到这里就表示这个数字是被格式化的特殊时间, 比如20241122
                    DateFormatEnum dateFormatEnum = DateFormatEnum.parseDateToEnum(timeString);
                    if (dateFormatEnum != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                        long epochMilli = sdf.parse(timeString).getTime();
                        epochMilli = compatibilityDateOffset(epochMilli, controlEnum, num, timeEnum);
                        Date parse = new Date(epochMilli);
                        SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormatEnum.getValue());
                        T format = (T) sdf1.format(parse);
                        if (date instanceof Long){
                            return  (T) Long.valueOf(format.toString());
                        }
                        if (date instanceof Integer){
                            return  (T) Integer.valueOf(format.toString());
                        }
                        return format;
                    }
                }
            }


            return null;
        }
        return null;
    }


    private static Long compatibilityDateOffset(long epochMilli, DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) {
        long offset = timeEnum.toMillis(num);
        if (controlEnum == DateOffsetEnum.SUB) {
            epochMilli -= offset;
        } else if (controlEnum == DateOffsetEnum.ADD) {
            epochMilli += offset;
        }else if (controlEnum == DateOffsetEnum.START){
            epochMilli = getStartTimeOfUnit(epochMilli, timeEnum);
        }else if (controlEnum == DateOffsetEnum.END){
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


    public static <T> String toString(T value, DateFormatEnum dateFormatEnum) {

        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
            return sdf.format((Date) value);
        }
        if (value instanceof LocalDate) {
            LocalDate localDate = (LocalDate) value;
            long epochMilli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); //毫秒
            Date parse = new Date(epochMilli);
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
            return sdf.format(parse);
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDate = (LocalDateTime) value;
            long epochMilli = localDate.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            Date parse = new Date(epochMilli);
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
            return sdf.format(parse);
        }
        if (value instanceof Long || value instanceof Integer || value instanceof String) {
            if (value instanceof String) {
                //尝试识别时间字符串
                Date date1 = DateFormatEnum.parseDate(value.toString());
                if (date1 != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                    return sdf.format(date1);
                }
            }

            String timeString = value.toString();
            //判断是否是纯数字,并且>=10<=13位
            if (DateFormatEnum.isPositiveNumeric(timeString)) {
                if (timeString.length() >= 10 && timeString.length() <= 13) {
                    //长度不为13位的话,添加3个0
                    if (timeString.length() != 13) {
                        //先截取前10位
                        timeString = timeString.substring(0, 10);
                        timeString = timeString + "000";
                    }
                    long time = Long.parseLong(timeString);
                    Date parse = new Date(time);
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                    return sdf.format(parse);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormatEnum.getValue());
                    try {
                        return sdf.format(sdf.parse(timeString));
                    } catch (ParseException ignored) {

                    }
                }
            }
            return null;
        }
        return null;
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

    //自动识别全部时间类型,都转换为13位的时间戳
    private static Long toLong(Object date) {
        if (date instanceof Date) {
            Date date1 = (Date) date;
            return date1.getTime();
        }
        if (date instanceof LocalDateTime) {
            LocalDateTime localDate = (LocalDateTime) date;
            ZoneId defaultZoneId = ZoneId.systemDefault();
            return localDate.toInstant(defaultZoneId.getRules().getOffset(localDate)).toEpochMilli();
        }
        if (date instanceof LocalDate) {
            LocalDate localDate = (LocalDate) date;
            ZoneId defaultZoneId = ZoneId.systemDefault();
            return localDate.atStartOfDay(defaultZoneId).toInstant().toEpochMilli();
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
            if (DateFormatEnum.isPositiveNumeric(timeString) ) {
                if (timeString.length() >= 10 && timeString.length() <= 13){
                    //长度不为13位的话,添加3个0
                    if (timeString.length() != 13) {
                        //先截取前10位
                        timeString = timeString.substring(0, 10);
                        timeString = timeString + "000";
                    }
                    return Long.parseLong(timeString);
                }else{
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
        LocalDateTime localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
        switch (timeEnum) {
            case YEARS:
                return localDateTime.withDayOfYear(1).with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case MONTHS:
                return localDateTime.withDayOfMonth(1).with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case WEEKS:
                return localDateTime.with(DayOfWeek.MONDAY).with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case DAYS:
                return localDateTime.with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case HOURS:
                return localDateTime.withMinute(0).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case MINUTES:
                return localDateTime.withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case SECONDS:
                return localDateTime.withNano(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            default:
                return timestamp;
        }
    }

    private static long getEndTimeOfUnit(long timestamp, TimeEnum timeEnum) {
        //timestamp的单位默认是毫秒 ,先转换为时间
        LocalDateTime localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
        switch (timeEnum) {
            case YEARS:
                return localDateTime.withDayOfYear(localDateTime.toLocalDate().lengthOfYear()).with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case MONTHS:
                return localDateTime.withDayOfMonth(localDateTime.toLocalDate().lengthOfMonth()).with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case WEEKS:
                return localDateTime.with(DayOfWeek.SUNDAY).with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case DAYS:
                return localDateTime.with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case HOURS:
                return localDateTime.withMinute(59).withSecond(59).withNano(999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case MINUTES:
                return localDateTime.withSecond(59).withNano(999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            case SECONDS:
                return localDateTime.withNano(999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            default:
                return timestamp;
        }
    }

}
