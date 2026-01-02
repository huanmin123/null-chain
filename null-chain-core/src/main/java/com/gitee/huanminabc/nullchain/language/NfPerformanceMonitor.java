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
    
    private final Map<String, List<Long>> nodeExecutionTimes = new LinkedHashMap<>();
    private final Map<String, Integer> nodeExecutionCounts = new HashMap<>();
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
        
        nodeExecutionTimes.computeIfAbsent(nodeType, k -> new ArrayList<>()).add(durationNanos);
        nodeExecutionCounts.merge(nodeType, 1, Integer::sum);
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
        for (Map.Entry<String, List<Long>> entry : nodeExecutionTimes.entrySet()) {
            String nodeType = entry.getKey();
            List<Long> times = entry.getValue();
            
            long total = times.stream().mapToLong(Long::longValue).sum();
            long avg = total / times.size();
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            
            nodeStats.put(nodeType, new NodeStatistics(
                nodeType,
                entry.getValue().size(),
                total,
                avg,
                max,
                min
            ));
        }
        
        return new NfPerformanceReport(nodeStats, totalExecutionTime, totalDuration);
    }
    
    /**
     * 重置监控器
     */
    public void reset() {
        nodeExecutionTimes.clear();
        nodeExecutionCounts.clear();
        totalExecutionTime = 0;
        started = false;
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

