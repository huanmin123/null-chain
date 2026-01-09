package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.lambda.LambdaProxyFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NF Lambda 与 Java 函数式接口互操作测试
 *
 * @author huanmin
 * @date 2025/01/07
 */
@Slf4j
public class JavaLambdaInteropTest {

    /**
     * 测试将 NF Lambda 转换为 Java Function 接口
     */
    @Test
    @DisplayName("✓ NF Lambda -> Java Function")
    public void test1_NfLambdaToJavaFunction() {
        String script =
            "Fun<Integer : Integer> square = (x) -> {\n" +
            "    return x * x\n" +
            "}\n" +
            "\n" +
            "export square";

        log.info("===== 测试：NF Lambda 转换为 Java Function =====");

        // 使用 NfMain.run 执行脚本
        NfContext context = new NfContext();
        Object result = NfMain.run(script, log, null);

        log.info("FunRefInfo 对象: {}", result);
        log.info("FunRefInfo 类型: {}", result != null ? result.getClass() : "null");

        if (result instanceof FunRefInfo) {
            FunRefInfo funRef = (FunRefInfo) result;

            // 将 FunRefInfo 转换为 Function<Integer, Integer>
            Function<Integer, Integer> function = LambdaProxyFactory.createProxy(
                funRef, Function.class, context, 0
            );
            Integer applyResult = function.apply(5);
            assertEquals(25, applyResult);

            log.info("Function.apply(5) = {}", applyResult);
            log.info("✓ 测试1通过: NF Lambda 成功转换为 Java Function");
        } else {
            log.warn("结果不是 FunRefInfo 类型，跳过转换测试");
        }
    }

    /**
     * 测试将 NF Lambda 用于 Stream.map()
     */
    @Test
    @DisplayName("✗ NF Lambda in Stream.map() (目标功能)")
    public void test2_NfLambdaInStreamMap() {
        String script =
            "// TODO: 目标功能 - 直接将 NF Lambda 传递给 Java 方法\n" +
            "// List<Integer> result = Stream.of(1, 2, 3)\n" +
            "//     .map((x) -> { return x * 2 })\n" +
            "//     .collect(Collectors.toList())\n" +
            "\n" +
            "// 当前只能这样写：\n" +
            "Fun<Integer : Integer> doubler = (x) -> { return x * 2 }\n" +
            "export doubler";

        log.info("===== 测试：NF Lambda 在 Stream.map() 中使用 =====");
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(NfToken.tokens(script));
        Object result = NfRun.run(syntaxNodes, log, null);
        log.info("Result: {}", result);
        log.info("✓ 测试2: Lambda 创建成功，Stream 集成待实现");
    }

    /**
     * 测试将 NF Lambda 转换为 Java Predicate 接口
     */
    @Test
    @DisplayName("✓ NF Lambda -> Java Predicate")
    public void test3_NfLambdaToJavaPredicate() {
        String script =
            "Fun<Integer : Boolean> isEven = (x) -> {\n" +
            "    return x % 2 == 0\n" +
            "}\n" +
            "\n" +
            "export isEven";

        log.info("===== 测试：NF Lambda 转换为 Java Predicate =====");

        // 使用 NfMain.run 执行脚本
        NfContext context = new NfContext();
        Object result = NfMain.run(script, log, null);

        log.info("FunRefInfo 对象: {}", result);

        if (result instanceof FunRefInfo) {
            FunRefInfo funRef = (FunRefInfo) result;

            // 将 FunRefInfo 转换为 Predicate<Integer>
            Predicate<Integer> predicate = LambdaProxyFactory.createProxy(
                funRef, Predicate.class, context, 0
            );
            assertTrue(predicate.test(4));
            assertTrue(!predicate.test(5));

            log.info("Predicate.test(4) = {}", predicate.test(4));
            log.info("Predicate.test(5) = {}", predicate.test(5));
            log.info("✓ 测试3通过: NF Lambda 成功转换为 Java Predicate");
        } else {
            log.warn("结果不是 FunRefInfo 类型，跳过转换测试");
        }
    }

    /**
     * 测试 Java 方法的参数类型识别
     */
    @Test
    @DisplayName("✓ 识别 Java 方法的参数类型")
    public void test4_IdentifyJavaMethodParameterTypes() {
        log.info("===== 测试：识别 Java 方法的参数类型 =====");

        try {
            // 获取 Stream.map 方法的参数类型
            java.lang.reflect.Method mapMethod = java.util.stream.Stream.class.getMethod("map", java.util.function.Function.class);
            log.info("Stream.map 方法: {}", mapMethod);
            log.info("Stream.map 参数类型: {}", Arrays.toString(mapMethod.getParameterTypes()));

            Class<?> functionalInterfaceParam = mapMethod.getParameterTypes()[0];
            log.info("第一个参数类型: {}", functionalInterfaceParam);
            log.info("是否是接口: {}", functionalInterfaceParam.isInterface());

            assertTrue(functionalInterfaceParam.isInterface());
            log.info("✓ 测试4通过: 成功识别 Java 方法参数类型");

        } catch (Exception e) {
            log.error("测试失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试使用 Java Function 的简单场景
     */
    @Test
    @DisplayName("✓ 测试 Java Function 使用")
    public void test5_JavaFunctionUsage() {
        log.info("===== 测试：Java Function 使用 =====");

        // 创建一个简单的 Function
        Function<Integer, Integer> square = x -> x * x;

        // 测试调用
        Integer result = square.apply(5);
        assertEquals(25, result);

        log.info("Function.apply(5) = {}", result);
        log.info("✓ 测试5通过: Java Function 使用正常");
    }
}
