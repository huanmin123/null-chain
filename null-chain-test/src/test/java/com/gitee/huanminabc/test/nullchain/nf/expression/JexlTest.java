package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JexlTest {

    @Test
    public void testJexlBasic() {
        NfContext context = new NfContext();
        String mainScopeId = context.generateScopeId();
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        
        // 添加变量
        scope.addVariable(new NfVariableInfo("i", 1, Integer.class));
        
        log.info("Current Scope ID: {}", context.getCurrentScopeId());
        log.info("Scope variables: {}", scope.toMap());
        
        Object result = NfCalculator.arithmetic("i <= 5", context);
        log.info("Result: {}", result);
    }
}
