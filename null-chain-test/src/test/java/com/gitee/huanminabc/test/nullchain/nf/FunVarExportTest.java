package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Fun 变量导出测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunVarExportTest {

    @Test
    @DisplayName("✓ 测试能否 export 函数引用变量")
    public void test1_ExportFunVar() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "export add";

        Object result = NfMain.run(script, log, null);
        log.info("Result: {}", result);
        log.info("Result class: {}", result != null ? result.getClass() : null);
    }
}
