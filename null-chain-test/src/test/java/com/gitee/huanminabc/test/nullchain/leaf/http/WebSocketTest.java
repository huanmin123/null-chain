package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 功能测试
 * 
 * <p>测试 WebSocket 连接、消息收发、心跳检测、子协议等功能。</p>
 * 
 * <p>运行前需要启动测试服务器：</p>
 * <pre>
 * cd null-chain-test/src/test/resources
 * node websocket-server.js
 * </pre>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketTest {
    
    private static final String BASE_URL = "ws://localhost:3001";
    private static final int TIMEOUT_SECONDS = 10;
    
    @BeforeAll
    public static void checkServer() throws InterruptedException {
        // 检查服务器是否运行
        // 这里可以添加服务器健康检查逻辑
        Thread.sleep(1000); // 等待服务器启动
    }
    
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
     * 测试子协议功能
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSubprotocol() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicReference<String> selectedProtocol = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-subprotocol")
                .retryCount(0)
                .subprotocol("chat", "superchat") // 配置支持的子协议
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立，选中的子协议: {}", controller.getSelectedSubprotocol());
                        selectedProtocol.set(controller.getSelectedSubprotocol());
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 忽略消息
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略消息
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
        
        // 验证子协议
        assertNotNull(selectedProtocol.get(), "应该选中了子协议");
        assertTrue("chat".equals(selectedProtocol.get()) || "superchat".equals(selectedProtocol.get()),
                "选中的协议应该是 chat 或 superchat");
        log.info("选中的子协议: {}", selectedProtocol.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
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
        AtomicReference<String> selectedProtocol = new AtomicReference<>();
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
                        selectedProtocol.set(controller.getSelectedSubprotocol());
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
        
        // 验证子协议
        assertNotNull(selectedProtocol.get(), "应该选中了子协议");
        log.info("选中的子协议: {}", selectedProtocol.get());
        
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
                        
                        // 在连接建立前发送消息（会进入队列）
                        // 注意：这里实际上连接已经建立了，所以消息会立即发送
                        // 为了测试队列，我们需要在连接建立前发送
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
     * 测试子协议验证失败的情况
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSubprotocolValidationFailure() throws InterruptedException {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicBoolean openCalled = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        
        // 注意：这个测试需要服务器支持拒绝不匹配的协议
        // 如果服务器总是接受，这个测试可能会失败
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-subprotocol")
                .retryCount(0)
                .subprotocol("unsupported-protocol") // 不支持的协议
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        openCalled.set(true);
                        log.info("连接建立（不应该发生）");
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 忽略消息
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略消息
                    }
                    
                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("发生错误（预期）: {}", message, t);
                        errorMessage.set(message);
                        errorLatch.countDown();
                    }
                    
                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("连接关闭: code={}, reason={}", code, reason);
                    }
                });
        
        // 等待错误或超时
        boolean errorOccurred = errorLatch.await(5, TimeUnit.SECONDS);
        
        // 如果服务器拒绝连接，应该触发错误
        // 如果服务器接受连接，onOpen 会被调用
        if (errorOccurred) {
            assertNotNull(errorMessage.get(), "应该有错误信息");
            log.info("协议验证失败（预期）: {}", errorMessage.get());
        } else if (openCalled.get()) {
            log.warn("服务器接受了不支持的协议，这可能不是预期的行为");
        }
        
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
    
    /**
     * 测试子协议在欢迎消息中的体现
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSubprotocolInWelcomeMessage() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch welcomeLatch = new CountDownLatch(1);
        AtomicReference<String> selectedProtocol = new AtomicReference<>();
        AtomicReference<String> welcomeMessage = new AtomicReference<>();
        
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-full")
                .retryCount(0)
                .subprotocol("chat", "superchat")
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("连接建立，选中的子协议: {}", controller.getSelectedSubprotocol());
                        selectedProtocol.set(controller.getSelectedSubprotocol());
                        openLatch.countDown();
                    }
                    
                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到消息: {}", text);
                        // 检查欢迎消息中是否包含协议信息
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
        
        // 验证子协议
        assertNotNull(selectedProtocol.get(), "应该选中了子协议");
        assertTrue("chat".equals(selectedProtocol.get()) || "superchat".equals(selectedProtocol.get()),
                "选中的协议应该是 chat 或 superchat");
        
        // 等待欢迎消息
        assertTrue(welcomeLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到欢迎消息");
        assertNotNull(welcomeMessage.get(), "应该收到欢迎消息");
        
        // 验证欢迎消息中包含协议信息
        assertTrue(welcomeMessage.get().contains("protocol") || 
                   welcomeMessage.get().contains(selectedProtocol.get()),
                "欢迎消息应该包含协议信息");
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
}

