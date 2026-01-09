package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NF Lambda 自动转换测试（简化版）
 * 使用 NF 函数包装 Java 方法，然后测试 Lambda 自动转换
 *
 * @author huanmin
 * @date 2025/01/08
 */
@Slf4j
public class AutoLambdaConversionSimpleTest {

    /**
     * 测试 NF Lambda 自动转换为 Function
     */
    @Test
    @DisplayName("✓ NF Lambda 自动转换 -> Java Function")
    public void testAutoLambdaConversion() {
        String script =
            "// 定义 Lambda\n" +
            "Fun<Integer : Integer> doubler = (x) -> { return x * 2 }\n" +
            "\n" +
            "// 定义 NF 函数包装 Java 方法\n" +
            "fun applyFunction(Fun<Integer, Integer : Integer> func, Integer value) Integer {\n" +
            "    return func(value)\n" +
            "}\n" +
            "\n" +
            "// 调用 NF 函数，Lambda 会自动转换\n" +
            "Integer result = applyFunction(doubler, 5)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：NF Lambda 自动转换为 Java Function =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        assertEquals(10, result);
        log.info("✓ 测试通过: NF Lambda 自动转换成功，结果 = {}", result);
    }

    /**
     * 测试 NF Lambda 在函数链中使用
     */
    @Test
    @DisplayName("✓ NF Lambda 函数链")
    public void testLambdaChaining() {
        String script =
            "// 定义多个 Lambda\n" +
            "Fun<Integer : Integer> doubler = (x) -> { return x * 2 }\n" +
            "Fun<Integer : Integer> tripler = (x) -> { return x * 3 }\n" +
            "\n" +
            "// 定义组合函数\n" +
            "fun compose(Fun<Integer, Integer : Integer> f, Fun<Integer, Integer : Integer> g, Integer x) Integer {\n" +
            "    return g(f(x))\n" +
            "}\n" +
            "\n" +
            "// 组合使用\n" +
            "Integer result = compose(doubler, tripler, 5)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：NF Lambda 函数链 =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        // 5 * 2 = 10, 10 * 3 = 30
        assertEquals(30, result);
        log.info("✓ 测试通过: Lambda 函数链成功，结果 = {}", result);
    }

    /**
     * 测试 Lambda 作为返回值
     */
    @Test
    @DisplayName("✓ Lambda 作为返回值")
    public void testLambdaAsReturnValue() {
        String script =
            "// 定义返回 Lambda 的函数\n" +
            "fun createMultiplier(Integer factor) Fun<Integer, Integer : Integer> {\n" +
            "    Fun<Integer : Integer> multiplier = (x) -> { return x * factor }\n" +
            "    return multiplier\n" +
            "}\n" +
            "\n" +
            "// 获取 Lambda\n" +
            "Fun<Integer : Integer> timesFive = createMultiplier(5)\n" +
            "\n" +
            "// 使用 Lambda\n" +
            "fun apply(Fun<Integer, Integer : Integer> func, Integer x) Integer {\n" +
            "    return func(x)\n" +
            "}\n" +
            "\n" +
            "Integer result = apply(timesFive, 3)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：Lambda 作为返回值 =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        // 3 * 5 = 15
        assertEquals(15, result);
        log.info("✓ 测试通过: Lambda 作为返回值成功，结果 = {}", result);
    }

    /**
     * 测试高阶函数（接受多个 Lambda）
     */
    @Test
    @DisplayName("✓ 高阶函数 - 多个 Lambda 参数")
    public void testHigherOrderMultipleLambdas() {
        String script =
            "// 定义 Lambda\n" +
            "Fun<Integer : Integer> adder = (x) -> { return x + 10 }\n" +
            "Fun<Integer : Integer> multiplier = (x) -> { return x * 2 }\n" +
            "\n" +
            "// 定义接受两个 Lambda 的函数\n" +
            "fun combine(Fun<Integer, Integer : Integer> f1, Fun<Integer, Integer : Integer> f2, Integer x) Integer {\n" +
            "    Integer temp = f1(x)\n" +
            "    return f2(temp)\n" +
            "}\n" +
            "\n" +
            "// 组合使用\n" +
            "Integer result = combine(adder, multiplier, 5)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：高阶函数 - 多个 Lambda 参数 =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        // (5 + 10) * 2 = 30
        assertEquals(30, result);
        log.info("✓ 测试通过: 多个 Lambda 参数自动转换成功，结果 = {}", result);
    }
}
