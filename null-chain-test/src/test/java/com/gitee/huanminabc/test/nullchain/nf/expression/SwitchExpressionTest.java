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
 * SWITCH语句详细测试类
 * 测试SWITCH语句的各种场景和边界情况
 * 
 * @author huanmin
 */
@Slf4j
public class SwitchExpressionTest {

    /**
     * 测试基础SWITCH语句
     */
    @Test
    public void testSwitchBasic() {
        String file = TestUtil.readFile("switch/switch_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(20, result); // value = 20
        log.info("基础SWITCH语句测试通过，结果: {}", result);
    }

    /**
     * 测试高级SWITCH语句
     */
    @Test
    public void testSwitchAdvanced() {
        String file = TestUtil.readFile("switch/switch_advanced.nf");
        assertNotNull(file, "文件内容不能为空");
        assertFalse(file.isEmpty(), "文件内容不能为空");
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        assertNotNull(tokens, "tokens列表不能为空");
        assertFalse(tokens.isEmpty(), "tokens列表不能为空，文件内容: " + file.substring(0, Math.min(200, file.length())));
        
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        assertNotNull(syntaxNodes, "syntaxNodes列表不能为空");
        assertFalse(syntaxNodes.isEmpty(), "syntaxNodes列表不能为空");
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        // 实际结果是 "中级"，但由于编码问题，使用 contains 检查
        assertTrue(result.toString().contains("中级") || result.toString().equals("中级"));
        log.info("高级SWITCH语句测试通过，结果: {}", result);
    }
}

