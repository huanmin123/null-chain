/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.gitee.huanminabc.nullchain.enums;

import java.time.Year;
import java.time.YearMonth;
import java.util.function.Supplier;

public enum TimeEnum {


    /**
     * 毫秒
     */
    MILLISECONDS {

        public long toMillis(long d) {
            return d;
        }

        public long toSeconds(long d) {
            return d / (C3 / C2);
        }

        public long toMinutes(long d) {
            return d / (C4 / C2);
        }

        public long toHours(long d) {
            return d / (C5 / C2);
        }

        public long toDays(long d) {
            return d / (C6 / C2);
        }

        public long toWeeks(long d) {
            return d / (C7 / C2);
        }

        public long toMonths(long d) {
            return d / (C8.get() / C2);
        }

        public long toYears(long d) {
            return d / (C9.get() / C2);
        }

        public long convert(long d, TimeEnum u) {
            return u.toMillis(d);
        }
    },

    /**
     * 秒
     */
    SECONDS {

        public long toMillis(long d) {
            return x(d, C3 / C2, MAX / (C3 / C2));
        }

        public long toSeconds(long d) {
            return d;
        }

        public long toMinutes(long d) {
            return d / (C4 / C3);
        }

        public long toHours(long d) {
            return d / (C5 / C3);
        }

        public long toDays(long d) {
            return d / (C6 / C3);
        }

        public long toWeeks(long d) {
            return d / (C7 / C3);
        }

        public long toMonths(long d) {
            return d / (C8.get() / C3);
        }

        public long toYears(long d) {
            return d / (C9.get() / C3);
        }

        public long convert(long d, TimeEnum u) {
            return u.toSeconds(d);
        }

    },

    /**
     * 分钟
     */
    MINUTES {

        public long toMillis(long d) {
            return x(d, C4 / C2, MAX / (C4 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C4 / C3, MAX / (C4 / C3));
        }

        public long toMinutes(long d) {
            return d;
        }

        public long toHours(long d) {
            return d / (C5 / C4);
        }

        public long toDays(long d) {
            return d / (C6 / C4);
        }

        public long toWeeks(long d) {
            return d / (C7 / C4);
        }

        public long toMonths(long d) {
            return d / (C8.get() / C4);
        }

        public long toYears(long d) {
            return d / (C9.get() / C4);
        }


        public long convert(long d, TimeEnum u) {
            return u.toMinutes(d);
        }

    },

    /**
     * 处理小时的情况
     */
    HOURS {

        public long toMillis(long d) {
            return x(d, C5 / C2, MAX / (C5 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C5 / C3, MAX / (C5 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C5 / C4, MAX / (C5 / C4));
        }

        public long toHours(long d) {
            return d;
        }

        public long toDays(long d) {
            return d / (C6 / C5);
        }

        public long toWeeks(long d) {
            return d / (C7 / C5);
        }

        public long toMonths(long d) {
            return d / (C8.get() / C5);
        }

        public long toYears(long d) {
            return d / (C9.get() / C5);
        }

        public long convert(long d, TimeEnum u) {
            return u.toHours(d);
        }
    },

    /**
     * 处理天的情况
     */
    DAYS {

        public long toMillis(long d) {
            return x(d, C6 / C2, MAX / (C6 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C6 / C3, MAX / (C6 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C6 / C4, MAX / (C6 / C4));
        }

        public long toHours(long d) {
            return x(d, C6 / C5, MAX / (C6 / C5));
        }

        public long toDays(long d) {
            return d;
        }

        public long toWeeks(long d) {
            return d / (C7 / C6);
        }

        public long toMonths(long d) {
            return d / (C8.get() / C6);
        }

        public long toYears(long d) {
            return d / (C9.get() / C6);
        }

        public long convert(long d, TimeEnum u) {
            return u.toDays(d);
        }

    },

    /**
     * 处理星期的情况
     */
    WEEKS {

        public long toMillis(long d) {
            return x(d, C7 / C2, MAX / (C7 / C2));
        }

        public long toSeconds(long d) {
            return x(d, C7 / C3, MAX / (C7 / C3));
        }

        public long toMinutes(long d) {
            return x(d, C7 / C4, MAX / (C7 / C4));
        }

        public long toHours(long d) {
            return x(d, C7 / C5, MAX / (C7 / C5));
        }

        public long toDays(long d) {
            return x(d, C7 / C6, MAX / (C7 / C6));
        }

        public long toWeeks(long d) {
            return d;
        }

        public long toMonths(long d) {
            return x(d, C7 / C8.get(), MAX / (C7 / C8.get()));
        }

        public long toYears(long d) {
            return x(d, C7 / C9.get(), MAX / (C7 / C9.get()));
        }


        public long convert(long d, TimeEnum u) {
            return u.toWeeks(d);
        }

    },

    /**
     * 处理月的情况
     */
    MONTHS {

        public long toMillis(long d) {
            return x(d, C8.get() / C2, MAX / (C8.get() / C2));
        }

        public long toSeconds(long d) {
            return x(d, C8.get() / C3, MAX / (C8.get() / C3));
        }

        public long toMinutes(long d) {
            return x(d, C8.get() / C4, MAX / (C8.get() / C4));
        }

        public long toHours(long d) {
            return x(d, C8.get() / C5, MAX / (C8.get() / C5));
        }

        public long toDays(long d) {
            return x(d, C8.get() / C6, MAX / (C8.get() / C6));
        }

        public long toWeeks(long d) {
            return x(d, C8.get() / C7, MAX / (C8.get() / C7));
        }

        public long toMonths(long d) {
            return d;
        }

        public long toYears(long d) {
            return d / (C8.get() / C9.get());
        }

        public long convert(long d, TimeEnum u) {
            return u.toMonths(d);
        }

    },
    /**
     * 处理年的情况
     */
    YEARS {

        public long toMillis(long d) {
            return x(d, C9.get() / C2, MAX / (C9.get() / C2));
        }

        public long toSeconds(long d) {
            return x(d, C9.get() / C3, MAX / (C9.get() / C3));
        }

        public long toMinutes(long d) {
            return x(d, C9.get() / C4, MAX / (C9.get() / C4));
        }

        public long toHours(long d) {
            return x(d, C9.get() / C5, MAX / (C9.get() / C5));
        }

        public long toDays(long d) {
            return x(d, C9.get() / C6, MAX / (C9.get() / C6));
        }
        public long toWeeks(long d) {
            return x(d, C9.get() / C7, MAX / (C9.get() / C7));
        }
        public long toMonths(long d) {
            return x(d, C9.get() / C8.get(), MAX / (C9.get() / C8.get()));
        }

        public long toYears(long d) {
            return d;
        }

        public long convert(long d, TimeEnum u) {
            return u.toYears(d);
        }

    };


    // Handy constants for conversion methods
    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L; //一天的小时数

    //获取星期的天数
    static final long C7 = C6 * 7L;

    //获取月的天数
    static final Supplier<Long> C8 = () -> YearMonth.now().lengthOfMonth() * C6;

    //获取年的天数
    static final Supplier<Long> C9 = () -> (long) (C6 * (Year.of(Year.now().getValue()).isLeap() ? 366 : 365));



    static final long MAX = Long.MAX_VALUE;

    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     */
    static long x(long d, long m, long over) {
        if (d > over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    // To maintain full signature compatibility with 1.5, and to improve the
    // clarity of the generated javadoc (see 6287639: Abstract methods in
    // enum classes should not be listed as abstract), method convert
    // etc. are not declared abstract but otherwise act as abstract methods.

    /**
     * Converts the given time duration in the given unit to this unit.
     * Conversions from finer to coarser granularities truncate, so
     * lose precision. For example, converting {@code 999} milliseconds
     * to seconds results in {@code 0}. Conversions from coarser to
     * finer granularities with arguments that would numerically
     * overflow saturate to {@code Long.MIN_VALUE} if negative or
     * {@code Long.MAX_VALUE} if positive.
     *
     * <p>For example, to convert 10 minutes to milliseconds, use:
     * {@code TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)}
     *
     * @param sourceDuration the time duration in the given {@code sourceUnit}
     * @param sourceUnit     the unit of the {@code sourceDuration} argument
     * @return the converted duration in this unit,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long convert(long sourceDuration, TimeEnum sourceUnit) {
        throw new AbstractMethodError();
    }


    /**
     * Equivalent to
     * {@link #convert(long, TimeEnum) MILLISECONDS.convert(duration, this)}.
     *
     * @param duration the duration
     * @return the converted duration,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, TimeEnum) SECONDS.convert(duration, this)}.
     *
     * @param duration the duration
     * @return the converted duration,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, TimeEnum) MINUTES.convert(duration, this)}.
     *
     * @param duration the duration
     * @return the converted duration,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     * @since 1.6
     */
    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to
     * {@link #convert(long, TimeEnum) HOURS.convert(duration, this)}.
     *
     * @param duration the duration
     * @return the converted duration,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     * @since 1.6
     */
    public long toHours(long duration) {
        throw new AbstractMethodError();
    }


    /**
     * Equivalent to
     * {@link #convert(long, TimeEnum) DAYS.convert(duration, this)}.
     *
     * @param duration the duration
     * @return the converted duration
     * @since 1.6
     */
    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    public long toYears(long duration) {
        throw new AbstractMethodError();
    }

    public long toWeeks(long duration) {
        throw new AbstractMethodError();
    }

    public long toMonths(long duration) {
        throw new AbstractMethodError();
    }


}
