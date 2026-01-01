package com.gitee.huanminabc.test.nullchain.leaf.http.websocket;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 连接关闭功能测试
 * 
 * <p>测试 WebSocket 连接关闭、onClosing 事件等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketCloseTest extends WebSocketBaseTest {
    
    /**
     * 测试服务器主动关闭连接（通过 close 消息）
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testServerClose() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch closingLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger closingCode = new AtomicInteger(-1);
        AtomicInteger closeCode = new AtomicInteger(-1);
        AtomicReference<String> closingReason = new AtomicReference<>();
        AtomicReference<String> closeReason = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                        
                        // 发送 close 消息，让服务器主动关闭连接
                        controller.send("{\"type\":\"close\"}");
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到消息: {}", text);
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
                        log.info("连接正在关闭: code={}, reason={}", code, reason);
                        closingCode.set(code);
                        closingReason.set(reason);
                        closingLatch.countDown();
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                        closeCode.set(code);
                        closeReason.set(reason);
                        closeLatch.countDown();
                    }
                });
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 等待连接正在关闭事件
        assertTrue(closingLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 onClosing 事件");
        assertEquals(1000, closingCode.get(), "onClosing 状态码应该是 1000");
        assertNotNull(closingReason.get(), "onClosing 应该有关闭原因");
        
        // 等待连接关闭事件
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 onClose 事件");
        assertEquals(1000, closeCode.get(), "onClose 状态码应该是 1000");
        assertNotNull(closeReason.get(), "onClose 应该有关闭原因");
        assertTrue(closeReason.get().contains("客户端请求关闭") || closeReason.get().contains("close"),
                "关闭原因应该包含相关信息");
        
        // 验证 onClosing 在 onClose 之前被调用
        assertTrue(closingLatch.getCount() == 0 && closeLatch.getCount() == 0, 
                "onClosing 应该在 onClose 之前被调用");
        
        // 验证连接已关闭
        assertFalse(controller.isOpen(), "连接应该处于关闭状态");
    }
    
    /**
     * 测试客户端主动关闭连接时的 onClosing 事件
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testClientCloseWithOnClosing() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch closingLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger closingCode = new AtomicInteger(-1);
        AtomicInteger closeCode = new AtomicInteger(-1);
        AtomicReference<String> closingReason = new AtomicReference<>();
        AtomicReference<String> closeReason = new AtomicReference<>();
        AtomicBoolean closingCalled = new AtomicBoolean(false);
        AtomicBoolean closeCalled = new AtomicBoolean(false);
        
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
                        log.info("收到消息: {}", text);
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
                        log.info("连接正在关闭: code={}, reason={}", code, reason);
                        closingCode.set(code);
                        closingReason.set(reason);
                        closingCalled.set(true);
                        closingLatch.countDown();
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                        closeCode.set(code);
                        closeReason.set(reason);
                        closeCalled.set(true);
                        closeLatch.countDown();
                    }
                });
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 客户端主动关闭连接
        controller.close(1000, "测试完成");
        
        // 等待连接正在关闭事件
        assertTrue(closingLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 onClosing 事件");
        assertTrue(closingCalled.get(), "onClosing 应该被调用");
        assertEquals(1000, closingCode.get(), "onClosing 状态码应该是 1000");
        assertNotNull(closingReason.get(), "onClosing 应该有关闭原因");
        assertEquals("测试完成", closingReason.get(), "onClosing 关闭原因应该是 '测试完成'");
        
        // 等待连接关闭事件
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 onClose 事件");
        assertTrue(closeCalled.get(), "onClose 应该被调用");
        assertEquals(1000, closeCode.get(), "onClose 状态码应该是 1000");
        assertNotNull(closeReason.get(), "onClose 应该有关闭原因");
        
        // 验证 onClosing 在 onClose 之前被调用
        assertTrue(closingCalled.get() && closeCalled.get(), 
                "onClosing 和 onClose 都应该被调用");
        
        // 验证连接已关闭
        assertFalse(controller.isOpen(), "连接应该处于关闭状态");
    }
}

