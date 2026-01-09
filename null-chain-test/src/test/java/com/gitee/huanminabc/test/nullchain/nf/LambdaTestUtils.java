package com.gitee.huanminabc.test.nullchain.nf;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Lambda 测试工具类
 * 提供 Java 方法供 NF 脚本调用
 *
 * @author huanmin
 * @date 2025/01/08
 */
public class LambdaTestUtils {

    /**
     * 应用 Function 到值
     */
    public static Integer applyFunction(Function<Integer, Integer> function, Integer value) {
        return function.apply(value);
    }

    /**
     * 测试 Predicate
     */
    public static Boolean testPredicate(Predicate<Integer> predicate, Integer value) {
        return predicate.test(value);
    }

    /**
     * 使用 Stream.map 处理列表
     */
    public static List<Integer> streamMap(List<Integer> numbers, Function<Integer, Integer> mapper) {
        return numbers.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 使用 Stream.filter 处理列表
     */
    public static List<Integer> streamFilter(List<Integer> numbers, Predicate<Integer> predicate) {
        return numbers.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 组合使用 map 和 filter
     */
    public static List<Integer> streamMapAndFilter(List<Integer> numbers,
                                                    Function<Integer, Integer> mapper,
                                                    Predicate<Integer> predicate) {
        return numbers.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toList());
    }
}
