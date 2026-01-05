package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 作用域检查测试类
 * 测试变量名在作用域内的重复检查功能
 *
 * @author huanmin
 * @date 2024/01/04
 */
@Slf4j
public class ScopeCheckTest {

    /**
     * 每个测试前清除语法缓存
     */
    @BeforeEach
    public void clearSyntaxCache() throws Exception {
        try {
            Field cacheField = NfMain.class.getDeclaredField("syntaxCache");
            cacheField.setAccessible(true);
            Object cache = cacheField.get(null);
            if (cache instanceof com.github.benmanes.caffeine.cache.Cache) {
                ((com.github.benmanes.caffeine.cache.Cache<?, ?>) cache).invalidateAll();
            }
        } catch (Exception e) {
            // 忽略反射错误
        }
    }

    /**
     * 直接解析并运行脚本（绕过缓存）
     */
    private Object runScriptDirectly(String script) {
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        return NfRun.run(syntaxNodes, log, null);
    }

    /**
     * 测试：if块内变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testIfBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_if_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("outer"),
                   "错误信息应包含变量名'outer'，实际: " + exception.getMessage());
        log.info("if块内变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：for块内变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testForBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_for_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("counter"),
                   "错误信息应包含变量名'counter'，实际: " + exception.getMessage());
        log.info("for块内变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：switch块内变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testSwitchBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_switch_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        log.info("switch块内变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：while块内变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testWhileBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_while_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        log.info("while块内变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：do-while块内变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testDoWhileBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_dowhile_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        log.info("do-while块内变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：函数内变量可以遮蔽全局变量
     * 预期：应该正常工作，函数内的变量会遮蔽全局变量
     */
    @Test
    public void testFunctionVariableDuplicateWithGlobal() {
        String script = TestUtil.readFile("scope/scope_function_global_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(200, result); // 函数内的 globalVar (200) 遮蔽了全局 globalVar (100)
        log.info("函数内变量遮蔽全局变量测试通过，结果: {}", result);
    }

    /**
     * 测试：函数内块级变量不能和函数作用域内变量重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testFunctionInnerBlockVariableDuplicate() {
        String script = TestUtil.readFile("scope/scope_function_inner_block_duplicate.nf");

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"),
                   "错误信息应包含'变量重复声明'或'已声明'，实际: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("funcVar"),
                   "错误信息应包含变量名'funcVar'，实际: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("函数") || exception.getMessage().contains("Function"),
                   "错误信息应包含'函数作用域'，实际: " + exception.getMessage());
        log.info("函数内块级变量与函数变量重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：不同函数之间的变量可以重名
     * 预期：应该正常工作
     */
    @Test
    public void testDifferentFunctionsSameVariableName() {
        String script = TestUtil.readFile("scope/scope_different_functions_same_name.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(300, result); // 100 + 200 = 300
        log.info("不同函数间变量重名测试通过，结果: {}", result);
    }

    /**
     * 测试：正常的嵌套块，不重复变量名
     * 预期：应该正常工作
     */
    @Test
    public void testNormalNestedBlocks() {
        String script = TestUtil.readFile("scope/scope_normal_nested_blocks.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(100, result);
        log.info("正常嵌套块测试通过，结果: {}", result);
    }

    /**
     * 测试：块内变量在块外可以重新定义
     * 预期：应该正常工作
     */
    @Test
    public void testBlockOuterRedefine() {
        String script = TestUtil.readFile("scope/scope_block_outer_redefine.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(200, result);
        log.info("块外重新定义变量测试通过，结果: {}", result);
    }

    /**
     * 测试：复杂的嵌套结构
     * 预期：应该正常工作
     */
    @Test
    public void testComplexNesting() {
        String script = TestUtil.readFile("scope/scope_complex_nesting.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(100, result);
        log.info("复杂嵌套结构测试通过，结果: {}", result);
    }

    // ========== 直接编写脚本的内联测试 ==========

    /**
     * 测试：if块内var变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testIfBlockVarDuplicate() {
        String script = "Integer outer = 100\n" +
                       "if true {\n" +
                       "    var outer = 200\n" +
                       "}\n" +
                       "export outer";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        log.info("if块内var变量重复检查测试通过");
    }

    /**
     * 测试：for循环变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testForLoopVariableDuplicate() {
        String script = "Integer i = 100\n" +
                       "for i in 1..5 {\n" +
                       "}\n" +
                       "export i";

        Exception exception = assertThrows(Exception.class, () -> {
            runScriptDirectly(script);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        log.info("for循环变量重复检查测试通过");
    }

    /**
     * 测试：嵌套if块的同级变量重复（应该允许）
     * 预期：应该正常工作
     */
    @Test
    public void testSiblingIfBlocksSameVariable() {
        String script = "Integer result = 0\n" +
                       "if true {\n" +
                       "    Integer temp = 100\n" +
                       "}\n" +
                       "if false {\n" +
                       "    Integer temp = 200\n" +
                       "}\n" +
                       "export result";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(0, result);
        log.info("同级if块变量重名测试通过，结果: {}", result);
    }

    /**
     * 测试：嵌套函数的变量不互相干扰
     * 预期：应该正常工作
     */
    @Test
    public void testNestedFunctionVariables() {
        String script = "fun outer()Integer {\n" +
                       "    Integer x = 100\n" +
                       "    fun inner()Integer {\n" +
                       "        Integer x = 200\n" +
                       "        return x\n" +
                       "    }\n" +
                       "    Integer innerResult = inner()\n" +
                       "    return x + innerResult\n" +
                       "}\n" +
                       "Integer result = outer()\n" +
                       "export result";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(300, result); // 100 + 200
        log.info("嵌套函数变量测试通过，结果: {}", result);
    }

    /**
     * 测试：函数内for块变量不能和函数变量重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testFunctionForBlockVariableDuplicate() {
        String script = "fun test()Integer {\n" +
                       "    Integer sum = 0\n" +
                       "    for i in 1..5 {\n" +
                       "        Integer sum = i\n" +
                       "    }\n" +
                       "    return sum\n" +
                       "}\n" +
                       "Integer result = test()\n" +
                       "export result";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        assertTrue(exception.getMessage().contains("sum"));
        log.info("函数内for块变量重复检查测试通过");
    }

    /**
     * 测试：多级嵌套块内变量不能和上层重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testMultiLevelNestingDuplicate() {
        String script = "Integer level1 = 10\n" +
                       "if true {\n" +
                       "    Integer level2 = 20\n" +
                       "    if true {\n" +
                       "        Integer level1 = 30\n" +
                       "    }\n" +
                       "}\n" +
                       "export level1";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        assertTrue(exception.getMessage().contains("level1"));
        log.info("多级嵌套块变量重复检查测试通过");
    }

    /**
     * 测试：正常的多级嵌套（不重复）
     * 预期：应该正常工作
     */
    @Test
    public void testMultiLevelNestingNormal() {
        String script = "Integer level1 = 10\n" +
                       "if true {\n" +
                       "    Integer level2 = 20\n" +
                       "    if true {\n" +
                       "        Integer level3 = 30\n" +
                       "        Integer result = level1 + level2 + level3\n" +
                       "    }\n" +
                       "}\n" +
                       "export level1";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(10, result);
        log.info("正常多级嵌套测试通过，结果: {}", result);
    }

    /**
     * 测试：else块变量不能和外部重复
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testElseBlockVariableDuplicate() {
        String script = "Integer outer = 100\n" +
                       "if false {\n" +
                       "    Integer inner = 50\n" +
                       "} else {\n" +
                       "    Integer outer = 200\n" +
                       "}\n" +
                       "export outer";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        log.info("else块变量重复检查测试通过");
    }

    /**
     * 测试：declare语句作用域检查
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testDeclareVariableDuplicate() {
        String script = "int x = 10\n" +
                       "if true {\n" +
                       "    int x = 20\n" +
                       "}\n" +
                       "export x";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        log.info("declare语句作用域检查测试通过");
    }

    /**
     * 测试：函数参数可以遮蔽全局变量（允许重名）
     * 预期：应该正常工作，参数 param 会遮蔽全局 param
     */
    @Test
    public void testFunctionParameterShadowGlobal() {
        String script = "Integer param = 100\n" +
                       "fun test(int param)Integer {\n" +
                       "    return param\n" +
                       "}\n" +
                       "Integer result = test(50)\n" +
                       "export result";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(50, result); // 参数 param (50) 遮蔽了全局 param (100)
        log.info("函数参数遮蔽全局变量测试通过，结果: {}", result);
    }

    /**
     * 测试：函数体内变量不能与函数参数重名
     * 预期：应该抛出变量重复声明错误
     */
    @Test
    public void testFunctionBodyVariableDuplicateWithParameter() {
        String script = "fun test(int x)Integer {\n" +
                       "    Integer x = 100\n" +  // 这里应该报错：x 已经是参数名
                       "    return x\n" +
                       "}\n" +
                       "Integer result = test(50)\n" +
                       "export result";

        Map<String, Object> context = new HashMap<>();
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, context);
        });

        assertTrue(exception.getMessage().contains("变量重复声明") ||
                   exception.getMessage().contains("已声明"));
        assertTrue(exception.getMessage().contains("x"));
        log.info("函数体内变量与参数重复检查测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试：使用 global.xxx 访问被遮蔽的全局变量
     * 预期：应该正常工作，可以访问被遮蔽的全局变量
     */
    @Test
    public void testGlobalAccess() {
        String script = TestUtil.readFile("global/global_access.nf");

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(150, result); // 50 (参数) + 100 (全局变量) = 150
        log.info("global.xxx 访问测试通过，结果: {}", result);
    }
}
