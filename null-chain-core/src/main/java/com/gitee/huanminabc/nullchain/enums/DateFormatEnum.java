package com.gitee.huanminabc.nullchain.enums;

import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public enum DateFormatEnum {

    /**
     * 显示年月日时分秒，例如 2015-08-11 00:00:00.
     */
    DATETIME_PATTERN_START("yyyy-MM-dd '00:00:00'"),
    /**
     * 显示年月日时分秒，例如 2015-08-11 09:51:53.
     */
    DATETIME_PATTERN_END("yyyy-MM-dd '23:59:59'"),


    /**
     * 显示年月日时分秒，例如 2015-08-11 09:51:53.
     */
    DATETIME_PATTERN("yyyy-MM-dd HH:mm:ss"),


    DATETIME_T_PATTERN("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
    /**
     * 仅显示年月日，例如 2015-08-11.
     */
    DATE_PATTERN("yyyy-MM-dd"),

    /**
     * 仅显示时分秒，例如 09:51:53.
     */
    TIME_PATTERN("HH:mm:ss"),

    /**
     * 仅显示年月,例如 2015-08
     */
    MONTH_PATTERN("yyyy-MM"),
    /**
     * 显示年月日时分秒(无符号)，例如 20150811095153.
     */
    NUM_DATETIME_PATTERN("yyyyMMddHHmmss"),
    /**
     * 仅显示年月日(无符号)，例如 20150811.
     */
    NUM_DATE_PATTERN("yyyyMMdd"),

    /**
     * 仅显示年月(无符号)，例如 201508.
     */
    NUM_MONTH_PATTERN("yyyyMM"),
    /**
     * 仅显示年月(无符号)，例如 201508.
     */
    NUM_YEAR_PATTERN("yyyy"),

    ;


    /**
     * 显示年月日时分秒，例如 2015-08-11 09:51:53.
     */

    private final String value;

    DateFormatEnum(String value) {
        this.value = value;
    }

    private static final List<DateFormatEnum> datePatterns = Arrays.asList(
            DATETIME_PATTERN,
            DATETIME_T_PATTERN,
            DATE_PATTERN,
            MONTH_PATTERN,
            TIME_PATTERN
    );
    private static final List<DateFormatEnum> numDatePatterns = Arrays.asList(
            NUM_DATETIME_PATTERN,
            NUM_DATE_PATTERN,
            NUM_MONTH_PATTERN,
            NUM_YEAR_PATTERN
    );
    //长度Map
    private static final Map<Integer, DateFormatEnum> lengthMap = new HashMap<>();
    private static final Map<Integer, DateFormatEnum> numLengthMap = new HashMap<>();

    static {
        for (DateFormatEnum pattern : datePatterns) {
            lengthMap.put(pattern.value.length(), pattern);
        }
        for (DateFormatEnum pattern : numDatePatterns) {
            numLengthMap.put(pattern.value.length(), pattern);
        }

    }

    //自动识别日期格式并且转换
    public static Date parseDate(String input) {
        //10和13位存数字的字符串 这里特殊处理,在数字匹配中没有10和13位的数字
        if (isPositiveNumeric(input) && (input.length() == 10 || input.length() == 13)) {
            return null;
        }
        try {
            DateFormatEnum patternLen = lengthMap.get(input.length());
            if (patternLen != null) {
                return new SimpleDateFormat(patternLen.value).parse(input);
            }

        } catch (ParseException ignore) {
        }

        try {
            DateFormatEnum numPatternLen = numLengthMap.get(input.length());
            if (numPatternLen != null) {
                return new SimpleDateFormat(numPatternLen.value).parse(input);
            }
        } catch (ParseException ignore) {

        }

        for (DateFormatEnum pattern : datePatterns) {
            try {
                return new SimpleDateFormat(pattern.value).parse(input);
            } catch (ParseException e) {
                // ignore
            }
        }
        for (DateFormatEnum pattern : numDatePatterns) {
            try {
                Date parse = new SimpleDateFormat(pattern.value).parse(input);
                //开头年开头匹配 ,不符合, 这种属于错误数据
                if (!input.startsWith(getYear())) {
                    return null;
                }
                return parse;
            } catch (ParseException e) {
                // ignore
            }
        }
        return null;
    }

    //自动识别日期格式返回枚举
    public static DateFormatEnum parseDateToEnum(String input) {
        //10和13位存数字的字符串 这里特殊处理,在数字匹配中没有10和13位的数字
        if (isPositiveNumeric(input) && (input.length() == 10 || input.length() == 13)) {
            return null;
        }
        try {
            DateFormatEnum patternLen = lengthMap.get(input.length());
            if (patternLen != null) {
                new SimpleDateFormat(patternLen.value).parse(input);
                return patternLen;
            }

        } catch (ParseException ignore) {

        }

        try {
            DateFormatEnum numPatternLen = numLengthMap.get(input.length());
            if (numPatternLen != null) {
                new SimpleDateFormat(numPatternLen.value).parse(input);
                return numPatternLen;
            }
        } catch (ParseException ignore) {
        }


        for (DateFormatEnum pattern : datePatterns) {
            try {
                new SimpleDateFormat(pattern.value).parse(input);
                return pattern;
            } catch (ParseException ignore) {
            }
        }
        for (DateFormatEnum pattern : numDatePatterns) {
            try {
                new SimpleDateFormat(pattern.value).parse(input);
                //开头年开头匹配 ,不符合, 这种属于错误数据
                if (!input.startsWith(getYear())) {
                    return null;
                }
                return pattern;
            } catch (ParseException ignore) {
            }
        }
        return null;
    }

    //判断是否是正数字
    public static boolean isPositiveNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        //截取掉-号
        if (str.startsWith("-")) {
            return false;
        }
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //获取当前时间的年
    public static String getYear() {
        return new SimpleDateFormat(NUM_YEAR_PATTERN.value).format(new Date());
    }

}