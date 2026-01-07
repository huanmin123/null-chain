package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 函数引用作用域测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunRefReturnSimpleTest {

    @Test
    @DisplayName("✓ 函数内部直接返回已定义的函数名")
    public void test1_ReturnFunctionName() {
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
        log.info("✓ 测试通过: 函数返回函数引用 = {}", result);
    }
}
