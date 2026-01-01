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
 * WebSocket 重连机制测试
 * 
 * <p>测试 WebSocket 重连机制、重连延迟计算等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketReconnectTest extends WebSocketBaseTest {
    
    /**
     * 测试重连机制（有消息队列时）
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testReconnectWithMessageQueue() throws InterruptedException {
        CountDownLatch firstOpenLatch = new CountDownLatch(1);
        CountDownLatch reconnectOpenLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger messageCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-reconnect")
                .retryCount(2) // 允许重试2次
                .retryInterval(1000) // 重试间隔1秒
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        int count = openCount.incrementAndGet();
                        log.info("连接建立，第{}次，连接状态: {}", count, controller.isOpen());
                        if (count == 1) {
                            firstOpenLatch.countDown();
                            // 发送消息后立即关闭连接（模拟网络断开）
                            log.info("准备发送第一条测试消息");
                            boolean sent1 = controller.send("{\"type\":\"test\",\"message\":\"before close\"}");
                            log.info("第一条消息发送结果: {}, 待发送消息数: {}", sent1, controller.getPendingMessageCount());
                            
                            // 在连接建立后发送消息，然后触发服务器关闭
                            try {
                                log.info("等待500ms后发送关闭请求");
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            
                            log.info("准备发送关闭请求，连接状态: {}", controller.isOpen());
                            boolean sent2 = controller.send("{\"type\":\"close\"}");
                            log.info("关闭请求发送结果: {}, 待发送消息数: {}", sent2, controller.getPendingMessageCount());
                        } else {
                            log.info("重连成功，第{}次连接", count);
                            reconnectOpenLatch.countDown();
                        }
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        messageCount.incrementAndGet();
                        log.info("收到消息 {}: {}", messageCount.get(), text);
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
                    public void onClosing(WebSocketController controller, int code, String reason) {
                        log.info("连接正在关闭: code={}, reason={}, 连接状态: {}", code, reason, controller.isOpen());
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接已关闭: code={}, reason={}, 连接状态: {}", code, reason, controller.isOpen());
                        closeLatch.countDown();
                    }
                });
        
        // 等待第一次连接建立
        assertTrue(firstOpenLatch.await(5, TimeUnit.SECONDS), "第一次连接应该在5秒内建立");
        log.info("第一次连接已建立，当前连接状态: {}", controller.isOpen());
        
        // 等待一小段时间，确保消息发送完成
        Thread.sleep(1000);
        log.info("等待1秒后，连接状态: {}, 待发送消息数: {}", controller.isOpen(), controller.getPendingMessageCount());
        
        // 等待连接关闭
        boolean closed = closeLatch.await(5, TimeUnit.SECONDS);
        log.info("等待关闭结果: {}, 当前连接状态: {}, 待发送消息数: {}", closed, controller.isOpen(), controller.getPendingMessageCount());
        assertTrue(closed, "连接应该在5秒内关闭");
        
        // 在连接关闭后发送消息（这些消息会进入队列，触发重连）
        controller.send("{\"type\":\"queued1\"}");
        controller.send("{\"type\":\"queued2\"}");
        log.info("连接关闭后发送了2条消息，待发送消息数: {}", controller.getPendingMessageCount());
        
        // 等待重连
        assertTrue(reconnectOpenLatch.await(10, TimeUnit.SECONDS), "应该在10秒内重连成功");
        assertTrue(openCount.get() >= 2, "应该至少重连一次");
        
        // 等待消息发送
        Thread.sleep(2000);
        
        // 验证消息队列已清空
        assertEquals(0, controller.getPendingMessageCount(), "重连后消息队列应该为空");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
}

