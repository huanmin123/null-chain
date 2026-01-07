package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Fun 类型调试测试
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Slf4j
public class FunDebugTest {

    @Test
    @DisplayName("调试：打印多行脚本的 tokens")
    public void test1_PrintTokens() {
        String line = "Fun addFunc = getAdder()\nInteger result = addFunc(5, 10)";
        List<Token> tokens = NfToken.tokens(line);
        log.info("Line: {}", line.replace("\n", "\\n"));
        log.info("Total tokens: {}", tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            log.info("  [{}]: type={}, value='{}'", i, t.type, t.value.replace("\n", "\\n"));
        }
    }

    @Test
    @DisplayName("调试：测试简化 Fun 赋值")
    public void test2_SimpleFunAssign() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "Fun addFunc = add\n" +
            "export addFunc";

        Object result = NfMain.run(script, log, null);
        log.info("Result: {}", result);
    }

    @Test
    @DisplayName("调试：测试函数返回 Fun 引用")
    public void test3_FunctionReturnFun() {
        String script =
            "fun add(int a, int b) Integer {\n" +
            "    return a + b\n" +
            "}\n" +
            "fun getAdder() Fun {\n" +
            "    return add\n" +
            "}\n" +
            "export getAdder";

        Object result = NfMain.run(script, log, null);
        log.info("Result: {}", result);
    }
}
