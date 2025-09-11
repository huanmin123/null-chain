package com.gitee.huanminabc.nullchain.common.logic;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Predicate 工厂，基于 {@link Condition} 的简写。
 *
 * 用于直接在 Java Stream/NullStream 中进行条件筛选：
 * <pre>
 *   list.stream().filter(Conds.ge(10).and(Conds.le(20))).toList();
 * </pre>
 */
public final class Conds {

    private Conds() {}

    // ===== 相等/不等 =====
    /** 相等 */
    public static <T> Predicate<T> eq(T other) {
        return v -> Condition.when(v).eq(other).test();
    }

    /** 不等 */
    public static <T> Predicate<T> ne(T other) {
        return v -> Condition.when(v).ne(other).test();
    }

    // ===== 可比较（Comparable 路径） =====
    /** 大于 */
    public static <T> Predicate<T> gt(T other) {
        return v -> Condition.when(v).gt(other).test();
    }

    /** 大于等于 */
    public static <T> Predicate<T> ge(T other) {
        return v -> Condition.when(v).ge(other).test();
    }

    /** 小于 */
    public static <T> Predicate<T> lt(T other) {
        return v -> Condition.when(v).lt(other).test();
    }

    /** 小于等于 */
    public static <T> Predicate<T> le(T other) {
        return v -> Condition.when(v).le(other).test();
    }

    // ===== 显式 Comparator 路径（参数语义：baseline/comparator） =====
    /** 使用比较器的大于 */
    public static <T> Predicate<T> gt(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).gt(baseline).test();
    }

    /** 使用比较器的大于等于 */
    public static <T> Predicate<T> ge(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).ge(baseline).test();
    }

    /** 使用比较器的小于 */
    public static <T> Predicate<T> lt(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).lt(baseline).test();
    }

    /** 使用比较器的小于等于 */
    public static <T> Predicate<T> le(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).le(baseline).test();
    }

    // ===== 组合与区间 =====
    /**
     * 将多个 Predicate 取并且（AND）。空数组返回恒真。
     */
    @SafeVarargs
    public static <T> Predicate<T> andAll(Predicate<T>... predicates) {
        Predicate<T> p = t -> true;
        if (predicates == null) { return p; }
        for (Predicate<T> it : predicates) {
            if (it != null) { p = p.and(it); }
        }
        return p;
    }

    /**
     * 将多个 Predicate 取或（OR）。空数组返回恒假。
     */
    @SafeVarargs
    public static <T> Predicate<T> orAny(Predicate<T>... predicates) {
        Predicate<T> p = t -> false;
        if (predicates == null) { return p; }
        for (Predicate<T> it : predicates) {
            if (it != null) { p = p.or(it); }
        }
        return p;
    }

    /**
     * 取反（NOT）。
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return predicate.negate();
    }

    /**
     * 区间：[low, high]。等价 ge(low).and(le(high))。
     */
    public static <T> Predicate<T> between(T low, T high) {
        return ge(low).and(le(high));
    }

    /**
     * 带比较器的区间：[low, high]。
     */
    public static <T> Predicate<T> between(T low, T high, Comparator<T> comparator) {
        return ge(low, comparator).and(le(high, comparator));
    }
}


