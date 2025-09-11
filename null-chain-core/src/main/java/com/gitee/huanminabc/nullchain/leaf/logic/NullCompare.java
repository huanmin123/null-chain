package com.gitee.huanminabc.nullchain.leaf.logic;

import java.util.Comparator;
import java.util.Objects;

/**
 * 提供基础的比较工具方法。
 * 约束：仅当 T 实现 Comparable<T> 或显式传入 Comparator<T> 时允许使用大小比较。
 * null 规则：
 *  - eq/ne 使用 Objects.equals 进行判等
 *  - 其余关系比较（>, <, >=, <=）若任一为 null，返回 false
 */
public final class NullCompare {

    private NullCompare() {}

    public static <T> boolean eq(T a, T b) {
        return Objects.equals(a, b);
    }

    public static <T> boolean ne(T a, T b) {
        return !Objects.equals(a, b);
    }

    public static <T extends Comparable<T>> boolean gt(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) > 0;
    }

    public static <T extends Comparable<T>> boolean ge(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) >= 0;
    }

    public static <T extends Comparable<T>> boolean lt(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) < 0;
    }

    public static <T extends Comparable<T>> boolean le(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) <= 0;
    }

    public static <T> boolean gt(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) > 0;
    }

    public static <T> boolean ge(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) >= 0;
    }

    public static <T> boolean lt(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) < 0;
    }

    public static <T> boolean le(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) <= 0;
    }

    private static <T> void requireComparator(Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null for non-Comparable types");
        }
    }
}


