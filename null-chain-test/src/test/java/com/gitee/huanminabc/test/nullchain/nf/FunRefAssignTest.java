package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 函数引用赋值测试（正确用法）
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunRefAssignTest {

    @Test
    @DisplayName("✓ 函数引用赋值 - 直接赋值")
    public void test1_DirectAssign() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Fun addFunc = add\n" +
            "Integer result = addFunc(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试通过: 函数引用赋值 = {}", result);
    }

    @Test
    @DisplayName("✓ 函数引用作为参数传递")
    public void test2_FunRefAsParameter() {
        String script =
            "fun apply(Fun<Integer, Integer : Integer> operation, Integer x, Integer y) Integer {\n" +
            "    return operation(x, y)\n" +
            "}\n" +
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Integer result = apply(add, 10, 20)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(30, result);
        log.info("✓ 测试通过: 函数引用作为参数 = {}", result);
    }
}
