package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * var 关键字测试类
 * 测试 var 关键字的自动类型推导和手动类型指定功能
 * 
 * @author huanmin
 */
@Slf4j
public class VarExpressionTest {

    @BeforeEach
    public void clearCache() {
        NfMain.shutdown();
    }
    /**
     * 测试 var 基础功能 - 自动类型推导和手动类型指定
     */
    @Test
    public void testVarBasic() {
        String file = TestUtil.readFile("var/var_basic.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("var基础测试完成", result);
        log.info("var 基础测试通过，结果: {}", result);
    }

    /**
     * 测试 var 类型推导功能
     */
    @Test
    public void testVarTypeInference() {
        String file = TestUtil.readFile("var/var_type_inference.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("类型推导测试完成", result);
        log.info("var 类型推导测试通过，结果: {}", result);
    }

    /**
     * 测试 var 手动类型指定功能
     */
    @Test
    public void testVarManualType() {
        String file = TestUtil.readFile("var/var_manual_type.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("手动类型测试完成", result);
        log.info("var 手动类型测试通过，结果: {}", result);
    }

    /**
     * 测试 var 作用域功能
     */
    @Test
    public void testVarScope() {
        String file = TestUtil.readFile("var/var_scope.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("作用域测试完成", result);
        log.info("var 作用域测试通过，结果: {}", result);
    }

    /**
     * 测试 var 高级功能
     */
    @Test
    public void testVarAdvanced() {
        String file = TestUtil.readFile("var/var_advanced.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("var高级测试完成", result);
        log.info("var 高级测试通过，结果: {}", result);
    }

    /**
     * 测试 var 集成功能
     */
    @Test
    public void testVarIntegration() {
        String file = TestUtil.readFile("var/var_integration.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertEquals("集成测试完成", result);
        log.info("var 集成测试通过，结果: {}", result);
    }

    /**
     * 测试 var 自动类型推导 - 字符串
     */
    @Test
    public void testVarAutoInferString() {
        String script = "var name = \"test123\"\necho \"name = {name}\"\nexport name";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals("test123", result);
        log.info("var 字符串自动推导测试通过，结果: {}", result);
    }

    /**
     * 测试 var 自动类型推导 - 整数
     */
    @Test
    public void testVarAutoInferInteger() {
        String script = "var num = 123\necho \"num = {num}\"\nexport num";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(123, result);
        log.info("var 整数自动推导测试通过，结果: {}", result);
    }

    /**
     * 测试 var 手动指定类型
     */
    @Test
    public void testVarManualTypeSpecification() {
        String script = "var name:String = \"test\"\nvar num:Integer = 456\necho \"name = {name}, num = {num}\"\nexport num";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(456, result);
        log.info("var 手动类型指定测试通过，结果: {}", result);
    }

    /**
     * 测试 var 表达式自动推导
     */
    @Test
    public void testVarExpressionAutoInfer() {
        String script = "var sum = 10 + 20 + 30\necho \"sum = {sum}\"\nexport sum";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(60, result);
        log.info("var 表达式自动推导测试通过，结果: {}", result);
    }

    /**
     * 测试 var 在 if 块中的作用域
     */
    @Test
    public void testVarScopeInIf() {
        String script = "var outer = \"outer\"\nif true {\n    var inner = \"inner\"\n    echo \"outer = {outer}, inner = {inner}\"\n}\necho \"outer = {outer}\"\nexport outer";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals("outer", result);
        log.info("var if 作用域测试通过，结果: {}", result);
    }

    /**
     * 测试 var 在 for 循环中的作用域
     */
    @Test
    public void testVarScopeInFor() {
        String script = "var total = 0\nfor i in 1..5 {\n    var item = i * 10\n    total = total + item\n}\necho \"total = {total}\"\nexport total";
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(150, result); // 10 + 20 + 30 + 40 + 50 = 150
        log.info("var for 作用域测试通过，结果: {}", result);
    }
}

