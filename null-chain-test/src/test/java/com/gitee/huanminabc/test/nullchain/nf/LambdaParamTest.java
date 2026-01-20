package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Lambda 作为参数直接传递的测试
 */
@Slf4j
public class LambdaParamTest {

    @Test
    @DisplayName("✓ Lambda 作为参数 - 简单场景")
    public void test1_LambdaAsParam_Simple() {
        String script =
            "fun applyOperation(Fun<Integer, Integer : Integer> operation, Integer x, Integer y) Integer {\n" +
            "    return operation(x, y)\n" +
            "}\n" +
            "Integer result = applyOperation((a, b) -> { " +
                            "return a + b " +
                    "}, 10, 20)\n" +
            "export result";

        log.info("===== 测试：Lambda 作为参数 - 简单场景 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        assertEquals(30, result);
        log.info("✓ 测试1通过: result = {}", result);
    }

    @Test
    @DisplayName("✓ Lambda 作为参数 - 多个 Lambda")
    public void test2_LambdaAsParam_MultipleLambdas() {
        String script =
            "fun combine(\n" +
            "    Fun<Integer : Integer> op1,\n" +
            "    Fun<Integer : Integer> op2,\n" +
            "    Integer a\n" +
            ") Integer {\n" +
            "    return op1(a) + op2(a)\n" +
            "}\n" +
            "Integer result = combine((x) -> { return x * 2 }, (y) -> { return y * 3 }, 10)\n" +
            "export result";

        log.info("===== 测试：Lambda 作为参数 - 多个 Lambda =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        // 10 * 2 + 10 * 3 = 20 + 30 = 50
        assertEquals(50, result);
        log.info("✓ 测试2通过: result = {}", result);
    }

    @Test
    @DisplayName("✓ Lambda 作为参数 - 嵌套调用")
    public void test3_LambdaAsParam_Nested() {
        String script =
            "fun apply(Fun<Integer : Integer> func, Integer x) Integer {\n" +
            "    return func(x)\n" +
            "}\n" +
            "Integer result = apply((n) -> { return n * n }, 5)\n" +
            "export result";

        log.info("===== 测试：Lambda 作为参数 - 嵌套调用 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        // 5 * 5 = 25
        assertEquals(25, result);
        log.info("✓ 测试3通过: result = {}", result);
    }

    @Test
    @DisplayName("✓ Lambda 作为参数 - 复杂表达式")
    public void test4_LambdaAsParam_Complex() {
        String script =
            "fun process(Fun<Integer, Integer, Integer : Integer> calculator, Integer a, Integer b, Integer c) Integer {\n" +
            "    Integer temp = calculator(a, b)\n" +
            "    return calculator(temp, c)\n" +
            "}\n" +
            "Integer result = process((x, y) -> { return x + y }, 10, 20, 30)\n" +
            "export result";

        log.info("===== 测试：Lambda 作为参数 - 复杂表达式 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        // (10 + 20) + 30 = 60
        assertEquals(60, result);
        log.info("✓ 测试4通过: result = {}", result);
    }
}
