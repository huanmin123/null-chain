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
 * WebSocket 基础功能测试
 * 
 * <p>测试 WebSocket 连接、消息收发、URL 转换等基础功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketBasicTest extends WebSocketBaseTest {
    
    /**
     * 测试基础 WebSocket 连接和消息收发
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testBasicWebSocket() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch echoLatch = new CountDownLatch(1);
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicReference<String> echoResponse = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0) // 测试时不重连
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        connected.set(true);
                        openLatch.countDown();
                        
                        // 发送测试消息
                        controller.send("{\"type\":\"echo\",\"message\":\"Hello Server\"}");
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到文本消息: {}", text);
                        // 忽略欢迎消息，只等待 echo 响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("original")) {
                            echoResponse.set(text);
                            echoLatch.countDown();
                        }
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        log.info("收到二进制消息，长度: {}", bytes.length);
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
        assertTrue(connected.get(), "连接状态应该为已连接");
        
        // 等待收到 echo 响应消息（忽略欢迎消息）
        assertTrue(echoLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 echo 响应消息");
        assertNotNull(echoResponse.get(), "应该收到 echo 响应消息");
        assertTrue(echoResponse.get().contains("echo") && echoResponse.get().contains("original"), 
                "echo 响应消息应该包含 echo 和 original 字段");
        
        // 关闭连接
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试欢迎消息的接收和解析
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testWelcomeMessage() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch welcomeLatch = new CountDownLatch(1);
        AtomicReference<String> welcomeMessage = new AtomicReference<>();
        
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
                        // 检查是否是欢迎消息
                        if (text.contains("\"type\":\"welcome\"") || text.contains("连接成功")) {
                            welcomeMessage.set(text);
                            welcomeLatch.countDown();
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
        
        // 等待欢迎消息
        assertTrue(welcomeLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到欢迎消息");
        assertNotNull(welcomeMessage.get(), "应该收到欢迎消息");
        assertTrue(welcomeMessage.get().contains("welcome") || welcomeMessage.get().contains("连接成功"),
                "欢迎消息应该包含 welcome 或连接成功");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试 echo 类型消息
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testEchoMessage() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch echoLatch = new CountDownLatch(1);
        AtomicReference<String> echoResponse = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                        
                        // 发送 echo 消息
                        controller.send("{\"type\":\"echo\",\"message\":\"Hello Echo Test\"}");
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到消息: {}", text);
                        // 检查是否是 echo 响应
                        if (text.contains("\"type\":\"echo\"") || text.contains("original")) {
                            echoResponse.set(text);
                            echoLatch.countDown();
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
        
        // 等待 echo 响应
        assertTrue(echoLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 echo 响应");
        assertNotNull(echoResponse.get(), "应该收到 echo 响应");
        assertTrue(echoResponse.get().contains("echo") || echoResponse.get().contains("original"),
                "响应应该包含 echo 或 original");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试普通文本消息的回显
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testPlainTextEcho() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch echoLatch = new CountDownLatch(1);
        AtomicReference<String> echoResponse = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                        
                        // 发送普通文本消息（非 JSON）
                        controller.send("Plain Text Message");
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到消息: {}", text);
                        // 检查是否是 echo 响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("Plain Text Message")) {
                            echoResponse.set(text);
                            echoLatch.countDown();
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
        
        // 等待 echo 响应
        assertTrue(echoLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到 echo 响应");
        assertNotNull(echoResponse.get(), "应该收到 echo 响应");
        assertTrue(echoResponse.get().contains("Plain Text Message"),
                "响应应该包含原始消息内容");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试二进制消息发送和接收
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testBinaryMessage() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch binaryLatch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedBytes = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        openLatch.countDown();
                        
                        // 发送二进制消息
                        byte[] testData = "Hello Binary".getBytes();
                        controller.send(testData);
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到文本消息: {}", text);
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        log.info("收到二进制消息，长度: {}", bytes.length);
                        receivedBytes.set(bytes);
                        binaryLatch.countDown();
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
        
        // 等待收到二进制消息
        assertTrue(binaryLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到二进制消息");
        assertNotNull(receivedBytes.get(), "应该收到二进制消息");
        assertTrue(receivedBytes.get().length > 0, "二进制消息长度应该大于0");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试服务器定期发送的测试消息
     */
    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testPeriodicMessages() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicInteger notificationCount = new AtomicInteger(0);
        
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
                        // 检查是否是定期测试消息
                        if (text.contains("\"type\":\"notification\"") || 
                            text.contains("这是一条定期测试消息")) {
                            notificationCount.incrementAndGet();
                            log.info("收到定期测试消息 {}: {}", notificationCount.get(), text);
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
        
        // 等待至少12秒，应该收到至少2条定期消息（每5秒一条）
        Thread.sleep(12000);
        
        // 验证收到了定期消息
        assertTrue(notificationCount.get() >= 2, 
                "应该在12秒内收到至少2条定期测试消息，实际收到: " + notificationCount.get());
        log.info("收到定期测试消息总数: {}", notificationCount.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试 URL 转换（http → ws, https → wss）
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testUrlConversion() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        
        // 测试 http:// 转换为 ws://
        WebSocketController controller1 = Null.ofHttp("http://localhost:3001/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立（http转换）");
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 忽略消息
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
        
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        controller1.close(1000, "测试完成");
        Thread.sleep(500);
        
        // 测试 ws:// 直接使用
        CountDownLatch openLatch2 = new CountDownLatch(1);
        WebSocketController controller2 = Null.ofHttp("ws://localhost:3001/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立（ws直接）");
                        openLatch2.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 忽略消息
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
        
        assertTrue(openLatch2.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        controller2.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试连接状态检查
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testConnectionStatus() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicBoolean connectionStatus = new AtomicBoolean(false);
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立");
                        connectionStatus.set(controller.isOpen());
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 忽略消息
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
                        connectionStatus.set(controller.isOpen());
                    }
                });
        
        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        
        // 验证连接状态
        assertTrue(controller.isOpen(), "连接应该处于打开状态");
        assertTrue(connectionStatus.get(), "onOpen 回调中的连接状态应该为 true");
        
        // 关闭连接
        controller.close(1000, "测试完成");
        Thread.sleep(500);
        
        // 验证连接已关闭
        assertFalse(controller.isOpen(), "连接应该处于关闭状态");
    }
}

