package com.gitee.huanminabc.nullchain.leaf.http.websocket;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket 控制器
 * 
 * <p>管理 WebSocket 连接和消息队列，提供发送消息、关闭连接等功能。
 * 支持消息队列管理，在网络异常时自动重发待发送的消息。
 * 支持心跳检测机制，定期发送心跳并检测超时。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketController {
    /** 消息队列 */
    private final ConcurrentLinkedQueue<WebSocketMessage> messageQueue = new ConcurrentLinkedQueue<>();
    
    /** 连接状态 */
    private final AtomicBoolean isOpen = new AtomicBoolean(false);
    
    /** 内部 WebSocket 实例（由策略类设置） */
    private volatile okhttp3.WebSocket webSocket;
    
    /** 心跳处理器 */
    private volatile WebSocketHeartbeatHandler heartbeatHandler;
    
    /** 心跳间隔（毫秒） */
    private volatile long heartbeatInterval = 30000;
    
    /** 心跳超时时间（毫秒） */
    private volatile long heartbeatTimeout = 10000;
    
    /** 心跳定时任务执行器 */
    private volatile ScheduledExecutorService heartbeatExecutor;
    
    /** 心跳发送任务 */
    private volatile ScheduledFuture<?> heartbeatSendTask;
    
    /** 心跳超时检测任务 */
    private volatile ScheduledFuture<?> heartbeatTimeoutTask;
    
    /** 最后一次收到心跳回复的时间 */
    private final AtomicLong lastHeartbeatResponseTime = new AtomicLong(0);
    
    /** 服务器返回的选中子协议 */
    private volatile String selectedSubprotocol;
    
    /**
     * 设置内部 WebSocket 实例
     * 
     * @param webSocket OkHttp WebSocket 实例
     */
    public void setWebSocket(okhttp3.WebSocket webSocket) {
        this.webSocket = webSocket;
    }
    
    /**
     * 设置连接状态
     * 
     * @param open 是否打开
     */
    public void setOpen(boolean open) {
        this.isOpen.set(open);
    }
    
    /**
     * 发送文本消息
     * 
     * <p>如果连接已建立，立即发送；否则将消息加入队列，等待连接建立后发送。</p>
     * 
     * @param text 文本消息内容
     * @return 如果连接已建立且发送成功返回 true，否则返回 false（消息已加入队列）
     */
    public boolean send(String text) {
        if (text == null) {
            return false;
        }
        
        WebSocketMessage message = WebSocketMessage.text(text);
        messageQueue.offer(message);
        
        return trySend();
    }
    
    /**
     * 发送二进制消息
     * 
     * <p>如果连接已建立，立即发送；否则将消息加入队列，等待连接建立后发送。</p>
     * 
     * @param bytes 二进制消息内容
     * @return 如果连接已建立且发送成功返回 true，否则返回 false（消息已加入队列）
     */
    public boolean send(byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        
        WebSocketMessage message = WebSocketMessage.binary(bytes);
        messageQueue.offer(message);
        
        return trySend();
    }
    
    /**
     * 尝试发送队列中的消息
     * 
     * @return 是否成功发送了消息
     */
    public boolean trySend() {
        if (!isOpen.get() || webSocket == null) {
            return false;
        }
        
        // 发送队列中的所有消息
        while (!messageQueue.isEmpty()) {
            WebSocketMessage message = messageQueue.peek();
            if (message == null) {
                break;
            }
            
            boolean sent = false;
            try {
                if (message.getType() == WebSocketMessage.MessageType.TEXT) {
                    sent = webSocket.send(message.getText());
                } else {
                    sent = webSocket.send(okio.ByteString.of(message.getBytes()));
                }
                
                if (sent) {
                    // 发送成功，从队列中移除
                    messageQueue.poll();
                } else {
                    // 发送失败，停止尝试
                    break;
                }
            } catch (Exception e) {
                // 发送异常，停止尝试
                break;
            }
        }
        
        return messageQueue.isEmpty();
    }
    
    /**
     * 启动心跳检测
     * 
     * @param handler 心跳处理器
     * @param interval 心跳间隔（毫秒）
     * @param timeout 超时时间（毫秒）
     */
    public void startHeartbeat(WebSocketHeartbeatHandler handler, long interval, long timeout) {
        if (handler == null) {
            return;
        }
        
        this.heartbeatHandler = handler;
        this.heartbeatInterval = interval > 0 ? interval : 30000;
        this.heartbeatTimeout = timeout > 0 ? timeout : 10000;
        
        // 停止旧的心跳任务
        stopHeartbeat();
        
        // 创建新的执行器
        heartbeatExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "WebSocket-Heartbeat-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        
        // 初始化最后回复时间为当前时间（连接刚建立时）
        lastHeartbeatResponseTime.set(System.currentTimeMillis());
        
        // 启动心跳发送任务
        heartbeatSendTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("发送心跳异常", e);
            }
        }, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
        
        // 启动超时检测任务（每秒检查一次）
        heartbeatTimeoutTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                checkHeartbeatTimeout();
            } catch (Exception e) {
                log.error("心跳超时检测异常", e);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
        
        log.debug("心跳检测已启动，间隔: {}ms, 超时: {}ms", heartbeatInterval, heartbeatTimeout);
    }
    
    /**
     * 停止心跳检测
     */
    public void stopHeartbeat() {
        if (heartbeatSendTask != null) {
            heartbeatSendTask.cancel(false);
            heartbeatSendTask = null;
        }
        if (heartbeatTimeoutTask != null) {
            heartbeatTimeoutTask.cancel(false);
            heartbeatTimeoutTask = null;
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
            try {
                if (!heartbeatExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            heartbeatExecutor = null;
        }
    }
    
    /**
     * 发送心跳消息
     */
    private void sendHeartbeat() {
        if (!isOpen.get() || webSocket == null || heartbeatHandler == null) {
            return;
        }
        
        try {
            // 优先使用文本格式
            String text = heartbeatHandler.generateHeartbeat();
            if (text != null) {
                webSocket.send(text);
                log.debug("发送心跳消息（文本）: {}", text);
            } else {
                // 使用二进制格式
                byte[] bytes = heartbeatHandler.generateHeartbeatBytes();
                if (bytes != null) {
                    webSocket.send(okio.ByteString.of(bytes));
                    log.debug("发送心跳消息（二进制），长度: {}", bytes.length);
                }
            }
        } catch (Exception e) {
            log.warn("发送心跳消息失败", e);
        }
    }
    
    /**
     * 检查心跳超时
     */
    private void checkHeartbeatTimeout() {
        if (!isOpen.get() || heartbeatHandler == null) {
            return;
        }
        
        long lastResponseTime = lastHeartbeatResponseTime.get();
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastResponseTime;
        
        if (elapsed > heartbeatTimeout) {
            log.warn("心跳超时，已超过 {}ms 未收到回复，主动断开连接", elapsed);
            // 主动断开连接
            close(1000, "心跳超时");
        }
    }
    
    /**
     * 更新心跳回复时间（由策略类调用）
     * 
     * @param handler 心跳处理器
     */
    public void updateHeartbeatResponseTime(WebSocketHeartbeatHandler handler) {
        if (handler != null && handler == this.heartbeatHandler) {
            lastHeartbeatResponseTime.set(System.currentTimeMillis());
            log.debug("收到心跳回复，更新时间: {}", lastHeartbeatResponseTime.get());
        }
    }
    
    /**
     * 关闭连接
     * 
     * @param code 关闭状态码
     * @param reason 关闭原因
     */
    public void close(int code, String reason) {
        isOpen.set(false);
        
        // 停止心跳检测
        stopHeartbeat();
        
        if (webSocket != null) {
            try {
                webSocket.close(code, reason);
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
        // 清空消息队列
        messageQueue.clear();
    }
    
    /**
     * 检查连接是否已打开
     * 
     * @return 如果连接已打开返回 true，否则返回 false
     */
    public boolean isOpen() {
        return isOpen.get();
    }
    
    /**
     * 获取待发送消息数量
     * 
     * @return 待发送消息数量
     */
    public int getPendingMessageCount() {
        return messageQueue.size();
    }
    
    /**
     * 获取消息队列（用于策略类访问）
     * 
     * @return 消息队列
     */
    ConcurrentLinkedQueue<WebSocketMessage> getMessageQueue() {
        return messageQueue;
    }
    
    /**
     * 设置服务器返回的选中子协议（由策略类调用）
     * 
     * @param subprotocol 服务器返回的子协议
     */
    public void setSelectedSubprotocol(String subprotocol) {
        this.selectedSubprotocol = subprotocol;
    }
    
    /**
     * 获取服务器返回的选中子协议
     * 
     * <p>在握手成功后，如果服务器返回了子协议，可以通过此方法获取。</p>
     * 
     * @return 服务器返回的子协议，如果未设置则返回 null
     */
    public String getSelectedSubprotocol() {
        return selectedSubprotocol;
    }
}

