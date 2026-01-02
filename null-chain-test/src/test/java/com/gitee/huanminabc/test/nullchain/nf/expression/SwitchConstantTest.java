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
 * SWITCH常量测试类
 * 测试SWITCH语句使用常量的功能
 * 
 * @author huanmin
 */
@Slf4j
public class SwitchConstantTest {

    /**
     * 测试SWITCH使用整数常量
     */
    @Test
    public void testSwitchWithIntegerConstant() {
        String file = TestUtil.readFile("switch/switch_constant.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(10, result); // switch 1 应该匹配 case 1，result = 10
        log.info("SWITCH常量测试通过，结果: {}", result);
    }

    /**
     * 测试SWITCH嵌套FOR
     */
    @Test
    public void testSwitchNestedFor() {
        String file = TestUtil.readFile("switch/switch_nested_for.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(15, result); // switch 1 匹配 case 1，for 循环 1+2+3+4+5 = 15
        log.info("SWITCH嵌套FOR测试通过，结果: {}", result);
    }
}

