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
 * 控制语句详细测试类（break, continue, breakall）
 * 测试控制语句的各种场景和边界情况
 * 
 * @author huanmin
 */
@Slf4j
public class ControlExpressionTest {

    /**
     * 测试基础控制语句
     */
    @Test
    public void testControlBasic() {
        String file = TestUtil.readFile("control/control_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertTrue((Integer) result > 0);
        log.info("基础控制语句测试通过，结果: {}", result);
    }

    /**
     * 测试高级控制语句
     */
    @Test
    public void testControlAdvanced() {
        String file = TestUtil.readFile("control/control_advanced.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertTrue((Integer) result > 0);
        log.info("高级控制语句测试通过，结果: {}", result);
    }
}



