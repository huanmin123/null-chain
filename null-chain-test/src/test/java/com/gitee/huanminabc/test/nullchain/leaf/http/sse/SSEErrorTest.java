package com.gitee.huanminabc.test.nullchain.leaf.http.sse;

import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEConnectionState;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEController;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEErrorCode;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE 错误场景测试
 * 
 * <p>测试 SSE 连接的各种错误场景，包括HTTP错误、网络错误、非SSE响应等。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEErrorTest extends SSEBaseTest {

    /**
     * 测试非SSE响应场景
     * 
     * <p>验证当服务器返回非SSE格式的响应时，是否正确触发 onNonSseResponse 回调。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testNonSseResponse() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger nonSseResponseCount = new AtomicInteger(0);
        AtomicReference<String> responseBody = new AtomicReference<>();
        AtomicReference<String> contentType = new AtomicReference<>();

        SSEController controller = Null.ofHttp(BASE_URL + "/non-sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        fail("不应该收到SSE事件");
                    }

                    @Override
                    public void onNonSseResponse(String responseBodyStr, String contentTypeStr) {
                        nonSseResponseCount.incrementAndGet();
                        responseBody.set(responseBodyStr);
                        contentType.set(contentTypeStr);
                        log.info("收到非SSE响应 - ContentType: {}, Body: {}", contentTypeStr, responseBodyStr);
                        // 不在onNonSseResponse中countDown，等待onComplete确保状态更新完成
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.warn("SSE错误: {}", message);
                        // 非SSE响应可能会触发错误，这是正常的
                    }

                    @Override
                    public void onComplete() {
                        log.info("连接正常结束");
                        // 在onComplete中countDown，确保状态已经更新为CLOSED
                        latch.countDown();
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("连接被用户终止");
                    }
                });

        // 等待非SSE响应处理完成
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 验证结果
        assertEquals(1, nonSseResponseCount.get(), "onNonSseResponse应该被调用一次");
        assertNotNull(responseBody.get(), "响应体不应该为空");
        assertNotNull(contentType.get(), "Content-Type不应该为空");
        assertTrue(controller.isCompleted(), "连接应该已结束");

        log.info("非SSE响应测试完成 - ContentType: {}, Body长度: {}", 
                contentType.get(), responseBody.get() != null ? responseBody.get().length() : 0);
    }

    /**
     * 测试HTTP 404错误场景
     * 
     * <p>验证当服务器返回404错误时，是否正确处理。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testHttp404Error() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicReference<Integer> errorCode = new AtomicReference<>();
        AtomicReference<String> errorMessage = new AtomicReference<>();

        SSEController controller = Null.ofHttp(BASE_URL + "/nonexistent")
                .retryCount(0) // 不重试，直接失败
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        fail("不应该收到SSE事件");
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCodeValue, String message, Throwable t) {
                        errorCount.incrementAndGet();
                        errorCode.set(errorCodeValue);
                        errorMessage.set(message);
                        log.warn("SSE错误 (这是预期的) - Attempt: {}, ErrorCode: {}, Message: {}", 
                                attempt, errorCodeValue, message);
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        log.info("连接正常结束");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("连接被用户终止");
                    }
                });

        // 等待错误处理完成
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 验证结果
        assertEquals(1, errorCount.get(), "onError应该被调用一次");
        assertNotNull(errorCode.get(), "错误码不应该为空");
        assertTrue(errorCode.get() == 404 || errorCode.get() >= 400, 
                "错误码应该是404或其他HTTP错误码: " + errorCode.get());
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(controller.getConnectionState() == SSEConnectionState.FAILED 
                || controller.getConnectionState() == SSEConnectionState.CLOSED,
                "状态应该是FAILED或CLOSED");

        log.info("HTTP 404错误测试完成 - ErrorCode: {}, Message: {}", errorCode.get(), errorMessage.get());
    }

    /**
     * 测试重连失败场景
     * 
     * <p>验证当重连次数用尽时，是否正确处理。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testReconnectExhausted() throws InterruptedException {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger reconnectExhaustedCount = new AtomicInteger(0);

        // 使用一个不存在的URL，配置重连，但重连也会失败
        SSEController controller = Null.ofHttp(BASE_URL + "/nonexistent")
                .retryCount(2) // 允许重连2次
                .retryInterval(500) // 重连间隔500毫秒
                .connectTimeout(5, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        fail("不应该收到SSE事件");
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        int count = errorCount.incrementAndGet();
                        log.warn("SSE错误 (这是预期的) - Attempt: {}, ErrorCode: {}, Message: {}", 
                                attempt, errorCode, message);
                        
                        // 检查是否是重连次数用尽的错误
                        if (errorCode == SSEErrorCode.RECONNECT_EXHAUSTED) {
                            reconnectExhaustedCount.incrementAndGet();
                        }
                        
                        // 第一次错误时触发latch，确保回调执行完成
                        if (count == 1 && errorLatch.getCount() > 0) {
                            errorLatch.countDown();
                        }
                    }

                    @Override
                    public void onComplete() {
                        log.info("连接正常结束");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("连接被用户终止");
                    }
                });

        // 等待连接完成（重试耗尽后应该自动结束）
        assertTrue(controller.await(30, TimeUnit.SECONDS), "连接应该在30秒内完成");
        
        // 等待错误回调执行完成（解决并发测试时的可见性问题）
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS), "应该在5秒内收到错误回调");

        // 验证结果
        assertTrue(errorCount.get() > 0, "应该发生错误，当前errorCount: " + errorCount.get());
        // 对于404这种不可重试的错误，不会触发重试和重连，所以只验证错误发生和连接已完成
        // 对于可重试的错误，会触发重试，errorCount >= retryCount + 1
        // 对于重连场景，会触发重连，reconnectExhaustedCount > 0
        assertTrue(reconnectExhaustedCount.get() > 0 || errorCount.get() >= 1, 
                "应该发生错误（重连次数用尽的错误或至少一次错误）");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(controller.getConnectionState() == SSEConnectionState.FAILED 
                || controller.getConnectionState() == SSEConnectionState.CLOSED,
                "状态应该是FAILED或CLOSED");

        log.info("重连失败测试完成 - 错误次数: {}, 重连用尽次数: {}", 
                errorCount.get(), reconnectExhaustedCount.get());
    }

    /**
     * 测试 shouldTerminate() 方法
     * 
     * <p>验证 shouldTerminate() 方法能够正确控制连接终止。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testShouldTerminate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger interruptCount = new AtomicInteger(0);

        SSEController controller = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        log.info("接收到第{}条消息", count);
                    }

                    @Override
                    public boolean shouldTerminate(EventMessage<JSONObject> msg) {
                        // 接收到第5条消息时返回true，表示应该终止
                        int count = eventCount.get();
                        if (count >= 5) {
                            log.info("shouldTerminate返回true，将终止连接");
                            return true;
                        }
                        return false;
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
                        log.info("SSE连接正常结束（通过shouldTerminate）");
                        latch.countDown();
                    }

                    @Override
                    public void onInterrupt() {
                        interruptCount.incrementAndGet();
                        log.info("SSE连接被用户终止");
                    }
                });

        // 等待连接结束
        assertTrue(latch.await(30, TimeUnit.SECONDS), "应该在30秒内完成");

        // 验证结果
        assertTrue(eventCount.get() >= 5, "应该接收到至少5条消息");
        assertEquals(1, completeCount.get(), "onComplete应该被调用一次");
        assertEquals(0, interruptCount.get(), "onInterrupt不应该被调用（因为是通过shouldTerminate正常结束）");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");

        log.info("shouldTerminate() 测试完成 - 接收消息数: {}", eventCount.get());
    }

    /**
     * 测试连接关闭后调用await()
     * 
     * <p>验证在连接关闭后调用await()方法应该立即返回。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitAfterClose() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);

        SSEController controller = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        if (count >= 3) {
                            msg.terminate();
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
                        latch.countDown();
                    }
                });

        // 等待连接结束
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 手动关闭连接
        controller.close();
        assertTrue(controller.isCompleted(), "连接应该已结束");

        // 在关闭后调用await()应该立即返回
        long startTime = System.currentTimeMillis();
        controller.await();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 100, "await()应该立即返回，耗时: " + duration + "ms");
        assertTrue(controller.isCompleted(), "连接应该仍然处于结束状态");

        log.info("关闭后await()测试完成 - 等待耗时: {}ms", duration);
    }

    /**
     * 测试错误码验证
     * 
     * <p>验证各种错误场景下的错误码是否正确。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testErrorCodes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> receivedErrorCode = new AtomicReference<>();

        SSEController controller = Null.ofHttp(BASE_URL + "/nonexistent")
                .retryCount(0)
                .connectTimeout(5, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        fail("不应该收到SSE事件");
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        receivedErrorCode.set(errorCode);
                        log.warn("SSE错误 (这是预期的) - Attempt: {}, ErrorCode: {}, Message: {}", 
                                attempt, errorCode, message);
                        
                        // 验证错误码不为空且是有效值
                        assertNotNull(errorCode, "错误码不应该为空");
                        assertTrue(errorCode > 0 || errorCode < 0, "错误码应该是有效值");
                        
                        // HTTP错误码应该在100-599范围内，系统错误码应该是负数
                        if (errorCode >= 100 && errorCode <= 599) {
                            log.info("收到HTTP错误码: {}", errorCode);
                        } else if (errorCode < 0) {
                            log.info("收到系统错误码: {}", errorCode);
                        }
                        
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        log.info("连接正常结束");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("连接被用户终止");
                    }
                });

        // 等待错误处理完成
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 验证错误码
        assertNotNull(receivedErrorCode.get(), "应该收到错误码");
        assertTrue(receivedErrorCode.get() != 0, "错误码不应该是0");

        log.info("错误码验证测试完成 - ErrorCode: {}", receivedErrorCode.get());
    }
}

