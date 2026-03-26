package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhileTokenPrintTest {

    @Test
    public void testPrintTokens() {
        String script = "Integer i = 1\nwhile i <= 3 {\n    i = i + 1\n}";
        List<Token> tokens = NfToken.tokens(script);

        assertTrue(tokens.stream().anyMatch(token -> token.type == TokenType.WHILE));
        assertTrue(tokens.stream().anyMatch(token -> token.type == TokenType.LE));
        assertTrue(tokens.stream().anyMatch(token -> token.type == TokenType.LBRACE));
        assertTrue(tokens.stream().anyMatch(token -> token.type == TokenType.RBRACE));
        assertTrue(tokens.stream().anyMatch(token -> token.type == TokenType.LINE_END));
    }
}
