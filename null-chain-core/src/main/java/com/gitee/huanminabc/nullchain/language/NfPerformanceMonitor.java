package com.gitee.huanminabc.nullchain.language;

import java.util.*;

/**
 * NF脚本性能监控器
 * 
 * <p>用于监控脚本执行过程中的性能指标，包括：
 * <ul>
 *   <li>每个语法节点的执行时间</li>
 *   <li>总执行时间</li>
 *   <li>各类型节点的执行次数</li>
 *   <li>最慢的节点</li>
 * </ul>
 * </p>
 * 
 * <p>使用示例：
 * <pre>
 * NfPerformanceMonitor monitor = new NfPerformanceMonitor();
 * monitor.start();
 * // 执行脚本...
 * NfPerformanceReport report = monitor.generateReport();
 * logger.info("性能报告：\n{}", report);
 * </pre>
 * </p>
 * 
 * @author huanmin
 * @date 2024/11/22
 */
public class NfPerformanceMonitor {
    
    private final Map<String, MutableNodeStatistics> nodeExecutionStats = new LinkedHashMap<>();
    private long totalExecutionTime = 0;
    private long startTime;
    private boolean started = false;
    
    /**
     * 开始性能监控
     */
    public void start() {
        startTime = System.nanoTime();
        started = true;
    }
    
    /**
     * 记录语法节点的执行时间
     * 
     * @param nodeType 节点类型名称
     * @param durationNanos 执行时间（纳秒）
     */
    public void recordNodeExecution(String nodeType, long durationNanos) {
        if (!started) {
            return;
        }
        
        nodeExecutionStats.computeIfAbsent(nodeType, MutableNodeStatistics::new).record(durationNanos);
        totalExecutionTime += durationNanos;
    }
    
    /**
     * 生成性能报告
     * 
     * @return 性能报告
     */
    public NfPerformanceReport generateReport() {
        if (!started) {
            throw new IllegalStateException("性能监控未启动，请先调用start()方法");
        }
        
        long totalDuration = System.nanoTime() - startTime;
        
        // 计算各类型节点的统计信息
        Map<String, NodeStatistics> nodeStats = new LinkedHashMap<>();
        for (Map.Entry<String, MutableNodeStatistics> entry : nodeExecutionStats.entrySet()) {
            nodeStats.put(entry.getKey(), entry.getValue().toImmutable());
        }
        
        return new NfPerformanceReport(nodeStats, totalExecutionTime, totalDuration);
    }
    
    /**
     * 重置监控器
     */
    public void reset() {
        nodeExecutionStats.clear();
        totalExecutionTime = 0;
        started = false;
    }

    private static final class MutableNodeStatistics {
        private final String nodeType;
        private int executionCount;
        private long totalTime;
        private long maxTime;
        private long minTime = Long.MAX_VALUE;

        private MutableNodeStatistics(String nodeType) {
            this.nodeType = nodeType;
        }

        private void record(long durationNanos) {
            executionCount++;
            totalTime += durationNanos;
            if (durationNanos > maxTime) {
                maxTime = durationNanos;
            }
            if (durationNanos < minTime) {
                minTime = durationNanos;
            }
        }

        private NodeStatistics toImmutable() {
            long avgTime = executionCount == 0 ? 0 : totalTime / executionCount;
            long safeMinTime = executionCount == 0 ? 0 : minTime;
            return new NodeStatistics(nodeType, executionCount, totalTime, avgTime, maxTime, safeMinTime);
        }
    }
    
    /**
     * 节点统计信息
     */
    public static class NodeStatistics {
        private final String nodeType;
        private final int executionCount;
        private final long totalTime;
        private final long avgTime;
        private final long maxTime;
        private final long minTime;
        
        public NodeStatistics(String nodeType, int executionCount, long totalTime, 
                             long avgTime, long maxTime, long minTime) {
            this.nodeType = nodeType;
            this.executionCount = executionCount;
            this.totalTime = totalTime;
            this.avgTime = avgTime;
            this.maxTime = maxTime;
            this.minTime = minTime;
        }
        
        public String getNodeType() {
            return nodeType;
        }
        
        public int getExecutionCount() {
            return executionCount;
        }
        
        public long getTotalTime() {
            return totalTime;
        }
        
        public long getAvgTime() {
            return avgTime;
        }
        
        public long getMaxTime() {
            return maxTime;
        }
        
        public long getMinTime() {
            return minTime;
        }
    }
}
