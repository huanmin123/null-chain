package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class WhileParseTest {

    @Test
    public void testWhileParse() {
        String script = "Integer i = 1\nwhile i <= 5 {\n    i = i + 1\n}";
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        log.info("SyntaxNodes count: {}", syntaxNodes.size());
        for (SyntaxNode node : syntaxNodes) {
            log.info("Node type: {}", node.getType());
        }
    }
}
