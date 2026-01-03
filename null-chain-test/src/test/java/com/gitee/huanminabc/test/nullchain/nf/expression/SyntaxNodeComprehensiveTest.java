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
 * 语法节点综合测试类
 * 测试所有语法节点的正常和异常情况
 * 从正常人写代码的角度来完善测试，包括各种语法错误场景
 * 
 * @author huanmin
 */
@Slf4j
public class SyntaxNodeComprehensiveTest {

    // ==================== Import语法节点测试 ====================
    
    /**
     * 测试Import正常情况
     */
    @Test
    public void testImportNormal() {
        String file = TestUtil.readFile("syntax/import_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Import正常情况测试通过");
    }

    /**
     * 测试Import异常情况 - 类不存在
     */
    @Test
    public void testImportClassNotFound() {
        String file = TestUtil.readFile("syntax/import_error_class_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Import类不存在异常测试通过");
    }

    /**
     * 测试Import异常情况 - 语法错误（缺少类名）
     */
    @Test
    public void testImportSyntaxError() {
        String file = TestUtil.readFile("syntax/import_error_syntax.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        // 缺少类名会在解析阶段抛出异常（空字符串无法找到类）
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Import语法错误测试通过");
    }

    // ==================== Task语法节点测试 ====================
    
    /**
     * 测试Task正常情况
     */
    @Test
    public void testTaskNormal() {
        String file = TestUtil.readFile("syntax/task_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Task正常情况测试通过");
    }

    /**
     * 测试Task异常情况 - 缺少as关键字
     */
    @Test
    public void testTaskMissingAs() {
        String file = TestUtil.readFile("syntax/task_error_missing_as.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Task缺少as关键字异常测试通过");
    }

    /**
     * 测试Task异常情况 - as后面不是标识符
     */
    @Test
    public void testTaskInvalidIdentifier() {
        String file = TestUtil.readFile("syntax/task_error_invalid_identifier.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Task无效标识符异常测试通过");
    }

    /**
     * 测试Task异常情况 - 类不存在
     */
    @Test
    public void testTaskClassNotFound() {
        String file = TestUtil.readFile("syntax/task_error_class_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Task类不存在异常测试通过");
    }

    // ==================== Declare语法节点测试 ====================
    
    /**
     * 测试Declare正常情况
     */
    @Test
    public void testDeclareNormal() {
        String file = TestUtil.readFile("syntax/declare_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Declare正常情况测试通过");
    }

    /**
     * 测试Declare异常情况 - 类型不存在
     */
    @Test
    public void testDeclareTypeNotFound() {
        String file = TestUtil.readFile("syntax/declare_error_type_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Declare类型不存在异常测试通过");
    }

    /**
     * 测试Declare异常情况 - 变量名是禁用关键字
     */
    @Test
    public void testDeclareForbiddenKeyword() {
        String file = TestUtil.readFile("syntax/declare_error_forbidden_keyword.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Declare禁用关键字异常测试通过");
    }

    /**
     * 测试Declare异常情况 - 重复变量声明
     */
    @Test
    public void testDeclareDuplicateVariable() {
        String file = TestUtil.readFile("syntax/declare_error_duplicate_var.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Declare重复变量声明异常测试通过");
    }

    // ==================== Assign语法节点测试 ====================
    
    /**
     * 测试Assign异常情况 - 变量不存在
     */
    @Test
    public void testAssignVariableNotFound() {
        String file = TestUtil.readFile("syntax/assign_error_variable_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Assign变量不存在异常测试通过");
    }

    /**
     * 测试Assign异常情况 - 类型不匹配
     */
    @Test
    public void testAssignTypeMismatch() {
        String file = TestUtil.readFile("syntax/assign_error_type_mismatch.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Assign类型不匹配异常测试通过");
    }

    /**
     * 测试Assign异常情况 - 表达式为空
     */
    @Test
    public void testAssignEmptyExpression() {
        String file = TestUtil.readFile("syntax/assign_error_empty_expression.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Assign表达式为空异常测试通过");
    }

    /**
     * 测试Assign异常情况 - 变量名是禁用关键字
     */
    @Test
    public void testAssignForbiddenKeyword() {
        String file = TestUtil.readFile("syntax/assign_error_forbidden_keyword.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Assign禁用关键字异常测试通过");
    }

    // ==================== Echo语法节点测试 ====================
    
    /**
     * 测试Echo异常情况 - 表达式计算错误
     */
    @Test
    public void testEchoExpressionError() {
        String file = TestUtil.readFile("syntax/echo_error_expression.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Echo表达式错误异常测试通过");
    }

    // ==================== Export语法节点测试 ====================
    
    /**
     * 测试Export异常情况 - export为空
     */
    @Test
    public void testExportEmpty() {
        String file = TestUtil.readFile("syntax/export_error_empty.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Export为空异常测试通过");
    }

    /**
     * 测试Export异常情况 - 变量不存在且表达式计算失败
     */
    @Test
    public void testExportVariableNotFound() {
        String file = TestUtil.readFile("syntax/export_error_variable_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Export变量不存在异常测试通过");
    }

    // ==================== FunExe语法节点测试 ====================
    
    /**
     * 测试FunExe正常情况
     */
    @Test
    public void testFunExeNormal() {
        String file = TestUtil.readFile("syntax/funexe_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("FunExe正常情况测试通过");
    }

    /**
     * 测试FunExe异常情况 - 函数执行错误
     */
    @Test
    public void testFunExeError() {
        String file = TestUtil.readFile("syntax/funexe_error.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("FunExe执行错误异常测试通过");
    }

    // ==================== Run语法节点测试 ====================
    
    /**
     * 测试Run异常情况 - 任务不存在
     */
    @Test
    public void testRunTaskNotFound() {
        String file = TestUtil.readFile("syntax/run_error_task_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Run任务不存在异常测试通过");
    }

    /**
     * 测试Run异常情况 - 变量不存在
     */
    @Test
    public void testRunVariableNotFound() {
        String file = TestUtil.readFile("syntax/run_error_variable_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Run变量不存在异常测试通过");
    }

    // ==================== IF语法节点测试 ====================
    
    /**
     * 测试IF异常情况 - 条件表达式错误
     */
    @Test
    public void testIfConditionError() {
        String file = TestUtil.readFile("syntax/if_error_condition.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("IF条件表达式错误异常测试通过");
    }

    /**
     * 测试IF异常情况 - 语法错误（缺少大括号）
     */
    @Test
    public void testIfSyntaxError() {
        String file = TestUtil.readFile("syntax/if_error_syntax.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        // 语法错误应该在解析阶段就抛出异常
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("IF语法错误测试通过");
    }

    // ==================== Switch语法节点测试 ====================
    
    /**
     * 测试Switch异常情况 - 条件值类型错误
     */
    @Test
    public void testSwitchConditionTypeError() {
        String file = TestUtil.readFile("syntax/switch_error_condition_type.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("Switch条件值类型错误异常测试通过");
    }

    /**
     * 测试Switch异常情况 - 变量不存在
     */
    @Test
    public void testSwitchVariableNotFound() {
        String file = TestUtil.readFile("syntax/switch_error_variable_not_found.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Switch变量不存在异常测试通过");
    }

    // ==================== For语法节点测试 ====================
    
    /**
     * 测试For异常情况 - 缺少in关键字
     */
    @Test
    public void testForMissingIn() {
        String file = TestUtil.readFile("syntax/for_error_missing_in.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("For缺少in关键字异常测试通过");
    }

    /**
     * 测试For异常情况 - 缺少..符号
     */
    @Test
    public void testForMissingDot2() {
        String file = TestUtil.readFile("syntax/for_error_missing_dot2.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("For缺少..符号异常测试通过");
    }

    /**
     * 测试For异常情况 - ..前后不是整数
     */
    @Test
    public void testForInvalidRange() {
        String file = TestUtil.readFile("syntax/for_error_invalid_range.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("For无效范围异常测试通过");
    }

    /**
     * 测试For异常情况 - 起始值大于结束值
     */
    @Test
    public void testForInvalidRangeOrder() {
        String file = TestUtil.readFile("syntax/for_error_invalid_range_order.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        
        assertThrows(NfException.class, () -> {
            NfSynta.buildMainStatement(tokens);
        });
        log.info("For范围顺序错误异常测试通过");
    }

    // ==================== Break/Continue/BreakAll语法节点测试 ====================
    
    /**
     * 测试Break异常情况 - 不在for循环内
     */
    @Test
    public void testBreakOutsideFor() {
        String file = TestUtil.readFile("syntax/break_error_outside_for.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertThrows(NfException.class, () -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Break不在for循环内异常测试通过");
    }

    /**
     * 测试Continue正常情况
     */
    @Test
    public void testContinueNormal() {
        String file = TestUtil.readFile("syntax/continue_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("Continue正常情况测试通过");
    }

    /**
     * 测试BreakAll正常情况
     */
    @Test
    public void testBreakAllNormal() {
        String file = TestUtil.readFile("syntax/breakall_normal.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        assertDoesNotThrow(() -> {
            NfRun.run(syntaxNodes, context, log, null);
        });
        log.info("BreakAll正常情况测试通过");
    }
}

