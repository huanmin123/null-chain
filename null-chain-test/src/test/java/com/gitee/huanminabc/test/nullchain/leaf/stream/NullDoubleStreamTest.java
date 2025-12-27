package com.gitee.huanminabc.test.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * NullDoubleStream测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullDoubleStreamTest {

    // ========== sum() 方法测试 ==========

    @Test
    public void testSum() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        double sum = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .sum();
        Assertions.assertEquals(16.5, sum, 0.01);
    }

    @Test
    public void testSumWithEmpty() {
        List<Double> data = Arrays.asList();
        double sum = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .sum();
        Assertions.assertEquals(0.0, sum, 0.01);
    }

    // ========== min() 方法测试 ==========

    @Test
    public void testMin() {
        List<Double> data = Arrays.asList(5.5, 2.2, 8.8, 1.1, 9.9);
        double min = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .min();
        Assertions.assertEquals(1.1, min, 0.01);
    }

    // ========== max() 方法测试 ==========

    @Test
    public void testMax() {
        List<Double> data = Arrays.asList(5.5, 2.2, 8.8, 1.1, 9.9);
        double max = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .max();
        Assertions.assertEquals(9.9, max, 0.01);
    }

    // ========== count() 方法测试 ==========

    @Test
    public void testCount() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        long count = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .count();
        Assertions.assertEquals(5L, count);
    }

    // ========== average() 方法测试 ==========

    @Test
    public void testAverage() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        double avg = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .average();
        Assertions.assertEquals(3.3, avg, 0.01);
    }

    // ========== map() 方法测试 ==========

    @Test
    public void testMap() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        double sum = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .map(x -> x * 2)
                .sum();
        Assertions.assertEquals(33.0, sum, 0.01);
    }

    // ========== flatMap() 方法测试 ==========

    @Test
    public void testFlatMap() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3);
        double sum = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToDouble(Double::doubleValue))
                .sum();
        Assertions.assertEquals(19.8, sum, 0.01);
    }

    // ========== boxed() 方法测试 ==========

    @Test
    public void testBoxed() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        List<Double> boxed = Null.ofStream(data)
                .mapToDouble(Double::doubleValue)
                .boxed()
                .toList();
        Assertions.assertEquals(5, boxed.size());
        Assertions.assertEquals(1.1, boxed.get(0), 0.01);
        Assertions.assertEquals(5.5, boxed.get(4), 0.01);
    }
}

