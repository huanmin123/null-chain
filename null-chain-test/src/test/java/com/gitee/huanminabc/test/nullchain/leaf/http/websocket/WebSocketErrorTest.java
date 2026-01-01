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
 * WebSocket 错误处理测试
 * 
 * <p>测试 WebSocket 连接失败、错误处理等功能。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class WebSocketErrorTest extends WebSocketBaseTest {
    
    /**
     * 测试连接失败场景
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testConnectionFailure() throws InterruptedException {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        AtomicBoolean openCalled = new AtomicBoolean(false);
        
        // 尝试连接到不存在的服务器
        WebSocketController controller = Null.ofHttp("ws://localhost:9999/nonexistent")
                .retryCount(0) // 不重试
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
        
        // 等待错误
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到错误");
        assertFalse(openCalled.get(), "onOpen 不应该被调用");
        assertNotNull(errorMessage.get(), "应该有错误信息");
        
        // 验证连接已关闭
        assertFalse(controller.isOpen(), "连接应该处于关闭状态");
    }
}

