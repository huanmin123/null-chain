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
 * WHILE和DO-WHILE语句测试类
 * 测试while和do-while循环的各种场景
 *
 * @author huanmin
 */
@Slf4j
public class WhileExpressionTest {

    /**
     * 测试基础while语句
     */
    @Test
    public void testWhileBasic() {
        String file = TestUtil.readFile("while/while_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        assertEquals(120, result); // 5的阶乘 = 120
        log.info("基础while语句测试通过，结果: {}", result);
    }

    /**
     * 测试while循环中的break语句
     */
    @Test
    public void testWhileWithBreak() {
        String file = TestUtil.readFile("while/while_with_break.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        assertEquals(120, result); // 1×2×3×4×5=120
        log.info("while循环break测试通过，结果: {}", result);
    }

    /**
     * 测试while循环中的continue语句
     */
    @Test
    public void testWhileWithContinue() {
        String file = TestUtil.readFile("while/while_with_continue.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        assertEquals(25, result); // 1+3+5+7+9=25
        log.info("while循环continue测试通过，结果: {}", result);
    }

    /**
     * 测试基础do-while语句
     */
    @Test
    public void testDoWhileBasic() {
        String file = TestUtil.readFile("while/do_while_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        assertEquals(15, result); // 1+2+3+4+5=15
        log.info("do-while循环基础测试通过，结果: {}", result);
    }

    /**
     * 测试do-while只执行一次的情况
     */
    @Test
    public void testDoWhileOnce() {
        String file = TestUtil.readFile("while/do_while_once.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        assertEquals(10, result); // 只执行一次，i=10
        log.info("do-while循环执行一次测试通过，结果: {}", result);
    }
}
