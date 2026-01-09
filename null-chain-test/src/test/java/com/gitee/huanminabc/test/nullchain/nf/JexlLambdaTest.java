package com.gitee.huanminabc.test.nullchain.nf;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.JexlExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试 JEXL3 原生的 Lambda 支持
 *
 * @author huanmin
 * @date 2025/01/07
 */
@Slf4j
public class JexlLambdaTest {

    /**
     * 测试 JEXL 版本
     */
    @Test
    @DisplayName("查看 JEXL 版本")
    public void test0_CheckJexlVersion() {
        JexlEngine jexl = new JexlBuilder().create();
        log.info("JEXL 版本: {}", jexl.getClass().getPackage().getImplementationVersion());
        log.info("JEXL 类: {}", jexl.getClass().getName());
    }

    /**
     * 测试 JEXL Lambda 语法 - 简单 Lambda
     */
    @Test
    @DisplayName("测试 JEXL Lambda: (x) -> x * 2")
    public void test1_SimpleLambda() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();

        try {
            // JEXL 3.1+ 支持 Lambda 语法: (x) -> x * 2
            JexlExpression expr = jexl.createExpression("((x) -> x * 2)(5)");
            Object result = expr.evaluate(context);

            log.info("Lambda 表达式: ((x) -> x * 2)(5)");
            log.info("结果: {} ({})", result, result.getClass());
            assertEquals(10, result);

        } catch (Exception e) {
            log.warn("JEXL 不支持 Lambda 语法: {}", e.getMessage());
        }
    }

    /**
     * 测试 JEXL Lambda 调用 Java 方法
     */
    @Test
    @DisplayName("测试 JEXL Lambda 调用 Java Function")
    public void test2_LambdaWithFunction() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();

        // 创建一个 Java Function
        Function<Integer, Integer> square = x -> x * x;
        context.set("square", square);

        try {
            // 尝试使用 Lambda 作为参数
            JexlExpression expr = jexl.createExpression("square((x) -> x * 2)");
            Object result = expr.evaluate(context);

            log.info("结果: {}", result);

        } catch (Exception e) {
            log.warn("JEXL 不支持 Lambda 作为参数: {}", e.getMessage());
        }
    }

    /**
     * 测试 JEXL Lambda 用于 Stream.map()
     */
    @Test
    @DisplayName("测试 JEXL Lambda + Stream.map()")
    public void test3_LambdaWithStream() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();

        // 准备数据
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        context.set("numbers", numbers);

        try {
            // 尝试使用 Stream + Lambda
            JexlExpression expr = jexl.createExpression(
                "numbers.stream().map((x) -> x * 2).collect(java.util.stream.Collectors.toList())"
            );
            Object result = expr.evaluate(context);

            log.info("Stream.map 结果: {}", result);
            log.info("结果类型: {}", result.getClass());

            if (result instanceof List) {
                List<?> list = (List<?>) result;
                log.info("列表内容: {}", list);
                assertEquals(Arrays.asList(2, 4, 6, 8, 10), list);
            }

        } catch (Exception e) {
            log.error("JEXL Lambda + Stream 失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试 JEXL 链式调用 + Lambda
     */
    @Test
    @DisplayName("测试 JEXL 链式调用: map + filter")
    public void test4_ChainedLambda() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        context.set("numbers", numbers);

        try {
            // 链式调用
            JexlExpression expr = jexl.createExpression(
                "numbers.stream()" +
                ".map((x) -> x * 2)" +
                ".filter((x) -> x > 5)" +
                ".collect(java.util.stream.Collectors.toList())"
            );
            Object result = expr.evaluate(context);

            log.info("链式调用结果: {}", result);
            log.info("结果类型: {}", result.getClass());

            if (result instanceof List) {
                List<?> list = (List<?>) result;
                log.info("列表内容: {}", list);
                assertEquals(Arrays.asList(6, 8, 10, 12), list);
            }

        } catch (Exception e) {
            log.error("JEXL 链式 Lambda 失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试 JEXL 3.3+ 的新 Lambda 语法
     */
    @Test
    @DisplayName("测试 JEXL 3.3+ Lambda 语法: #{ x -> x * 2 }")
    public void test5_NewLambdaSyntax() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();

        try {
            // JEXL 3.3+ 支持的新语法
            JexlExpression expr = jexl.createExpression("#{ x -> x * 2 }(5)");
            Object result = expr.evaluate(context);

            log.info("新 Lambda 语法结果: {}", result);
            assertEquals(10, result);

        } catch (Exception e) {
            log.warn("JEXL 不支持新 Lambda 语法: {}", e.getMessage());
        }
    }
}
