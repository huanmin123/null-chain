package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.token.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WhileTokenPrintTest {

    @Test
    public void testPrintTokens() {
        String script = "Integer i = 1\nwhile i <= 3 {\n    i = i + 1\n}";
        List<Token> tokens = NfToken.tokens(script);
        System.out.println("Total tokens: " + tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            System.out.println(i + ": " + t.type + " = '" + t.value + "'");
        }
    }
}
