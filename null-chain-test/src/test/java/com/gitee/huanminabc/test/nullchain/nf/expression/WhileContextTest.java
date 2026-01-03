package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class WhileContextTest {

    @Test
    public void testWhileContext() {
        String script = "Integer i = 1\nInteger product = 1";
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        NfContext context = new NfContext();
        String mainScopeId = NfContext.generateScopeId();
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        
        // 手动执行语法节点
        for (SyntaxNode node : syntaxNodes) {
            node.run(context, node);
        }
        
        // 检查变量是否被正确添加
        log.info("After assignment - Scope ID: {}", context.getCurrentScopeId());
        log.info("After assignment - Scope variables: {}", scope.toMap());
        
        // 尝试计算表达式
        Object result = NfCalculator.arithmetic("i <= 5", context);
        log.info("Expression result: {}", result);
    }
}
