package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fun 函数定义测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunDefTest {

    @Test
    @DisplayName("✓ 函数定义返回简化 Fun 类型")
    public void test1_FunctionReturnSimpleFun() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun getAdder() Fun {\n" +
            "    return add\n" +
            "}\n" +
            "export getAdder";

        Object result = NfMain.run(script, log, null);
        log.info("Result: {}", result);
        // 应该返回一个 FunRefInfo 对象
    }

    @Test
    @DisplayName("✓ 调用返回 Fun 类型的函数")
    public void test2_CallFunctionReturnFun() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun getAdder() Fun {\n" +
            "    return add\n" +
            "}\n" +
            "FunRef adder = getAdder()\n" +
            "Integer result = adder(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试通过: 调用返回 Fun 类型的函数 = {}", result);
    }
}
