package com.gitee.huanminabc.test.nullchain.leaf.http.websocket;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 测试Demo
 * 
 * <p>使用本地WebSocket服务器进行测试，运行前需要启动测试服务器：</p>
 * <pre>
 * cd null-chain-test/src/test/resources
 * node websocket-server.js
 * </pre>
 * 
 * <p>测试端点：</p>
 * <ul>
 *   <li>ws://localhost:3001/ws - 基础WebSocket连接</li>
 *   <li>ws://localhost:3001/ws - 支持echo消息</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class WebSocketTestDemo extends WebSocketBaseTest {

    /**
     * 测试基础WebSocket连接和消息收发
     * 
     * <p>演示如何使用WebSocket进行连接、发送消息和接收消息。</p>
     */
    @Test
    public void testBasicWebSocket() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(3);
        AtomicInteger messageCount = new AtomicInteger(0);

        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 过滤掉欢迎消息，只统计echo响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("original")) {
                            int count = messageCount.incrementAndGet();
                            log.info("收到第{}条echo响应: {}", count, text);
                            messageLatch.countDown();
                        } else {
                            log.debug("收到其他消息（已过滤）: {}", text);
                        }
                    }

                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        log.info("收到二进制消息，长度: {}", bytes.length);
                    }

                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("WebSocket错误: {}", message, t);
                    }

                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                    }
                });

        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");

        // 发送多条消息
        controller.send("{\"type\":\"echo\",\"message\":\"Hello Server 1\"}");
        Thread.sleep(200);
        controller.send("{\"type\":\"echo\",\"message\":\"Hello Server 2\"}");
        Thread.sleep(200);
        controller.send("{\"type\":\"echo\",\"message\":\"Hello Server 3\"}");

        // 等待收到所有消息
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到所有消息");
        assertEquals(3, messageCount.get(), "应该收到3条消息");

        // 关闭连接
        controller.close(1000, "测试完成");
        Thread.sleep(500);
    }

    /**
     * 测试使用 await() 方法等待连接结束
     * 
     * <p>演示如何使用 await() 方法同步等待WebSocket连接结束。</p>
     */
    @Test
    public void testAwaitConnection() throws InterruptedException {
        AtomicInteger messageCount = new AtomicInteger(0);
        CountDownLatch openLatch = new CountDownLatch(1);

        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 过滤掉欢迎消息和定期通知消息，只统计echo响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("original")) {
                            int count = messageCount.incrementAndGet();
                            log.info("收到第{}条echo响应: {}", count, text);
                            // 接收5条echo响应后关闭连接
                            if (count >= 5) {
                                controller.close(1000, "接收完成");
                            }
                        } else {
                            log.debug("收到其他消息（已过滤）: {}", text);
                        }
                    }

                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }

                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("WebSocket错误: {}", message, t);
                    }

                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                    }
                });

        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");

        // 发送消息触发服务器响应
        for (int i = 1; i <= 5; i++) {
            controller.send("{\"type\":\"echo\",\"message\":\"test" + i + "\"}");
            Thread.sleep(200);
        }

        // 使用 await() 方法等待连接结束
        log.info("开始等待连接结束...");
        controller.await();
        log.info("连接已结束，await() 方法已返回");

        // 验证结果
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(messageCount.get() >= 5, "应该接收到至少5条消息");
        log.info("await() 测试完成 - 接收消息数: {}", messageCount.get());
    }

    /**
     * 测试使用 await(timeout, unit) 方法带超时等待连接结束
     * 
     * <p>演示如何使用带超时的 await() 方法等待WebSocket连接结束。</p>
     */
    @Test
    public void testAwaitWithTimeout() throws InterruptedException {
        AtomicInteger messageCount = new AtomicInteger(0);
        CountDownLatch openLatch = new CountDownLatch(1);

        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 过滤掉欢迎消息和定期通知消息，只统计echo响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("original")) {
                            int count = messageCount.incrementAndGet();
                            log.info("收到第{}条echo响应: {}", count, text);
                            // 接收3条echo响应后关闭连接
                            if (count >= 3) {
                                controller.close(1000, "接收完成");
                            }
                        } else {
                            log.debug("收到其他消息（已过滤）: {}", text);
                        }
                    }

                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }

                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("WebSocket错误: {}", message, t);
                    }

                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                    }
                });

        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");

        // 发送消息触发服务器响应
        for (int i = 1; i <= 3; i++) {
            controller.send("{\"type\":\"echo\",\"message\":\"test" + i + "\"}");
            Thread.sleep(200);
        }

        // 使用带超时的 await() 方法等待连接结束（最多等待30秒）
        log.info("开始等待连接结束（超时30秒）...");
        boolean completed = controller.await(30, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "await() 应该返回 true（连接已结束）");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(messageCount.get() >= 3, "应该接收到至少3条消息");
        log.info("await(timeout) 测试完成 - 接收消息数: {}", messageCount.get());
    }

    /**
     * 测试 await() 超时场景
     * 
     * <p>演示当连接未在超时时间内结束时，await() 方法返回 false。</p>
     */
    @Test
    public void testAwaitTimeout() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);

        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.info("收到消息: {}", text);
                        // 不关闭连接，让连接持续运行
                    }

                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }

                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("WebSocket错误: {}", message, t);
                    }

                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                    }
                });

        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");

        // 使用很短的超时时间（1秒）等待连接结束
        log.info("开始等待连接结束（超时1秒）...");
        long startTime = System.currentTimeMillis();
        boolean completed = controller.await(1, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证结果
        assertFalse(completed, "await() 应该返回 false（超时）");
        assertFalse(controller.isCompleted(), "连接应该还未结束");
        assertTrue(duration >= 900 && duration < 2000, "应该在大约1秒后返回，实际耗时: " + duration + "ms");
        log.info("await(timeout) 超时测试完成 - 等待耗时: {}ms", duration);

        // 清理：关闭连接
        controller.close(1000, "测试完成");
    }

    /**
     * 测试 isCompleted() 方法
     * 
     * <p>演示如何使用 isCompleted() 方法检查连接是否已结束。</p>
     */
    @Test
    public void testIsCompleted() throws InterruptedException {
        CountDownLatch openLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);

        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        log.debug("收到消息: {}", text);
                    }

                    @Override
                    public void onMessage(WebSocketController controller, byte[] bytes) {
                        // 忽略二进制消息
                    }

                    @Override
                    public void onError(WebSocketController controller, Throwable t, String message) {
                        log.error("WebSocket错误: {}", message, t);
                    }

                    @Override
                    public void onClose(WebSocketController controller, int code, String reason) {
                        log.info("WebSocket连接已关闭: code={}, reason={}", code, reason);
                        closeLatch.countDown();
                    }
                });

        // 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");

        // 连接建立后，应该未结束
        assertFalse(controller.isCompleted(), "连接建立后应该未结束");
        log.info("连接建立后，isCompleted() = {}", controller.isCompleted());

        // 关闭连接
        controller.close(1000, "测试完成");

        // 等待连接关闭
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内关闭");

        // 连接结束后，应该已结束
        assertTrue(controller.isCompleted(), "连接结束后应该已结束");
        log.info("连接关闭后，isCompleted() = {}", controller.isCompleted());
    }

    /**
     * 综合测试：完整的使用流程
     * 
     * <p>演示WebSocket的完整使用流程，包括连接、发送消息、接收消息、等待结束等。</p>
     */
    @Test
    public void testCompleteWorkflow() throws InterruptedException {
        AtomicInteger messageCount = new AtomicInteger(0);
        CountDownLatch openLatch = new CountDownLatch(1);

        log.info("=== 开始WebSocket完整流程测试 ===");

        // 1. 建立连接
        WebSocketController controller = Null.ofHttp(BASE_URL + "/ws")
                .retryCount(0)
                .toWebSocket(new WebSocketEventListener() {
                    @Override
                    public void onOpen(WebSocketController controller) {
                        log.info("步骤1: WebSocket连接已建立");
                        openLatch.countDown();
                    }

                    @Override
                    public void onMessage(WebSocketController controller, String text) {
                        // 过滤掉欢迎消息和定期通知消息，只统计echo响应
                        if (text.contains("\"type\":\"echo\"") && text.contains("original")) {
                            int count = messageCount.incrementAndGet();
                            log.info("步骤3: 收到第{}条echo响应: {}", count, text);
                            // 接收3条echo响应后关闭连接
                            if (count >= 3) {
                                log.info("步骤4: 已接收足够消息，准备关闭连接");
                                controller.close(1000, "测试完成");
                            }
                        } else {
                            log.debug("收到其他消息（已过滤）: {}", text);
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
                        log.info("步骤5: WebSocket连接已关闭: code={}, reason={}", code, reason);
                    }
                });

        // 2. 等待连接建立
        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "连接应该在5秒内建立");
        assertFalse(controller.isCompleted(), "连接建立后应该未结束");

        // 3. 发送消息
        log.info("步骤2: 开始发送消息");
        for (int i = 1; i <= 3; i++) {
            controller.send("{\"type\":\"echo\",\"message\":\"test" + i + "\"}");
            Thread.sleep(200);
        }

        // 4. 等待连接结束
        log.info("步骤6: 开始等待连接结束");
        boolean completed = controller.await(30, TimeUnit.SECONDS);

        // 5. 验证结果
        assertTrue(completed, "await() 应该返回 true");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(messageCount.get() >= 3, "应该接收到至少3条消息");

        log.info("=== WebSocket完整流程测试完成 ===");
        log.info("最终统计 - 接收消息数: {}, 连接状态: {}", 
                messageCount.get(), controller.getConnectionState());
    }
}

