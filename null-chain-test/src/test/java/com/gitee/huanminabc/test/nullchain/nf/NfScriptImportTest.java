package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfScriptRegistry;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NF脚本导入功能测试
 * 
 * @author huanmin
 * @date 2024/12/XX
 */
public class NfScriptImportTest {

    @BeforeEach
    public void setUp() {
        // 清理注册表
        NfScriptRegistry.clear();
        
        // 注册测试脚本文件
        registerTestScripts();
    }

    @AfterEach
    public void tearDown() {
        // 清理注册表
        NfScriptRegistry.clear();
    }

    /**
     * 注册测试脚本文件
     */
    private void registerTestScripts() {
        try {
            // 注册工具脚本
            NfScriptRegistry.registerScript("utils", TestUtil.readFile("import/utils.nf"));
            NfScriptRegistry.registerScript("math", TestUtil.readFile("import/math.nf"));
            NfScriptRegistry.registerScript("string", TestUtil.readFile("import/string.nf"));
            NfScriptRegistry.registerScript("config", TestUtil.readFile("import/config.nf"));
            NfScriptRegistry.registerScript("complex", TestUtil.readFile("import/complex.nf"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to register test scripts", e);
        }
    }

    /**
     * 测试脚本注册
     */
    @Test
    public void testScriptRegistration() {
        // setUp中已经注册了5个脚本，所以这里应该是5
        assertTrue(NfScriptRegistry.hasScript("utils"));
        assertTrue(NfScriptRegistry.hasScript("math"));
        assertTrue(NfScriptRegistry.hasScript("string"));
        assertTrue(NfScriptRegistry.hasScript("config"));
        assertTrue(NfScriptRegistry.hasScript("complex"));
        assertNotNull(NfScriptRegistry.getScriptSyntaxNodes("utils"));
        assertEquals(5, NfScriptRegistry.size());
    }

    /**
     * 测试脚本注册失败（空内容）
     */
    @Test
    public void testScriptRegistrationEmptyContent() {
        assertThrows(Exception.class, () -> {
            NfScriptRegistry.registerScript("utils", "");
        });
    }

    /**
     * 测试脚本注册失败（空名称）
     */
    @Test
    public void testScriptRegistrationEmptyName() {
        assertThrows(Exception.class, () -> {
            NfScriptRegistry.registerScript("", "String a = \"test\"");
        });
    }

    /**
     * 测试导入单个脚本
     */
    @Test
    public void testImportSingleScript() {
        // 注册脚本
        String scriptContent = "String utilsVar = \"hello\"";
        System.out.println("注册脚本 testUtils: " + scriptContent);
        NfScriptRegistry.registerScript("testUtils", scriptContent);
        System.out.println("脚本注册成功，已注册脚本数量: " + NfScriptRegistry.size());
        
        // 导入并使用
        String mainScript = "import nf testUtils\n" +
                           "String result = testUtils.utilsVar\n" +
                           "export result";
        
        System.out.println("执行脚本: " + mainScript);
        try {
            Object result = NfMain.run(mainScript, null, null);
            System.out.println("执行结果: " + result + ", 类型: " + (result != null ? result.getClass() : "null"));
            assertNotNull(result, "执行结果不能为null");
            assertEquals("hello", result);
        } catch (Exception e) {
            System.err.println("执行脚本时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 测试导入多个脚本（逗号分隔）
     */
    @Test
    public void testImportMultipleScripts() {
        // 注册多个脚本
        NfScriptRegistry.registerScript("utils1", "String var1 = \"value1\"");
        NfScriptRegistry.registerScript("utils2", "String var2 = \"value2\"");
        
        // 导入多个脚本
        String mainScript = "import nf utils1, utils2\n" +
                           "String result1 = utils1.var1\n" +
                           "String result2 = utils2.var2\n" +
                           "export result1 + \"-\" + result2";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("value1-value2", result);
    }

    /**
     * 测试访问导入脚本的变量
     */
    @Test
    public void testAccessImportedScriptVariable() {
        // 注册脚本
        String scriptContent = "Integer count = 100\n" +
                              "String name = \"test\"";
        NfScriptRegistry.registerScript("config", scriptContent);
        
        // 导入并使用变量
        String mainScript = "import nf config\n" +
                           "Integer total = config.count + 50\n" +
                           "String info = config.name\n" +
                           "export total";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals(150, result);
    }

    /**
     * 测试调用导入脚本的函数
     */
    @Test
    public void testCallImportedScriptFunction() {
        // 注册脚本（包含函数）- 使用正确的函数定义语法：fun 函数名(参数)返回类型 { 函数体 }
        String scriptContent = "fun format(String name, Integer age)String {\n" +
                              "    return name + \"-\" + age\n" +
                              "}\n" +
                              "String prefix = \"User\"";
        NfScriptRegistry.registerScript("testUtils", scriptContent);
        
        // 导入并调用函数
        String mainScript = "import nf testUtils\n" +
                           "String result = testUtils.format(\"张三\", 25)\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("张三-25", result);
    }

    /**
     * 测试导入脚本的函数访问自己的全局变量
     */
    @Test
    public void testImportedScriptFunctionAccessOwnVariables() {
        // 注册脚本（函数访问自己的全局变量）- 使用正确的函数定义语法
        String scriptContent = "String prefix = \"User\"\n" +
                              "fun format(String name)String {\n" +
                              "    return prefix + \":\" + name\n" +
                              "}";
        NfScriptRegistry.registerScript("testUtils", scriptContent);
        
        // 导入并调用函数
        String mainScript = "import nf testUtils\n" +
                           "String result = testUtils.format(\"张三\")\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("User:张三", result);
    }

    /**
     * 测试导入脚本的函数不能访问当前脚本的变量
     */
    @Test
    public void testImportedScriptFunctionCannotAccessCurrentScriptVariables() {
        // 注册脚本（函数尝试访问外部变量）- 使用正确的函数定义语法
        String scriptContent = "fun format(String name)String {\n" +
                              "    return externalVar + \":\" + name\n" +
                              "}";
        NfScriptRegistry.registerScript("testUtils", scriptContent);
        
        // 导入并调用函数（外部变量不存在）
        String mainScript = "import nf testUtils\n" +
                           "String externalVar = \"External\"\n" +
                           "String result = testUtils.format(\"张三\")\n" +
                           "export result";
        
        // 应该抛出异常，因为导入脚本的函数无法访问当前脚本的变量
        assertThrows(Exception.class, () -> {
            NfMain.run(mainScript, null, null);
        });
    }

    /**
     * 测试导入未注册的脚本
     */
    @Test
    public void testImportUnregisteredScript() {
        String mainScript = "import nf nonexistent\n" +
                           "export \"test\"";
        
        assertThrows(Exception.class, () -> {
            NfMain.run(mainScript, null, null);
        });
    }

    /**
     * 测试使用脚本文件：导入未注册的脚本
     */
    @Test
    public void testImportFromFileUnregistered() {
        String script = TestUtil.readFile("import/test_error_unregistered.nf");
        
        assertThrows(Exception.class, () -> {
            NfMain.run(script, null, null);
        });
    }

    /**
     * 测试使用脚本文件：访问不存在的变量
     */
    @Test
    public void testImportFromFileNonexistentVariable() {
        String script = TestUtil.readFile("import/test_error_nonexistent_var.nf");
        
        assertThrows(Exception.class, () -> {
            NfMain.run(script, null, null);
        });
    }

    /**
     * 测试使用脚本文件：调用不存在的函数
     */
    @Test
    public void testImportFromFileNonexistentFunction() {
        String script = TestUtil.readFile("import/test_error_nonexistent_function.nf");
        
        assertThrows(Exception.class, () -> {
            NfMain.run(script, null, null);
        });
    }

    /**
     * 测试访问不存在的变量
     */
    @Test
    public void testAccessNonExistentVariable() {
        // 注册脚本
        NfScriptRegistry.registerScript("utils", "String var1 = \"test\"");
        
        // 尝试访问不存在的变量
        String mainScript = "import nf utils\n" +
                           "String result = utils.nonexistent\n" +
                           "export result";
        
        assertThrows(Exception.class, () -> {
            NfMain.run(mainScript, null, null);
        });
    }

    /**
     * 测试调用不存在的函数
     */
    @Test
    public void testCallNonExistentFunction() {
        // 注册脚本
        NfScriptRegistry.registerScript("utils", "String var1 = \"test\"");
        
        // 尝试调用不存在的函数
        String mainScript = "import nf utils\n" +
                           "String result = utils.nonexistent()\n" +
                           "export result";
        
        assertThrows(Exception.class, () -> {
            NfMain.run(mainScript, null, null);
        });
    }

    /**
     * 测试重复导入同一个脚本
     */
    @Test
    public void testDuplicateImport() {
        // 注册脚本
        NfScriptRegistry.registerScript("testUtils", "String testVar = \"test\"");
        
        // 重复导入
        String mainScript = "import nf testUtils\n" +
                           "import nf testUtils\n" +
                           "String result = testUtils.testVar\n" +
                           "export result";
        
        // 应该成功（重复导入被忽略）
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("test", result);
    }

    /**
     * 测试复杂场景：多个脚本，多个函数和变量
     */
    @Test
    public void testComplexScenario() {
        // 主脚本使用多个导入脚本
        String mainScript = "import nf math, utils\n" +
                           "Integer sum = math.add(10, 20)\n" +
                           "Integer total = sum * 2\n" +
                           "String info = utils.formatUser(\"张三\", total)\n" +
                           "export info";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("User-张三-60", result); // sum = 30, total = 60
    }

    /**
     * 测试使用脚本文件：基础导入测试
     */
    @Test
    public void testImportFromFileBasic() {
        String script = TestUtil.readFile("import/test_basic.nf");
        Object result = NfMain.run(script, null, null);
        assertEquals("User-张三-25", result);
    }

    /**
     * 测试使用脚本文件：导入多个脚本
     */
    @Test
    public void testImportFromFileMultiple() {
        String script = TestUtil.readFile("import/test_multiple.nf");
        Object result = NfMain.run(script, null, null);
        assertNotNull(result);
        String resultStr = result.toString();
        assertTrue(resultStr.contains("李四"));
        assertTrue(resultStr.contains("30"));
        assertTrue(resultStr.contains("MyApp"));
    }

    /**
     * 测试使用脚本文件：函数访问自己的全局变量
     */
    @Test
    public void testImportFromFileFunctionAccessOwnVars() {
        String script = TestUtil.readFile("import/test_function_access_own_vars.nf");
        Object result = NfMain.run(script, null, null);
        assertEquals("User-王五-28", result);
    }

    /**
     * 测试使用脚本文件：复杂场景
     */
    @Test
    public void testImportFromFileComplex() {
        String script = TestUtil.readFile("import/test_complex.nf");
        Object result = NfMain.run(script, null, null);
        assertNotNull(result);
        String resultStr = result.toString();
        assertTrue(resultStr.contains("test"));
        assertTrue(resultStr.contains("25")); // square(5) = 25
        assertTrue(resultStr.contains("Base:30")); // process(3) = Base:30
    }

    /**
     * 测试访问导入脚本的多个变量
     */
    @Test
    public void testAccessMultipleVariables() {
        String mainScript = "import nf config\n" +
                           "String name = config.appName\n" +
                           "String version = config.appVersion\n" +
                           "Integer max = config.maxUsers\n" +
                           "export name + \"-\" + version + \"-\" + max";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("MyApp-1.0.0-1000", result);
    }

    /**
     * 测试调用导入脚本的多个函数
     */
    @Test
    public void testCallMultipleFunctions() {
        String mainScript = "import nf math\n" +
                           "Integer sq = math.square(5)\n" +
                           "Integer cb = math.cube(3)\n" +
                           "Integer maxVal = math.max(10, 20)\n" +
                           "Integer minVal = math.min(10, 20)\n" +
                           "export sq + \"-\" + cb + \"-\" + maxVal + \"-\" + minVal";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("25-27-20-10", result);
    }

    /**
     * 测试调用导入脚本的递归函数
     */
    @Test
    public void testCallRecursiveFunction() {
        String mainScript = "import nf complex\n" +
                           "Integer sum = complex.sum(5)\n" +
                           "export sum";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals(15, result); // 1+2+3+4+5 = 15
    }

    /**
     * 测试调用导入脚本的函数，函数内部调用其他函数
     */
    @Test
    public void testCallFunctionThatCallsOtherFunctions() {
        String mainScript = "import nf complex\n" +
                           "String result = complex.process(3)\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("Base:30", result); // process(3) -> calculate(3) = 30 -> formatResult(30) = "Base:30"
    }

    /**
     * 测试调用导入脚本的函数，函数使用可变参数
     */
    @Test
    public void testCallFunctionWithVarArgs() {
        String mainScript = "import nf string\n" +
                           "String result = string.join(\"-\", \"a\", \"b\", \"c\")\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("a-b-c", result);
    }

    /**
     * 测试调用导入脚本的函数，函数使用默认参数值
     */
    @Test
    public void testCallFunctionWithDefaultValues() {
        String mainScript = "import nf string\n" +
                           "String wrapped1 = string.wrap(\"test\")\n" +
                           "String wrapped2 = string.wrap(\"test\", \"<\", \">\")\n" +
                           "export wrapped1 + \"|\" + wrapped2";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("[test]|<test>", result);
    }

    /**
     * 测试在表达式中使用导入脚本的变量
     */
    @Test
    public void testUseImportedVariableInExpression() {
        String mainScript = "import nf math\n" +
                           "Integer result = math.PI + math.E\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals(585, result); // 314 + 271 = 585
    }

    /**
     * 测试在表达式中使用导入脚本的函数返回值
     */
    @Test
    public void testUseImportedFunctionInExpression() {
        String mainScript = "import nf math\n" +
                           "Integer result = math.square(5) + math.cube(2)\n" +
                           "export result";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals(33, result); // 25 + 8 = 33
    }

    /**
     * 测试导入脚本的函数访问自己的全局变量（通过函数调用）
     */
    @Test
    public void testImportedFunctionAccessOwnGlobalVariable() {
        String mainScript = "import nf utils\n" +
                           "Integer age = utils.getDefaultAge()\n" +
                           "export age";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals(18, result);
    }

    /**
     * 测试多个导入脚本之间的交互
     */
    @Test
    public void testInteractionBetweenImportedScripts() {
        String mainScript = "import nf math, string\n" +
                           "Integer square = math.square(4)\n" +
                           "String repeated = string.repeat(\"x\", square)\n" +
                           "export repeated";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("xxxxxxxxxxxxxxxx", result); // repeat("x", 16)
    }

    /**
     * 测试导入脚本的变量和函数混合使用
     */
    @Test
    public void testMixedUseOfVariablesAndFunctions() {
        String mainScript = "import nf utils, config\n" +
                           "String user = utils.formatUser(\"测试\", 20)\n" +
                           "String app = config.appName\n" +
                           "Integer defaultAge = utils.getDefaultAge()\n" +
                           "export user + \"|\" + app + \"|\" + defaultAge";
        
        Object result = NfMain.run(mainScript, null, null);
        assertEquals("User-测试-20|MyApp|18", result);
    }
}

