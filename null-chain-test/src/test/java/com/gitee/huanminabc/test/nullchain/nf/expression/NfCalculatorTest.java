package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试NfCalculator
 */
public class NfCalculatorTest {

    @Test
    public void testSimpleArithmetic() {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 5);
        params.put("b", 3);
        
        Object result = NfCalculator.arithmetic("a + b", params);
        assertEquals(8, result);
    }

    @Test
    public void testArithmeticWithContext() {
        NfContext context = new NfContext();
        
        // Create main scope
        String mainScopeId = NfContext.generateScopeId();
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        
        // Add variables
        scope.addVariable(new NfVariableInfo("i", 1, Integer.class));
        scope.addVariable(new NfVariableInfo("product", 1, Integer.class));
        
        // Test expression
        Object result = NfCalculator.arithmetic("i <= 5", context);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
        
        System.out.println("Test passed! result=" + result);
    }
}
