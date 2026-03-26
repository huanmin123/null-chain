package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.language.NfPerformanceMonitor;
import com.gitee.huanminabc.nullchain.language.NfPerformanceReport;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.ExportSyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NfMain 集成测试类
 * 合并了缓存功能测试和集成测试
 * 测试性能监控、缓存和脚本执行的集成使用
 * 
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NfMainIntegrationTest {
    

    // ========== 基本功能测试 ==========
    
    @Test
    public void testNormalExecution() {
        String script = "Integer a = 1\n" +
            "String b = \"test\"\n" +
            "echo a\n" +
            "export a\n";
        
        // 正常执行，不使用性能监控
        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            assertNotNull(result);
            assertEquals(1, result);
        });
    }
    
    @Test
    public void testPerformanceMonitoring() {
        String script = "Integer sum = 0\n" +
            "for i in 1..100 {\n" +
            "    sum = sum + i\n" +
            "}\n" +
            "echo sum\n" +
            "echo sum\n" +
            "export sum\n";
        
        // 启用性能监控
        Object result1 = NfMain.run(script, log, null, false);//预热
        Object result = NfMain.run(script, log, null, true);

        assertNotNull(result);
        assertEquals(5050, result);
    }

    @Test
    public void testNestedPerformanceMonitoring() {
        String script = "Integer sum = 0\n" +
            "for i in 1..3 {\n" +
            "    sum = sum + i\n" +
            "}\n" +
            "export sum\n";

        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        NfContext context = new NfContext();
        NfPerformanceMonitor monitor = new NfPerformanceMonitor();
        monitor.start();
        context.setPerformanceMonitor(monitor);

        NfContextScope mainScope = NfRun.prepareContext(context, log, null);
        try {
            SyntaxNodeFactory.executeAll(syntaxNodes, context);

            NfPerformanceReport report = monitor.generateReport();
            assertNotNull(report.getNodeStatistics().get("ASSIGN_EXP"));
            assertEquals(4, report.getNodeStatistics().get("ASSIGN_EXP").getExecutionCount());
            assertEquals(1, report.getNodeStatistics().get("FOR_EXP").getExecutionCount());
            assertEquals(6, mainScope.getVariable(ExportSyntaxNode.EXPORT).getValue());
        } finally {
            context.endExecution();
            context.clear();
        }
    }




}
