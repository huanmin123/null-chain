package com.gitee.huanminabc.test.nullchain.leaf.http.websocket;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 心跳检测功能测试
 * 
 * <p>测试 WebSocket 心跳检测、心跳超时等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketHeartbeatTest extends WebSocketBaseTest {
    
    /**
     * 测试心跳检测功能
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testHeartbeat() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger heartbeatCount = new AtomicInteger(0);
        AtomicInteger messageCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-heartbeat")
                .retryCount(0)
                .heartbeat(new WebSocketHeartbeatHandler() {
                    @Override
                    public String generateHeartbeat() {
                        return "{\"type\":\"ping\"}";
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(String text) {
                        boolean isHeartbeat = text != null && 
                                (text.contains("\"type\":\"pong\"") || text.trim().equals("{\"type\":\"pong\"}"));
                        if (isHeartbeat) {
                            heartbeatCount.incrementAndGet();
                            log.info("收到心跳回复，总数: {}", heartbeatCount.get());
                        }
                        return isHeartbeat;
                    }
                }, 3000, 5000) // 间隔3秒，超时5秒
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 心跳回复会被自动处理，不会到达这里
                        // 这里只会收到非心跳消息
                        if (!text.contains("\"type\":\"pong\"")) {
                            messageCount.incrementAndGet();
                            log.info("收到非心跳消息: {}", text);
                        }
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
        
        // 等待一段时间，让心跳机制工作
        Thread.sleep(15000); // 等待15秒，应该至少发送4-5次心跳
        
        // 验证心跳是否工作
        assertTrue(heartbeatCount.get() > 0, "应该收到至少一次心跳回复");
        log.info("心跳回复总数: {}", heartbeatCount.get());
        
        // 验证非心跳消息也能正常接收
        assertTrue(messageCount.get() > 0, "应该收到至少一条非心跳消息");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试完整功能（子协议 + 心跳）
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testFullFeatures() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger heartbeatCount = new AtomicInteger(0);
        AtomicInteger messageCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-full")
                .retryCount(0)
                .subprotocol("chat", "superchat", "notification")
                .heartbeat(new WebSocketHeartbeatHandler() {
                    @Override
                    public String generateHeartbeat() {
                        return "{\"type\":\"ping\"}";
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(String text) {
                        boolean isHeartbeat = text != null && text.contains("\"type\":\"pong\"");
                        if (isHeartbeat) {
                            heartbeatCount.incrementAndGet();
                        }
                        return isHeartbeat;
                    }
                }, 3000, 5000)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立，选中的子协议: {}", controller.getSelectedSubprotocol());
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        if (!text.contains("\"type\":\"pong\"")) {
                            messageCount.incrementAndGet();
                            log.info("收到消息: {}", text);
                        }
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
        
        // 等待一段时间，让心跳和消息机制工作
        Thread.sleep(12000);
        
        // 验证心跳
        assertTrue(heartbeatCount.get() > 0, "应该收到至少一次心跳回复");
        log.info("心跳回复总数: {}", heartbeatCount.get());
        
        // 验证消息
        assertTrue(messageCount.get() > 0, "应该收到至少一条消息");
        log.info("收到消息总数: {}", messageCount.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试心跳的字符串格式（服务端支持两种格式）
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testHeartbeatStringFormat() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger heartbeatCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-heartbeat")
                .retryCount(0)
                .heartbeat(new WebSocketHeartbeatHandler() {
                    @Override
                    public String generateHeartbeat() {
                        // 使用字符串格式的心跳（服务端也支持）
                        return "{\"type\":\"ping\"}";
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(String text) {
                        // 服务端可能返回 {"type":"pong"} 或 {"type":"pong"}
                        boolean isHeartbeat = text != null && 
                                (text.contains("\"type\":\"pong\"") || 
                                 text.trim().equals("{\"type\":\"pong\"}"));
                        if (isHeartbeat) {
                            heartbeatCount.incrementAndGet();
                            log.info("收到心跳回复 {}: {}", heartbeatCount.get(), text);
                        }
                        return isHeartbeat;
                    }
                }, 3000, 5000)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 心跳回复会被自动处理
                        log.debug("收到消息: {}", text);
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
        
        // 等待一段时间，让心跳机制工作
        Thread.sleep(12000); // 等待12秒，应该至少发送3-4次心跳
        
        // 验证心跳是否工作
        assertTrue(heartbeatCount.get() > 0, 
                "应该收到至少一次心跳回复，实际收到: " + heartbeatCount.get());
        log.info("心跳回复总数: {}", heartbeatCount.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试心跳超时检测
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testHeartbeatTimeout() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger closeCode = new AtomicInteger(-1);
        AtomicReference<String> closeReason = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-heartbeat-timeout")
                .retryCount(0)
                .heartbeat(new WebSocketHeartbeatHandler() {
                    @Override
                    public String generateHeartbeat() {
                        return "{\"type\":\"ping\"}";
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(String text) {
                        // 服务器不回复心跳，导致超时
                        return false;
                    }
                }, 2000, 3000) // 间隔2秒，超时3秒
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
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                        closeCode.set(code);
                        closeReason.set(reason);
                        closeLatch.countDown();
                    }
                });
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 等待心跳超时（3秒超时 + 2秒间隔 = 至少5秒后才会超时）
        assertTrue(closeLatch.await(10, TimeUnit.SECONDS), "应该在10秒内因心跳超时关闭");
        assertEquals(1000, closeCode.get(), "关闭状态码应该是 1000");
        assertTrue(closeReason.get() != null && closeReason.get().contains("心跳超时"),
                "关闭原因应该包含'心跳超时'");
        
        // 验证连接已关闭
        assertFalse(controller.isOpen(), "连接应该处于关闭状态");
    }
    
    /**
     * 测试二进制格式心跳
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testBinaryHeartbeat() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger heartbeatCount = new AtomicInteger(0);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-heartbeat-binary")
                .retryCount(0)
                .heartbeat(new WebSocketHeartbeatHandler() {
                    @Override
                    public String generateHeartbeat() {
                        // 返回 null，使用二进制格式
                        return null;
                    }
                    
                    @Override
                    public byte[] generateHeartbeatBytes() {
                        // 返回二进制心跳
                        return "PING".getBytes();
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(String text) {
                        return false;
                    }
                    
                    @Override
                    public boolean isHeartbeatResponse(byte[] bytes) {
                        // 检查是否是二进制心跳回复
                        if (bytes != null && bytes.length == 4) {
                            String response = new String(bytes);
                            if ("PONG".equals(response)) {
                                heartbeatCount.incrementAndGet();
                                log.info("收到二进制心跳回复，总数: {}", heartbeatCount.get());
                                return true;
                            }
                        }
                        return false;
                    }
                }, 3000, 5000)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到文本消息: {}", text);
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 心跳回复会被自动处理，不会到达这里
                        log.debug("收到二进制消息，长度: {}", bytes.length);
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
        
        // 等待一段时间，让心跳机制工作
        Thread.sleep(12000);
        
        // 验证心跳是否工作
        assertTrue(heartbeatCount.get() > 0, 
                "应该收到至少一次二进制心跳回复，实际收到: " + heartbeatCount.get());
        log.info("二进制心跳回复总数: {}", heartbeatCount.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
}

