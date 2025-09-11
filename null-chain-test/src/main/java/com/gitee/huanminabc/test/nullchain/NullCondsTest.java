package com.gitee.huanminabc.test.nullchain;

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

public class NullCondsTest {


    @Test
    public void testSimpleNumberStreamWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(Conds.gt(5).and(Conds.lt(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15), filtered);
    }

    @Test
    public void testConditionChainInLambda() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = data.stream()
                .filter(v -> Condition.when(v).gt(5).and().lt(20).test())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15), filtered);
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
        Assertions.assertEquals(Arrays.asList(5, 10, 15), filtered);
    }

    @Test
    public void testNullStreamWithConds() {
        List<Integer> data = Arrays.asList(1, 5, 10, 15, 20, null);
        List<Integer> filtered = Null.ofStream(data)
                .filter(Conds.gt(5).and(Conds.lt(20)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15), filtered);
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
        // (>=10 AND <=20) OR ==5, then NOT
        List<Integer> positives = data.stream()
                .filter(Conds.ge(10).and(Conds.le(20)).or(Conds.eq(5)))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(5, 10, 15, 20), positives);

        List<Integer> negatives = data.stream()
                .filter(Conds.ge(10).and(Conds.le(20)).or(Conds.eq(5)).negate())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 25), negatives);
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
        List<Integer> notList = data.stream()
                .filter(Conds.not(Conds.andAll(Conds.ge(10), Conds.le(20))))
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 5, 25), notList);
    }

    @Test
    public void testIfGoWithComparator() {
        User adult = new User("baseline", 18);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        // ifGo：年龄 >= 18 才继续
        Null.of(new User("bob", 20))
                .ifGo(Conds.ge(adult, byAge))
                .then(u -> Assertions.assertEquals("bob", u.getName()));
    }

    @Test
    public void testIfNeGoWithComparatorBetween() {
        User low = new User("low", 18);
        User high = new User("high", 60);
        Comparator<User> byAge = Comparator.comparingInt(u -> u.age);

        // 在区间内则 ifNeGo 为 false -> 继续；不在区间内则 ifNeGo 为 true -> 短路
        Null.of(new User("alice", 30))
                .ifNeGo(Conds.between(low, high, byAge))
                .then(u -> Assertions.assertEquals("alice", u.getName()));

        // 不在 [18,60]，ifNeGo 短路，不会进入 then
        Null.of(new User("old", 75))
                .ifNeGo(Conds.between(low, high, byAge))
                .then(u -> Assertions.fail("should not reach here"));
    }
}


