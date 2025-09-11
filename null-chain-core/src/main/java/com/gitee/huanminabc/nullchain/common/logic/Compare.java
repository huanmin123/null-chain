package com.gitee.huanminabc.nullchain.common.logic;

import java.util.Comparator;
import java.util.Objects;

/**
 * 基础比较工具。
 *
 * 约束：仅当 T 实现 {@link Comparable} 或显式传入 {@link Comparator} 时允许使用大小比较。
 * 空值规则：
 *  - {@link #eq(Object, Object)} 与 {@link #ne(Object, Object)} 使用 {@link Objects#equals(Object, Object)}
 *  - 其余关系比较（>, <, >=, <=）若任一参与方为 null，则返回 false
 */
public final class Compare {

    private Compare() {}

    /**
     * 判断相等。
     *
     * @param a 左值，可为 null
     * @param b 右值，可为 null
     * @return 使用 {@link Objects#equals(Object, Object)} 的结果
     */
    public static <T> boolean eq(T a, T b) {
        return Objects.equals(a, b);
    }

    /**
     * 判断不等。
     *
     * @param a 左值，可为 null
     * @param b 右值，可为 null
     * @return 非 {@link #eq(Object, Object)} 的结果
     */
    public static <T> boolean ne(T a, T b) {
        return !Objects.equals(a, b);
    }

    /**
     * 大于：a > b。
     *
     * @param a 左值，非 null 且实现 {@link Comparable}
     * @param b 右值，非 null 且实现 {@link Comparable}
     * @return 若任一为 null 返回 false；否则 a.compareTo(b) > 0
     */
    public static <T extends Comparable<T>> boolean gt(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) > 0;
    }

    /**
     * 大于等于：a >= b。
     */
    public static <T extends Comparable<T>> boolean ge(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) >= 0;
    }

    /**
     * 小于：a < b。
     */
    public static <T extends Comparable<T>> boolean lt(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) < 0;
    }

    /**
     * 小于等于：a <= b。
     */
    public static <T extends Comparable<T>> boolean le(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) <= 0;
    }

    /**
     * 使用自定义比较器判断 a > b。
     *
     * @param comparator 比较器，非 null
     */
    public static <T> boolean gt(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) > 0;
    }

    /**
     * 使用自定义比较器判断 a >= b。
     */
    public static <T> boolean ge(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) >= 0;
    }

    /**
     * 使用自定义比较器判断 a < b。
     */
    public static <T> boolean lt(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) < 0;
    }

    /**
     * 使用自定义比较器判断 a <= b。
     */
    public static <T> boolean le(T a, T b, Comparator<T> comparator) {
        if (a == null || b == null) { return false; }
        requireComparator(comparator);
        return comparator.compare(a, b) <= 0;
    }

    /**
     * 通用比较入口，根据 {@link Op} 路由。
     * 等价于调用 gt/ge/lt/le/eq/ne。
     */
    public static <T> boolean apply(Op op, T a, T b) {
        switch (op) {
            case EQ: return eq(a, b);
            case NE: return ne(a, b);
            case GT: return relationByComparable(a, b, 1);
            case GE: return relationByComparable(a, b, 0) || relationByComparable(a, b, 1);
            case LT: return relationByComparable(a, b, -1);
            case LE: return relationByComparable(a, b, 0) || relationByComparable(a, b, -1);
            default: throw new IllegalStateException("Unknown op: " + op);
        }
    }

    /**
     * 通用比较入口（自定义比较器）。
     */
    public static <T> boolean apply(Op op, T a, T b, Comparator<T> comparator) {
        switch (op) {
            case GT: return gt(a, b, comparator);
            case GE: return ge(a, b, comparator);
            case LT: return lt(a, b, comparator);
            case LE: return le(a, b, comparator);
            case EQ: return eq(a, b);
            case NE: return ne(a, b);
            default: throw new IllegalStateException("Unknown op: " + op);
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean relationByComparable(Object a, Object b, int targetSign) {
        // 返回 true 当 compareTo 符号与 targetSign 匹配；targetSign 可为 -1,0,1
        if (a == null || b == null) { return false; }
        if (!(a instanceof Comparable)) {
            throw new IllegalArgumentException("Type must implement Comparable or provide a Comparator");
        }
        Comparable<Object> ca = (Comparable<Object>) a;
        int cmp = ca.compareTo(b);
        if (targetSign > 0) { return cmp > 0; }
        if (targetSign < 0) { return cmp < 0; }
        return cmp == 0;
    }

    /**
     * 比较器为空时抛出异常。
     */
    private static <T> void requireComparator(Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null for non-Comparable types");
        }
    }
}


