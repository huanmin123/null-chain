package com.gitee.huanminabc.test.nullchain.common.logic;

import com.gitee.huanminabc.nullchain.common.logic.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Condition 条件构建器测试
 * 
 * 测试链式条件构建功能，包括：
 * - 基本比较操作
 * - 链式组合（and/or/not）
 * - withComparator 功能
 * - null 值处理
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class ConditionTest {

    // ========== 基本比较操作测试 ==========

    @Test
    public void testEq() {
        Assertions.assertTrue(Condition.when(5).eq(5).test());
        Assertions.assertFalse(Condition.when(5).eq(3).test());
        Assertions.assertTrue(Condition.when(null).eq(null).test());
        Assertions.assertFalse(Condition.when(null).eq(5).test());
    }

    @Test
    public void testNe() {
        Assertions.assertTrue(Condition.when(5).ne(3).test());
        Assertions.assertFalse(Condition.when(5).ne(5).test());
        Assertions.assertTrue(Condition.when(null).ne(5).test());
        Assertions.assertFalse(Condition.when(null).ne(null).test());
    }

    @Test
    public void testGt() {
        Assertions.assertTrue(Condition.when(5).gt(3).test());
        Assertions.assertFalse(Condition.when(3).gt(5).test());
        Assertions.assertFalse(Condition.when(5).gt(5).test());
        Assertions.assertFalse(Condition.when(null).gt(5).test());
    }

    @Test
    public void testGe() {
        Assertions.assertTrue(Condition.when(5).ge(3).test());
        Assertions.assertTrue(Condition.when(5).ge(5).test());
        Assertions.assertFalse(Condition.when(3).ge(5).test());
        Assertions.assertFalse(Condition.when(null).ge(5).test());
    }

    @Test
    public void testLt() {
        Assertions.assertTrue(Condition.when(3).lt(5).test());
        Assertions.assertFalse(Condition.when(5).lt(3).test());
        Assertions.assertFalse(Condition.when(5).lt(5).test());
        Assertions.assertFalse(Condition.when(null).lt(5).test());
    }

    @Test
    public void testLe() {
        Assertions.assertTrue(Condition.when(3).le(5).test());
        Assertions.assertTrue(Condition.when(5).le(5).test());
        Assertions.assertFalse(Condition.when(5).le(3).test());
        Assertions.assertFalse(Condition.when(null).le(5).test());
    }

    // ========== 链式组合测试 ==========

    @Test
    public void testAnd() {
        // and() 是语义连接器，不影响逻辑
        Assertions.assertTrue(Condition.when(5).gt(3).and().lt(10).test());
        Assertions.assertFalse(Condition.when(5).gt(3).and().lt(5).test());
        Assertions.assertFalse(Condition.when(5).gt(10).and().lt(20).test());
    }

    @Test
    public void testOr() {
        Predicate<Integer> p2 = x -> x < 5;
        
        // 5 不满足 gt(10)，也不满足 p2
        Assertions.assertFalse(Condition.when(5).gt(10).or(p2).test());
        
        // 15 满足 gt(10)
        Assertions.assertTrue(Condition.when(15).gt(10).or(p2).test());
        
        // 3 满足 p2
        Assertions.assertTrue(Condition.when(3).gt(10).or(p2).test());
        
        // null predicate 应该抛异常
        Assertions.assertThrows(NullPointerException.class, 
            () -> Condition.when(5).gt(3).or(null));
    }

    @Test
    public void testNot() {
        Assertions.assertFalse(Condition.when(5).gt(3).not().test());
        Assertions.assertTrue(Condition.when(3).gt(5).not().test());
        
        // 组合使用
        Assertions.assertFalse(Condition.when(5).eq(5).not().test());
        Assertions.assertTrue(Condition.when(5).eq(3).not().test());
    }

    @Test
    public void testComplexChain() {
        // (x > 3) AND (x < 10) AND NOT (x == 5)
        boolean result1 = Condition.when(4)
            .gt(3).and().lt(10).and().ne(5)
            .test();
        Assertions.assertTrue(result1);
        
        boolean result2 = Condition.when(5)
            .gt(3).and().lt(10).and().ne(5)
            .test();
        Assertions.assertFalse(result2);
        
        // (x > 10) OR (x < 5)
        Predicate<Integer> lt5 = x -> x < 5;
        boolean result3 = Condition.when(15)
            .gt(10).or(lt5)
            .test();
        Assertions.assertTrue(result3);
        
        boolean result4 = Condition.when(7)
            .gt(10).or(lt5)
            .test();
        Assertions.assertFalse(result4);
    }

    // ========== withComparator 测试 ==========

    static class Person {
        final String name;
        final int age;
        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void testWithComparator() {
        Comparator<Person> byAge = Comparator.comparingInt(p -> p.age);
        Person p1 = new Person("Alice", 20);
        Person p2 = new Person("Bob", 25);
        Person baseline = new Person("Baseline", 22);
        
        // 使用 Comparator 进行比较
        Assertions.assertTrue(Condition.when(p2)
            .withComparator(byAge)
            .gt(baseline)
            .test());
        
        Assertions.assertTrue(Condition.when(p1)
            .withComparator(byAge)
            .lt(baseline)
            .test());
        
        Assertions.assertTrue(Condition.when(p2)
            .withComparator(byAge)
            .ge(baseline)
            .test());
        
        Assertions.assertTrue(Condition.when(p1)
            .withComparator(byAge)
            .le(baseline)
            .test());
    }

    @Test
    public void testWithComparatorChain() {
        Comparator<Person> byAge = Comparator.comparingInt(p -> p.age);
        Person p = new Person("Alice", 25);
        Person low = new Person("Low", 20);
        Person high = new Person("High", 30);
        
        // 链式组合 withComparator
        Assertions.assertTrue(Condition.when(p)
            .withComparator(byAge)
            .ge(low).and().le(high)
            .test());
        
        Assertions.assertFalse(Condition.when(p)
            .withComparator(byAge)
            .gt(high)
            .test());
    }

    // ========== 边界情况测试 ==========

    @Test
    public void testEmptyCondition() {
        // 空条件应该返回 true（初始 predicate 是恒真）
        Assertions.assertTrue(Condition.when(5).test());
        Assertions.assertTrue(Condition.when(null).test());
    }

    @Test
    public void testNullValue() {
        // null 值的比较
        Assertions.assertTrue(Condition.when(null).eq(null).test());
        Assertions.assertTrue(Condition.when(null).ne("x").test());
        Assertions.assertFalse(Condition.when(null).gt("x").test());
        Assertions.assertFalse(Condition.when(null).lt("x").test());
        Assertions.assertFalse(Condition.when(null).ge("x").test());
        Assertions.assertFalse(Condition.when(null).le("x").test());
    }

    @Test
    public void testNonComparableType() {
        class NonComparable {
            int value;
        }
        
        NonComparable a = new NonComparable();
        NonComparable b = new NonComparable();
        
        // 非 Comparable 类型应该抛异常（除非提供 Comparator）
        Assertions.assertThrows(IllegalArgumentException.class, 
            () -> Condition.when(a).gt(b).test());
        
        // 提供 Comparator 后应该正常工作
        Comparator<NonComparable> comp = Comparator.comparingInt(x -> x.value);
        a.value = 5;
        b.value = 3;
        Assertions.assertTrue(Condition.when(a)
            .withComparator(comp)
            .gt(b)
            .test());
    }
}

