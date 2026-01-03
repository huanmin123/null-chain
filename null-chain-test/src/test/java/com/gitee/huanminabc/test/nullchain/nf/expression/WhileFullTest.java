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
public class WhileFullTest {

    @Test
    public void testWhileFull() {
        String script = "Integer i = 1\nwhile i <= 3 {\n    i = i + 1\n}";
        
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        
        // 初始化上下文
        NfContext context = new NfContext();
        String mainScopeId = NfContext.generateScopeId();
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        
        // 打印初始状态
        log.info("Initial - Current Scope ID: {}", context.getCurrentScopeId());
        
        // 执行第一个语法节点（Integer i = 1）
        SyntaxNode assignNode = syntaxNodes.get(0);
        log.info("Executing: {}", assignNode.getType());
        assignNode.run(context, assignNode);
        
        // 检查变量
        log.info("After assignment - Scope variables: {}", scope.toMap());
        log.info("Current Scope ID: {}", context.getCurrentScopeId());
        
        // 尝试计算表达式
        Object result = NfCalculator.arithmetic("i <= 3", context);
        log.info("Expression result: {}", result);
    }
}
