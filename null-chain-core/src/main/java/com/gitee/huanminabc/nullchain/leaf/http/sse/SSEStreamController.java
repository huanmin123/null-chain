package com.gitee.huanminabc.nullchain.leaf.http.sse;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SSE 流控制器接口
 * 
 * <p>用于控制 SSE 流的终止状态，提供线程安全的终止机制。
 * 用户可以通过 EventMessage 的 terminate() 方法来请求终止流。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>用户在处理事件时发现结果不符合预期，需要提前终止流</li>
 *   <li>用户需要主动控制流的生命周期</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 */
public interface SSEStreamController {
    /**
     * 请求终止 SSE 流
     * 
     * <p>调用此方法后，流会在读取完当前行后停止，并触发 onInterrupt() 回调。</p>
     */
    void terminate();
    
    /**
     * 检查是否已请求终止
     * 
     * @return true 表示已请求终止，false 表示流仍在运行
     */
    boolean isTerminated();
    
    /**
     * 创建默认的流控制器实现
     * 
     * <p>使用 AtomicBoolean 保证线程安全。</p>
     * 
     * @return 流控制器实例
     */
    static SSEStreamController create() {
        return new DefaultSSEStreamController();
    }
    
    /**
     * 默认的流控制器实现
     * 使用 AtomicBoolean 保证线程安全
     */
    class DefaultSSEStreamController implements SSEStreamController {
        private final AtomicBoolean terminated = new AtomicBoolean(false);
        
        @Override
        public void terminate() {
            terminated.set(true);
        }
        
        @Override
        public boolean isTerminated() {
            return terminated.get();
        }
    }
}

