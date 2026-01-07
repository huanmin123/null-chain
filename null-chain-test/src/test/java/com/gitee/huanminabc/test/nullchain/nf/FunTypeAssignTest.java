package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fun 类型赋值测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunTypeAssignTest {

    @Test
    @DisplayName("✓ Fun 类型作为函数返回值（顶层）")
    public void test1_FunReturnType() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun getAdder() Fun {\n" +
            "    return add\n" +
            "}\n" +
            "Fun addFunc = getAdder()\n" +
            "Integer result = addFunc(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试1通过: 函数返回 Fun 类型 = {}", result);
    }

    @Test
    @DisplayName("✓ 函数体内声明 Fun 变量")
    public void test2_FunVarInsideFunction() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun useFun() Integer {\n" +
            "    Fun op = add\n" +
            "    return op(5, 10)\n" +
            "}\n" +
            "Integer result = useFun()\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试2通过: 函数内声明 Fun 变量 = {}", result);
    }
}
