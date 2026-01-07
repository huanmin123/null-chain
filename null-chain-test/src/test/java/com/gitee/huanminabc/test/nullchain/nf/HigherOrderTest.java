package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 高阶函数测试
 * 测试函数引用作为函数参数的功能
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class HigherOrderTest {

    /**
     * 测试基础高阶函数：接受函数引用作为参数
     */
    @Test
    @DisplayName("✓ 高阶函数 - 接受函数引用作为参数")
    public void test1_HigherOrderFunction() {
        String script =
            "fun add(Integer a, Integer b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "\n" +
            "fun applyOperation(Fun<Integer, Integer : Integer> operation, Integer x, Integer y) Integer {\n" +
            "    return operation(x, y)\n" +
            "}\n" +
            "\n" +
            "Integer result = applyOperation(add, 10, 20)\n" +
            "export result";

        log.info("===== 测试高阶函数 - 接受函数引用作为参数 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        assertEquals(30, result);
        log.info("✓ 测试1通过: 高阶函数 = {}", result);
    }

    /**
     * 测试高阶函数接受 Lambda 表达式
     */
    @Test
    @DisplayName("✓ 高阶函数 - 接受 Lambda 表达式")
    public void test2_HigherOrderWithLambda() {
        String script =
            "fun applyOperation(Fun<Integer, Integer : Integer> operation, Integer x, Integer y) Integer {\n" +
            "    return operation(x, y)\n" +
            "}\n" +
            "\n" +
            "Fun<Integer, Integer : Integer> multiply = (a, b) -> {\n" +
            "    return a * b\n" +
            "}\n" +
            "\n" +
            "Integer result = applyOperation(multiply, 5, 6)\n" +
            "export result";

        log.info("===== 测试高阶函数 - 接受 Lambda 表达式 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        assertEquals(30, result);
        log.info("✓ 测试2通过: 高阶函数接受 Lambda = {}", result);
    }

    /**
     * 测试多个函数引用参数
     */
    @Test
    @DisplayName("✓ 高阶函数 - 多个函数引用参数")
    public void test3_MultipleFunctionParams() {
        String script =
            "fun add(Integer a, Integer b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "\n" +
            "fun multiply(Integer a, Integer b) Integer {\n" +
            "    return a * b\n" +
            "}\n" +
            "\n" +
            "fun combine(\n" +
            "    Fun<Integer, Integer : Integer> op1,\n" +
            "    Fun<Integer, Integer : Integer> op2,\n" +
            "    Integer a,\n" +
            "    Integer b\n" +
            ") Integer {\n" +
            "    Integer r1 = op1(a, b)\n" +
            "    Integer r2 = op2(a, b)\n" +
            "    return r1 + r2\n" +
            "}\n" +
            "\n" +
            "Integer result = combine(add, multiply, 5, 3)\n" +
            "export result";

        log.info("===== 测试高阶函数 - 多个函数引用参数 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        // add(5, 3) = 8, multiply(5, 3) = 15, 8 + 15 = 23
        assertEquals(23, result);
        log.info("✓ 测试3通过: 多个函数引用参数 = {}", result);
    }

    /**
     * 测试高阶函数链式调用
     */
    @Test
    @DisplayName("✓ 高阶函数 - 链式调用")
    public void test4_HigherOrderChaining() {
        String script =
            "fun add(Integer a, Integer b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "\n" +
            "fun applyTwice(\n" +
            "    Fun<Integer, Integer : Integer> operation,\n" +
            "    Integer value\n" +
            ") Integer {\n" +
            "    Integer result1 = operation(value, value)\n" +
            "    Integer result2 = operation(result1, result1)\n" +
            "    return result2\n" +
            "}\n" +
            "\n" +
            "Integer result = applyTwice(add, 5)\n" +
            "export result";

        log.info("===== 测试高阶函数 - 链式调用 =====");
        Object result = assertDoesNotThrow(() -> NfMain.run(script, log, null));
        // add(5, 5) = 10, add(10, 10) = 20
        assertEquals(20, result);
        log.info("✓ 测试4通过: 高阶函数链式调用 = {}", result);
    }

    /**
     * 测试返回函数引用的高阶函数（未来实现）
     */
    @Test
    @DisplayName("✗ 高阶函数 - 返回函数引用（暂未实现）")
    public void test5_ReturnFunctionReference() {
        // 这个功能需要支持返回函数引用，暂时跳过
        log.info("===== 测试跳过: 返回函数引用（暂未实现） =====");
    }
}
