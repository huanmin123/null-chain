package com.gitee.huanminabc.test.nullchain.common.logic;

import com.gitee.huanminabc.nullchain.common.logic.Compare;
import com.gitee.huanminabc.nullchain.common.logic.Op;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

/**
 * Compare 工具类测试
 * 
 * 测试基础比较功能，包括：
 * - 相等/不等比较
 * - 大小比较（Comparable）
 * - 带 Comparator 的比较
 * - 通用 apply 方法
 * - null 值处理
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class CompareTest {

    // ========== 相等/不等比较测试 ==========

    @Test
    public void testEq() {
        // 正常值相等
        Assertions.assertTrue(Compare.eq(1, 1));
        Assertions.assertTrue(Compare.eq("hello", "hello"));
        
        // 不相等
        Assertions.assertFalse(Compare.eq(1, 2));
        Assertions.assertFalse(Compare.eq("hello", "world"));
        
        // null 值处理
        Assertions.assertTrue(Compare.eq(null, null));
        Assertions.assertFalse(Compare.eq(null, "x"));
        Assertions.assertFalse(Compare.eq("x", null));
    }

    @Test
    public void testNe() {
        // 正常值不等
        Assertions.assertTrue(Compare.ne(1, 2));
        Assertions.assertTrue(Compare.ne("hello", "world"));
        
        // 相等
        Assertions.assertFalse(Compare.ne(1, 1));
        Assertions.assertFalse(Compare.ne("hello", "hello"));
        
        // null 值处理
        Assertions.assertFalse(Compare.ne(null, null));
        Assertions.assertTrue(Compare.ne(null, "x"));
        Assertions.assertTrue(Compare.ne("x", null));
    }

    // ========== Comparable 大小比较测试 ==========

    @Test
    public void testGt() {
        // 正常比较
        Assertions.assertTrue(Compare.gt(5, 3));
        Assertions.assertTrue(Compare.gt("z", "a"));
        Assertions.assertFalse(Compare.gt(3, 5));
        Assertions.assertFalse(Compare.gt(5, 5));
        
        // null 值处理
        Assertions.assertFalse(Compare.gt(null, 5));
        Assertions.assertFalse(Compare.gt(5, null));
        Assertions.assertFalse(Compare.gt(null, null));
    }

    @Test
    public void testGe() {
        // 正常比较
        Assertions.assertTrue(Compare.ge(5, 3));
        Assertions.assertTrue(Compare.ge(5, 5));
        Assertions.assertTrue(Compare.ge("z", "a"));
        Assertions.assertTrue(Compare.ge("a", "a"));
        Assertions.assertFalse(Compare.ge(3, 5));
        
        // null 值处理
        Assertions.assertFalse(Compare.ge(null, 5));
        Assertions.assertFalse(Compare.ge(5, null));
        Assertions.assertFalse(Compare.ge(null, null));
    }

    @Test
    public void testLt() {
        // 正常比较
        Assertions.assertTrue(Compare.lt(3, 5));
        Assertions.assertTrue(Compare.lt("a", "z"));
        Assertions.assertFalse(Compare.lt(5, 3));
        Assertions.assertFalse(Compare.lt(5, 5));
        
        // null 值处理
        Assertions.assertFalse(Compare.lt(null, 5));
        Assertions.assertFalse(Compare.lt(5, null));
        Assertions.assertFalse(Compare.lt(null, null));
    }

    @Test
    public void testLe() {
        // 正常比较
        Assertions.assertTrue(Compare.le(3, 5));
        Assertions.assertTrue(Compare.le(5, 5));
        Assertions.assertTrue(Compare.le("a", "z"));
        Assertions.assertTrue(Compare.le("a", "a"));
        Assertions.assertFalse(Compare.le(5, 3));
        
        // null 值处理
        Assertions.assertFalse(Compare.le(null, 5));
        Assertions.assertFalse(Compare.le(5, null));
        Assertions.assertFalse(Compare.le(null, null));
    }

    // ========== 带 Comparator 的比较测试 ==========

    @Test
    public void testGtWithComparator() {
        Comparator<Integer> comparator = Integer::compareTo;
        
        // 正常比较
        Assertions.assertTrue(Compare.gt(5, 3, comparator));
        Assertions.assertFalse(Compare.gt(3, 5, comparator));
        Assertions.assertFalse(Compare.gt(5, 5, comparator));
        
        // null 值处理
        Assertions.assertFalse(Compare.gt(null, 5, comparator));
        Assertions.assertFalse(Compare.gt(5, null, comparator));
        Assertions.assertFalse(Compare.gt(null, null, comparator));
        
        // null comparator 应该抛异常
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.gt(5, 3, null));
    }

    @Test
    public void testGeWithComparator() {
        Comparator<Integer> comparator = Integer::compareTo;
        
        // 正常比较
        Assertions.assertTrue(Compare.ge(5, 3, comparator));
        Assertions.assertTrue(Compare.ge(5, 5, comparator));
        Assertions.assertFalse(Compare.ge(3, 5, comparator));
        
        // null 值处理
        Assertions.assertFalse(Compare.ge(null, 5, comparator));
        Assertions.assertFalse(Compare.ge(5, null, comparator));
        
        // null comparator 应该抛异常
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.ge(5, 3, null));
    }

    @Test
    public void testLtWithComparator() {
        Comparator<Integer> comparator = Integer::compareTo;
        
        // 正常比较
        Assertions.assertTrue(Compare.lt(3, 5, comparator));
        Assertions.assertFalse(Compare.lt(5, 3, comparator));
        Assertions.assertFalse(Compare.lt(5, 5, comparator));
        
        // null 值处理
        Assertions.assertFalse(Compare.lt(null, 5, comparator));
        Assertions.assertFalse(Compare.lt(5, null, comparator));
        
        // null comparator 应该抛异常
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.lt(3, 5, null));
    }

    @Test
    public void testLeWithComparator() {
        Comparator<Integer> comparator = Integer::compareTo;
        
        // 正常比较
        Assertions.assertTrue(Compare.le(3, 5, comparator));
        Assertions.assertTrue(Compare.le(5, 5, comparator));
        Assertions.assertFalse(Compare.le(5, 3, comparator));
        
        // null 值处理
        Assertions.assertFalse(Compare.le(null, 5, comparator));
        Assertions.assertFalse(Compare.le(5, null, comparator));
        
        // null comparator 应该抛异常
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.le(3, 5, null));
    }

    // ========== 自定义对象 Comparator 测试 ==========

    static class Person {
        final String name;
        final int age;
        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void testComparatorWithCustomObject() {
        Comparator<Person> byAge = Comparator.comparingInt(p -> p.age);
        
        Person p1 = new Person("Alice", 20);
        Person p2 = new Person("Bob", 25);
        Person p3 = new Person("Charlie", 20);
        
        // 年龄比较
        Assertions.assertTrue(Compare.gt(p2, p1, byAge));
        Assertions.assertTrue(Compare.ge(p2, p1, byAge));
        Assertions.assertTrue(Compare.ge(p1, p3, byAge));
        Assertions.assertTrue(Compare.lt(p1, p2, byAge));
        Assertions.assertTrue(Compare.le(p1, p2, byAge));
        Assertions.assertTrue(Compare.le(p1, p3, byAge));
    }

    // ========== apply 方法测试 ==========

    @Test
    public void testApply() {
        // EQ
        Assertions.assertTrue(Compare.apply(Op.EQ, 5, 5));
        Assertions.assertFalse(Compare.apply(Op.EQ, 5, 3));
        Assertions.assertTrue(Compare.apply(Op.EQ, null, null));
        
        // NE
        Assertions.assertTrue(Compare.apply(Op.NE, 5, 3));
        Assertions.assertFalse(Compare.apply(Op.NE, 5, 5));
        Assertions.assertFalse(Compare.apply(Op.NE, null, null));
        
        // GT
        Assertions.assertTrue(Compare.apply(Op.GT, 5, 3));
        Assertions.assertFalse(Compare.apply(Op.GT, 3, 5));
        Assertions.assertFalse(Compare.apply(Op.GT, null, 5));
        
        // GE
        Assertions.assertTrue(Compare.apply(Op.GE, 5, 3));
        Assertions.assertTrue(Compare.apply(Op.GE, 5, 5));
        Assertions.assertFalse(Compare.apply(Op.GE, 3, 5));
        
        // LT
        Assertions.assertTrue(Compare.apply(Op.LT, 3, 5));
        Assertions.assertFalse(Compare.apply(Op.LT, 5, 3));
        Assertions.assertFalse(Compare.apply(Op.LT, null, 5));
        
        // LE
        Assertions.assertTrue(Compare.apply(Op.LE, 3, 5));
        Assertions.assertTrue(Compare.apply(Op.LE, 5, 5));
        Assertions.assertFalse(Compare.apply(Op.LE, 5, 3));
    }

    @Test
    public void testApplyWithComparator() {
        Comparator<Integer> comparator = Integer::compareTo;
        
        // EQ
        Assertions.assertTrue(Compare.apply(Op.EQ, 5, 5, comparator));
        Assertions.assertFalse(Compare.apply(Op.EQ, 5, 3, comparator));
        
        // NE
        Assertions.assertTrue(Compare.apply(Op.NE, 5, 3, comparator));
        Assertions.assertFalse(Compare.apply(Op.NE, 5, 5, comparator));
        
        // GT
        Assertions.assertTrue(Compare.apply(Op.GT, 5, 3, comparator));
        Assertions.assertFalse(Compare.apply(Op.GT, 3, 5, comparator));
        
        // GE
        Assertions.assertTrue(Compare.apply(Op.GE, 5, 3, comparator));
        Assertions.assertTrue(Compare.apply(Op.GE, 5, 5, comparator));
        
        // LT
        Assertions.assertTrue(Compare.apply(Op.LT, 3, 5, comparator));
        Assertions.assertFalse(Compare.apply(Op.LT, 5, 3, comparator));
        
        // LE
        Assertions.assertTrue(Compare.apply(Op.LE, 3, 5, comparator));
        Assertions.assertTrue(Compare.apply(Op.LE, 5, 5, comparator));
    }

    // ========== 异常情况测试 ==========

    @Test
    public void testNonComparableType() {
        // 非 Comparable 类型应该抛异常
        class NonComparable {
            // 测试用的非 Comparable 类型
        }
        
        NonComparable a = new NonComparable();
        NonComparable b = new NonComparable();
        
        // 通过 apply 方法测试非 Comparable 类型
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.apply(Op.GT, a, b));
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.apply(Op.GE, a, b));
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.apply(Op.LT, a, b));
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Compare.apply(Op.LE, a, b));
    }
}

