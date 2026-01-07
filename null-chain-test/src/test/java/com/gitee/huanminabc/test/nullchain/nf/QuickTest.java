package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 快速测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class QuickTest {

    @Test
    @DisplayName("调试：测试 add 函数名是否被识别")
    public void test1() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Fun adder = add\n" +
            "Integer result = adder(5, 10)\n" +
            "export result";

        Object result = NfMain.run(script, log, null);
        log.info("Result: {}", result);
    }
}
