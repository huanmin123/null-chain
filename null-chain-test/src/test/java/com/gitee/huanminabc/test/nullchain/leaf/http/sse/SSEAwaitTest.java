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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE 等待功能测试
 * 
 * <p>测试 SSEController 的 await() 和 isCompleted() 方法。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEAwaitTest extends SSEBaseTest {

    /**
     * 测试 await() 方法 - 正常结束场景
     * 
     * <p>验证连接正常结束时，await() 方法能够正确等待并返回。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitNormalCompletion() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicBoolean awaitCompleted = new AtomicBoolean(false);
        AtomicReference<SSEController> controllerRef = new AtomicReference<>();

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
                        // 接收3条消息后终止
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
                    }
                });

        controllerRef.set(controller);

        // 在后台线程中等待连接结束
        Thread awaitThread = new Thread(() -> {
            try {
                controller.await();
                awaitCompleted.set(true);
                log.info("await() 方法已返回，连接已结束");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待线程被中断", e);
            }
        });
        awaitThread.start();

        // 等待连接结束（最多等待15秒）
        awaitThread.join(15000);

        // 验证结果
        assertTrue(awaitCompleted.get(), "await() 方法应该在连接结束后返回");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");

        log.info("await() 正常结束测试完成 - 接收消息数: {}", eventCount.get());
    }

    /**
     * 测试 await() 方法 - 连接已结束场景
     * 
     * <p>验证当连接已经结束时，await() 方法应该立即返回。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitAlreadyCompleted() throws InterruptedException {
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

        // 验证连接已结束
        assertTrue(controller.isCompleted(), "连接应该已结束");

        // 此时调用 await() 应该立即返回
        long startTime = System.currentTimeMillis();
        controller.await();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证立即返回（应该在100毫秒内返回）
        assertTrue(duration < 100, "await() 应该立即返回，耗时: " + duration + "ms");
        assertTrue(controller.isCompleted(), "连接应该仍然处于结束状态");

        log.info("await() 已结束场景测试完成 - 等待耗时: {}ms", duration);
    }

    /**
     * 测试 await(timeout, unit) 方法 - 正常结束场景
     * 
     * <p>验证带超时的 await() 方法在连接正常结束时能够正确返回 true。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitWithTimeoutNormalCompletion() throws InterruptedException {
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
                    }
                });

        // 使用带超时的 await() 方法等待连接结束
        boolean completed = controller.await(30, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "await() 应该返回 true（连接已结束）");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");

        log.info("await(timeout) 正常结束测试完成 - 接收消息数: {}", eventCount.get());
    }

    /**
     * 测试 await(timeout, unit) 方法 - 超时场景
     * 
     * <p>验证当连接未在超时时间内结束时，await() 方法应该返回 false。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitWithTimeoutExpired() throws InterruptedException {
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
                        eventCount.incrementAndGet();
                        // 不终止，让连接持续运行
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

        // 使用很短的超时时间（500毫秒）等待连接结束
        long startTime = System.currentTimeMillis();
        boolean completed = controller.await(500, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证结果
        assertFalse(completed, "await() 应该返回 false（超时）");
        assertFalse(controller.isCompleted(), "连接应该还未结束");
        assertTrue(duration >= 450 && duration < 1000, "应该在大约500毫秒后返回，实际耗时: " + duration + "ms");

        // 清理：关闭连接
        controller.close();

        log.info("await(timeout) 超时测试完成 - 等待耗时: {}ms", duration);
    }

    /**
     * 测试 await(timeout, unit) 方法 - 连接已结束场景
     * 
     * <p>验证当连接已经结束时，带超时的 await() 方法应该立即返回 true。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitWithTimeoutAlreadyCompleted() throws InterruptedException {
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

        // 验证连接已结束
        assertTrue(controller.isCompleted(), "连接应该已结束");

        // 此时调用带超时的 await() 应该立即返回 true
        long startTime = System.currentTimeMillis();
        boolean completed = controller.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证立即返回
        assertTrue(completed, "await() 应该返回 true");
        assertTrue(duration < 100, "await() 应该立即返回，耗时: " + duration + "ms");

        log.info("await(timeout) 已结束场景测试完成 - 等待耗时: {}ms", duration);
    }

    /**
     * 测试 isCompleted() 方法
     * 
     * <p>验证 isCompleted() 方法能够正确反映连接状态。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testIsCompleted() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicReference<SSEController> controllerRef = new AtomicReference<>();

        SSEController controller = Null.ofHttp(BASE_URL + "/sse")
                .retryCount(0)
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立");
                        // 连接刚建立时，应该未结束
                        SSEController ctrl = controllerRef.get();
                        if (ctrl != null) {
                            assertFalse(ctrl.isCompleted(), "连接刚建立时应该未结束");
                        }
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

        controllerRef.set(controller);

        // 连接建立后，应该未结束
        assertFalse(controller.isCompleted(), "连接建立后应该未结束");

        // 等待连接结束
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 连接结束后，应该已结束
        assertTrue(controller.isCompleted(), "连接结束后应该已结束");
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");

        log.info("isCompleted() 测试完成 - 接收消息数: {}", eventCount.get());
    }

    /**
     * 测试 await() 方法 - 失败状态场景
     * 
     * <p>验证当连接失败时，await() 方法能够正确等待并返回。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAwaitFailedState() throws InterruptedException {
        AtomicBoolean awaitCompleted = new AtomicBoolean(false);

        // 使用一个不存在的URL，导致连接失败
        SSEController controller = Null.ofHttp(BASE_URL + "/nonexistent")
                .retryCount(0) // 不重试，直接失败
                .connectTimeout(5, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        // 不会收到消息
                    }

                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.warn("SSE错误 (这是预期的): {}", message);
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

        // 在后台线程中等待连接结束
        Thread awaitThread = new Thread(() -> {
            try {
                controller.await();
                awaitCompleted.set(true);
                log.info("await() 方法已返回，连接已结束（失败）");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待线程被中断", e);
            }
        });
        awaitThread.start();

        // 等待连接结束（最多等待10秒）
        awaitThread.join(10000);

        // 验证结果
        assertTrue(awaitCompleted.get(), "await() 方法应该在连接结束后返回");
        assertTrue(controller.isCompleted(), "连接应该已结束");
        assertTrue(controller.getConnectionState() == SSEConnectionState.FAILED 
                || controller.getConnectionState() == SSEConnectionState.CLOSED, 
                "状态应该是FAILED或CLOSED");

        log.info("await() 失败状态测试完成 - 最终状态: {}", controller.getConnectionState());
    }

    /**
     * 测试多次调用 await() 方法
     * 
     * <p>验证多次调用 await() 方法都是安全的，且都能正确返回。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testMultipleAwaitCalls() throws InterruptedException {
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

        // 多次调用 await() 方法，都应该立即返回
        long startTime = System.currentTimeMillis();
        controller.await();
        controller.await();
        controller.await(1, TimeUnit.SECONDS);
        controller.await(1, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证所有调用都立即返回
        assertTrue(duration < 100, "多次调用 await() 都应该立即返回，总耗时: " + duration + "ms");
        assertTrue(controller.isCompleted(), "连接应该已结束");

        log.info("多次调用 await() 测试完成 - 总耗时: {}ms", duration);
    }
}

