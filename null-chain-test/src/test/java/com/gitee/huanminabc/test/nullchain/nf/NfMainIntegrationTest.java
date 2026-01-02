package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    
    @BeforeEach
    public void setUp() {
        // 清理缓存，确保测试环境干净
        NfMain.shutdown();
    }
    
    @AfterEach
    public void tearDown() {
        // 测试后清理缓存
        NfMain.shutdown();
    }
    
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
            "export sum\n";
        
        // 启用性能监控
        Object result = NfMain.run(script, log, null, true);
        
        assertNotNull(result);
        // 性能报告应该已经输出到日志
    }
    
    // ========== 缓存功能测试 ==========
    
    @Test
    public void testCacheHit() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 第一次执行，应该缓存
        Object result1 = NfMain.run(script, log, null);
        
        // 第二次执行相同脚本，应该从缓存获取
        Object result2 = NfMain.run(script, log, null);
        
        // 结果应该相同
        assertEquals(result1, result2);
        assertEquals(1, result1);
        
        // 验证缓存统计
        String stats = NfMain.getCacheStats();
        assertTrue(stats.contains("命中"));
        log.info("缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheMiss() {
        String script1 = "Integer a = 1\n" +
            "export a\n";
        String script2 = "Integer b = 2\n" +
            "export b\n";
        
        // 执行不同的脚本
        Object result1 = NfMain.run(script1, log, null);
        Object result2 = NfMain.run(script2, log, null);
        
        assertEquals(1, result1);
        assertEquals(2, result2);
        
        // 验证缓存统计
        String stats = NfMain.getCacheStats();
        assertNotNull(stats);
        log.info("缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheStats() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 执行脚本
        Object result = NfMain.run(script, log, null);
        assertEquals(1, result);
        
        // 获取缓存统计
        String stats = NfMain.getCacheStats();
        
        assertNotNull(stats);
        assertTrue(stats.contains("NF脚本缓存统计"));
        assertTrue(stats.contains("命中率") || stats.contains("命中"));
        log.info("缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheShutdown() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 执行脚本，填充缓存
        Object result = NfMain.run(script, log, null);
        assertEquals(1, result);
        
        // 验证缓存有内容
        String statsBefore = NfMain.getCacheStats();
        assertNotNull(statsBefore);
        
        // 关闭缓存
        NfMain.shutdown();
        
        // 验证缓存已清理
        String statsAfter = NfMain.getCacheStats();
        assertNotNull(statsAfter);
        // 缓存大小应该为0或很小
        log.info("关闭前统计: {}", statsBefore);
        log.info("关闭后统计: {}", statsAfter);
    }
    
    @Test
    public void testCacheWithPerformanceMonitoring() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 启用性能监控执行脚本
        Object result = NfMain.run(script, log, null, true);
        assertEquals(1, result);
        
        // 验证缓存统计
        String stats = NfMain.getCacheStats();
        assertNotNull(stats);
        log.info("性能监控模式缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheConsistency() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 多次执行相同脚本
        Object result1 = NfMain.run(script, log, null);
        Object result2 = NfMain.run(script, log, null);
        Object result3 = NfMain.run(script, log, null);
        
        // 结果应该一致
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertEquals(1, result1);
        
        // 验证缓存命中率应该很高
        String stats = NfMain.getCacheStats();
        log.info("一致性测试缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheWithDifferentScripts() {
        // 执行多个不同的脚本
        for (int i = 0; i < 5; i++) {
            String script = "Integer a" + i + " = " + i + "\n" +
                "export a" + i + "\n";
            Object result = NfMain.run(script, log, null);
            assertEquals(i, result);
        }
        
        // 验证缓存统计
        String stats = NfMain.getCacheStats();
        assertNotNull(stats);
        log.info("多脚本缓存统计: {}", stats);
    }
    
    @Test
    public void testCacheFunctionality() {
        String script = "Integer a = 1\n" +
            "export a\n";
        
        // 正常模式会缓存
        Object result = NfMain.run(script, log, null);
        assertNotNull(result);
        assertEquals(1, result);
        
        // 验证缓存统计
        String stats = NfMain.getCacheStats();
        assertNotNull(stats);
        log.info("缓存统计: {}", stats);
    }
}
