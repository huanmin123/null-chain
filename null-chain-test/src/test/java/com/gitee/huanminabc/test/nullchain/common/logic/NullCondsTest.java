package com.gitee.huanminabc.test.nullchain.common.logic;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.logic.Condition;
import com.gitee.huanminabc.nullchain.common.logic.Conds;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conds 条件工厂测试
 * 
 * 测试 Predicate 工厂功能，包括：
 * - 基本比较操作（eq/ne/gt/ge/lt/le）
 * - 带 Comparator 的比较
 * - 组合操作（andAll/orAny/not）
 * - 区间操作（between）
 * - 边界情况处理
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullCondsTest {


    @Test
    public void testSimpleNumberStreamWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.gt(5).and(Conds.lt(20)))
                .collect(Collectors.toList());
        // gt(5) 表示大于 5，不包含 5；lt(20) 表示小于 20，不包含 20
        Assertions.assertEquals(Arrays.asList(10, 15), filtered);
    }

    @Test
    public void testConditionChainInLambda() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(v -> Condition.when(v).gt(5).and().lt(20).test())
                .collect(Collectors.toList());
        // gt(5) 表示大于 5，不包含 5；lt(20) 表示小于 20，不包含 20
        Assertions.assertEquals(Arrays.asList(10, 15), filtered);
    }

    @Data
    static class User {
        final String name;
        final int age;
        User(String name, int age) { this.name = name; this.age = age; }
    }

    @Test
    public void testComparatorBasedComparison() {
        List<User> users = Arrays.asList(
                new User("a", 18),
                new User("b", 25),
                new User("c", 30)
        );
        User threshold = new User("x", 20);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        List<User> older = users.stream()
                .filter(Conds.gt(threshold, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, older.size());
        Assertions.assertEquals("b", older.get(0).name);
        Assertions.assertEquals("c", older.get(1).name);
    }

    @Test
    public void testNullComparisons() {
        Assertions.assertTrue(Condition.when(null).eq(null).test());
        Assertions.assertTrue(Condition.when(null).ne("x").test());
        Assertions.assertFalse(Condition.when(null).gt("x").test());
        Assertions.assertFalse(Condition.when(null).lt("x").test());
        Assertions.assertFalse(Condition.when(null).gt("x").test());
        Assertions.assertFalse(Condition.when(null).lt("x").test());
    }

    @Test
    public void testJavaStreamWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.gt(5).and(Conds.lt(20)))
                .collect(Collectors.toList());
        // gt(5) 表示大于 5，不包含 5；lt(20) 表示小于 20，不包含 20
        Assertions.assertEquals(Arrays.asList(10, 15), filtered);
    }

    @Test
    public void testNullStreamWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = Null.ofStream(data)
                .filter(Conds.gt(5).and(Conds.lt(20)))
                .collect(Collectors.toList());
        // gt(5) 表示大于 5，不包含 5；lt(20) 表示小于 20，不包含 20
        Assertions.assertEquals(Arrays.asList(10, 15), filtered);
    }

    @Test
    public void testGeLeRangeWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 21, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.ge(5).and(Conds.le(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15, 20), filtered);
    }

    @Test
    public void testConditionGeLeChain() {
        List<Integer> data = Arrays.asList(5, 10, 15, 20, 25);
        List<Integer> filtered = data.stream()
                .filter(v -> Condition.when(v).ge(10).and().le(20).test())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(10, 15, 20), filtered);
    }

    @Test
    public void testAndOrNotCombinations() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 25, null);
        // (>=10 AND <=20) OR ==5
        List<Integer> positives = data.stream()
                .filter(Conds.ge(10).and(Conds.le(20)).or(Conds.eq(5)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15, 20), positives);

        // NOT ((>=10 AND <=20) OR ==5)
        // null 值不满足条件，取反后满足，所以 null 会被包含
        List<Integer> negatives = data.stream()
                .filter(Conds.ge(10).and(Conds.le(20)).or(Conds.eq(5)).negate())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 25, null), negatives);
    }

    @Test
    public void testComparatorGeLe() {
        List<User> users = Arrays.asList(
                new User("a", 18),
                new User("b", 20),
                new User("c", 21)
        );
        User baseline = new User("x", 20);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        List<User> ge = users.stream()
                .filter(Conds.ge(baseline, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, ge.size());
        Assertions.assertEquals("b", ge.get(0).getName());
        Assertions.assertEquals("c", ge.get(1).getName());

        List<User> le = users.stream()
                .filter(Conds.le(baseline, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, le.size());
        Assertions.assertEquals("a", le.get(0).getName());
        Assertions.assertEquals("b", le.get(1).getName());
    }

    @Test
    public void testBetween() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 25, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.between(5, 20))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15, 20), filtered);
    }

    @Test
    public void testAndAllOrAnyNot() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 25, null);

        // andAll: ge(10) AND le(20)
        List<Integer> andAll = data.stream()
                .filter(Conds.andAll(Conds.ge(10), Conds.le(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(10, 15, 20), andAll);

        // orAny: (<5) OR (>20)
        List<Integer> orAny = data.stream()
                .filter(Conds.orAny(Conds.lt(5), Conds.gt(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 25), orAny);

        // not: NOT (ge(10) AND le(20))
        // null 值不满足条件，取反后满足，所以 null 会被包含
        List<Integer> notList = data.stream()
                .filter(Conds.not(Conds.andAll(Conds.ge(10), Conds.le(20))))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 5, 25, null), notList);
    }

    @Test
    public void testIfGoWithComparator() {
        User adult = new User("baseline", 18);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        // ifGo：年龄 >= 18 才继续
        Null.of(new User("bob", 20))
                .ifGo(Conds.ge(adult, byAge))
                .peek(u -> Assertions.assertEquals("bob", u.getName()));
    }

    @Test
    public void testIfNeGoWithComparatorBetween() {
        User low = new User("low", 18);
        User high = new User("high", 60);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        // 在区间内则 ifNeGo 为 false -> 继续；不在区间内则 ifNeGo 为 true -> 短路
        Null.of(new User("alice", 30))
                .ifNeGo(Conds.between(low, high, byAge))
                .peek(u -> Assertions.assertEquals("alice", u.getName()));

        // 不在 [18,60]，ifNeGo 短路，不会进入 then
        Null.of(new User("old", 75))
                .ifNeGo(Conds.between(low, high, byAge))
                .peek(u -> Assertions.fail("should not reach here"));
    }

    // ========== 补充缺失的测试用例 ==========

    @Test
    public void testEq() {
        List<Integer> data = Arrays.asList(1, 2, 3, 2, 5, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.eq(2))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(2, 2), filtered);
    }

    @Test
    public void testNe() {
        List<Integer> data = Arrays.asList(1, 2, 3, 2, 5, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.ne(2))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 3, 5, null), filtered);
    }

    @Test
    public void testLt() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.lt(10))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 5), filtered);
    }

    @Test
    public void testComparatorLt() {
        List<User> users = Arrays.asList(
                new User("a", 18),
                new User("b", 25),
                new User("c", 30)
        );
        User threshold = new User("x", 20);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        List<User> younger = users.stream()
                .filter(Conds.lt(threshold, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(1, younger.size());
        Assertions.assertEquals("a", younger.get(0).name);
    }

    @Test
    public void testComparatorLe() {
        List<User> users = Arrays.asList(
                new User("a", 18),
                new User("b", 20),
                new User("c", 25)
        );
        User threshold = new User("x", 20);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        List<User> le = users.stream()
                .filter(Conds.le(threshold, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, le.size());
        Assertions.assertEquals("a", le.get(0).name);
        Assertions.assertEquals("b", le.get(1).name);
    }

    @Test
    public void testAndAllEdgeCases() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 25);

        // 空数组应该返回恒真（所有元素都通过）
        List<Integer> all = data.stream()
                .filter(Conds.andAll())
                .collect(Collectors.toList());
        Assertions.assertEquals(data, all);

        // null 参数应该被忽略
        List<Integer> result = data.stream()
                .filter(Conds.andAll(Conds.ge(10), null, Conds.le(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(10, 15, 20), result);
    }

    @Test
    public void testOrAnyEdgeCases() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, 25);

        // 空数组应该返回恒假（没有元素通过）
        List<Integer> none = data.stream()
                .filter(Conds.orAny())
                .collect(Collectors.toList());
        Assertions.assertTrue(none.isEmpty());

        // null 参数应该被忽略
        List<Integer> result = data.stream()
                .filter(Conds.orAny(Conds.lt(5), null, Conds.gt(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 25), result);
    }

    @Test
    public void testNotWithNull() {
        // null predicate 应该抛异常
        Assertions.assertThrows(NullPointerException.class, 
            () -> Conds.not(null));
    }

    @Test
    public void testBetweenWithComparator() {
        User low = new User("low", 18);
        User high = new User("high", 60);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        List<User> users = Arrays.asList(
                new User("a", 15),
                new User("b", 25),
                new User("c", 50),
                new User("d", 70)
        );

        List<User> inRange = users.stream()
                .filter(Conds.between(low, high, byAge))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, inRange.size());
        Assertions.assertEquals("b", inRange.get(0).name);
        Assertions.assertEquals("c", inRange.get(1).name);
    }
}

