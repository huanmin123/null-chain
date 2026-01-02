package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfException;
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
 * 补充语法测试类
 * 测试未被使用的测试文件，完善测试覆盖率
 * 
 * @author huanmin
 */
@Slf4j
public class AdditionalSyntaxTest {

    // ==================== Assign 补充测试 ====================

    /**
     * 测试模板字符串赋值
     */
    @Test
    public void testAssignTemplateString() {
        String file = TestUtil.readFile("syntax/assign_template_string.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals("Hello test", result);
        log.info("模板字符串赋值测试通过，结果: {}", result);
    }

    /**
     * 测试复杂表达式赋值
     */
    @Test
    public void testAssignComplexExpressions() {
        String file = TestUtil.readFile("syntax/assign_complex_expressions.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(60, result); // (10 + 20) * 2 = 60
        log.info("复杂表达式赋值测试通过，结果: {}", result);
    }

    /**
     * 测试声明和赋值组合
     */
    @Test
    public void testDeclareAndAssign() {
        String file = TestUtil.readFile("syntax/declare_and_assign.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(30, result); // 10 + 20 = 30
        log.info("声明和赋值组合测试通过，结果: {}", result);
    }

    /**
     * 测试赋值错误 - 无效表达式
     */
    @Test
    public void testAssignErrorInvalidExpression() {
        String file = TestUtil.readFile("syntax/assign_error_invalid_expression.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("赋值无效表达式错误测试通过");
    }

    /**
     * 测试赋值错误 - 接口无实现
     */
    @Test
    public void testAssignErrorInterfaceNoImpl() {
        String file = TestUtil.readFile("syntax/assign_error_interface_no_impl.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        NfContext context = new NfContext();
        NfRun.run(syntaxNodes, context, log, null);
        log.info("赋值接口无实现错误测试通过");
    }

    /**
     * 测试声明错误 - 缺少变量名
     */
    @Test
    public void testDeclareErrorMissingVariable() {
        String file = TestUtil.readFile("syntax/declare_error_missing_variable.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("声明缺少变量名错误测试通过");
    }

    // ==================== Export 补充测试 ====================

    /**
     * 测试导出表达式
     */
    @Test
    public void testExportExpression() {
        String file = TestUtil.readFile("syntax/export_expression.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(30, result); // 10 + 20 = 30
        log.info("导出表达式测试通过，结果: {}", result);
    }

    /**
     * 测试导出错误 - 无效表达式
     */
    @Test
    public void testExportErrorInvalidExpression() {
        String file = TestUtil.readFile("syntax/export_error_invalid_expression.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("导出无效表达式错误测试通过");
    }

    // ==================== Echo 补充测试 ====================

    /**
     * 测试复杂 Echo
     */
    @Test
    public void testEchoComplex() {
        String file = TestUtil.readFile("syntax/echo_complex.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(10, result);
        log.info("复杂 Echo 测试通过，结果: {}", result);
    }

    /**
     * 测试空 Echo
     */
    @Test
    public void testEchoEmpty() {
        String file = TestUtil.readFile("syntax/echo_empty.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(10, result);
        log.info("空 Echo 测试通过，结果: {}", result);
    }

    // ==================== If 补充测试 ====================

    /**
     * 测试嵌套 If
     */
    @Test
    public void testIfNested() {
        String file = TestUtil.readFile("syntax/if_nested.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(30, result); // a > 5 && b > 15, so a = 30
        log.info("嵌套 If 测试通过，结果: {}", result);
    }

    /**
     * 测试 If 错误 - 空条件
     */
    @Test
    public void testIfErrorEmptyCondition() {
        String file = TestUtil.readFile("syntax/if_error_empty_condition.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);

        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("If 空条件错误测试通过");
    }

    // ==================== For 补充测试 ====================

    /**
     * 测试嵌套 For
     */
    @Test
    public void testForNested() {
        String file = TestUtil.readFile("syntax/for_nested.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(18, result); // sum = 1*1 + 1*2 + 2*1 + 2*2 + 3*1 + 3*2 = 18
        log.info("嵌套 For 测试通过，结果: {}", result);
    }

    /**
     * 测试 For 中使用 Break
     */
    @Test
    public void testForWithBreak() {
        String file = TestUtil.readFile("syntax/for_with_break.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(15, result); // sum = 1 + 2 + 3 + 4 + 5 = 15 (breaks at i > 5)
        log.info("For 中使用 Break 测试通过，结果: {}", result);
    }

    /**
     * 测试 For 中使用 Continue
     */
    @Test
    public void testForWithContinue() {
        String file = TestUtil.readFile("syntax/for_with_continue.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(25, result); // sum = 1 + 3 + 5 + 7 + 9 = 25 (skips even numbers)
        log.info("For 中使用 Continue 测试通过，结果: {}", result);
    }

    /**
     * 测试 For 中使用 BreakAll
     */
    @Test
    public void testForWithBreakAll() {
        String file = TestUtil.readFile("syntax/for_with_breakall.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(3, result); // sum = 1 + 2 = 3 (breakall at j > 2)
        log.info("For 中使用 BreakAll 测试通过，结果: {}", result);
    }

    /**
     * 测试 For 错误 - 缺少变量名
     */
    @Test
    public void testForErrorMissingVariable() {
        String file = TestUtil.readFile("syntax/for_error_missing_variable.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("For 缺少变量名错误测试通过");
    }

    /**
     * 测试 For 错误 - 空循环体
     */
    @Test
    public void testForErrorEmptyBody() {
        String file = TestUtil.readFile("syntax/for_error_empty_body.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(0, result);
        log.info("For 空循环体测试通过，结果: {}", result);
    }

    // ==================== Switch 补充测试 ====================

    /**
     * 测试 Switch 多个 Case
     */
    @Test
    public void testSwitchMultipleCases() {
        String file = TestUtil.readFile("syntax/switch_multiple_cases.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(20, result); // value = 2, matches case 2, 3
        log.info("Switch 多个 Case 测试通过，结果: {}", result);
    }

    /**
     * 测试 Switch 错误 - 空条件
     */
    @Test
    public void testSwitchErrorEmptyCondition() {
        String file = TestUtil.readFile("syntax/switch_error_empty_condition.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Switch 空条件错误测试通过");
    }

    // ==================== 混合语句测试 ====================

    /**
     * 测试混合语句
     */
    @Test
    public void testMixedStatements() {
        String file = TestUtil.readFile("syntax/mixed_statements.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(100, result); // a = 10, then a = 10 + 1 + 2 + 3 = 16, then switch case 16 -> a = 100
        log.info("混合语句测试通过，结果: {}", result);
    }

    // ==================== 其他错误测试 ====================

    /**
     * 测试函数执行错误 - 无效语法
     */
    @Test
    public void testFunExeErrorInvalidSyntax() {
        String file = TestUtil.readFile("syntax/funexe_error_invalid_syntax.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("函数执行无效语法错误测试通过");
    }

    /**
     * 测试 Run 错误 - 无效参数
     */
    @Test
    public void testRunErrorInvalidParams() {
        String file = TestUtil.readFile("syntax/run_error_invalid_params.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Run 无效参数错误测试通过");
    }

    /**
     * 测试 Task 错误 - 缺少类名
     */
    @Test
    public void testTaskErrorMissingClass() {
        String file = TestUtil.readFile("syntax/task_error_missing_class.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Task 缺少类名错误测试通过");
    }
}

