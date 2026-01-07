package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 if-else 基本功能是否被破坏
 */
@Slf4j
public class IfElseBasicTest {

    @Test
    @DisplayName("测试基本的 if-else 语句")
    public void testBasicIfElse() {
        String script = 
            "Integer x = 10\n" +
            "if x > 5 {\n" +
            "    Integer y = 100\n" +
            "} else {\n" +
            "    Integer y = 200\n" +
            "}\n" +
            "export \"测试完成\"";
        
        Object result = NfMain.run(script, log, null);
        System.out.println("Result: " + result);
    }

    @Test
    @DisplayName("测试 if 语句没有 else")
    public void testIfWithoutElse() {
        String script = 
            "Integer x = 10\n" +
            "if x > 5 {\n" +
            "    Integer y = 100\n" +
            "}\n" +
            "export \"测试完成\"";
        
        Object result = NfMain.run(script, log, null);
        System.out.println("Result: " + result);
    }

    @Test
    @DisplayName("测试 if-else if-else")
    public void testIfElseIfElse() {
        String script = 
            "Integer x = 10\n" +
            "if x > 20 {\n" +
            "    Integer y = 300\n" +
            "} else if x > 10 {\n" +
            "    Integer y = 200\n" +
            "} else {\n" +
            "    Integer y = 100\n" +
            "}\n" +
            "export \"测试完成\"";
        
        Object result = NfMain.run(script, log, null);
        System.out.println("Result: " + result);
    }
}
