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
 * 复杂组合表达式测试类
 * 测试所有表达式的混合使用和复杂场景
 * 
 * @author huanmin
 */
@Slf4j
public class ComplexExpressionTest {

    /**
     * 测试复杂集成场景
     * 包含所有表达式类型的混合使用
     */
    @Test
    public void testComplexIntegration() {
        String file = TestUtil.readFile("complex/complex_integration.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        // (annualSalary - tax) / 12 = (60006 - 6000.6) / 12 = 4500.45
        assertTrue(result instanceof Double);
        Double expected = (60006.0 - 6000.6) / 12.0;
        Double actual = (Double) result;
        assertEquals(expected, actual, 0.01);
        log.info("复杂集成测试通过，结果: {}", result);
    }

    /**
     * 测试复杂嵌套场景
     * 测试多层嵌套结构
     */
    @Test
    public void testComplexNested() {
        String file = TestUtil.readFile("complex/complex_nested.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertTrue((Integer) result > 0);
        log.info("复杂嵌套测试通过，结果: {}", result);
    }

    /**
     * 测试复杂计算场景
     * 测试各种计算表达式
     */
    @Test
    public void testComplexCalculation() {
        String file = TestUtil.readFile("complex/complex_calculation.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(5, result); // average = (1+2+...+10) / 10 = 55 / 10 = 5.5，但整数除法是5
        log.info("复杂计算测试通过，结果: {}", result);
    }
}

