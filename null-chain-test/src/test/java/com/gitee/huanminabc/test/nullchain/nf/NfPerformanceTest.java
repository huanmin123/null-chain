package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfPerformanceMonitor;
import com.gitee.huanminabc.nullchain.language.NfPerformanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能监控和报告测试类
 * 合并了 NfPerformanceMonitor 和 NfPerformanceReport 的测试
 * 
 * @author huanmin
 * @date 2024/11/22
 */
public class NfPerformanceTest {
    
    private NfPerformanceMonitor monitor;
    
    @BeforeEach
    public void setUp() {
        monitor = new NfPerformanceMonitor();
    }
    
    // ========== NfPerformanceMonitor 测试 ==========
    
    @Test
    public void testStart() {
        monitor.start();
        // start方法没有返回值，主要验证不抛出异常
        assertDoesNotThrow(() -> monitor.start());
    }
    
    @Test
    public void testRecordNodeExecution() {
        monitor.start();
        
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L); // 1ms
        monitor.recordNodeExecution("ECHO_EXP", 500_000L); // 0.5ms
        
        NfPerformanceReport report = monitor.generateReport();
        assertNotNull(report);
        assertEquals(2, report.getNodeStatistics().size());
    }
    
    @Test
    public void testRecordNodeExecution_WithoutStart() {
        // 未调用start时，recordNodeExecution应该被忽略
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        
        // 应该抛出异常，因为未调用start
        assertThrows(IllegalStateException.class, () -> {
            monitor.generateReport();
        });
    }
    
    @Test
    public void testRecordMultipleExecutions() {
        monitor.start();
        
        // 记录同一个节点的多次执行
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ASSIGN_EXP", 2_000_000L);
        monitor.recordNodeExecution("ASSIGN_EXP", 1_500_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        Map<String, NfPerformanceMonitor.NodeStatistics> stats = report.getNodeStatistics();
        
        assertTrue(stats.containsKey("ASSIGN_EXP"));
        NfPerformanceMonitor.NodeStatistics stat = stats.get("ASSIGN_EXP");
        assertEquals(3, stat.getExecutionCount());
        assertEquals(4_500_000L, stat.getTotalTime());
        assertEquals(1_500_000L, stat.getAvgTime());
        assertEquals(2_000_000L, stat.getMaxTime());
        assertEquals(1_000_000L, stat.getMinTime());
    }
    
    @Test
    public void testGenerateReport() {
        monitor.start();
        
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ECHO_EXP", 500_000L);
        monitor.recordNodeExecution("FOR_EXP", 10_000_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        
        assertNotNull(report);
        assertEquals(3, report.getNodeStatistics().size());
        assertTrue(report.getTotalExecutionTime() > 0);
        assertTrue(report.getTotalDuration() > 0);
    }
    
    @Test
    public void testReset() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        
        assertTrue(monitor.generateReport().getNodeStatistics().size() > 0);
        
        monitor.reset();
        
        // 重置后应该抛出异常，因为需要重新start
        assertThrows(IllegalStateException.class, () -> {
            monitor.generateReport();
        });
    }
    
    @Test
    public void testNodeStatistics() {
        monitor.start();
        monitor.recordNodeExecution("TEST_NODE", 1_000_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        Map<String, NfPerformanceMonitor.NodeStatistics> stats = report.getNodeStatistics();
        
        assertTrue(stats.containsKey("TEST_NODE"));
        NfPerformanceMonitor.NodeStatistics stat = stats.get("TEST_NODE");
        
        assertEquals("TEST_NODE", stat.getNodeType());
        assertEquals(1, stat.getExecutionCount());
        assertEquals(1_000_000L, stat.getTotalTime());
        assertEquals(1_000_000L, stat.getAvgTime());
        assertEquals(1_000_000L, stat.getMaxTime());
        assertEquals(1_000_000L, stat.getMinTime());
    }
    
    // ========== NfPerformanceReport 测试 ==========
    
    @Test
    public void testReportGetNodeStatistics() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ECHO_EXP", 500_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        Map<String, NfPerformanceMonitor.NodeStatistics> stats = report.getNodeStatistics();
        
        assertEquals(2, stats.size());
        assertTrue(stats.containsKey("ASSIGN_EXP"));
        assertTrue(stats.containsKey("ECHO_EXP"));
    }
    
    @Test
    public void testGetSlowestNodes() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ECHO_EXP", 5_000_000L);
        monitor.recordNodeExecution("FOR_EXP", 10_000_000L);
        monitor.recordNodeExecution("IF_EXP", 2_000_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        List<NfPerformanceMonitor.NodeStatistics> slowest = report.getSlowestNodes(2);
        
        assertEquals(2, slowest.size());
        assertEquals("FOR_EXP", slowest.get(0).getNodeType());
        assertEquals("ECHO_EXP", slowest.get(1).getNodeType());
    }
    
    @Test
    public void testGetMostExecutedNodes() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ECHO_EXP", 500_000L);
        monitor.recordNodeExecution("ECHO_EXP", 500_000L);
        monitor.recordNodeExecution("FOR_EXP", 10_000_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        List<NfPerformanceMonitor.NodeStatistics> mostExecuted = report.getMostExecutedNodes(2);
        
        assertEquals(2, mostExecuted.size());
        assertEquals("ASSIGN_EXP", mostExecuted.get(0).getNodeType());
        assertEquals(3, mostExecuted.get(0).getExecutionCount());
        assertEquals("ECHO_EXP", mostExecuted.get(1).getNodeType());
        assertEquals(2, mostExecuted.get(1).getExecutionCount());
    }
    
    @Test
    public void testReportToString() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        monitor.recordNodeExecution("ECHO_EXP", 500_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        String reportStr = report.toString();
        
        assertNotNull(reportStr);
        assertTrue(reportStr.contains("NF脚本性能报告"));
        assertTrue(reportStr.contains("总体统计"));
        assertTrue(reportStr.contains("ASSIGN_EXP"));
        assertTrue(reportStr.contains("ECHO_EXP"));
    }
    
    @Test
    public void testEmptyReport() {
        monitor.start();
        // 不记录任何执行，只等待一段时间
        try {
            Thread.sleep(10); // 等待10ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        NfPerformanceReport report = monitor.generateReport();
        
        assertNotNull(report);
        assertEquals(0, report.getNodeStatistics().size());
        assertTrue(report.getTotalExecutionTime() == 0);
        assertTrue(report.getTotalDuration() > 0); // 总耗时应该大于0
    }
    
    @Test
    public void testGetSlowestNodes_MoreThanAvailable() {
        monitor.start();
        monitor.recordNodeExecution("ASSIGN_EXP", 1_000_000L);
        
        NfPerformanceReport report = monitor.generateReport();
        List<NfPerformanceMonitor.NodeStatistics> slowest = report.getSlowestNodes(10);
        
        // 即使请求10个，但只有1个节点，应该只返回1个
        assertEquals(1, slowest.size());
    }
}

