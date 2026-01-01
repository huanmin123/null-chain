package com.gitee.huanminabc.nullchain.leaf.http.sse;

import lombok.extern.slf4j.Slf4j;

/**
 * SSE 重连管理器
 * 
 * <p>封装重连策略和状态管理，提供统一的重连间隔计算和状态控制。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEReconnectManager {
    
    /**
     * 重连策略枚举
     */
    public enum ReconnectStrategy {
        /** 固定间隔 */
        FIXED,
        /** 指数退避 */
        EXPONENTIAL_BACKOFF,
        /** 线性递增 */
        LINEAR
    }
    
    /**
     * 计算重连延迟时间
     * 
     * <p>注意：此方法会检查溢出情况，确保返回值在合理范围内。</p>
     * 
     * @param attempt 当前重连次数（从1开始）
     * @param baseInterval 基础间隔（毫秒）
     * @param strategy 重连策略
     * @param maxDelay 最大延迟时间（毫秒，0表示无限制）
     * @return 延迟时间（毫秒）
     */
    public static long calculateDelay(int attempt, long baseInterval, ReconnectStrategy strategy, long maxDelay) {
        if (attempt <= 0 || baseInterval <= 0) {
            return baseInterval;
        }
        
        long delay;
        try {
            switch (strategy) {
                case EXPONENTIAL_BACKOFF:
                    // 指数退避：baseInterval * 2^(attempt-1)
                    // 检查溢出：如果 attempt-1 >= 63，则 1L << (attempt-1) 会溢出
                    if (attempt - 1 >= 63) {
                        log.warn("[SSE重连] 重连次数过大({})，指数退避计算可能溢出，使用最大延迟", attempt);
                        delay = Long.MAX_VALUE;
                    } else {
                        long multiplier = 1L << (attempt - 1);
                        // 检查乘法溢出
                        if (multiplier > Long.MAX_VALUE / baseInterval) {
                            log.warn("[SSE重连] 延迟计算可能溢出，attempt: {}, baseInterval: {}ms，使用最大延迟", 
                                    attempt, baseInterval);
                            delay = Long.MAX_VALUE;
                        } else {
                            delay = baseInterval * multiplier;
                        }
                    }
                    break;
                case LINEAR:
                    // 线性递增：baseInterval * attempt
                    // 检查乘法溢出
                    if (attempt > Long.MAX_VALUE / baseInterval) {
                        log.warn("重连延迟计算可能溢出（attempt={}, baseInterval={}），使用最大延迟", 
                                attempt, baseInterval);
                        delay = Long.MAX_VALUE;
                    } else {
                        delay = baseInterval * attempt;
                    }
                    break;
                case FIXED:
                default:
                    // 固定间隔
                    delay = baseInterval;
                    break;
            }
        } catch (ArithmeticException e) {
            log.warn("[SSE重连] 延迟计算溢出，使用最大延迟", e);
            delay = Long.MAX_VALUE;
        }
        
        // 限制最大延迟（防止溢出值）
        if (maxDelay > 0) {
            if (delay > maxDelay || delay < 0) {
                delay = maxDelay;
            }
        } else if (delay < 0 || delay == Long.MAX_VALUE) {
            // 如果没有设置最大延迟，但计算结果溢出，使用默认最大延迟（60秒）
            delay = 60000;
            log.warn("[SSE重连] 延迟计算结果异常，使用默认最大延迟60秒");
        }
        
        return delay;
    }
    
    /**
     * 计算重连延迟时间（使用默认策略：指数退避，最大延迟60秒）
     * 
     * @param attempt 当前重连次数（从1开始）
     * @param baseInterval 基础间隔（毫秒）
     * @return 延迟时间（毫秒）
     */
    public static long calculateDelay(int attempt, long baseInterval) {
        return calculateDelay(attempt, baseInterval, ReconnectStrategy.EXPONENTIAL_BACKOFF, 60000);
    }
    
    /**
     * 判断HTTP错误是否可重试
     * 
     * <p>根据HTTP状态码判断错误是否可重试：
     * <ul>
     *   <li>5xx 服务器错误：可重试（临时错误）</li>
     *   <li>429 请求过多：可重试（限流，等待后重试）</li>
     *   <li>408 请求超时：可重试（网络问题）</li>
     *   <li>4xx 客户端错误：不可重试（永久错误，如404、401）</li>
     *   <li>其他：不可重试</li>
     * </ul>
     * </p>
     * 
     * @param httpCode HTTP状态码
     * @return 如果可重试返回 true，否则返回 false
     */
    public static boolean isRetryableHttpError(int httpCode) {
        // 5xx 服务器错误：可重试（临时错误）
        if (httpCode >= 500 && httpCode < 600) {
            return true;
        }
        
        // 429 请求过多：可重试（限流，等待后重试）
        if (httpCode == 429) {
            return true;
        }
        
        // 408 请求超时：可重试（网络问题）
        if (httpCode == 408) {
            return true;
        }
        
        // 其他状态码：不可重试
        return false;
    }
}

