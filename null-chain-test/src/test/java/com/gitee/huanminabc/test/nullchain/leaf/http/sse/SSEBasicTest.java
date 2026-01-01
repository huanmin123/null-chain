package com.gitee.huanminabc.test.nullchain.leaf.http.sse;

import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEConnectionState;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEController;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE 基础功能测试
 * 
 * <p>测试 SSE 连接、消息接收、终止等基础功能。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEBasicTest extends SSEBaseTest {

    /**
     * 测试基础 SSE 连接和消息接收
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testBasicSSE() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);

        SSEController controller = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        openCount.incrementAndGet();
                        log.info("SSE连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        JSONObject data = msg.getData();
                        if (data != null) {
                            log.info("接收到第{}条消息 - ID: {}, Event: {}, Data: {}", 
                                    count, msg.getId(), msg.getEvent(), data);
                            
                            // 验证数据不为空
                            assertNotNull(data, "数据不应该为空");
                            assertNotNull(msg.getDataRaw(), "原始数据不应该为空");
                            
                            // 每接收一条消息就减少latch计数
                            if (count <= 5) {
                                latch.countDown();
                            }
                        }
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应 - ContentType: {}, Body: {}", contentType, responseBody);
                        fail("不应该收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.error("SSE错误 - Attempt: {}, ErrorCode: {}, Message: {}", attempt, errorCode, message, t);
                        fail("不应该发生错误: " + message);
                    }

                    @Override
                    public void onComplete() {
                        completeCount.incrementAndGet();
                        log.info("SSE连接已关闭");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("SSE连接被用户终止");
                    }
                });

        // 验证controller不为null
        assertNotNull(controller, "SSEController不应该为null");
        
        // 等待接收至少5条消息，最多等待30秒
        boolean received = latch.await(30, TimeUnit.SECONDS);
        
        // 验证结果
        assertTrue(received, "应该在30秒内接收到至少5条消息");
        assertEquals(1, openCount.get(), "onOpen应该只被调用一次");
        assertTrue(eventCount.get() >= 5, "应该接收到至少5条消息");
        
        // 手动关闭连接
        controller.close();
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");
        
        log.info("基础SSE测试完成 - 共接收{}条消息", eventCount.get());
    }

    /**
     * 测试 SSE 连接终止功能
     * 
     * <p>测试通过 EventMessage.terminate() 方法主动终止SSE连接。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSSETermination() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger openCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger interruptCount = new AtomicInteger(0);

        SSEController controller = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        openCount.incrementAndGet();
                        log.info("SSE连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        JSONObject data = msg.getData();
                        if (data != null) {
                            log.info("接收到第{}条消息 - ID: {}", count, msg.getId());
                            
                            // 接收到第3条消息后主动终止流
                            if (count >= 3) {
                                log.info("已接收到{}条消息，通过 msg.terminate() 主动终止SSE流", count);
                                msg.terminate();
                            }
                        }
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                        fail("不应该收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.error("SSE错误: {}", message, t);
                        fail("不应该发生错误: " + message);
                    }

                    @Override
                    public void onComplete() {
                        completeCount.incrementAndGet();
                        log.info("SSE连接已关闭（正常结束）");
                    }

                    @Override
                    public void onInterrupt() {
                        interruptCount.incrementAndGet();
                        log.info("SSE流已被用户主动终止");
                        latch.countDown();
                    }
                });

        // 等待终止完成，最多等待30秒
        boolean interrupted = latch.await(30, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(interrupted, "应该在30秒内完成终止");
        assertEquals(1, openCount.get(), "onOpen应该只被调用一次");
        assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");
        assertEquals(1, interruptCount.get(), "onInterrupt应该只被调用一次（用户主动终止）");
        assertEquals(0, completeCount.get(), "onComplete不应该被调用（因为是被中断的，不是自然结束）");
        
        // 验证连接状态
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");
        
        log.info("SSE终止测试完成 - 共接收{}条消息，已主动终止", eventCount.get());
    }

    /**
     * 测试 SSEController 返回值
     * 
     * <p>验证 toSSE 方法是否正确返回 SSEController。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSSEControllerReturn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);

        // 测试 toSSEText
        SSEController controller1 = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEText(new SSEEventListener<String>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立 - toSSEText");
                    }

                    @Override
                    public void onEvent(EventMessage<String> msg) {
                        int count = eventCount.incrementAndGet();
                        if (count >= 3) {
                            msg.terminate();
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.error("SSE错误: {}", message, t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("SSE连接正常结束");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("SSE连接被用户终止");
                    }
                });

        assertNotNull(controller1, "toSSEText应该返回SSEController");
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");
        
        log.info("SSEController返回值测试完成");
    }
}

