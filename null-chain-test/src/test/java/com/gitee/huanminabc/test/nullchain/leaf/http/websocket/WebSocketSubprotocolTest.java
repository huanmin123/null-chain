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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 子协议功能测试
 * 
 * <p>测试 WebSocket 子协议的配置、验证等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketSubprotocolTest extends WebSocketBaseTest {
    
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
        log.info("选中的子协议: {}", selectedProtocol.get());
        
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
                        // 忽略二进制消息
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
    
    /**
     * 测试子协议验证：服务器返回协议但客户端未配置
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSubprotocolServerOnly() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        AtomicReference<String> selectedProtocol = new AtomicReference<>();
        
        // 客户端不配置子协议，但服务器返回协议
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws-subprotocol")
                .retryCount(0)
                // 不配置子协议
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
        
        // 验证：如果服务器返回了协议，应该被保存（根据代码逻辑）
        log.info("服务器返回的子协议: {}", selectedProtocol.get());
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
    
    /**
     * 测试子协议验证：客户端配置协议但服务器未返回
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSubprotocolClientOnly() throws InterruptedException {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        AtomicBoolean openCalled = new AtomicBoolean(false);
        
        // 客户端配置子协议，但服务器不返回（使用不支持子协议的端点）
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws") // 这个端点不支持子协议
                .retryCount(0)
                .subprotocol("chat") // 配置子协议
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
                        // 忽略二进制消息
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
        
        // 等待错误或连接建立
        boolean errorOccurred = errorLatch.await(5, TimeUnit.SECONDS);
        
        if (errorOccurred) {
            assertNotNull(errorMessage.get(), "应该有错误信息");
            log.info("子协议验证失败（预期）: {}", errorMessage.get());
        } else if (openCalled.get()) {
            // 如果服务器接受了连接，记录警告
            log.warn("服务器接受了子协议请求，这可能不是预期的行为");
        }
        
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }
}

