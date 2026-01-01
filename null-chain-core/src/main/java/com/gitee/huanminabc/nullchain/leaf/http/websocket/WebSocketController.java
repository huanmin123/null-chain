package com.gitee.huanminabc.nullchain.leaf.http.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 控制器
 * 
 * <p>管理 WebSocket 连接和消息队列，提供发送消息、关闭连接等功能。
 * 支持消息队列管理，在网络异常时自动重发待发送的消息。
 * 支持心跳检测机制，定期发送心跳并检测超时。</p>
 * 
 * <p>实现了 {@link AutoCloseable} 接口，支持 try-with-resources 语法自动释放资源：</p>
 * <pre>{@code
 * try (WebSocketController controller = Null.ofHttp("ws://example.com/ws")
 *         .toWebSocket(listener)) {
 *     // 使用 controller
 *     controller.send("Hello");
 * } // 自动调用 close() 释放资源
 * }</pre>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketController implements AutoCloseable {
    /** 消息队列 */
    private final ConcurrentLinkedQueue<WebSocketMessage> messageQueue = new ConcurrentLinkedQueue<>();
    
    /** 消息队列大小计数器（避免 ConcurrentLinkedQueue.size() 的 O(n) 操作） */
    private final AtomicInteger queueSize = new AtomicInteger(0);
    
    /** 消息队列操作锁（确保检查-添加操作的原子性） */
    private final Object queueLock = new Object();
    
    /** 消息队列最大大小（默认1000，0表示无限制） */
    private volatile int maxQueueSize = 1000;

    /** 队列满时的处理策略 */
    public enum QueueFullPolicy {
        /** 拒绝新消息 */
        REJECT,
        /** 丢弃最旧的消息 */
        DROP_OLDEST
    }
    
    /** 队列满时的处理策略（默认丢弃最旧的消息）
     * -- GETTER --
     *  获取队列满时的处理策略
     * 处理策略
     */
    @Getter
    private volatile QueueFullPolicy queueFullPolicy = QueueFullPolicy.DROP_OLDEST;
    
    /** 连接状态（统一管理，基于此状态计算 isOpen） */
    private volatile WebSocketConnectionState connectionState = WebSocketConnectionState.INITIAL;
    
    /** 内部 WebSocket 实例（由策略类设置）
     * -- SETTER --
     *  设置内部 WebSocket 实例
     * webSocket OkHttp WebSocket 实例
     * -- GETTER --
     *  获取内部 WebSocket 实例
     *  <p>注意：此方法主要用于策略类在重连时关闭旧连接，一般用户不应直接使用。</p>
     *  OkHttp WebSocket 实例，如果未设置则返回 null
     */
    @Setter
    @Getter
    private volatile okhttp3.WebSocket webSocket;
    
    /** 心跳处理器 */
    private volatile WebSocketHeartbeatHandler heartbeatHandler;
    
    /** 心跳间隔（毫秒） */
    private volatile long heartbeatInterval = 30000;
    
    /** 心跳超时时间（毫秒） */
    private volatile long heartbeatTimeout = 10000;
    
    /** 共享的线程池（用于重连任务） */
    private static final ExecutorService SHARED_EXECUTOR = createSharedExecutor();
    
    /** 心跳定时任务执行器（复用，避免频繁创建销毁） */
    private static final ScheduledExecutorService SHARED_HEARTBEAT_EXECUTOR = createSharedHeartbeatExecutor();
    
    /**
     * 创建共享的重连线程池
     * 
     * <p>注意：使用 daemon 线程，JVM 关闭时会自动终止，不需要关闭钩子。
     * 如果需要显式关闭，可以调用 {@link ExecutorService#shutdown()}。</p>
     * 
     * @return 线程池
     */
    private static ExecutorService createSharedExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "WebSocket-Reconnect-" + threadNumber.getAndIncrement());
                t.setDaemon(true); // Daemon 线程，JVM 关闭时自动终止
                return t;
            }
        });
    }
    
    /**
     * 创建共享的心跳执行器
     * 
     * <p>注意：使用 daemon 线程，JVM 关闭时会自动终止，不需要关闭钩子。
     * 如果需要显式关闭，可以调用 {@link ScheduledExecutorService#shutdown()}。</p>
     * 
     * @return 定时任务执行器
     */
    private static ScheduledExecutorService createSharedHeartbeatExecutor() {
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "WebSocket-Heartbeat-" + threadNumber.getAndIncrement());
                t.setDaemon(true); // Daemon 线程，JVM 关闭时自动终止
                return t;
            }
        });
    }
    
    /** 心跳发送任务 */
    private volatile ScheduledFuture<?> heartbeatSendTask;
    
    /** 心跳超时检测任务 */
    private volatile ScheduledFuture<?> heartbeatTimeoutTask;
    
    /** 最后一次收到心跳回复的时间 */
    private final AtomicLong lastHeartbeatResponseTime = new AtomicLong(0);
    
    /** 服务器返回的选中子协议
     * -- SETTER --
     *  设置服务器返回的选中子协议（由策略类调用）
     * -- GETTER --
     *  获取服务器返回的选中子协议
     *  <p>在握手成功后，如果服务器返回了子协议，可以通过此方法获取。</p>
     *  subprotocol 服务器返回的子协议
     * 服务器返回的子协议，如果未设置则返回 null
     */
    @Getter
    @Setter
    private volatile String selectedSubprotocol;
    
    /** 重连触发器（由策略类设置）
     * -- SETTER --
     *  设置重连触发器
     *  <p>由策略类调用，用于在连接关闭后发送消息时触发重连。</p>
     *
     * trigger 重连触发器，当连接关闭后发送消息时会被调用
     */
    @Setter
    private volatile Runnable reconnectTrigger;
    
    /** 重连锁（确保重连操作的原子性） */
    private final Object reconnectLock = new Object();
    
    /** 重连计数器（统一管理，避免每次创建新的） */
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    
    /** 重连状态：是否正在重连中 */
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    
    /** 最大重连次数（由策略类设置） */
    @Setter
    private volatile int maxReconnectCount = 0;
    
    /** 重连间隔（毫秒，由策略类设置） */
    @Setter
    private volatile long reconnectInterval = 1000;
    
    /** 心跳超时导致的重连次数（防止死循环） */
    private final AtomicInteger heartbeatTimeoutReconnectCount = new AtomicInteger(0);
    
    /** 心跳超时导致的最大重连次数（默认3次，防止死循环） */
    private static final int MAX_HEARTBEAT_TIMEOUT_RECONNECT = 3;
    
    /** 控制器是否已销毁（防止销毁后继续使用） */
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * 检查是否已设置重连触发器
     * 
     * @return 如果已设置返回 true，否则返回 false
     */
    public boolean hasReconnectTrigger() {
        return reconnectTrigger != null;
    }
    
    /**
     * 获取当前重连次数
     * 
     * @return 当前重连次数
     */
    public int getReconnectAttempt() {
        return reconnectAttempt.get();
    }
    
    /**
     * 重置重连计数器
     */
    public void resetReconnectAttempt() {
        reconnectAttempt.set(0);
        isReconnecting.set(false);
        // 注意：不重置心跳超时重连次数，因为这是为了防止死循环的累计计数
    }
    
    /**
     * 重置心跳超时重连计数（在连接成功建立且收到心跳回复后调用）
     */
    public void resetHeartbeatTimeoutReconnectCount() {
        heartbeatTimeoutReconnectCount.set(0);
    }
    
    /**
     * 检查是否正在重连
     * 
     * @return 如果正在重连返回 true，否则返回 false
     */
    public boolean isReconnecting() {
        return isReconnecting.get();
    }
    
    /**
     * 设置重连状态
     * 
     * @param reconnecting 是否正在重连
     * @return 如果状态设置成功返回 true，如果已经在目标状态返回 false
     */
    public boolean setReconnecting(boolean reconnecting) {
        if (reconnecting) {
            // 尝试设置为重连中，如果已经是重连中则返回 false（防止重复重连）
            return isReconnecting.compareAndSet(false, true);
        } else {
            // 取消重连状态
            isReconnecting.set(false);
            return true;
        }
    }
    
    /**
     * 触发重连（统一入口，确保原子性）
     * 
     * <p>此方法确保重连操作的原子性，防止重复触发。</p>
     * 
     * @param reason 触发重连的原因
     * @return 如果成功触发重连返回 true，否则返回 false
     */
    public boolean triggerReconnect(String reason) {
        // 检查控制器是否已销毁
        if (isDestroyed()) {
            log.debug("控制器已销毁，无法触发重连: {}", reason);
            return false;
        }
        
        // 使用同步块确保原子性
        synchronized (reconnectLock) {
            // 再次检查控制器是否已销毁（双重检查）
            if (isDestroyed()) {
                log.debug("控制器已销毁，无法触发重连: {}", reason);
                return false;
            }
            
            // 检查是否正在重连
            if (isReconnecting.get()) {
                log.debug("重连已在进行中，跳过触发: {}", reason);
                return false;
            }
            
            // 检查是否允许重连
            if (maxReconnectCount <= 0) {
                log.debug("未配置重连，跳过触发: {}", reason);
                return false;
            }
            
            // 检查重连触发器是否设置
            Runnable trigger = reconnectTrigger;
            if (trigger == null) {
                log.debug("重连触发器未设置，跳过触发: {}", reason);
                return false;
            }
            
            // 尝试设置重连状态
            if (!setReconnecting(true)) {
                log.debug("无法设置重连状态，可能正在重连: {}", reason);
                return false;
            }
            
            // 触发重连
            log.info("触发重连: {}, 待发送消息数: {}", reason, getPendingMessageCount());
            trigger.run();
            return true;
        }
    }
    
    /**
     * 增加重连次数
     * 
     * @return 增加后的重连次数
     */
    public int incrementReconnectAttempt() {
        return reconnectAttempt.incrementAndGet();
    }
    
    /**
     * 检查是否超过最大重连次数
     * 
     * @return 如果超过最大重连次数返回 true，否则返回 false
     */
    public boolean isReconnectExhausted() {
        return maxReconnectCount > 0 && reconnectAttempt.get() > maxReconnectCount;
    }

    /**
     * 设置连接状态（统一管理）
     * 
     * @param newState 新状态
     */
    public void setConnectionState(WebSocketConnectionState newState) {
        WebSocketConnectionState oldState = this.connectionState;
        if (oldState != newState) {
            this.connectionState = newState;
            log.debug("连接状态变化: {} -> {}", oldState, newState);
        }
    }
    
    /**
     * 设置连接状态（兼容旧 API）
     * 
     * @param open 是否打开
     */
    public void setOpen(boolean open) {
        if (open) {
            setConnectionState(WebSocketConnectionState.CONNECTED);
        } else {
            // 只有在非关闭状态时才设置为关闭
            WebSocketConnectionState current = this.connectionState;
            if (current != WebSocketConnectionState.CLOSING && 
                current != WebSocketConnectionState.CLOSED &&
                current != WebSocketConnectionState.FAILED) {
                setConnectionState(WebSocketConnectionState.CLOSED);
            }
        }
    }
    
    /**
     * 获取连接状态
     * 
     * @return 连接状态
     */
    public WebSocketConnectionState getConnectionState() {
        return connectionState;
    }
    
    /**
     * 发送文本消息
     * 
     * <p>如果连接已建立，立即发送；否则将消息加入队列，等待连接建立后发送。</p>
     * 
     * <p>注意：如果连接已关闭，消息会被加入队列，但不会立即发送。
     * 如果配置了重连，会在重连成功后自动发送；否则消息可能丢失。</p>
     * 
     * @param text 文本消息内容
     * @return 如果连接已建立且发送成功返回 true，否则返回 false（消息已加入队列）
     */
    public boolean send(String text) {
        // 检查控制器是否已销毁
        if (isDestroyed()) {
            log.warn("控制器已销毁，无法发送消息");
            return false;
        }
        
        if (text == null) {
            return false;
        }
        
        WebSocketMessage message = WebSocketMessage.text(text);
        
        // 检查队列大小限制
        if (!addToQueue(message)) {
            log.warn("消息队列已满，无法添加新消息");
            return false;
        }
        
        boolean sent = trySend();
        
        // 如果连接已关闭且消息队列不为空，尝试触发重连
        if (!sent && !isOpen() && getPendingMessageCount() > 0 && reconnectTrigger != null) {
            log.debug("连接已关闭，消息已加入队列，待发送消息数: {}", getPendingMessageCount());
            triggerReconnect("连接已关闭，但有待发送消息");
        } else if (!sent && !isOpen()) {
            log.debug("连接已关闭，消息已加入队列，待发送消息数: {}，但未配置重连机制", getPendingMessageCount());
        }
        
        return sent;
    }
    
    /**
     * 发送二进制消息
     * 
     * <p>如果连接已建立，立即发送；否则将消息加入队列，等待连接建立后发送。</p>
     * 
     * <p>注意：如果连接已关闭，消息会被加入队列，但不会立即发送。
     * 如果配置了重连，会在重连成功后自动发送；否则消息可能丢失。</p>
     * 
     * @param bytes 二进制消息内容
     * @return 如果连接已建立且发送成功返回 true，否则返回 false（消息已加入队列）
     */
    public boolean send(byte[] bytes) {
        // 检查控制器是否已销毁
        if (isDestroyed()) {
            log.warn("控制器已销毁，无法发送消息");
            return false;
        }
        
        if (bytes == null) {
            return false;
        }
        
        WebSocketMessage message = WebSocketMessage.binary(bytes);
        
        // 检查队列大小限制
        if (!addToQueue(message)) {
            log.warn("消息队列已满，无法添加新消息");
            return false;
        }
        
        boolean sent = trySend();
        
        // 如果连接已关闭且消息队列不为空，尝试触发重连
        if (!sent && !isOpen() && getPendingMessageCount() > 0 && reconnectTrigger != null) {
            log.debug("连接已关闭，消息已加入队列，待发送消息数: {}", getPendingMessageCount());
            triggerReconnect("连接已关闭，但有待发送消息");
        } else if (!sent && !isOpen()) {
            log.debug("连接已关闭，消息已加入队列，待发送消息数: {}，但未配置重连机制", getPendingMessageCount());
        }
        
        return sent;
    }
    
    /**
     * 将消息添加到队列（处理队列大小限制）
     * 
     * <p>使用同步块确保检查-添加操作的原子性，防止竞态条件。</p>
     * 
     * @param message 消息
     * @return 如果成功添加返回 true，否则返回 false
     */
    private boolean addToQueue(WebSocketMessage message) {
        if (message == null) {
            log.warn("尝试添加 null 消息到队列，忽略");
            return false;
        }
        
        // 使用同步块确保检查-添加操作的原子性
        synchronized (queueLock) {
            // 使用原子计数器检查队列大小，避免 O(n) 的 size() 操作
            int currentSize = queueSize.get();
            
            if (maxQueueSize > 0 && currentSize >= maxQueueSize) {
                // 队列已满，根据策略处理
                if (queueFullPolicy == QueueFullPolicy.DROP_OLDEST) {
                    // 丢弃最旧的消息（原子操作）
                    WebSocketMessage dropped = messageQueue.poll();
                    if (dropped != null) {
                        queueSize.decrementAndGet();
                        log.warn("队列已满（大小: {}），丢弃最旧的消息（类型: {}），添加新消息（类型: {}）", 
                                currentSize, dropped.getType(), message.getType());
                    }
                    // 尝试添加新消息
                    if (messageQueue.offer(message)) {
                        queueSize.incrementAndGet();
                        return true;
                    }
                    log.error("队列已满，丢弃旧消息后仍无法添加新消息，可能发生异常");
                    return false;
                } else {
                    // 拒绝新消息
                    log.warn("队列已满（大小: {}），拒绝新消息（类型: {}），策略: REJECT", 
                            currentSize, message.getType());
                    return false;
                }
            } else {
                // 尝试添加消息
                if (messageQueue.offer(message)) {
                    queueSize.incrementAndGet();
                    log.debug("消息已添加到队列，队列大小: {}, 消息类型: {}", queueSize.get(), message.getType());
                    return true;
                }
                log.error("无法添加消息到队列，可能发生异常");
                return false;
            }
        }
    }
    
    /**
     * 尝试发送队列中的消息
     * 
     * <p>注意：此方法可能被多个线程并发调用，需要保证线程安全。</p>
     * 
     * @return 是否成功发送了消息
     */
    public boolean trySend() {
        // 检查控制器是否已销毁
        if (isDestroyed()) {
            return false;
        }
        
        // 发送队列中的所有消息
        while (!messageQueue.isEmpty()) {
            // 再次检查控制器是否已销毁
            if (isDestroyed()) {
                return false;
            }
            // 每次循环都重新获取 webSocket 和检查连接状态，确保线程安全
            okhttp3.WebSocket ws = this.webSocket;
            if (!isOpen() || ws == null) {
                // 连接已关闭或未建立，停止发送
                return false;
            }
            
            WebSocketMessage message = messageQueue.peek();
            if (message == null) {
                break;
            }
            
            boolean sent = false;
            try {
                // 在发送前再次检查连接状态和 webSocket 引用
                if (!isOpen() || this.webSocket != ws) {
                    // 连接状态已改变，停止发送
                    return false;
                }
                
                if (message.getType() == WebSocketMessage.MessageType.TEXT) {
                    sent = ws.send(message.getText());
                } else {
                    sent = ws.send(okio.ByteString.of(message.getBytes()));
                }
                
                // 发送后再次检查连接状态，确保在发送过程中连接未关闭
                if (!isOpen() || this.webSocket != ws) {
                    // 连接状态已改变，停止发送
                    return false;
                }
                
                if (sent) {
                    // 发送成功，从队列中移除
                    WebSocketMessage removed = messageQueue.poll();
                    if (removed != null) {
                        queueSize.decrementAndGet();
                    }
                    log.debug("消息发送成功，队列剩余: {}", queueSize.get());
                } else {
                    // 发送失败，增加重试次数
                    int retryCount = message.incrementRetryCount();
                    if (message.isRetryExhausted()) {
                        // 超过最大重试次数，丢弃消息
                        WebSocketMessage removed = messageQueue.poll();
                        if (removed != null) {
                            queueSize.decrementAndGet();
                        }
                        log.warn("消息发送失败，已超过最大重试次数（{}），丢弃消息。消息类型: {}, 队列剩余: {}", 
                                retryCount, message.getType(), queueSize.get());
                    } else {
                        // 未超过最大重试次数，稍后重试
                        log.debug("消息发送失败，重试次数: {}/{}, 稍后重试。消息类型: {}", 
                                retryCount, WebSocketMessage.getMaxRetryCount(), message.getType());
                    }
                    // 停止尝试，等待下次重试
                    break;
                }
            } catch (Exception e) {
                // 发送异常，可能是连接已关闭
                // 再次检查连接状态
                if (!isOpen() || this.webSocket != ws) {
                    // 连接已关闭，停止发送
                    log.debug("连接已关闭，停止发送消息");
                    return false;
                }
                
                // 发送异常，增加重试次数
                int retryCount = message.incrementRetryCount();
                if (message.isRetryExhausted()) {
                    // 超过最大重试次数，丢弃消息
                    WebSocketMessage removed = messageQueue.poll();
                    if (removed != null) {
                        queueSize.decrementAndGet();
                    }
                    log.warn("消息发送异常，已超过最大重试次数（{}），丢弃消息。消息类型: {}, 异常: {}, 队列剩余: {}", 
                            retryCount, message.getType(), e.getClass().getSimpleName(), queueSize.get(), e);
                } else {
                    // 未超过最大重试次数，稍后重试
                    log.debug("消息发送异常，重试次数: {}/{}, 稍后重试。消息类型: {}, 异常: {}", 
                            retryCount, WebSocketMessage.getMaxRetryCount(), message.getType(), 
                            e.getClass().getSimpleName(), e);
                }
                // 停止尝试，等待下次重试
                break;
            }
        }
        
        return messageQueue.isEmpty();
    }
    
    /**
     * 设置消息队列最大大小
     * 
     * @param maxSize 最大大小，0表示无限制
     */
    public void setMaxQueueSize(int maxSize) {
        this.maxQueueSize = maxSize >= 0 ? maxSize : 0;
    }
    
    /**
     * 获取消息队列最大大小
     * 
     * @return 最大大小，0表示无限制
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    /**
     * 设置队列满时的处理策略
     * 
     * @param policy 处理策略
     */
    public void setQueueFullPolicy(QueueFullPolicy policy) {
        this.queueFullPolicy = policy != null ? policy : QueueFullPolicy.DROP_OLDEST;
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
        
        // 验证心跳参数合理性：超时时间应该大于间隔时间，否则可能导致误判
        if (this.heartbeatTimeout <= this.heartbeatInterval) {
            log.warn("心跳超时时间（{}ms）应该大于心跳间隔（{}ms），调整为间隔的2倍", 
                    this.heartbeatTimeout, this.heartbeatInterval);
            this.heartbeatTimeout = this.heartbeatInterval * 2;
        }
        
        // 停止旧的心跳任务
        stopHeartbeat();
        
        // 重置心跳超时处理标志
        heartbeatTimeoutHandled = false;
        
        // 使用共享的心跳执行器（复用，避免频繁创建销毁）
        
        // 初始化最后回复时间为当前时间（连接刚建立时）
        lastHeartbeatResponseTime.set(System.currentTimeMillis());
        
        // 启动心跳发送任务
        heartbeatSendTask = SHARED_HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("发送心跳异常", e);
            }
        }, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
        
        // 启动超时检测任务（根据心跳间隔和超时时间动态调整检测频率）
        // 检测频率 = min(心跳间隔/2, 心跳超时/4, 1000ms)，但至少每秒一次
        long checkInterval = Math.min(Math.min(heartbeatInterval / 2, heartbeatTimeout / 4), 1000);
        checkInterval = Math.max(checkInterval, 1000); // 至少每秒检查一次
        
        heartbeatTimeoutTask = SHARED_HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                checkHeartbeatTimeout();
            } catch (Exception e) {
                log.error("心跳超时检测异常", e);
            }
        }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        
        log.debug("心跳超时检测频率: {}ms", checkInterval);
        
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
        // 重置心跳超时处理标志
        heartbeatTimeoutHandled = false;
        // 注意：不再关闭共享执行器，因为它是静态共享的
    }
    
    /**
     * 获取共享的线程池（用于重连任务）
     * 
     * @return 共享的线程池
     */
    public static ExecutorService getSharedExecutor() {
        return SHARED_EXECUTOR;
    }
    
    /**
     * 获取共享的心跳执行器（用于定时任务）
     * 
     * @return 共享的心跳执行器
     */
    public static ScheduledExecutorService getSharedHeartbeatExecutor() {
        return SHARED_HEARTBEAT_EXECUTOR;
    }
    
    /**
     * 发送心跳消息
     */
    private void sendHeartbeat() {
        if (!isOpen() || webSocket == null || heartbeatHandler == null) {
            return;
        }
        
        try {
            // 优先使用文本格式
            String text = heartbeatHandler.generateHeartbeat();
            if (text != null) {
                boolean sent = webSocket.send(text);
                if (sent) {
                    log.debug("发送心跳消息（文本）: {}", text);
                } else {
                    log.warn("心跳消息发送失败（文本），连接可能已关闭");
                }
            } else {
                // 使用二进制格式
                byte[] bytes = heartbeatHandler.generateHeartbeatBytes();
                if (bytes != null) {
                    boolean sent = webSocket.send(okio.ByteString.of(bytes));
                    if (sent) {
                        log.debug("发送心跳消息（二进制），长度: {}", bytes.length);
                    } else {
                        log.warn("心跳消息发送失败（二进制），连接可能已关闭");
                    }
                } else {
                    log.warn("心跳处理器未生成任何心跳消息（文本和二进制都返回null），可能导致心跳超时");
                }
            }
        } catch (Exception e) {
            log.warn("发送心跳消息异常", e);
        }
    }
    
    /** 心跳超时检测锁（防止并发检测） */
    private final Object heartbeatTimeoutLock = new Object();
    
    /** 心跳超时检测标志（防止重复处理） */
    private volatile boolean heartbeatTimeoutHandled = false;
    
    /**
     * 检查心跳超时
     * 
     * <p>注意：此方法可能被多个线程并发调用，使用同步块确保线程安全。</p>
     */
    private void checkHeartbeatTimeout() {
        // 快速检查，避免不必要的同步
        if (!isOpen() || heartbeatHandler == null || heartbeatTimeoutHandled) {
            return;
        }
        
        long lastResponseTime = lastHeartbeatResponseTime.get();
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastResponseTime;
        
        if (elapsed > heartbeatTimeout) {
            // 使用同步块确保只有一个线程处理超时
            synchronized (heartbeatTimeoutLock) {
                // 双重检查，防止重复处理
                if (heartbeatTimeoutHandled || !isOpen()) {
                    return;
                }
                
                // 标记为已处理
                heartbeatTimeoutHandled = true;
                
                // 再次检查连接状态，防止在检查过程中连接已关闭
                WebSocketConnectionState currentState = this.connectionState;
                if (currentState != WebSocketConnectionState.CONNECTED) {
                    // 连接状态已经不是 CONNECTED（可能已经在关闭中），重置标志并返回
                    heartbeatTimeoutHandled = false;
                    return;
                }
                
                // 原子性地更新状态为 CLOSING（表示正在关闭）
                setConnectionState(WebSocketConnectionState.CLOSING);
            
            log.warn("心跳超时，已超过 {}ms 未收到回复", elapsed);
            
            // 检查心跳超时导致的重连次数，防止死循环
            int heartbeatReconnectCount = heartbeatTimeoutReconnectCount.incrementAndGet();
            if (heartbeatReconnectCount > MAX_HEARTBEAT_TIMEOUT_RECONNECT) {
                log.error("心跳超时导致的重连次数已达到上限（{}次），停止重连，避免死循环。"
                        + "可能是服务器不支持心跳或心跳配置不正确。", MAX_HEARTBEAT_TIMEOUT_RECONNECT);
                // 直接关闭连接，不再重连
                setConnectionState(WebSocketConnectionState.CLOSED);
                okhttp3.WebSocket ws = this.webSocket;
                if (ws != null) {
                    // 再次检查连接状态，防止重复关闭
                    if (this.connectionState == WebSocketConnectionState.CLOSED) {
                        try {
                            ws.close(1000, "心跳超时次数过多，停止重连");
                        } catch (Exception e) {
                            log.debug("关闭连接时发生异常", e);
                        }
                    }
                }
                    stopHeartbeat();
                    heartbeatTimeoutHandled = false; // 重置标志
                    return;
                }
                
                // 心跳超时后，先关闭当前连接，然后触发重连（如果有重连触发器）
                // 注意：状态已经在上面设置为 CLOSING
                okhttp3.WebSocket ws = this.webSocket;
                if (ws != null) {
                    // 再次检查连接状态，防止在关闭过程中状态已改变
                    if (this.connectionState == WebSocketConnectionState.CLOSING) {
                        try {
                            ws.close(1000, "心跳超时");
                        } catch (Exception e) {
                            log.debug("关闭连接时发生异常", e);
                        }
                    } else {
                        log.debug("连接状态已改变，跳过关闭操作: {}", this.connectionState);
                    }
                }
                
                // 停止心跳检测
                stopHeartbeat();
                
                // 触发重连（使用统一入口）
                if (triggerReconnect("心跳超时（心跳超时重连次数: " + heartbeatReconnectCount + "/" + MAX_HEARTBEAT_TIMEOUT_RECONNECT + "）")) {
                    log.info("心跳超时，已触发重连（心跳超时重连次数: {}/{}）", 
                            heartbeatReconnectCount, MAX_HEARTBEAT_TIMEOUT_RECONNECT);
                } else {
                    log.warn("心跳超时，但无法触发重连（可能正在重连或未配置重连机制）");
                }
                
                // 重置标志，允许下次检测
                heartbeatTimeoutHandled = false;
            }
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
            
            // 收到心跳回复后，重置心跳超时重连计数（说明心跳机制正常工作）
            // 这样可以避免因为临时网络问题导致的心跳超时累积
            if (heartbeatTimeoutReconnectCount.get() > 0) {
                log.debug("收到心跳回复，重置心跳超时重连计数");
                resetHeartbeatTimeoutReconnectCount();
            }
        }
    }
    
    /**
     * 关闭连接并释放所有资源（无参数版本，使用默认值）
     * 
     * <p>此方法会调用 {@link #close(int, String)} 方法，使用默认的关闭状态码 1000 和原因 "正常关闭"。</p>
     * 
     * <p>实现了 {@link AutoCloseable} 接口，支持 try-with-resources 语法自动释放资源。</p>
     * 
     * @see #close(int, String)
     */
    @Override
    public void close() {
        close(1000, "正常关闭");
    }
    
    /**
     * 关闭连接并释放所有资源
     * 
     * <p>用户手动调用此方法关闭连接后，会完全阻止后续的所有操作（包括自动重连）。
     * 这是设计上的特性：用户主动关闭表示不再需要连接，不应该自动重连。</p>
     * 
     * <p>此方法会：
     * <ul>
     *   <li>清空重连触发器，阻止自动重连</li>
     *   <li>停止所有正在进行的重连任务</li>
     *   <li>停止心跳检测</li>
     *   <li>清空消息队列</li>
     *   <li>关闭 WebSocket 连接</li>
     *   <li>重置所有状态</li>
     *   <li>清空所有引用</li>
     * </ul>
     * </p>
     * 
     * <p>调用此方法后，控制器将不再可用。应该确保不再使用此控制器。</p>
     * 
     * <p>注意：此方法使用 CAS 操作确保只执行一次，多次调用是安全的。</p>
     * 
     * @param code 关闭状态码
     * @param reason 关闭原因
     */
    public void close(int code, String reason) {
        // 使用 CAS 确保只执行一次
        if (!destroyed.compareAndSet(false, true)) {
            log.debug("控制器已经关闭，跳过重复关闭");
            return;
        }
        
        log.info("关闭 WebSocket 连接并释放所有资源: code={}, reason={}", code, reason);
        
        // 阻止重连触发器继续工作（必须在关闭连接前设置）
        reconnectTrigger = null;
        
        // 停止所有正在进行的重连（设置重连状态为 false，防止新的重连）
        setReconnecting(false);
        
        // 停止心跳检测（必须在关闭连接前停止）
        stopHeartbeat();
        
        // 关闭连接（无论当前状态如何，都尝试关闭）
        okhttp3.WebSocket ws = this.webSocket;
        if (ws != null) {
            try {
                ws.close(code, reason);
            } catch (Exception e) {
                log.debug("关闭连接时发生异常", e);
            }
        }
        
        // 更新连接状态为已关闭
        setConnectionState(WebSocketConnectionState.CLOSED);
        
        // 清空消息队列
        int clearedCount = queueSize.getAndSet(0);
        messageQueue.clear();
        if (clearedCount > 0) {
            log.info("连接关闭，清空消息队列，丢弃 {} 条消息", clearedCount);
        }
        
        // 重置状态
        resetReconnectAttempt();
        resetHeartbeatTimeoutReconnectCount();
        
        // 清空引用
        webSocket = null;
        heartbeatHandler = null;
        selectedSubprotocol = null;
        
        log.info("WebSocket 连接已关闭，所有资源已释放");
    }
    
    /**
     * 检查控制器是否已关闭
     * 
     * @return 如果已关闭返回 true，否则返回 false
     */
    public boolean isDestroyed() {
        return destroyed.get();
    }
    
    /**
     * 检查连接是否已打开
     * 
     * <p>基于 connectionState 计算，确保状态一致性。</p>
     * 
     * @return 如果连接已打开返回 true，否则返回 false
     */
    public boolean isOpen() {
        WebSocketConnectionState state = this.connectionState;
        return state == WebSocketConnectionState.CONNECTED;
    }
    
    /**
     * 获取待发送消息数量
     * 
     * @return 待发送消息数量
     */
    public int getPendingMessageCount() {
        // 使用原子计数器，避免 O(n) 的 size() 操作
        return queueSize.get();
    }

    /**
     * 清空消息队列
     * 
     * <p>用于在连接关闭时，如果不需要重连，清空未发送的消息。</p>
     * 
     * @return 清空的消息数量
     */
    public int clearMessageQueue() {
        int clearedCount = queueSize.getAndSet(0);
        messageQueue.clear();
        if (clearedCount > 0) {
            log.debug("清空消息队列，丢弃 {} 条消息", clearedCount);
        }
        return clearedCount;
    }

}

