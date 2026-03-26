package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhileParseTest {

    @Test
    public void testWhileParse() {
        String script = "Integer i = 1\nwhile i <= 5 {\n    i = i + 1\n}";
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        assertEquals(2, syntaxNodes.size());
        assertEquals(SyntaxNodeType.ASSIGN_EXP, syntaxNodes.get(0).getType());
        assertEquals(SyntaxNodeType.WHILE_EXP, syntaxNodes.get(1).getType());
        assertTrue(tokens.isEmpty(), "while 脚本解析完成后不应残留 token");
    }
}
