package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模板字符串功能测试类
 * 
 * <p>测试 NF 脚本中模板字符串的功能，包括：
 * - 基本模板字符串功能
 * - 多行内容保留
 * - 变量占位符替换
 * - 表达式占位符替换
 * - 在赋值、export、echo、run 等场景下的使用</p>
 * 
 * @author huanmin
 * @since 1.1.5
 */
public class TemplateStringTest {

    // ========== 基本功能测试 ==========

    @Test
    public void testBasicTemplateString() {
        // 测试基本模板字符串功能
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\n{preValue}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test", result.trim());
    }

    @Test
    public void testTemplateStringWithVariable() {
        // 测试模板字符串中的变量占位符
        String input = "world";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String name = \"hello\"; String result = ```\n{name} {preValue}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @Test
    public void testTemplateStringWithExpression() {
        // 测试模板字符串中的表达式占位符
        Integer input = 10;
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\nValue: {preValue + 5}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("15"));
    }

    // ========== 多行内容测试 ==========

    @Test
    public void testMultiLineTemplateString() {
        // 测试多行模板字符串保留换行
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\nLine 1: {preValue}\nLine 2: {preValue}\nLine 3: {preValue}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        // 检查是否包含换行符
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
        assertTrue(result.contains("Line 3"));
    }

    @Test
    public void testEmptyTemplateString() {
        // 测试空模板字符串
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\n```; export result"))
                .type(String.class)
                .orElse("");
        
        assertNotNull(result);
        // 空模板字符串应该返回空字符串或只包含换行
        assertTrue(result.trim().isEmpty() || result.equals("\n"));
    }

    // ========== 在 export 中使用模板字符串 ==========

    @Test
    public void testExportTemplateString() {
        // 测试在 export 中直接使用模板字符串
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("export ```\n{preValue}\n```"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertEquals("test", result.trim());
    }

    @Test
    public void testExportTemplateStringWithVariable() {
        // 测试在 export 中使用带变量的模板字符串
        String input = "world";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String name = \"hello\"; export ```\n{name} {preValue}\n```"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    // ========== 在赋值中使用模板字符串 ==========

    @Test
    public void testAssignTemplateString() {
        // 测试在赋值中使用模板字符串
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\nValue: {preValue}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("Value:"));
        assertTrue(result.contains("test"));
    }

    @Test
    public void testAssignTemplateStringWithMultiplePlaceholders() {
        // 测试赋值中使用多个占位符的模板字符串
        String input = "world";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String greeting = \"Hello\"; String result = ```\n{greeting} {preValue}!\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("world"));
    }

    // ========== 在 echo 中使用模板字符串 ==========

    @Test
    public void testEchoTemplateString() {
        // 测试在 echo 中使用模板字符串
        String input = "test";
        try {
            // echo 不会返回值，需要添加 export 或者使用 orElse 处理
            // 这里添加一个 export 语句来避免链式调用返回 null
            String result = Null.of(input)
                    .nfTask(NullGroupNfTask.task("echo ```\nEc       h      o: {preValue}\n```; export \"success\""))
                    .type(String.class)
                    .orElse("failed");
            // echo 执行成功
            assertEquals("success", result);
        } catch (Exception e) {
            fail("Echo template string should not throw exception: " + e.getMessage());
        }
    }

    // ========== 复杂场景测试 ==========

    @Test
    public void testTemplateStringWithComplexExpression() {
        // 测试模板字符串中使用复杂表达式
        Integer input = 10;
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\nResult: {(preValue + 5) * 2}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("30")); // (10 + 5) * 2 = 30
    }

    @Test
    public void testTemplateStringWithStringConcatenation() {
        // 测试模板字符串中使用字符串拼接表达式
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\n{preValue + \"_suffix\"}\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("test_suffix"));
    }

    @Test
    public void testTemplateStringInChain() {
        // 测试在链式调用中使用模板字符串
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String step1 = ```\nStep1: {preValue}\n```; String step2 = step1 + \"_step2\"; export step2"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("Step1:"));
        assertTrue(result.contains("test"));
        assertTrue(result.contains("_step2"));
    }

    // ========== 边界情况测试 ==========

    @Test
    public void testTemplateStringWithOnlyNewline() {
        // 测试只有换行的模板字符串
        // 注意：如果模板字符串只包含换行符，处理后的结果可能是空字符串
        // 空字符串会被 Null.is() 判断为空，导致链变成空链，get() 会抛出异常
        // 因此使用 orElse("") 来处理可能返回空字符串的情况
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\n\n```; export result"))
                .type(String.class)
                .orElse(""); // 使用 orElse 而不是 get，因为结果可能是空字符串
        
        assertNotNull(result);
        // 应该包含换行符或者是空字符串
        assertTrue(result.contains("\n") || result.isEmpty());
    }

    @Test
    public void testTemplateStringWithNoPlaceholders() {
        // 测试没有占位符的模板字符串
        String input = "test";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.task("String result = ```\nStatic text\n```; export result"))
                .type(String.class)
                .get();
        
        assertNotNull(result);
        assertTrue(result.contains("Static text"));
    }

    // ========== 异常场景测试 ==========

    @Test
    public void testTemplateStringWithUndefinedVariable() {
        // 测试模板字符串中使用未定义的变量
        String input = "test";
        try {
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("String result = ```\n{undefinedVar}\n```; export result"))
                    .get();
            fail("Should throw exception for undefined variable");
        } catch (Exception e) {
            // 应该抛出异常
            assertNotNull(e);
        }
    }

    // ========== 性能测试 ==========

    @Test
    public void testTemplateStringPerformance() {
        // 测试模板字符串性能
        String input = "test";
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            String result = Null.of(input)
                    .nfTask(NullGroupNfTask.task("String result = ```\n{preValue}\n```; export result"))
                    .type(String.class)
                    .get();
            assertNotNull(result);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 100次调用应该在合理时间内完成（例如5秒内）
        assertTrue(duration < 5000, "Template string processing took too long: " + duration + "ms");
    }
}

