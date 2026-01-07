package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 简单变量导出测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class SimpleVarExportTest {

    @Test
    @DisplayName("✓ 测试 export 普通变量")
    public void test1_ExportNormalVar() {
        String script =
            "Integer x = 10\n" +
            "export x";

        Object result = NfMain.run(script, log, null);
        assertEquals(10, result);
        log.info("✓ 测试通过: export 普通变量 = {}", result);
    }
}
