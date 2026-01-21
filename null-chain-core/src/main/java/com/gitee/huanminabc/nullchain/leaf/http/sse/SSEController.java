package com.gitee.huanminabc.nullchain.leaf.http.sse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SSE 控制器
 * 
 * <p>管理 SSE 连接状态和 Last-Event-ID，提供连接控制和状态查询功能。
 * 支持自动重连机制，在连接断开后自动恢复连接。</p>
 * 
 * <pre>{@code
 * try (SSEController controller = Null.ofHttp("https://api.example.com/sse")
 *         .get()
 *         .toSSEText(listener)) {
 *     // 使用 controller
 *     // 连接会自动管理
 * } // 自动调用 close() 释放资源
 * }</pre>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEController {
    
    /** 连接状态（统一管理） */
    @Getter
    private volatile SSEConnectionState connectionState = SSEConnectionState.INITIAL;
    
    /** 最后收到的 Event ID（用于 Last-Event-ID 头） */
    private final AtomicReference<String> lastEventId = new AtomicReference<>();
    
    /** 重连触发器（由策略类设置） */
    @Setter
    private volatile Runnable reconnectTrigger;
    
    /** 重连锁（确保重连操作的原子性） */
    private final Object reconnectLock = new Object();
    
    /** 当前重连次数 */
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    
    /** 重连状态：是否正在重连中 */
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    
    /** 最大重连次数（由策略类设置） */
    @Setter
    @Getter
    private volatile int maxReconnectCount = 0;
    
    /** 重连间隔（毫秒，由策略类设置） */
    @Getter
    @Setter
    private volatile long reconnectInterval = 1000;
    
    /** 控制器是否已销毁（防止销毁后继续使用） */
    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    
    /** 连接结束的同步锁（用于等待连接结束） */
    private final CountDownLatch completionLatch = new CountDownLatch(1);
    
    /** 是否已经触发完成信号（确保只触发一次） */
    private final AtomicBoolean completionSignaled = new AtomicBoolean(false);
    
    /** 共享的线程池（用于异步执行重连任务） */
    private static final ExecutorService SHARED_EXECUTOR = createSharedExecutor();
    
    /** 共享的定时任务执行器（用于延迟执行重连任务） */
    private static final ScheduledExecutorService SHARED_SCHEDULED_EXECUTOR = createSharedScheduledExecutor();
    
    /** 当前重连任务的Future（用于取消） */
    private final AtomicReference<ScheduledFuture<?>> reconnectFuture = new AtomicReference<>();

    /** SSE 流控制器（用于中断正在进行的 SSE 流读取）
     * <p>
     * 此字段由 {@link com.gitee.huanminabc.nullchain.leaf.http.strategy.response.SSEResponseStrategy}
     * 在开始处理 SSE 流时设置，用于支持用户通过 {@link #close()} 方法主动中断正在进行的流读取。
     * </p>
     * <p>
     * <b>生命周期：</b>
     * <ul>
     *   <li>创建：在 SSE 流处理开始时由策略类设置</li>
     *   <li>使用：用户调用 close() 时会调用 terminate() 终止流读取</li>
     *   <li>销毁：流处理结束后，随控制器一起被回收</li>
     * </ul>
     * </p>
     * -- SETTER --
     *  设置 SSE 流控制器（由 SSEResponseStrategy 内部调用）
     * -- GETTER --
     *  获取 SSE 流控制器（用于测试或调试）
     */
    @Getter
    @Setter
    private volatile SSEStreamController streamController;

    /**
     * 创建共享的线程池
     * 
     * <p>注意：使用 daemon 线程，JVM 关闭时会自动终止，不需要关闭钩子。</p>
     * 
     * @return 线程池
     */
    private static ExecutorService createSharedExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SSE-Reconnect-" + threadNumber.getAndIncrement());
                t.setDaemon(true); // Daemon 线程，JVM 关闭时自动终止
                return t;
            }
        });
    }
    
    /**
     * 创建共享的定时任务执行器
     * 
     * <p>注意：使用 daemon 线程，JVM 关闭时会自动终止，不需要关闭钩子。</p>
     * 
     * @return 定时任务执行器
     */
    private static ScheduledExecutorService createSharedScheduledExecutor() {
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SSE-Scheduled-" + threadNumber.getAndIncrement());
                t.setDaemon(true); // Daemon 线程，JVM 关闭时自动终止
                return t;
            }
        });
    }
    
    /**
     * 获取共享的线程池（用于外部访问）
     * 
     * @return 共享的线程池
     */
    public static ExecutorService getSharedExecutor() {
        return SHARED_EXECUTOR;
    }
    
    /**
     * 获取共享的定时任务执行器（用于外部访问）
     * 
     * @return 共享的定时任务执行器
     */
    public static ScheduledExecutorService getSharedScheduledExecutor() {
        return SHARED_SCHEDULED_EXECUTOR;
    }
    
    /**
     * 获取最后收到的 Event ID
     * 
     * @return 最后收到的 Event ID，如果未收到则返回 null
     */
    public String getLastEventId() {
        return lastEventId.get();
    }
    
    /**
     * 更新最后收到的 Event ID
     * 
     * @param eventId 事件 ID
     */
    public void updateLastEventId(String eventId) {
        if (eventId != null && !eventId.trim().isEmpty()) {
            lastEventId.set(eventId);
            log.debug("[SSE] 更新Last-Event-ID: {}", eventId);
        }
    }
    
    /**
     * 设置连接状态（统一管理）
     * 
     * @param newState 新状态
     */
    public void setConnectionState(SSEConnectionState newState) {
        SSEConnectionState oldState = this.connectionState;
        if (oldState != newState) {
            this.connectionState = newState;
            log.debug("[SSE] 连接状态变化: {} -> {}", oldState, newState);
            
            // 如果状态变为CLOSED或FAILED，触发完成信号（只触发一次）
            if ((newState == SSEConnectionState.CLOSED || newState == SSEConnectionState.FAILED) 
                    && completionSignaled.compareAndSet(false, true)) {
                completionLatch.countDown();
                log.debug("[SSE] 连接已结束，触发完成信号，状态: {}", newState);
            }
        }
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
        boolean current = isReconnecting.get();
        if (current == reconnecting) {
            return false;
        }
        isReconnecting.set(reconnecting);
        return true;
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
            log.debug("[SSE重连] 控制器已销毁，无法触发重连，原因: {}", reason);
            return false;
        }
        
        // 使用同步块确保原子性
        synchronized (reconnectLock) {
            // 再次检查控制器是否已销毁（双重检查）
            if (isDestroyed()) {
                log.debug("[SSE重连] 控制器已销毁，无法触发重连，原因: {}", reason);
                return false;
            }
            
            // 检查是否正在重连
            if (isReconnecting.get()) {
                log.debug("[SSE重连] 重连已在进行中，跳过触发，原因: {}", reason);
                return false;
            }
            
            // 检查是否允许重连
            if (maxReconnectCount <= 0) {
                log.debug("[SSE重连] 未配置重连，跳过触发，原因: {}", reason);
                return false;
            }
            
            // 检查重连触发器是否设置
            Runnable trigger = reconnectTrigger;
            if (trigger == null) {
                log.debug("[SSE重连] 重连触发器未设置，跳过触发，原因: {}", reason);
                return false;
            }
            
            // 尝试设置重连状态
            if (!setReconnecting(true)) {
                log.debug("[SSE重连] 无法设置重连状态，可能正在重连，原因: {}", reason);
                return false;
            }
            
            // 取消之前的重连任务（如果存在）
            ScheduledFuture<?> oldFuture = reconnectFuture.getAndSet(null);
            if (oldFuture != null && !oldFuture.isDone()) {
                oldFuture.cancel(false);
            }
            
            // 使用 ScheduledExecutorService 延迟执行重连，避免阻塞线程
            // 使用指数退避策略计算延迟
            // 注意：重连次数会在重连触发器中增加，这里使用当前值+1来计算延迟
            int currentAttempt = reconnectAttempt.get() + 1;
            long delay = SSEReconnectManager.calculateDelay(currentAttempt, reconnectInterval);
            log.debug("[SSE重连] 计算重连延迟: {}ms，第{}次重连", delay, currentAttempt);
            
            ScheduledFuture<?> future = SHARED_SCHEDULED_EXECUTOR.schedule(() -> {
                // 检查控制器是否已销毁（可能在延迟期间被关闭）
                if (isDestroyed()) {
                    log.debug("[SSE重连] 控制器已销毁，取消重连任务");
                    setReconnecting(false);
                    return;
                }
                
                // 再次检查是否正在重连（可能在延迟期间状态已改变）
                if (!isReconnecting.get()) {
                    log.debug("[SSE重连] 重连状态已改变，取消重连任务");
                    return;
                }
                
                // 在共享线程池中执行实际的重连逻辑
                SHARED_EXECUTOR.execute(() -> {
                    try {
                        // 触发重连
                        log.info("[SSE重连] 开始执行重连，原因: {}", reason);
                        trigger.run();
                    } catch (Exception e) {
                        log.error("[SSE重连] 执行重连时发生异常", e);
                        setReconnecting(false);
                    }
                });
            }, delay, TimeUnit.MILLISECONDS);
            
            // 保存future以便后续取消
            reconnectFuture.set(future);
            
            log.info("[SSE重连] 触发重连，原因: {}，延迟: {}ms", reason, delay);
            return true;
        }
    }
    
    /**
     * 检查连接是否已打开
     * 
     * <p>基于 connectionState 计算，确保状态一致性。</p>
     * 
     * @return 如果连接已打开返回 true，否则返回 false
     */
    public boolean isOpen() {
        SSEConnectionState state = this.connectionState;
        return state == SSEConnectionState.CONNECTED || state == SSEConnectionState.RECONNECTING;
    }
    
    /**
     * 关闭连接并释放所有资源
     *
     * <p><b>使用场景：</b>用于用户主动提前关闭 SSE 连接。
     * <ul>
     *   <li>正常情况下，SSE 连接会在服务器关闭连接或异常时自动结束，无需手动调用</li>
     *   <li>此方法仅在用户需要提前终止连接时使用（如业务逻辑判断无需继续监听）</li>
     * </ul>
     * </p>
     *
     * <p><b>关闭效果：</b>用户手动调用此方法后，会完全阻止后续的所有操作（包括自动重连）。
     * 这是设计上的特性：用户主动关闭表示不再需要连接，不应该自动重连。</p>
     *
     * <p><b>执行步骤：</b>
     * <ol>
     *   <li>终止正在进行的 SSE 流读取（通过 streamController.terminate()）</li>
     *   <li>取消正在进行的重连任务（如果存在）</li>
     *   <li>清空重连触发器，阻止自动重连</li>
     *   <li>更新连接状态为 CLOSED</li>
     *   <li>重置所有状态并清空引用</li>
     * </ol>
     * </p>
     *
     * <p><b>回调触发：</b>会触发 {@link com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener#onInterrupt()}
     * 回调，而非 {@code onComplete()}，以区别于正常结束。</p>
     *
     * <p><b>注意事项：</b>调用此方法后，控制器将不再可用。此方法使用 CAS 操作确保只执行一次，
     * 多次调用是安全的（后续调用会被忽略）。</p>
     */
    public void close() {
        close("用户主动关闭");
    }

    /**
     * 关闭连接并释放所有资源（带原因说明）
     *
     * @param reason 关闭原因（用于日志记录）
     * @see #close()
     */
    public void close(String reason) {
        // 使用 CAS 确保只执行一次（防止重复关闭）
        if (!destroyed.compareAndSet(false, true)) {
            log.debug("[SSE] 控制器已经关闭，跳过重复关闭操作");
            return;
        }

        log.info("[SSE] 关闭连接并释放所有资源，原因: {}", reason);

        // 步骤1：终止正在进行的 SSE 流读取（必须最先执行，否则流读取可能继续）
        SSEStreamController streamCtrl = streamController;
        if (streamCtrl != null) {
            streamCtrl.terminate();  // 设置终止标志，流读取循环会在读取下一行前检测到并退出
            log.debug("[SSE] 已终止 SSE 流读取");
        }

        // 步骤2：取消正在进行的重连任务（如果存在）
        ScheduledFuture<?> future = reconnectFuture.getAndSet(null);
        if (future != null && !future.isDone()) {
            future.cancel(false);  // 不中断正在执行的任务，仅取消尚未开始的任务
            log.debug("[SSE] 已取消正在进行的重连任务");
        }

        // 步骤3：阻止重连触发器继续工作（必须在其他操作之前）
        reconnectTrigger = null;  // 清空引用，使得后续无法触发重连

        // 步骤4：停止所有正在进行的重连（设置重连状态为 false，防止新的重连）
        setReconnecting(false);

        // 步骤5：更新连接状态为已关闭（会触发完成信号，唤醒 await() 等待线程）
        setConnectionState(SSEConnectionState.CLOSED);

        // 步骤6：重置状态（清理计数器）
        resetReconnectAttempt();

        // 步骤7：清空引用（释放内存）
        lastEventId.set(null);

        log.info("[SSE] 连接已关闭，所有资源已释放");
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
     * 等待SSE连接结束（无限等待）
     *
     * <p><b>⚠️ 阻塞警告：</b>此方法会阻塞当前线程，直到SSE连接结束（状态变为CLOSED或FAILED）。
     * 如果连接长时间不结束，调用线程会被无限期阻塞。</p>
     *
     * <p><b>❌ 禁止使用的场景：</b>
     * <ul>
     *   <li><b>Web 应用的 HTTP 请求处理线程中</b> - 会阻塞请求响应，导致接口超时</li>
     *   <li><b>前端接口或 UI 线程中</b> - 会导致界面卡死</li>
     *   <li><b>任何需要快速响应的场景</b> - 可能导致性能问题</li>
     * </ul>
     * </p>
     *
     * <p><b>✅ 适用场景：</b>
     * <ul>
     *   <li><b>单元测试或集成测试</b> - 验证 SSE 连接的正确性</li>
     *   <li><b>独立的后台任务线程</b> - 不影响主业务流程的异步任务</li>
     *   <li><b>命令行应用或批处理任务</b> - 需要等待 SSE 流处理完成后再退出</li>
     *   <li><b>应用关闭前的资源清理</b> - 确保 SSE 连接正确关闭</li>
     * </ul>
     * </p>
     *
     * <p><b>替代方案：</b>如果需要在 Web 应用中监听 SSE 连接状态，推荐使用回调：
     * <pre>{@code
     * // ✅ 推荐：使用回调监听连接状态
     * SSEEventListener<String> listener = new SSEEventListener<String>() {
     *     @Override
     *     public void onStateChanged(SSEController controller,
     *                              SSEConnectionState oldState,
     *                              SSEConnectionState newState) {
     *         if (newState == SSEConnectionState.CLOSED) {
     *             // 连接已关闭，执行后续逻辑
     *         }
     *     }
     * };
     * }</pre>
     * </p>
     *
     * <p><b>正确使用示例：</b>
     * <pre>{@code
     * // ✅ 在测试中使用
     * SSEController controller = Null.ofHttp("https://api.example.com/sse")
     *         .get()
     *         .toSSEText(listener);
     *
     * // 在测试线程中等待（不会影响其他请求）
     * controller.await();
     *
     * // 验证结果
     * assertEquals(expected, result);
     * }</pre>
     * </p>
     *
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>SSE 连接可能是长连接，如果没有明确结束条件，await() 可能永久阻塞</li>
     *   <li>推荐使用 {@link #await(long, TimeUnit)} 带超时的版本，避免永久阻塞</li>
     *   <li>如果需要提前结束等待，可以调用 {@link #close()} 主动关闭连接</li>
     * </ul>
     * </p>
     *
     * @throws InterruptedException 如果等待过程中线程被中断
     * @see #await(long, TimeUnit)
     * @see #close()
     */
    public void await() throws InterruptedException {
        completionLatch.await();
    }
    
    /**
     * 等待SSE连接结束（带超时）
     *
     * <p><b>⚠️ 阻塞警告：</b>此方法会阻塞当前线程，直到SSE连接结束（状态变为CLOSED或FAILED）或超时。
     * 虽然有超时限制，但仍会阻塞调用线程指定的时长。</p>
     *
     * <p><b>❌ 禁止使用的场景：</b>
     * <ul>
     *   <li><b>Web 应用的 HTTP 请求处理线程中</b> - 会阻塞请求响应，导致接口超时</li>
     *   <li><b>前端接口或 UI 线程中</b> - 会导致界面卡死</li>
     *   <li><b>任何需要快速响应的场景</b> - 即使有超时，也会影响性能</li>
     * </ul>
     * </p>
     *
     * <p><b>✅ 适用场景：</b>
     * <ul>
     *   <li><b>单元测试或集成测试</b> - 设置合理的超时时间，验证 SSE 连接的正确性</li>
     *   <li><b>独立的后台任务线程</b> - 不影响主业务流程的异步任务</li>
     *   <li><b>命令行应用或批处理任务</b> - 需要等待 SSE 流处理完成或超时后再退出</li>
     *   <li><b>应用关闭前的资源清理</b> - 设置超时避免无限等待</li>
     * </ul>
     * </p>
     *
     * <p><b>替代方案：</b>如果需要在 Web 应用中监听 SSE 连接状态，推荐使用回调：
     * <pre>{@code
     * // ✅ 推荐：使用回调监听连接状态
     * SSEEventListener<String> listener = new SSEEventListener<String>() {
     *     @Override
     *     public void onStateChanged(SSEController controller,
     *                              SSEConnectionState oldState,
     *                              SSEConnectionState newState) {
     *         if (newState == SSEConnectionState.CLOSED) {
     *             // 连接已关闭，执行后续逻辑
     *         }
     *     }
     * };
     * }</pre>
     * </p>
     *
     * <p><b>正确使用示例：</b>
     * <pre>{@code
     * // ✅ 在测试中使用带超时的等待
     * SSEController controller = Null.ofHttp("https://api.example.com/sse")
     *         .get()
     *         .toSSEText(listener);
     *
     * // 在测试线程中等待（最多等待30秒）
     * boolean completed = controller.await(30, TimeUnit.SECONDS);
     * if (completed) {
     *     // 连接正常结束
     *     assertEquals(expected, result);
     * } else {
     *     // 等待超时，连接未在30秒内结束
     *     fail("SSE 连接未在30秒内结束");
     * }
     * }</pre>
     * </p>
     *
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>超时时间应根据实际业务场景合理设置（建议测试场景 5-30 秒）</li>
     *   <li>超时后连接不会自动关闭，需要手动调用 {@link #close()} 来终止连接</li>
     *   <li>如果需要提前结束等待，可以调用 {@link #close()} 主动关闭连接</li>
     * </ul>
     * </p>
     *
     * @param timeout 超时时间（必须大于 0）
     * @param unit 时间单位（如 TimeUnit.SECONDS、TimeUnit.MILLISECONDS 等）
     * @return 如果连接在超时前结束返回 true，如果超时返回 false
     * @throws InterruptedException 如果等待过程中线程被中断
     * @see #await()
     * @see #close()
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return completionLatch.await(timeout, unit);
    }
    
    /**
     * 检查连接是否已结束
     * 
     * <p>连接结束是指状态变为CLOSED或FAILED。</p>
     * 
     * @return 如果连接已结束返回 true，否则返回 false
     */
    public boolean isCompleted() {
        SSEConnectionState state = this.connectionState;
        return state == SSEConnectionState.CLOSED || state == SSEConnectionState.FAILED;
    }
}

