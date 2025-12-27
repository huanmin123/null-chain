package com.gitee.huanminabc.test.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.Null;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NullStream 新增方法测试用例
 * 
 * @author huanmin
 */
public class NullStreamTest {

    // ========== toArray() 测试 ==========

    @Test
    public void testToArray() {
        List<String> data = Arrays.asList("a", "b", "c");
        Object[] array = Null.ofStream(data).toArray();
        
        Assertions.assertEquals(3, array.length);
        Assertions.assertEquals("a", array[0]);
        Assertions.assertEquals("b", array[1]);
        Assertions.assertEquals("c", array[2]);
    }

    @Test
    public void testToArrayWithGenerator() {
        List<String> data = Arrays.asList("a", "b", "c");
        String[] array = Null.ofStream(data).toArray(String[]::new);
        
        Assertions.assertEquals(3, array.length);
        Assertions.assertEquals("a", array[0]);
        Assertions.assertEquals("b", array[1]);
        Assertions.assertEquals("c", array[2]);
    }

    @Test
    public void testToArrayWithEmptyStream() {
        List<String> data = Arrays.asList();
        Object[] array = Null.ofStream(data).toArray();
        
        Assertions.assertEquals(0, array.length);
    }

    @Test
    public void testToArrayWithNullStream() {
        Object[] array = Null.ofStream((List<String>) null).toArray();
        
        Assertions.assertEquals(0, array.length);
    }

    @Test
    public void testToArrayWithIntegerArray() {

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        Integer[] array = Null.ofStream(data).toArray(Integer[]::new);
        
        Assertions.assertEquals(5, array.length);
        Assertions.assertEquals(Integer.valueOf(1), array[0]);
        Assertions.assertEquals(Integer.valueOf(5), array[4]);
    }

    // ========== flatMapToInt() 测试 ==========

    @Test
    public void testFlatMapToInt() {
        List<List<Integer>> data = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(3, 4),
            Arrays.asList(5, 6)
        );
        
        int sum = Null.ofStream(data)
            .flatMapToInt(list -> Null.ofStream(list).mapToInt(Integer::intValue))
            .sum();
        
        Assertions.assertEquals(21, sum); // 1+2+3+4+5+6 = 21
    }

    @Test
    public void testFlatMapToIntWithEmptyLists() {
        List<List<Integer>> data = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(),
            Arrays.asList(3, 4)
        );
        
        int sum = Null.ofStream(data)
            .flatMapToInt(list -> Null.ofStream(list).mapToInt(Integer::intValue))
            .sum();
        
        Assertions.assertEquals(10, sum); // 1+2+3+4 = 10
    }

    @Test
    public void testFlatMapToIntWithNullStream() {
        int sum = Null.ofStream((List<List<Integer>>) null)
            .flatMapToInt(list -> Null.ofStream(list).mapToInt(Integer::intValue))
            .sum();
        
        Assertions.assertEquals(0, sum);
    }

    // ========== flatMapToLong() 测试 ==========

    @Test
    public void testFlatMapToLong() {
        List<List<Long>> data = Arrays.asList(
            Arrays.asList(1L, 2L),
            Arrays.asList(3L, 4L),
            Arrays.asList(5L, 6L)
        );
        
        long sum = Null.ofStream(data)
            .flatMapToLong(list -> Null.ofStream(list).mapToLong(Long::longValue))
            .sum();
        
        Assertions.assertEquals(21L, sum); // 1+2+3+4+5+6 = 21
    }

    @Test
    public void testFlatMapToLongWithEmptyLists() {
        List<List<Long>> data = Arrays.asList(
            Arrays.asList(1L, 2L),
            Arrays.asList(),
            Arrays.asList(3L, 4L)
        );
        
        long sum = Null.ofStream(data)
            .flatMapToLong(list -> Null.ofStream(list).mapToLong(Long::longValue))
            .sum();
        
        Assertions.assertEquals(10L, sum); // 1+2+3+4 = 10
    }

    // ========== flatMapToDouble() 测试 ==========

    @Test
    public void testFlatMapToDouble() {
        List<List<Double>> data = Arrays.asList(
            Arrays.asList(1.1, 2.2),
            Arrays.asList(3.3, 4.4),
            Arrays.asList(5.5, 6.6)
        );
        
        double sum = Null.ofStream(data)
            .flatMapToDouble(list -> Null.ofStream(list).mapToDouble(Double::doubleValue))
            .sum();
        
        Assertions.assertEquals(23.1, sum, 0.01); // 1.1+2.2+3.3+4.4+5.5+6.6 = 23.1
    }

    @Test
    public void testFlatMapToDoubleWithEmptyLists() {
        List<List<Double>> data = Arrays.asList(
            Arrays.asList(1.1, 2.2),
            Arrays.asList(),
            Arrays.asList(3.3, 4.4)
        );
        
        double sum = Null.ofStream(data)
            .flatMapToDouble(list -> Null.ofStream(list).mapToDouble(Double::doubleValue))
            .sum();
        
        Assertions.assertEquals(11.0, sum, 0.01); // 1.1+2.2+3.3+4.4 = 11.0
    }

    // ========== 数值流 map() 测试 ==========

    @Test
    public void testIntStreamMap() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        
        int sum = Null.ofStream(data)
            .mapToInt(Integer::intValue)
            .map(x -> x * 2)
            .sum();
        
        Assertions.assertEquals(30, sum); // (1+2+3+4+5)*2 = 30
    }

    @Test
    public void testLongStreamMap() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        
        long sum = Null.ofStream(data)
            .mapToLong(Long::longValue)
            .map(x -> x * 2)
            .sum();
        
        Assertions.assertEquals(30L, sum); // (1+2+3+4+5)*2 = 30
    }

    @Test
    public void testDoubleStreamMap() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        
        double sum = Null.ofStream(data)
            .mapToDouble(Double::doubleValue)
            .map(x -> x * 2)
            .sum();
        
        Assertions.assertEquals(33.0, sum, 0.01); // (1.1+2.2+3.3+4.4+5.5)*2 = 33.0
    }

    // ========== 数值流 flatMap() 测试 ==========

    @Test
    public void testIntStreamFlatMap() {
        List<Integer> data = Arrays.asList(1, 2, 3);
        
        int sum = Null.ofStream(data)
            .mapToInt(Integer::intValue)
            .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToInt(Integer::intValue))
            .sum();
        
        Assertions.assertEquals(18, sum); // (1+2) + (2+4) + (3+6) = 18
    }

    @Test
    public void testLongStreamFlatMap() {
        List<Long> data = Arrays.asList(1L, 2L, 3L);
        
        long sum = Null.ofStream(data)
            .mapToLong(Long::longValue)
            .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToLong(Long::longValue))
            .sum();
        
        Assertions.assertEquals(18L, sum); // (1+2) + (2+4) + (3+6) = 18
    }

    @Test
    public void testDoubleStreamFlatMap() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3);
        
        double sum = Null.ofStream(data)
            .mapToDouble(Double::doubleValue)
            .flatMap(x -> Null.ofStream(Arrays.asList(x, x * 2)).mapToDouble(Double::doubleValue))
            .sum();
        
        Assertions.assertEquals(19.8, sum, 0.01); // (1.1+2.2) + (2.2+4.4) + (3.3+6.6) = 19.8
    }

    // ========== 数值流 boxed() 测试 ==========

    @Test
    public void testIntStreamBoxed() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        
        List<Integer> boxed = Null.ofStream(data)
            .mapToInt(Integer::intValue)
            .boxed()
            .toList();
        
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5), boxed);
    }

    @Test
    public void testLongStreamBoxed() {
        List<Long> data = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        
        List<Long> boxed = Null.ofStream(data)
            .mapToLong(Long::longValue)
            .boxed()
            .toList();
        
        Assertions.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), boxed);
    }

    @Test
    public void testDoubleStreamBoxed() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        
        List<Double> boxed = Null.ofStream(data)
            .mapToDouble(Double::doubleValue)
            .boxed()
            .toList();
        
        Assertions.assertEquals(5, boxed.size());
        Assertions.assertEquals(1.1, boxed.get(0), 0.01);
        Assertions.assertEquals(5.5, boxed.get(4), 0.01);
    }

    // ========== 组合测试 ==========

    @Test
    public void testComplexChain() {
        List<List<Integer>> data = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8)
        );
        
        // 扁平化 -> 映射为整数流 -> 每个数乘以2 -> 装箱 -> 转为数组
        Integer[] result = Null.ofStream(data)
            .flatMapToInt(list -> Null.ofStream(list).mapToInt(Integer::intValue))
            .map(x -> x * 2)
            .boxed()
            .toArray(Integer[]::new);
        
        Assertions.assertEquals(8, result.length);
        Assertions.assertEquals(Integer.valueOf(2), result[0]); // 1*2
        Assertions.assertEquals(Integer.valueOf(16), result[7]); // 8*2
    }

    @Test
    public void testIntStreamMapAndAverage() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        
        double avg = Null.ofStream(data)
            .mapToInt(Integer::intValue)
            .map(x -> x * 2)
            .average();
        
        Assertions.assertEquals(6.0, avg, 0.01); // (2+4+6+8+10)/5 = 6.0
    }

    @Test
    public void testLongStreamFlatMapAndSum() {
        List<Long> data = Arrays.asList(1L, 2L, 3L);
        
        long sum = Null.ofStream(data)
            .mapToLong(Long::longValue)
            .flatMap(x -> {
                // 为每个数生成 [x, x*2] 的流
                return Null.ofStream(Arrays.asList(x, x * 2)).mapToLong(Long::longValue);
            })
            .sum();
        
        Assertions.assertEquals(18L, sum); // (1+2) + (2+4) + (3+6) = 3 + 6 + 9 = 18
    }

    @Test
    public void testDoubleStreamMapAndMax() {
        List<Double> data = Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5);
        
        double max = Null.ofStream(data)
            .mapToDouble(Double::doubleValue)
            .map(x -> x * 2)
            .max();
        
        Assertions.assertEquals(11.0, max, 0.01); // 5.5*2 = 11.0
    }

    @Test
    public void testEmptyIntStreamBoxed() {
        List<Integer> empty = Arrays.asList();
        
        List<Integer> boxed = Null.ofStream(empty)
            .mapToInt(Integer::intValue)
            .boxed()
            .toList();
        
        Assertions.assertTrue(boxed.isEmpty());
    }

    @Test
    public void testNullStreamToArray() {
        Object[] array = Null.ofStream((List<String>) null)
            .toArray(String[]::new);
        
        Assertions.assertEquals(0, array.length);

        List<String> data =new ArrayList<>();
        Object[] array1 = Null.ofStream(data)
                .toArray(String[]::new);

        Assertions.assertEquals(0, array1.length);
    }
}

