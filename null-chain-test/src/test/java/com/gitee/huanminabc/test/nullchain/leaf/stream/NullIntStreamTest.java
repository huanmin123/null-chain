package com.gitee.huanminabc.test.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * NullIntStream测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullIntStreamTest {

    // ========== sum() 方法测试 ==========

    @Test
    public void testSum() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        int sum = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .sum();
        Assertions.assertEquals(15, sum);
    }

    @Test
    public void testSumWithEmpty() {
        List<Integer> data = Arrays.asList();
        int sum = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .sum();
        Assertions.assertEquals(0, sum);
    }

    @Test
    public void testSumWithNull() {
        int sum = Null.ofStream((List<Integer>) null)
                .mapToInt(Integer::intValue)
                .sum();
        Assertions.assertEquals(0, sum);
    }

    // ========== min() 方法测试 ==========

    @Test
    public void testMin() {
        List<Integer> data = Arrays.asList(5, 2, 8, 1, 9);
        int min = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .min();
        Assertions.assertEquals(1, min);
    }

    @Test
    public void testMinWithEmpty() {
        List<Integer> data = Arrays.asList();
        int min = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .min();
        Assertions.assertEquals(0, min);
    }

    // ========== max() 方法测试 ==========

    @Test
    public void testMax() {
        List<Integer> data = Arrays.asList(5, 2, 8, 1, 9);
        int max = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .max();
        Assertions.assertEquals(9, max);
    }

    @Test
    public void testMaxWithEmpty() {
        List<Integer> data = Arrays.asList();
        int max = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .max();
        Assertions.assertEquals(0, max);
    }

    // ========== count() 方法测试 ==========

    @Test
    public void testCount() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        long count = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .count();
        Assertions.assertEquals(5, count);
    }

    @Test
    public void testCountWithEmpty() {
        List<Integer> data = Arrays.asList();
        long count = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .count();
        Assertions.assertEquals(0, count);
    }

    // ========== average() 方法测试 ==========

    @Test
    public void testAverage() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        double avg = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .average();
        Assertions.assertEquals(3.0, avg, 0.01);
    }

    @Test
    public void testAverageWithEmpty() {
        List<Integer> data = Arrays.asList();
        double avg = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .average();
        Assertions.assertEquals(0.0, avg, 0.01);
    }

    // ========== map() 方法测试 ==========

    @Test
    public void testMap() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        int sum = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .map(x -> x * 2)
                .sum();
        Assertions.assertEquals(30, sum);
    }

    // ========== flatMap() 方法测试 ==========

    @Test
    public void testFlatMap() {
        List<Integer> data = Arrays.asList(1, 2, 3);
        int sum = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToInt(Integer::intValue))
                .sum();
        Assertions.assertEquals(18, sum); // (1+2) + (2+4) + (3+6) = 18
    }

    // ========== boxed() 方法测试 ==========

    @Test
    public void testBoxed() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> boxed = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .boxed()
                .toList();
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5), boxed);
    }

    // ========== 组合测试 ==========

    @Test
    public void testComplexChain() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        double result = Null.ofStream(data)
                .mapToInt(Integer::intValue)
                .map(x -> x * 2)
                .average();
        Assertions.assertEquals(6.0, result, 0.01);
    }
}

