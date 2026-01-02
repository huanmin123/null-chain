package com.gitee.huanminabc.nullchain.language;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NF脚本性能报告
 * 
 * <p>包含脚本执行的详细性能统计信息，用于分析和优化脚本性能。</p>
 * 
 * @author huanmin
 * @date 2024/11/22
 */
public class NfPerformanceReport {
    
    private final Map<String, NfPerformanceMonitor.NodeStatistics> nodeStatistics;
    private final long totalExecutionTime; // 所有节点执行时间总和（纳秒）
    private final long totalDuration; // 总耗时（纳秒，包括其他开销）
    
    public NfPerformanceReport(Map<String, NfPerformanceMonitor.NodeStatistics> nodeStatistics,
                              long totalExecutionTime,
                              long totalDuration) {
        this.nodeStatistics = new LinkedHashMap<>(nodeStatistics);
        this.totalExecutionTime = totalExecutionTime;
        this.totalDuration = totalDuration;
    }
    
    /**
     * 获取节点统计信息
     * 
     * @return 节点统计信息映射
     */
    public Map<String, NfPerformanceMonitor.NodeStatistics> getNodeStatistics() {
        return new LinkedHashMap<>(nodeStatistics);
    }
    
    /**
     * 获取总执行时间（纳秒）
     * 
     * @return 总执行时间
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    /**
     * 获取总耗时（纳秒）
     * 
     * @return 总耗时
     */
    public long getTotalDuration() {
        return totalDuration;
    }
    
    /**
     * 获取最慢的节点（按总执行时间排序）
     * 
     * @param topN 返回前N个最慢的节点
     * @return 最慢的节点列表
     */
    public List<NfPerformanceMonitor.NodeStatistics> getSlowestNodes(int topN) {
        return nodeStatistics.values().stream()
            .sorted(Comparator.comparingLong(NfPerformanceMonitor.NodeStatistics::getTotalTime).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取执行次数最多的节点
     * 
     * @param topN 返回前N个执行次数最多的节点
     * @return 执行次数最多的节点列表
     */
    public List<NfPerformanceMonitor.NodeStatistics> getMostExecutedNodes(int topN) {
        return nodeStatistics.values().stream()
            .sorted(Comparator.comparingInt(NfPerformanceMonitor.NodeStatistics::getExecutionCount).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
    
    /**
     * 格式化报告为字符串
     * 
     * @return 格式化的报告字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== NF脚本性能报告 ==========\n\n");
        
        // 总体统计
        sb.append("总体统计：\n");
        sb.append(String.format("  总执行时间: %.2f ms\n", totalExecutionTime / 1_000_000.0));
        sb.append(String.format("  总耗时: %.2f ms\n", totalDuration / 1_000_000.0));
        sb.append(String.format("  其他开销: %.2f ms\n", (totalDuration - totalExecutionTime) / 1_000_000.0));
        sb.append(String.format("  监控节点数: %d\n", nodeStatistics.size()));
        sb.append("\n");
        
        // 最慢的节点
        List<NfPerformanceMonitor.NodeStatistics> slowestNodes = getSlowestNodes(5);
        if (!slowestNodes.isEmpty()) {
            sb.append("最慢的节点（Top 5）：\n");
            for (int i = 0; i < slowestNodes.size(); i++) {
                NfPerformanceMonitor.NodeStatistics stat = slowestNodes.get(i);
                sb.append(String.format("  %d. %s: %.2f ms (执行%d次, 平均%.2f ms, 最大%.2f ms, 最小%.2f ms)\n",
                    i + 1,
                    stat.getNodeType(),
                    stat.getTotalTime() / 1_000_000.0,
                    stat.getExecutionCount(),
                    stat.getAvgTime() / 1_000_000.0,
                    stat.getMaxTime() / 1_000_000.0,
                    stat.getMinTime() / 1_000_000.0
                ));
            }
            sb.append("\n");
        }
        
        // 执行次数最多的节点
        List<NfPerformanceMonitor.NodeStatistics> mostExecutedNodes = getMostExecutedNodes(5);
        if (!mostExecutedNodes.isEmpty()) {
            sb.append("执行次数最多的节点（Top 5）：\n");
            for (int i = 0; i < mostExecutedNodes.size(); i++) {
                NfPerformanceMonitor.NodeStatistics stat = mostExecutedNodes.get(i);
                sb.append(String.format("  %d. %s: 执行%d次, 总耗时%.2f ms, 平均%.2f ms\n",
                    i + 1,
                    stat.getNodeType(),
                    stat.getExecutionCount(),
                    stat.getTotalTime() / 1_000_000.0,
                    stat.getAvgTime() / 1_000_000.0
                ));
            }
            sb.append("\n");
        }
        
        // 详细统计
        if (!nodeStatistics.isEmpty()) {
            sb.append("详细统计：\n");
            nodeStatistics.values().stream()
                .sorted(Comparator.comparingLong(NfPerformanceMonitor.NodeStatistics::getTotalTime).reversed())
                .forEach(stat -> {
                    sb.append(String.format("  %s:\n", stat.getNodeType()));
                    sb.append(String.format("    执行次数: %d\n", stat.getExecutionCount()));
                    sb.append(String.format("    总耗时: %.2f ms\n", stat.getTotalTime() / 1_000_000.0));
                    sb.append(String.format("    平均耗时: %.2f ms\n", stat.getAvgTime() / 1_000_000.0));
                    sb.append(String.format("    最大耗时: %.2f ms\n", stat.getMaxTime() / 1_000_000.0));
                    sb.append(String.format("    最小耗时: %.2f ms\n", stat.getMinTime() / 1_000_000.0));
                    sb.append("\n");
                });
        }
        
        sb.append("=====================================");
        return sb.toString();
    }
}

