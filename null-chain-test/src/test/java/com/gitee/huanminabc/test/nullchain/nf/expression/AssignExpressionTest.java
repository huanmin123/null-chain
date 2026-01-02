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
 * 赋值表达式详细测试类
 * 测试赋值表达式的各种场景和边界情况
 * 
 * @author huanmin
 */
@Slf4j
public class AssignExpressionTest {

    /**
     * 测试基础赋值表达式
     */
    @Test
    public void testAssignBasic() {
        String file = TestUtil.readFile("assign/assign_basic.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(55, result); // result = (10 + 20) * 2 - 5 = 55
        log.info("基础赋值表达式测试通过，结果: {}", result);
    }

    /**
     * 测试高级赋值表达式
     */
    @Test
    public void testAssignAdvanced() {
        String file = TestUtil.readFile("assign/assign_advanced.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(60, result); // sum = 10 + 20 + 30 = 60
        log.info("高级赋值表达式测试通过，结果: {}", result);
    }

    /**
     * 测试赋值表达式的作用域
     */
    @Test
    public void testAssignScope() {
        String file = TestUtil.readFile("assign/assign_scope.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertEquals(10, result); // level1 最终值应该是 10
        log.info("赋值表达式作用域测试通过，结果: {}", result);
    }

    /**
     * 测试创建新对象（使用内置类型，无需导入）
     */
    @Test
    public void testAssignNewObject() {
        String file = TestUtil.readFile("syntax/assign_new_object.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertTrue(result instanceof java.util.ArrayList);
        java.util.ArrayList<?> list = (java.util.ArrayList<?>) result;
        assertEquals(2, list.size());
        assertEquals("hello", list.get(0));
        assertEquals("world", list.get(1));
        log.info("创建新对象测试通过，结果: {}", result);
    }

    /**
     * 测试导入自定义类型并创建对象
     */
    @Test
    public void testAssignImportUserEntity() {
        String file = TestUtil.readFile("syntax/assign_import_user_entity.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);
        
        assertNotNull(result);
        assertTrue(result instanceof com.gitee.huanminabc.test.nullchain.entity.UserEntity);
        com.gitee.huanminabc.test.nullchain.entity.UserEntity user = 
            (com.gitee.huanminabc.test.nullchain.entity.UserEntity) result;
        assertEquals(1, user.getId());
        assertEquals("huanmin", user.getName());
        assertEquals(25, user.getAge());
        assertEquals("男", user.getSex());
        log.info("导入自定义类型并创建对象测试通过，结果: {}", user);
    }
}

