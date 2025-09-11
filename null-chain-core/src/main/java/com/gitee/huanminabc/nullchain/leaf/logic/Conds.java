package com.gitee.huanminabc.nullchain.leaf.logic;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Predicate 工厂，基于 Condition 封装的简写。
 */
public final class Conds {

    private Conds() {}

    // ===== 相等/不等 =====
    public static <T> Predicate<T> eq(T other) {
        return v -> Condition.when(v).eq(other).test();
    }

    public static <T> Predicate<T> ne(T other) {
        return v -> Condition.when(v).ne(other).test();
    }

    // ===== 可比较（Comparable 路径） =====
    public static <T> Predicate<T> gt(T other) {
        return v -> Condition.when(v).gt(other).test();
    }

    public static <T> Predicate<T> ge(T other) {
        return v -> Condition.when(v).ge(other).test();
    }

    public static <T> Predicate<T> lt(T other) {
        return v -> Condition.when(v).lt(other).test();
    }

    public static <T> Predicate<T> le(T other) {
        return v -> Condition.when(v).le(other).test();
    }

    // ===== 显式 Comparator 路径（参数语义：baseline/comparator） =====
    public static <T> Predicate<T> gt(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).gt(baseline).test();
    }

    public static <T> Predicate<T> ge(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).ge(baseline).test();
    }

    public static <T> Predicate<T> lt(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).lt(baseline).test();
    }

    public static <T> Predicate<T> le(T baseline, Comparator<T> comparator) {
        return v -> Condition.<T>when(v).withComparator(comparator).le(baseline).test();
    }

}


