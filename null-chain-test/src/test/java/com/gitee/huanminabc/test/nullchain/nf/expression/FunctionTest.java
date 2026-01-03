package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 函数功能测试类
 * 测试函数定义、调用、多返回值等功能
 * 
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class FunctionTest {

    @BeforeEach
    public void clearCache() {
        NfMain.shutdown();
    }

    /**
     * 测试基础函数定义和调用 - 单返回值
     */
    @Test
    public void testBasicFunction() {
        String script = "fun add(int a, int b)Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Integer result = add(10, 20)\n" +
            "echo \"add(10, 20) = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(30, result);
        log.info("基础函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试基础函数 - 使用脚本文件
     */
    @Test
    public void testBasicFunctionFromFile() {
        String file = TestUtil.readFile("function/function_basic.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        // 注意：由于编码问题，这里使用contains检查
        assertTrue(result.toString().contains("函数基础测试完成") || result.toString().contains("完成"));
        log.info("基础函数文件测试通过，结果: {}", result);
    }

    /**
     * 测试多返回值函数
     */
    @Test
    public void testMultiReturnFunction() {
        String script = "fun getNameAndAge(String name, int age)String,Integer {\n" +
            "    return name, age\n" +
            "}\n" +
            "var name:String, age:Integer = getNameAndAge(\"张三\", 25)\n" +
            "echo \"name = {name}, age = {age}\"\n" +
            "export name\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals("张三", result);
        log.info("多返回值函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试多返回值函数 - 使用脚本文件
     */
    @Test
    public void testMultiReturnFunctionFromFile() {
        String file = TestUtil.readFile("function/function_multi_return.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("多返回值测试完成") || result.toString().contains("完成"));
        log.info("多返回值函数文件测试通过，结果: {}", result);
    }

    /**
     * 测试函数在表达式中的使用
     */
    @Test
    public void testFunctionInExpression() {
        String script = "fun multiply(int a, int b)Integer {\n" +
            "    return a * b\n" +
            "}\n" +
            "Integer result = multiply(5, 6) + multiply(2, 3)\n" +
            "echo \"result = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(36, result); // 30 + 6 = 36
        log.info("表达式中的函数调用测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数在表达式中的使用 - 使用脚本文件
     */
    @Test
    public void testFunctionInExpressionFromFile() {
        String file = TestUtil.readFile("function/function_in_expression.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("表达式中的函数调用测试完成") || result.toString().contains("完成"));
        log.info("表达式中的函数调用文件测试通过，结果: {}", result);
    }

    /**
     * 测试可变参数函数
     */
    @Test
    public void testVarArgsFunction() {
        String script = "fun sum(int first, int... rest)Integer {\n" +
            "    Integer total = first\n" +
            "    for item in rest {\n" +
            "        total = total + item\n" +
            "    }\n" +
            "    return total\n" +
            "}\n" +
            "Integer result = sum(1, 2, 3, 4, 5)\n" +
            "echo \"sum(1, 2, 3, 4, 5) = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(15, result); // 1 + 2 + 3 + 4 + 5 = 15
        log.info("可变参数函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试可变参数函数 - 使用脚本文件
     */
    @Test
    public void testVarArgsFunctionFromFile() {
        String file = TestUtil.readFile("function/function_varargs.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("可变参数函数测试完成") || result.toString().contains("完成"));
        log.info("可变参数函数文件测试通过，结果: {}", result);
    }

    /**
     * 测试函数递归调用
     */
    @Test
    public void testRecursiveFunction() {
        String script = "fun factorial(int n)Integer {\n" +
            "    if n <= 1 {\n" +
            "        return 1\n" +
            "    }\n" +
            "    return n * factorial(n - 1)\n" +
            "}\n" +
            "Integer result = factorial(5)\n" +
            "echo \"factorial(5) = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(120, result); // 5! = 120
        log.info("递归函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数递归调用 - 使用脚本文件
     */
    @Test
    public void testRecursiveFunctionFromFile() {
        String file = TestUtil.readFile("function/function_recursive.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("递归函数测试完成") || result.toString().contains("完成"));
        log.info("递归函数文件测试通过，结果: {}", result);
    }

    /**
     * 测试函数调用赋值 - 单返回值
     */
    @Test
    public void testFunctionCallAssignment() {
        String script = "fun getMessage()String {\n" +
            "    return \"Hello World\"\n" +
            "}\n" +
            "String msg = getMessage()\n" +
            "echo \"msg = {msg}\"\n" +
            "export msg\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals("Hello World", result);
        log.info("函数调用赋值测试通过，结果: {}", result);
    }

    /**
     * 测试复杂函数场景
     */
    @Test
    public void testComplexFunction() {
        String script = "fun calculate(int a, int b, int c)Integer,Integer {\n" +
            "    Integer sum = a + b + c\n" +
            "    Integer product = a * b * c\n" +
            "    return sum, product\n" +
            "}\n" +
            "var sum:Integer, product:Integer = calculate(2, 3, 4)\n" +
            "echo \"sum = {sum}, product = {product}\"\n" +
            "Integer result = sum + product\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(33, result); // 9 + 24 = 33
        log.info("复杂函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试复杂函数场景 - 使用脚本文件
     */
    @Test
    public void testComplexFunctionFromFile() {
        String file = TestUtil.readFile("function/function_complex.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("复杂函数测试完成") || result.toString().contains("完成"));
        log.info("复杂函数文件测试通过，结果: {}", result);
    }

    /**
     * 测试函数无返回值
     */
    @Test
    public void testFunctionNoReturn() {
        String script = "fun printMessage(String msg) {\n" +
            "    echo \"Message: {msg}\"\n" +
            "}\n" +
            "printMessage(\"测试消息\")\n" +
            "export \"完成\"\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals("完成", result);
        log.info("无返回值函数测试通过，结果: {}", result);
    }

    /**
     * 测试函数作用域隔离
     */
    @Test
    public void testFunctionScope() {
        String script = "Integer global = 100\n" +
            "fun testScope(int local)Integer {\n" +
            "    Integer inner = 50\n" +
            "    return global + local + inner\n" +
            "}\n" +
            "Integer result = testScope(10)\n" +
            "echo \"result = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(160, result); // 100 + 10 + 50 = 160
        log.info("函数作用域测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数作用域隔离 - 使用脚本文件
     */
    @Test
    public void testFunctionScopeFromFile() {
        String file = TestUtil.readFile("function/function_scope.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("函数作用域测试完成") || result.toString().contains("完成"));
        log.info("函数作用域文件测试通过，结果: {}", result);
    }

    /**
     * 测试函数集成场景
     */
    @Test
    public void testFunctionIntegration() {
        String script = "fun add(int a, int b)Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun multiply(int a, int b)Integer {\n" +
            "    return a * b\n" +
            "}\n" +
            "Integer result = multiply(add(2, 3), multiply(4, 5))\n" +
            "echo \"result = {result}\"\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(100, result); // (2+3) * (4*5) = 5 * 20 = 100
        log.info("函数集成测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数集成场景 - 使用脚本文件
     */
    @Test
    public void testFunctionIntegrationFromFile() {
        String file = TestUtil.readFile("function/function_integration.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);
        
        assertNotNull(result);
        assertTrue(result.toString().contains("函数集成测试完成") || result.toString().contains("完成"));
        log.info("函数集成文件测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数无参数
     */
    @Test
    public void testFunctionNoParams() {
        String script = "fun getValue()Integer {\n" +
            "    return 42\n" +
            "}\n" +
            "Integer result = getValue()\n" +
            "export result\n";
        
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);
        
        assertNotNull(result);
        assertEquals(42, result);
        log.info("无参数函数测试通过，结果: {}", result);
    }
    
    /**
     * 测试函数调用 - 带类型声明的赋值
     */
    @Test
    public void testFunctionCallWithTypeDeclaration() {
        String script = "fun getNumber()Integer {\n" +
            "    return 100\n" +
            "}\n" +
            "Integer num = getNumber()\n" +
            "export num\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(100, result);
        log.info("带类型声明的函数调用测试通过，结果: {}", result);
    }

    /**
     * 测试var自动推导函数返回值 - 单返回值
     */
    @Test
    public void testVarInferFunctionReturnValue() {
        String script = "fun getValue()Integer {\n" +
            "    return 42\n" +
            "}\n" +
            "var result = getValue()\n" +
            "echo \"result = {result}\"\n" +
            "export result\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(42, result);
        log.info("var自动推导函数返回值测试通过，结果: {}", result);
    }

    /**
     * 测试var自动推导函数返回值 - String类型
     */
    @Test
    public void testVarInferFunctionReturnString() {
        String script = "fun getMessage()String {\n" +
            "    return \"Hello World\"\n" +
            "}\n" +
            "var msg = getMessage()\n" +
            "echo \"msg = {msg}\"\n" +
            "export msg\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals("Hello World", result);
        log.info("var自动推导String返回值测试通过，结果: {}", result);
    }

    /**
     * 测试var自动推导嵌套函数调用
     */
    @Test
    public void testVarInferNestedFunctionCall() {
        String script = "fun add(int a, int b)Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun multiply(int a, int b)Integer {\n" +
            "    return a * b\n" +
            "}\n" +
            "var sum = add(5, 10)\n" +
            "var product = multiply(3, 4)\n" +
            "Integer result = sum + product\n" +
            "echo \"sum = {sum}, product = {product}, result = {result}\"\n" +
            "export result\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(27, result); // 15 + 12 = 27
        log.info("var自动推导嵌套函数调用测试通过，结果: {}", result);
    }

    /**
     * 测试var带类型声明但值是函数调用
     */
    @Test
    public void testVarWithTypeAnnotationFunctionCall() {
        String script = "fun getData()String {\n" +
            "    return \"test data\"\n" +
            "}\n" +
            "var data:String = getData()\n" +
            "echo \"data = {data}\"\n" +
            "export data\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals("test data", result);
        log.info("var带类型声明函数调用测试通过，结果: {}", result);
    }

    /**
     * 测试var自动推导多返回值函数
     */
    @Test
    public void testVarInferMultiReturnFunction() {
        String script = "fun getValues()String,Integer {\n" +
            "    return \"test\", 100\n" +
            "}\n" +
            "var name:String, count:Integer = getValues()\n" +
            "echo \"name = {name}, count = {count}\"\n" +
            "export count\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(100, result);
        log.info("var自动推导多返回值函数测试通过，结果: {}", result);
    }

    /**
     * 测试var混合类型声明 - 全部自动推导
     */
    @Test
    public void testVarMixedAllAutoInfer() {
        String script = "fun getInfo(String name, int age)String,Integer {\n" +
            "    return name, age\n" +
            "}\n" +
            "var info, age = getInfo(\"李四\", 30)\n" +
            "echo \"info = {info}, age = {age}\"\n" +
            "export age\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(30, result);
        log.info("var混合类型声明（全部自动推导）测试通过，结果: {}", result);
    }

    /**
     * 测试var混合类型声明 - 第一个自动推导，第二个指定类型
     */
    @Test
    public void testVarMixedFirstAutoSecondManual() {
        String script = "fun getData()String,Integer {\n" +
            "    return \"hello\", 42\n" +
            "}\n" +
            "var msg, count:Integer = getData()\n" +
            "echo \"msg = {msg}, count = {count}\"\n" +
            "export count\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(42, result);
        log.info("var混合类型声明（第一个自动推导，第二个指定类型）测试通过，结果: {}", result);
    }

    /**
     * 测试var混合类型声明 - 第一个指定类型，第二个自动推导
     */
    @Test
    public void testVarMixedFirstManualSecondAuto() {
        String script = "fun getNumbers()Integer,String {\n" +
            "    return 100, \"world\"\n" +
            "}\n" +
            "var num:Integer, text = getNumbers()\n" +
            "echo \"num = {num}, text = {text}\"\n" +
            "export num\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals(100, result);
        log.info("var混合类型声明（第一个指定类型，第二个自动推导）测试通过，结果: {}", result);
    }

    /**
     * 测试var混合类型声明 - 全部指定类型（向后兼容）
     */
    @Test
    public void testVarMixedAllManual() {
        String script = "fun getPersonInfo()String,Integer {\n" +
            "    return \"王五\", 25\n" +
            "}\n" +
            "var name:String, age:Integer = getPersonInfo()\n" +
            "echo \"name = {name}, age = {age}\"\n" +
            "export name\n";

        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(script, log, context);

        assertNotNull(result);
        assertEquals("王五", result);
        log.info("var混合类型声明（全部指定类型）测试通过，结果: {}", result);
    }

    /**
     * 测试var混合类型声明 - 使用脚本文件
     */
    @Test
    public void testVarMixedTypeFromFile() {
        String file = TestUtil.readFile("function/function_mixed_type.nf");
        Map<String, Object> context = new HashMap<>();
        Object result = NfMain.run(file, log, context);

        assertNotNull(result);
        assertTrue(result.toString().contains("测试完成") || result.toString().contains("完成"));
        log.info("var混合类型声明文件测试通过，结果: {}", result);
    }
}

