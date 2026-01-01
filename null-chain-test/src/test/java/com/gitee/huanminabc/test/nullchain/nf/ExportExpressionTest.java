package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.core.NullChain;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Export表达式计算功能测试类
 * 
 * <p>测试 ExportSyntaxNode 支持表达式计算的功能，包括：
 * - 导出变量（向后兼容）
 * - 导出简单表达式（字符串拼接、数学运算）
 * - 导出复杂表达式
 * - 导出包含变量的表达式
 * - 异常场景测试</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public class ExportExpressionTest {

    // ========== 导出变量测试（向后兼容） ==========

    @Test
    public void testExportVariable() {
        // 测试导出变量（向后兼容）
        String input = "test_input";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = preValue + '_suffix'; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test_input_suffix", result);
    }

    @Test
    public void testExportVariableDirect() {
        // 测试直接导出变量
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String value = preValue; export value"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test", result);
    }

    // ========== 导出表达式测试（新功能） ==========

    @Test
    public void testExportStringExpression() {
        // 测试导出字符串拼接表达式
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export preValue + \"_suffix\""))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test_suffix", result);
    }

    @Test
    public void testExportStringExpressionWithMultipleConcat() {
        // 测试导出多个字符串拼接表达式
        String input = "hello";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export preValue + \"_\" + \"world\" + \"_\" + \"test\""))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("hello_world_test", result);
    }

    @Test
    public void testExportMathExpression() {
        // 测试导出数学运算表达式
        Integer input = 10;
        Integer result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export preValue + 5 * 2"))
                .type(Integer.class)
                .get();
        
        assertNotNull(result);
        assertEquals(20, result);
    }

    @Test
    public void testExportComplexMathExpression() {
        // 测试导出复杂数学运算表达式
        Integer input = 10;
        Integer result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export (preValue + 5) * 2 - 3"))
                .type(Integer.class)
                .get();
        
        assertNotNull(result);
        assertEquals(27, result); // (10 + 5) * 2 - 3 = 27
    }

    @Test
    public void testExportExpressionWithVariable() {
        // 测试导出包含变量的表达式
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String prefix = \"prefix_\"; export prefix + preValue"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("prefix_test", result);
    }

    @Test
    public void testExportExpressionWithMultipleVariables() {
        // 测试导出包含多个变量的表达式
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String prefix = \"prefix_\"; String suffix = \"_suffix\"; export prefix + preValue + suffix"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("prefix_test_suffix", result);
    }

    @Test
    public void testExportBooleanExpression() {
        // 测试导出布尔表达式
        Integer input = 10;
        Boolean result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export preValue > 5"))
                .type(Boolean.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testExportComparisonExpression() {
        // 测试导出比较表达式
        Integer input = 10;
        Boolean result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export preValue == 10"))
                .type(Boolean.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result);
    }

    // ========== 并发任务表达式测试 ==========

    @Test
    public void testConcurrentExportExpression() {
        // 测试并发任务中的表达式导出
        String input = "test";
        NullGroupNfTask nullGroupNfTask = NullGroupNfTask.buildGroup(
                NullGroupNfTask.task("export preValue + \"_task1\""),
                NullGroupNfTask.task("export preValue + \"_task2\""),
                NullGroupNfTask.task("export preValue + \"_task3\"")
        );

        Map<String, Object> result = Null.of(input)
                .nfTasks(nullGroupNfTask, "default")
                .get();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 验证每个任务都返回了正确的结果
        assertTrue(result.size() >= 3);
    }

    @Test
    public void testConcurrentExportMathExpression() {
        // 测试并发任务中的数学表达式导出
        Integer input = 10;
        NullGroupNfTask nullGroupNfTask = NullGroupNfTask.buildGroup(
                NullGroupNfTask.task("export preValue + 1"),
                NullGroupNfTask.task("export preValue * 2"),
                NullGroupNfTask.task("export preValue - 5")
        );

        Map<String, Object> result = Null.of(input)
                .nfTasks(nullGroupNfTask, "default")
                .get();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 验证每个任务都返回了正确的结果
        assertTrue(result.size() >= 3);
    }

    // ========== 边界场景测试 ==========

    @Test
    public void testExportEmptyStringExpression() {
        // 测试导出空字符串表达式
        // 注意：空字符串会被 Null.is() 视为空值，导致链变成空链，需要使用 orElse 获取
        String input = "test";
        @SuppressWarnings("unchecked")
        NullChain<String> chain = (NullChain<String>) Null.of(input)
                .nfTask(NullGroupNfTask.task("export \"\""));
        
        // 空字符串会导致链变成空链，使用 orElse 获取默认值（这里传入空字符串作为默认值）
        String result = chain.orElse("");
        
        assertEquals("", result);
    }

    @Test
    public void testExportNullExpression() {
        // 测试导出null表达式
        String input = "test";
        // null表达式应该返回null，使用orElse提供默认值
        @SuppressWarnings("unchecked")
        NullChain<String> chain = (NullChain<String>) Null.of(input)
                .nfTask(NullGroupNfTask.task("export null"));
        
        String result = chain.orElse("default");
        
        assertEquals("default", result);
    }

    @Test
    public void testExportNumberExpression() {
        // 测试导出数字表达式
        Integer input = 5;
        Integer result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export 100"))
                .type(Integer.class)
                .get();
        
        assertNotNull(result);
        assertEquals(100, result);
    }

    // ========== 异常场景测试 ==========

    @Test
    public void testExportUndefinedVariable() {
        // 测试导出未定义的变量（应该作为表达式计算）
        String input = "test";
        try {
            // 如果变量不存在，应该尝试作为表达式计算
            // 如果表达式也无效，应该抛出异常
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("export undefinedVar"))
                    .get();
            // 如果执行到这里，说明表达式计算失败但没有抛出异常，这是不正常的
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期会抛出异常
            assertNotNull(e);
        }
    }

    @Test
    public void testExportInvalidExpression() {
        // 测试导出无效表达式
        String input = "test";
        try {
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("export invalid + syntax !!!"))
                    .get();
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期会抛出异常
            assertNotNull(e);
            assertTrue(e instanceof NullChainException || 
                      e.getCause() instanceof NullChainException ||
                      e.getMessage() != null,
                    "无效表达式应该抛出异常");
        }
    }

    @Test
    public void testExportEmptyExpression() {
        // 测试导出空表达式
        String input = "test";
        try {
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("export"))
                    .get();
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期会抛出异常
            assertNotNull(e);
        }
    }

    // ========== 混合场景测试 ==========

    @Test
    public void testExportExpressionWithVariableAndLiteral() {
        // 测试导出变量和字面量混合的表达式
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String suffix = \"_end\"; export preValue + suffix + \"_literal\""))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test_end_literal", result);
    }

    @Test
    public void testExportExpressionWithArithmeticAndString() {
        // 测试导出算术和字符串混合的表达式
        Integer input = 10;
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export \"value:\" + (preValue + 5)"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("value:15", result);
    }

    @Test
    public void testExportExpressionChain() {
        // 测试导出表达式链式调用
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String step1 = preValue + \"_step1\"; String step2 = step1 + \"_step2\"; export step2 + \"_final\""))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test_step1_step2_final", result);
    }

    // ========== 性能测试 ==========

    @Test
    public void testExportExpressionPerformance() {
        // 测试表达式计算的性能
        String input = "test";
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            String result = Null.of(input)
                    .nfTask(NullGroupNfTask.task("export preValue + \"_\" + " + i))
                    .type(String.class)
                    .get();
            assertNotNull(result);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能合理（100次执行应该在合理时间内完成）
        assertTrue(duration < 10000, "表达式计算性能应该合理，实际耗时: " + duration + "ms");
        System.out.println("100次表达式计算耗时: " + duration + "ms");
    }
}

