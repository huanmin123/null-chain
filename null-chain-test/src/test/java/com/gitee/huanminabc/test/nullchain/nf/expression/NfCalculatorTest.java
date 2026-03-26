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

    private static NfContext createContext() {
        NfContext context = new NfContext();
        String mainScopeId = NfContext.generateScopeId();
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        return context;
    }

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
        NfContext context = createContext();
        NfContextScope scope = context.getCurrentScope();
        
        // Add variables
        scope.addVariable(new NfVariableInfo("i", 1, Integer.class));
        scope.addVariable(new NfVariableInfo("product", 1, Integer.class));
        
        // Test expression
        Object result = NfCalculator.arithmetic("i <= 5", context);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
        
        System.out.println("Test passed! result=" + result);
    }

    @Test
    public void testArithmeticUsesLatestParentScopeValueAfterMutation() {
        NfContext context = createContext();
        NfContextScope mainScope = context.getCurrentScope();
        mainScope.addVariable(new NfVariableInfo("a", 1, Integer.class));

        NfContextScope childScope = context.createChildScope(context.getMainScopeId(), NfContextScopeType.ALL);
        childScope.addVariable(new NfVariableInfo("b", 2, Integer.class));

        assertEquals(3, NfCalculator.arithmetic("a + b", context));

        mainScope.addVariable(new NfVariableInfo("a", 5, Integer.class));

        assertEquals(7, NfCalculator.arithmetic("a + b", context));
    }

    @Test
    public void testCurrentScopeStillOverridesParentAfterParentMutation() {
        NfContext context = createContext();
        NfContextScope mainScope = context.getCurrentScope();
        mainScope.addVariable(new NfVariableInfo("value", 1, Integer.class));

        NfContextScope childScope = context.createChildScope(context.getMainScopeId(), NfContextScopeType.ALL);
        childScope.addVariable(new NfVariableInfo("value", 2, Integer.class));

        assertEquals(2, NfCalculator.arithmetic("value", context));

        mainScope.addVariable(new NfVariableInfo("value", 5, Integer.class));

        assertEquals(2, NfCalculator.arithmetic("value", context));
    }

    @Test
    public void testImportCacheRefreshesAfterNewImport() {
        NfContext context = createContext();

        assertEquals(true, NfCalculator.arithmetic("UUID.randomUUID() != null", context));

        context.addImport("LocalDateAlias", "java.time.LocalDate");

        assertEquals(true, NfCalculator.arithmetic("LocalDateAlias.now() != null", context));
    }
}
