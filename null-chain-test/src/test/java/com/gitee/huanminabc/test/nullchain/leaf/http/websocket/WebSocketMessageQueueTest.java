package com.gitee.huanminabc.test.nullchain.leaf.http.websocket;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 消息队列功能测试
 * 
 * <p>测试 WebSocket 消息队列、队列满时的处理策略等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketMessageQueueTest extends WebSocketBaseTest {
    
    /**
     * 测试消息队列功能
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testMessageQueue() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger receivedCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        receivedCount.incrementAndGet();
                        log.info("收到消息 {}: {}", receivedCount.get(), text);
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }
                    
                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("发生错误: {}", message, t);
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                    }
                });
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 发送多条消息
        for (int i = 0; i < 5; i++) {
            controller.send("{\"type\":\"test\",\"index\":" + i + "}");
            Thread.sleep(100);
        }
        
        // 等待消息处理
        Thread.sleep(2000);
        
        // 验证消息是否发送成功（通过服务器回显）
        assertTrue(receivedCount.get() >= 5, "应该收到至少5条消息（包括服务器定期消息和回显）");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试消息队列功能（在连接建立前发送消息）
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testMessageQueueBeforeConnection() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger receivedCount = new AtomicInteger(0);
        AtomicInteger sentCount = new AtomicInteger(0);
        
        // 先创建控制器，但不立即连接
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立，待发送消息数: {}", controller.getPendingMessageCount());
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        receivedCount.incrementAndGet();
                        log.info("收到消息 {}: {}", receivedCount.get(), text);
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }
                    
                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("发生错误: {}", message, t);
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                    }
                });
        
        // 在连接建立前发送消息（这些消息应该进入队列）
        // 注意：由于 toWebSocket 是异步的，连接可能已经建立
        // 但我们可以通过检查队列来验证
        for (int i = 0; i < 3; i++) {
            boolean sent = controller.send("{\"type\":\"queued\",\"index\":" + i + "}");
            if (sent) {
                sentCount.incrementAndGet();
            }
            log.info("发送消息 {}，是否立即发送: {}", i, sent);
        }
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 等待消息处理
        Thread.sleep(3000);
        
        // 验证消息是否发送成功
        // 由于连接可能已经建立，消息可能立即发送，也可能从队列发送
        log.info("发送的消息数: {}, 收到的消息数: {}", sentCount.get() + (3 - sentCount.get()), receivedCount.get());
        
        // 验证队列已清空（连接建立后，队列中的消息应该都已发送）
        assertEquals(0, controller.getPendingMessageCount(), 
                "连接建立后，消息队列应该为空");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
}

