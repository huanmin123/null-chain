package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 关键字检查测试类
 * 测试变量名、函数名、参数名不能使用关键字
 *
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class KeywordCheckTest {

    /**
     * 测试不能用关键字作为变量名
     */
    @Test
    public void testVariableNameCannotBeKeyword() {
        // 测试 var 声明时使用关键字
        String script = "var fun = 100\nexport fun";
        assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });

        // 测试带类型声明时使用关键字
        String script2 = "Integer return = 100\nexport return";
        assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script2, log, new HashMap<>());
        });

        // 测试赋值时使用关键字
        String script3 = "global = 100\nexport global";
        assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script3, log, new HashMap<>());
        });

        log.info("变量名不能使用关键字测试通过");
    }

    /**
     * 测试不能用关键字作为函数名
     */
    @Test
    public void testFunctionNameCannotBeKeyword() {
        // 测试函数名使用 if 关键字（使用标准格式，{后换行）
        String script1 = "fun if(int x)Integer {\n    return x\n}\nInteger result = if(10)\nexport result";
        Exception ex1 = assertThrows(Exception.class, () -> {
            NfMain.run(script1, log, new HashMap<>());
        });
        log.info("函数名使用if关键字的异常: {}", ex1.getMessage());
        assertTrue(ex1.getMessage().contains("关键字") || ex1.getMessage().contains("不能用作函数名"));

        // 测试函数名使用 global 关键字
        String script2 = "fun global(int x)Integer {\n    return x\n}\nInteger result = global(10)\nexport result";
        Exception ex2 = assertThrows(Exception.class, () -> {
            NfMain.run(script2, log, new HashMap<>());
        });
        assertTrue(ex2.getMessage().contains("关键字") || ex2.getMessage().contains("不能用作函数名"));

        // 测试函数名使用 return 关键字
        String script3 = "fun return(int x)Integer {\n    return x\n}\nInteger result = return(10)\nexport result";
        Exception ex3 = assertThrows(Exception.class, () -> {
            NfMain.run(script3, log, new HashMap<>());
        });
        assertTrue(ex3.getMessage().contains("关键字") || ex3.getMessage().contains("不能用作函数名"));

        log.info("函数名不能使用关键字测试通过");
    }

    /**
     * 测试不能用关键字作为函数参数名
     */
    @Test
    public void testParameterNameCannotBeKeyword() {
        // 测试参数名使用 if 关键字
        String script1 = "fun test(int if)Integer {\n    return if\n}\nInteger result = test(10)\nexport result";
        Exception ex1 = assertThrows(Exception.class, () -> {
            NfMain.run(script1, log, new HashMap<>());
        });
        log.info("参数名使用if关键字的异常: {}", ex1.getMessage());
        assertTrue(ex1.getMessage().contains("关键字") || ex1.getMessage().contains("不能用作参数名"));

        // 测试参数名使用 for 关键字
        String script2 = "fun add(int for, int while)Integer {\n    return for + while\n}\nInteger result = add(5, 10)\nexport result";
        NfSyntaxException ex2 = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script2, log, new HashMap<>());
        });
        assertTrue(ex2.getMessage().contains("关键字") || ex2.getMessage().contains("不能用作参数名"));

        // 测试参数名使用 global 关键字
        String script3 = "fun test(int global)Integer {\n    return global\n}\nInteger result = test(10)\nexport result";
        NfSyntaxException ex3 = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script3, log, new HashMap<>());
        });
        assertTrue(ex3.getMessage().contains("关键字") || ex3.getMessage().contains("不能用作参数名"));

        log.info("参数名不能使用关键字测试通过");
    }

    /**
     * 测试所有常见关键字都不能作为变量名
     */
    @Test
    public void testAllCommonKeywordsAsVariableName() {
        String[] keywords = {"if", "else", "while", "for", "fun", "return", "global",
                             "true", "false", "and", "or", "break", "continue",
                             "switch", "case", "default", "new", "instanceof"};

        for (String keyword : keywords) {
            String script = String.format("var %s = 100\nexport %s", keyword, keyword);
            NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
                NfMain.run(script, log, new HashMap<>());
            });
            assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"),
                "关键字 " + keyword + " 应该被检测为非法变量名");
        }

        log.info("所有常见关键字作为变量名测试通过");
    }

    /**
     * 测试非关键字可以作为变量名
     */
    @Test
    public void testNonKeywordCanBeVariableName() {
        // globalVar 不是关键字，应该可以使用
        String script1 = "var globalVar = 100\nexport globalVar";
        Object result1 = NfMain.run(script1, log, new HashMap<>());
        assertEquals(100, result1);

        // funName 不是关键字，应该可以使用
        String script2 = "var funName = \"test\"\nexport funName";
        Object result2 = NfMain.run(script2, log, new HashMap<>());
        assertEquals("test", result2);

        // returnValue 不是关键字，应该可以使用
        String script3 = "Integer returnValue = 200\nexport returnValue";
        Object result3 = NfMain.run(script3, log, new HashMap<>());
        assertEquals(200, result3);

        log.info("非关键字可以作为变量名测试通过");
    }

    /**
     * 测试关键字检查脚本文件
     */
    @Test
    public void testKeywordCheckFile() {
        String file = TestUtil.readFile("keyword/keyword_check.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);

        assertNotNull(result);
        assertEquals(15, result); // add(5, 10) = 15
        log.info("关键字检查脚本文件测试通过，结果: {}", result);
    }

    /**
     * 测试不能用关键字作为全局变量名（赋值）
     */
    @Test
    public void testGlobalKeywordAssignment() {
        // 尝试使用 global 作为变量名进行赋值
        String script = "global = 100\nexport global";
        NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });
        assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"));

        log.info("全局变量名不能使用关键字测试通过");
    }

    /**
     * 测试不能用 fun 作为变量名
     */
    @Test
    public void testFunKeywordAsVariableName() {
        String script = "var fun = 100\nexport fun";
        NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });
        assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"));

        log.info("fun关键字作为变量名测试通过");
    }

    /**
     * 测试不能用 return 作为变量名
     */
    @Test
    public void testReturnKeywordAsVariableName() {
        String script = "Integer return = 100\nexport return";
        NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });
        assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"));

        log.info("return关键字作为变量名测试通过");
    }

    /**
     * 测试在 if 块内不能使用关键字作为变量名
     */
    @Test
    public void testKeywordInIfBlock() {
        String script = "Integer x = 10\nif x > 5 {\n    var fun = 100\n}\nexport x";
        NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });
        assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"));

        log.info("if块内使用关键字作为变量名测试通过");
    }

    /**
     * 测试在函数内不能使用关键字作为变量名
     */
    @Test
    public void testKeywordInFunctionBody() {
        String script = "fun test()Integer {\n    var if = 100\n    return if\n}\nInteger result = test()\nexport result";
        NfSyntaxException ex = assertThrows(NfSyntaxException.class, () -> {
            NfMain.run(script, log, new HashMap<>());
        });
        assertTrue(ex.getMessage().contains("关键字") || ex.getMessage().contains("禁止"));

        log.info("函数内使用关键字作为变量名测试通过");
    }
}
