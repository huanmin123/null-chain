package com.gitee.huanminabc.test.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * NullLongStream测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullLongStreamTest {

    // ========== sum() 方法测试 ==========

    @Test
    public void testSum() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        long sum = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .sum();
        Assertions.assertEquals(15L, sum);
    }

    @Test
    public void testSumWithEmpty() {
        List<Long> data = Arrays.asList();
        long sum = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .sum();
        Assertions.assertEquals(0L, sum);
    }

    // ========== min() 方法测试 ==========

    @Test
    public void testMin() {
        List<Long> data = Arrays.asList(5L, 2L, 8L, 1L, 9L);
        long min = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .min();
        Assertions.assertEquals(1L, min);
    }

    // ========== max() 方法测试 ==========

    @Test
    public void testMax() {
        List<Long> data = Arrays.asList(5L, 2L, 8L, 1L, 9L);
        long max = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .max();
        Assertions.assertEquals(9L, max);
    }

    // ========== count() 方法测试 ==========

    @Test
    public void testCount() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        long count = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .count();
        Assertions.assertEquals(5L, count);
    }

    // ========== average() 方法测试 ==========

    @Test
    public void testAverage() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        double avg = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .average();
        Assertions.assertEquals(3.0, avg, 0.01);
    }

    // ========== map() 方法测试 ==========

    @Test
    public void testMap() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        long sum = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .map(x -> x * 2)
                .sum();
        Assertions.assertEquals(30L, sum);
    }

    // ========== flatMap() 方法测试 ==========

    @Test
    public void testFlatMap() {
        List<Long> data = Arrays.asList(1L, 2L, 3L);
        long sum = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToLong(Long::longValue))
                .sum();
        Assertions.assertEquals(18L, sum);
    }

    // ========== boxed() 方法测试 ==========

    @Test
    public void testBoxed() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        List<Long> boxed = Null.ofStream(data)
                .mapToLong(Long::longValue)
                .boxed()
                .toList();
        Assertions.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), boxed);
    }
}

