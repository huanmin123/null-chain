package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 函数返回函数引用功能测试
 * 测试函数可以返回函数引用作为返回值
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunRefReturnTest {

    @Test
    @DisplayName("✓ 函数返回函数引用 - 返回已定义的函数引用")
    public void test1_ReturnExistingFunctionRef() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun getAdder() Fun<Integer, Integer : Integer> {\n" +
            "    Fun adder = add\n" +
            "    return adder\n" +
            "}\n" +
            "Fun addFunc = getAdder()\n" +
            "Integer result = addFunc(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试1通过: 函数返回已存在的函数引用 = {}", result);
    }

    @Test
    @DisplayName("✓ 函数返回 Lambda 表达式")
    public void test2_ReturnLambda() {
        String script =
            "fun createMultiplier(int factor) Fun<Integer : Integer> {\n" +
            "    Fun<Integer : Integer> multiplier = (x) -> { return x * factor }\n" +
            "    return multiplier\n" +
            "}\n" +
            "Fun times3 = createMultiplier(3)\n" +
            "Integer result = times3(10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(30, result);
        log.info("✓ 测试2通过: 函数返回 Lambda 表达式 = {}", result);
    }

    @Test
    @DisplayName("✓ 函数返回 Lambda - 单参数")
    public void test3_ReturnLambdaSingleParam() {
        String script =
            "fun getDoubler() Fun<Integer : Integer> {\n" +
            "    return (x) -> { return x * 2 }\n" +
            "}\n" +
            "Fun double = getDoubler()\n" +
            "Integer result = double(21)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(42, result);
        log.info("✓ 测试3通过: 函数返回单参数 Lambda = {}", result);
    }

    @Test
    @DisplayName("✓ 函数返回 Lambda - 多参数")
    public void test4_ReturnLambdaMultipleParams() {
        String script =
            "fun getAdder() Fun<Integer, Integer : Integer> {\n" +
            "    return (a, b) -> { return a + b }\n" +
            "}\n" +
            "Fun add = getAdder()\n" +
            "Integer result = add(100, 42)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(142, result);
        log.info("✓ 测试4通过: 函数返回多参数 Lambda = {}", result);
    }

    @Test
    @DisplayName("✓ 高阶函数 - 工厂模式")
    public void test5_HigherOrderFactory() {
        String script =
            "fun createOperation(String op) Fun<Integer, Integer : Integer> {\n" +
            "    if (op == \"add\") {\n" +
            "        return (a, b) -> { return a + b }\n" +
            "    }\n" +
            "    return (a, b) -> { return a * b }\n" +
            "}\n" +
            "Fun<Integer, Integer : Integer> adder = createOperation(\"add\")\n" +
            "Integer sum = adder(10, 20)\n" +
            "export sum";

        Object result = NfMain.run(script, log, null);
        assertEquals(30, result);
        log.info("✓ 测试5通过: 高阶函数工厂模式 = sum={}", result);
    }

    @Test
    @DisplayName("✓ 函数链式调用")
    public void test6_FunctionChaining() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun compose(Fun<Integer : Integer> f1, Fun<Integer : Integer> f2) Fun<Integer : Integer> {\n" +
            "    return (x) -> { return f2(f1(x)) }\n" +
            "}\n" +
            "Fun<Integer : Integer> increment = (x) -> { return x + 1 }\n" +
            "Fun<Integer : Integer> double = (x) -> { return x * 2 }\n" +
            "Fun<Integer : Integer> process = compose(increment, double)\n" +
            "Integer result = process(5)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        // compose(increment, double) 返回的 Lambda 执行: double(increment(5)) = double(6) = 12
        assertEquals(12, result);
        log.info("✓ 测试6通过: 函数链式调用 = {}", result);
    }
}
