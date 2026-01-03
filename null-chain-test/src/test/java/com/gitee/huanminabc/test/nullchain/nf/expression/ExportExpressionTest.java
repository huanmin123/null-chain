package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXPORT语句详细测试类
 * 测试EXPORT语句的各种场景和边界情况
 * 
 * @author huanmin
 */
@Slf4j
public class ExportExpressionTest {

    /**
     * 测试基础EXPORT语句
     */
    @Test
    public void testExportBasic() {
        String file = TestUtil.readFile("export/export_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals("张三", result);
        log.info("基础EXPORT语句测试通过，结果: {}", result);
    }

    /**
     * 测试高级EXPORT语句
     */
    @Test
    public void testExportAdvanced() {
        String file = TestUtil.readFile("export/export_advanced.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(15, result); // ((5 + 3) * 2) - 1 = 15
        log.info("高级EXPORT语句测试通过，结果: {}", result);
    }
}




