package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class WhileSimpleTest {

    @Test
    public void testWhileSimple() {
        String script = "while i <= 3 {\n    i = i + 1\n}";
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        log.info("Total tokens: {}", tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            com.gitee.huanminabc.nullchain.language.token.Token t = tokens.get(i);
            log.info("{}: {} = '{}'", i, t.type, t.value);
        }
        
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        log.info("SyntaxNodes count: {}", syntaxNodes.size());
        for (SyntaxNode node : syntaxNodes) {
            log.info("Node type: {}", node.getType());
        }
    }
}
