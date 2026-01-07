package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 简单 Fun 类型测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunSimpleTest {

    @Test
    @DisplayName("✓ 简化 Fun 类型赋值测试")
    public void test1_SimpleFunAssign() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Fun addFunc = add\n" +
            "Integer result = addFunc(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        assertEquals(15, result);
        log.info("✓ 测试通过: 简化 Fun 类型赋值 = {}", result);
    }
}
