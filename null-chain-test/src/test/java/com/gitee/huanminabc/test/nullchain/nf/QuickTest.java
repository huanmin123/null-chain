package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 快速测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
public class QuickTest {

    @Test
    @DisplayName("函数引用调用应返回正确结果")
    public void test1() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Fun adder = add\n" +
            "Integer result = adder(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, null, null);
        assertNotNull(result);
        assertEquals(15, result);
    }
}
