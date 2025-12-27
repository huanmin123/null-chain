package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE (Server-Sent Events) 测试Demo
 * 
 * <p>测试两个公开的SSE接口：
 * <ul>
 *   <li>https://echo.websocket.org/.sse - 返回普通文本的data</li>
 *   <li>https://stream.wikimedia.org/v2/stream/recentchange - 返回JSON格式的data</li>
 * </ul>
 * </p>
 * 
 * @author huanmin
 * @since 1.0.0
 */
@Slf4j
public class SSETestDemo {

    /**
     * 测试普通文本SSE接口
     * 
     * <p>测试 https://echo.websocket.org/.sse 接口，该接口返回普通文本数据</p>
     */
    @Test
    public void testTextSSE() throws InterruptedException {
        // 使用CountDownLatch来等待一定数量的消息接收
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);

        // 创建SSE事件监听器（使用字符串解码器）
        SSEEventListener<String> listener = new SSEEventListener<String>() {
            @Override
            public void onOpen() {
                openCount.incrementAndGet();
                log.info("SSE连接已建立 - 文本接口");
            }

            @Override
            public void onEvent(EventMessage<String> msg) {
                int count = eventCount.incrementAndGet();
                String data = msg.getData();
                String dataRaw = msg.getDataRaw();
                String id = msg.getId();
                String event = msg.getEvent();
                
                log.info("接收到第{}条SSE消息 - ID: {}, Event: {}, DataRaw: {}, Data: {}", 
                        count, id, event, dataRaw, data);
                
                // 验证数据不为空
                assertNotNull(dataRaw, "原始数据不应该为空");
                assertNotNull(data, "解码后的数据不应该为空");
                
                // 每接收5条消息就减少latch计数
                if (count <= 5) {
                    latch.countDown();
                }
            }

            @Override
            public void onNonSseResponse(String responseBody, String contentType) {
                log.warn("收到非SSE响应 - ContentType: {}, Body: {}", contentType, responseBody);
                fail("不应该收到非SSE响应");
            }

            @Override
            public void onError(int attempt, Integer httpCode, String message, Throwable t) {
                log.error("SSE错误 - Attempt: {}, HttpCode: {}, Message: {}", attempt, httpCode, message, t);
                fail("不应该发生错误: " + message);
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
                log.info("SSE连接已关闭 - 文本接口");
            }
        };

        // 执行SSE请求（使用字符串解码器）
        Null.ofHttp("https://echo.websocket.org/.sse")
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .async()
                .get()
                .toSSEText(listener);

        // 等待接收至少5条消息，最多等待30秒
        boolean received = latch.await(30, TimeUnit.SECONDS);
        
        // 验证结果
        assertTrue(received, "应该在30秒内接收到至少5条消息");
        assertEquals(1, openCount.get(), "onOpen应该只被调用一次");
        assertTrue(eventCount.get() >= 5, "应该接收到至少5条消息");
//        assertEquals(1, completeCount.get(), "onComplete应该只被调用一次");
        
        log.info("文本SSE测试完成 - 共接收{}条消息", eventCount.get());
    }

    /**
     * 测试JSON格式SSE接口（带终止条件） node sse-server.js  {@see  null-chain-test/src/test/resources/sse-server.js}
     * 在接收到3条消息后主动终止流，验证终止功能是否正常工作。</p>
     */
//    @Test
    public void testJsonSSE() throws InterruptedException {
        // 使用CountDownLatch来等待终止完成
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger interruptCount = new AtomicInteger(0);

        // 创建SSE事件监听器（使用JSON解码器）
        SSEEventListener<JSONObject> listener = new SSEEventListener<JSONObject>() {
            @Override
            public void onOpen() {
                openCount.incrementAndGet();
                log.info("SSE连接已建立 - JSON接口");
            }

            @Override
            public void onEvent(EventMessage<JSONObject> msg) {
                int count = eventCount.incrementAndGet();
                JSONObject data = msg.getData();
                if (data == null) {
                     return;
                }
                String dataRaw = msg.getDataRaw();
                String id = msg.getId();
                String event = msg.getEvent();
                
                log.info("接收到第{}条SSE消息 - ID: {}, Event: {}, DataRaw: {}", count, id, event, dataRaw);
                // 接收到3条消息后主动终止流
                if (count >= 3) {
                    log.info("已接收到{}条消息，主动终止SSE流", count);
                    msg.terminate();
                }
            }

            @Override
            public void onNonSseResponse(String responseBody, String contentType) {
                log.warn("收到非SSE响应 - ContentType: {}, Body: {}", contentType, responseBody);
                fail("不应该收到非SSE响应");
            }

            @Override
            public void onError(int attempt, Integer httpCode, String message, Throwable t) {
                log.error("SSE错误 - Attempt: {}, HttpCode: {}, Message: {}", attempt, httpCode, message, t);
                // Wikipedia的接口可能会因为网络问题失败，这里只记录错误，不直接fail
                if (httpCode != null && httpCode >= 400) {
                    log.warn("HTTP错误，但继续测试: {}", message);
                } else {
                    fail("不应该发生网络错误: " + message);
                }
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
                log.info("SSE连接已关闭 - JSON接口（正常结束）");
            }

            @Override
            public void onInterrupt() {
                interruptCount.incrementAndGet();
                log.info("SSE流已被用户主动终止 - JSON接口");
                latch.countDown();
            }
        };

        // 执行SSE请求（使用JSON解码器）
        // 注意：Wikipedia API 要求设置 User-Agent 并遵守机器人政策
        Null.ofHttp("http://localhost:3000/sse")
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .get()
                .toSSEJson(listener);

        // 等待终止完成，最多等待30秒
        boolean interrupted = latch.await(30, TimeUnit.SECONDS);
        
        // 验证结果
        assertTrue(interrupted, "应该在30秒内完成终止");
        assertEquals(1, openCount.get(), "onOpen应该只被调用一次");
        assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");
        assertEquals(1, interruptCount.get(), "onInterrupt应该只被调用一次（用户主动终止）");
        assertEquals(0, completeCount.get(), "onComplete不应该被调用（因为是被中断的，不是自然结束）");
        
        log.info("JSON SSE测试完成 - 共接收{}条消息，已主动终止", eventCount.get());
    }

    /**
     * 测试SSE连接终止功能
     * 
     * <p>测试通过 EventMessage.terminate() 方法主动终止SSE连接。
     * 验证终止后是否正确触发 onInterrupt() 回调，而不是 onComplete()。</p>
     */
    @Test
    public void testSSETermination() throws InterruptedException {
        // 使用CountDownLatch来等待终止完成
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger interruptCount = new AtomicInteger(0);

        // 创建SSE事件监听器，在接收到第3条消息时通过 msg.terminate() 终止连接
        SSEEventListener<String> listener = new SSEEventListener<String>() {
            @Override
            public void onOpen() {
                openCount.incrementAndGet();
                log.info("SSE连接已建立 - 终止测试");
            }

            @Override
            public void onEvent(EventMessage<String> msg) {
                int count = eventCount.incrementAndGet();
                String data = msg.getData();
                if (data == null) {
                    return;
                }
                String dataRaw = msg.getDataRaw();
                log.info("接收到第{}条SSE消息 - DataRaw: {}, Data: {}", count, dataRaw, data);
                // 接收到第3条消息后主动终止流
                if (count >= 3) {
                    log.info("已接收到{}条消息，通过 msg.terminate() 主动终止SSE流", count);
                    msg.terminate();
                }
            }

            @Override
            public void onNonSseResponse(String responseBody, String contentType) {
                log.warn("收到非SSE响应 - ContentType: {}, Body: {}", contentType, responseBody);
                fail("不应该收到非SSE响应");
            }

            @Override
            public void onError(int attempt, Integer httpCode, String message, Throwable t) {
                log.error("SSE错误 - Attempt: {}, HttpCode: {}, Message: {}", attempt, httpCode, message, t);
                fail("不应该发生错误: " + message);
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
                log.info("SSE连接已关闭 - 终止测试（正常结束）");
            }

            @Override
            public void onInterrupt() {
                interruptCount.incrementAndGet();
                log.info("SSE流已被用户主动终止 - 终止测试");
                latch.countDown();
            }
        };

        // 执行SSE请求
        Null.ofHttp("https://echo.websocket.org/.sse")
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .get()
                .toSSEText(listener);

        // 等待终止完成，最多等待30秒
        boolean interrupted = latch.await(30, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(interrupted, "应该在30秒内完成终止");
        assertEquals(1, openCount.get(), "onOpen应该只被调用一次");
        assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");
        assertEquals(1, interruptCount.get(), "onInterrupt应该只被调用一次（用户主动终止）");
        assertEquals(0, completeCount.get(), "onComplete不应该被调用（因为是被中断的，不是自然结束）");
        
        log.info("SSE终止测试完成 - 共接收{}条消息，已主动终止", eventCount.get());
    }
}

