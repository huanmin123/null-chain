package com.gitee.huanminabc.nullchain.common.logic;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 链式条件构建器，基于 {@link Compare} 的比较规则。
 *
 * 规则说明：
 *  - 相等/不等：允许 null，并使用 {@link Objects#equals(Object, Object)}
 *  - 关系比较：当任一参数为 null 时返回 false；否则遵循 Comparable 或 Comparator 比较
 */
public final class Condition<T> {

    private final T value;
    private Predicate<T> predicate;
    private Comparator<T> comparator;

    private Condition(T value) {
        this.value = value;
        this.predicate = t -> true;
    }

    /**
     * 以给定值创建条件构建器。
     */
    public static <T> Condition<T> when(T value) {
        return new Condition<>(value);
    }

    /**
     * 指定比较器，用于后续的大小比较。
     */
    public Condition<T> withComparator(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    /**
     * 执行已构建的条件。
     */
    public boolean test() {
        return predicate.test(value);
    }

    /**
     * 语义连接器（可读性增强）。
     */
    public Condition<T> and() {
        return this;
    }

    /**
     * 使用 或（OR）连接另一个断言。
     */
    public Condition<T> or(Predicate<T> other) {
        Objects.requireNonNull(other, "other predicate");
        this.predicate = this.predicate.or(other);
        return this;
    }

    /**
     * 取反（NOT）。
     */
    public Condition<T> not() {
        this.predicate = this.predicate.negate();
        return this;
    }

    // ===== 相等/不等 =====
    /** 相等 */
    public Condition<T> eq(T other) {
        this.predicate = this.predicate.and(t -> Compare.eq(t, other));
        return this;
    }

    /** 不等 */
    public Condition<T> ne(T other) {
        this.predicate = this.predicate.and(t -> Compare.ne(t, other));
        return this;
    }

    // ===== 可比较（Comparable 或 withComparator 提供） =====
    /** 大于 */
    public Condition<T> gt(T other) {
        this.predicate = this.predicate.and(t -> compareBy(t, other, Op.GT));
        return this;
    }

    /** 大于等于 */
    public Condition<T> ge(T other) {
        this.predicate = this.predicate.and(t -> compareBy(t, other, Op.GE));
        return this;
    }

    /** 小于 */
    public Condition<T> lt(T other) {
        this.predicate = this.predicate.and(t -> compareBy(t, other, Op.LT));
        return this;
    }

    /** 小于等于 */
    public Condition<T> le(T other) {
        this.predicate = this.predicate.and(t -> compareBy(t, other, Op.LE));
        return this;
    }

    private boolean compareBy(T a, T b, Op op) {
        if (comparator != null) {
            switch (op) {
                case GT:  return Compare.gt(a, b, comparator);
                case GE:  return Compare.ge(a, b, comparator);
                case LT:  return Compare.lt(a, b, comparator);
                case LE:  return Compare.le(a, b, comparator);
                case EQ:  return Compare.eq(a, b);
                case NE:  return Compare.ne(a, b);
                default:  throw new IllegalStateException("Unknown op: " + op);
            }
        }
        // 无 comparator 时，直接通过原生 Comparable 比较以避免泛型上界冲突
        if (a == null || b == null) {
            if (op == Op.EQ) { return Compare.eq(a, b); }
            if (op == Op.NE) { return Compare.ne(a, b); }
            return false;
        }
        if (!(a instanceof Comparable)) {
            throw new IllegalArgumentException("Type must implement Comparable or provide a Comparator");
        }
        @SuppressWarnings("unchecked")
        Comparable<Object> ca = (Comparable<Object>) a;
        int cmp = ca.compareTo(b);
        switch (op) {
            case GT:  return cmp > 0;
            case GE:  return cmp >= 0;
            case LT:  return cmp < 0;
            case LE:  return cmp <= 0;
            case EQ:  return Compare.eq(a, b);
            case NE:  return Compare.ne(a, b);
            default:  throw new IllegalStateException("Unknown op: " + op);
        }
    }
}


