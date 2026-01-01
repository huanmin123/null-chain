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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE 高级功能测试
 * 
 * <p>测试 Last-Event-ID 支持、自动重连、连接状态管理等新功能。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
@Slf4j
public class SSEAdvancedTest extends SSEBaseTest {

    /**
     * 测试 Last-Event-ID 支持
     * 
     * <p>验证重连时是否正确发送 Last-Event-ID 请求头，服务器是否从正确位置继续发送。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testLastEventId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicReference<String> lastReceivedId = new AtomicReference<>();
        AtomicReference<SSEController> controllerRef = new AtomicReference<>();

        SSEController controller = Null.ofHttp(BASE_URL + "/sse-reconnect")
                .retryCount(0) // 不自动重连，手动测试
                .connectTimeout(10, TimeUnit.SECONDS)
                .get()
                .toSSEJson(new SSEEventListener<JSONObject>() {
                    @Override
                    public void onOpen() {
                        log.info("SSE连接已建立 - Last-Event-ID测试");
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        JSONObject data = msg.getData();
                        if (data != null) {
                            String id = msg.getId();
                            lastReceivedId.set(id);
                            log.info("接收到第{}条消息 - ID: {}, Data: {}", count, id, data);
                            
                            // 接收5条消息后终止
                            if (count >= 5) {
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
                        log.info("SSE连接正常结束");
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("SSE连接被用户终止");
                        latch.countDown();
                    }
                });
        
        controllerRef.set(controller);

        // 等待接收完成
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");
        
        // 验证Last-Event-ID已记录
        assertNotNull(controller.getLastEventId(), "Last-Event-ID应该已记录");
        assertEquals(lastReceivedId.get(), controller.getLastEventId(), "Last-Event-ID应该等于最后收到的ID");
        assertTrue(eventCount.get() >= 5, "应该接收到至少5条消息");
        
        log.info("Last-Event-ID测试完成 - 最后收到的ID: {}", controller.getLastEventId());
    }

    /**
     * 测试自动重连功能
     * 
     * <p>验证连接断开后是否自动重连，并使用 Last-Event-ID 实现断点续传。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAutoReconnect() throws InterruptedException {
        CountDownLatch reconnectLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicInteger reconnectCount = new AtomicInteger(0);
        AtomicReference<String> firstIdAfterReconnect = new AtomicReference<>();
        AtomicInteger stateChangeCount = new AtomicInteger(0);

        SSEController controller = Null.ofHttp(BASE_URL + "/sse-reconnect")
                .retryCount(3) // 允许重连3次
                .retryInterval(1000) // 重连间隔1秒
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
                        JSONObject data = msg.getData();
                        if (data != null) {
                            String id = msg.getId();
                            log.info("接收到第{}条消息 - ID: {}", count, id);
                            
                            // 记录重连后的第一个ID（假设重连后从第4条开始）
                            if (count > 3 && firstIdAfterReconnect.get() == null) {
                                firstIdAfterReconnect.set(id);
                                reconnectLatch.countDown();
                            }
                            
                            // 接收10条消息后终止
                            if (count >= 10) {
                                msg.terminate();
                            }
                        }
                    }

                    @Override
                    public void onStateChanged(SSEController controller, SSEConnectionState oldState, SSEConnectionState newState) {
                        stateChangeCount.incrementAndGet();
                        log.info("连接状态变化: {} -> {}", oldState, newState);
                        
                        if (newState == SSEConnectionState.RECONNECTING) {
                            reconnectCount.incrementAndGet();
                        }
                    }
                    
                    @Override
                    public void onNonSseResponse(String responseBody, String contentType) {
                        log.warn("收到非SSE响应");
                    }

                    @Override
                    public void onError(int attempt, int errorCode, String message, Throwable t) {
                        log.warn("SSE错误 (Attempt: {}): {}", attempt, message);
                        // 重连过程中的错误是正常的，不fail
                    }
                    
                    @Override
                    public void onComplete() {
                        log.info("SSE连接正常结束");
                        // 连接完成时触发latch
                        if (completeLatch.getCount() > 0) {
                            completeLatch.countDown();
                        }
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("SSE连接被用户终止");
                        // 连接被终止时也触发latch
                        if (completeLatch.getCount() > 0) {
                            completeLatch.countDown();
                        }
                    }
                });

        // 等待重连完成（接收第4条消息）
        assertTrue(reconnectLatch.await(30, TimeUnit.SECONDS), "应该在30秒内完成重连");
        
        // 等待连接完成（接收10条消息后终止）
        assertTrue(completeLatch.await(30, TimeUnit.SECONDS), "应该在30秒内接收完10条消息");
        
        // 验证重连功能
        assertNotNull(controller, "controller不应该为null");
        assertTrue(reconnectCount.get() > 0, "应该发生了重连");
        assertTrue(stateChangeCount.get() > 0, "应该有状态变化");
        assertTrue(eventCount.get() >= 10, "应该接收到至少10条消息（包括重连后的），当前: " + eventCount.get());
        
        log.info("自动重连测试完成 - 重连次数: {}, 总消息数: {}", reconnectCount.get(), eventCount.get());
    }

    /**
     * 测试连接状态管理
     * 
     * <p>验证连接状态是否正确变化，状态变化回调是否正确触发。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testConnectionState() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SSEConnectionState> connectedState = new AtomicReference<>();
        AtomicReference<SSEConnectionState> finalState = new AtomicReference<>();
        AtomicInteger stateChangeCount = new AtomicInteger(0);
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
                        // 接收3条消息后终止
                        int count = eventCount.incrementAndGet();
                        if (count >= 3) {
                            msg.terminate();
                        }
                    }

                    @Override
                    public void onStateChanged(SSEController controller, SSEConnectionState oldState, SSEConnectionState newState) {
                        stateChangeCount.incrementAndGet();
                        log.info("状态变化: {} -> {}", oldState, newState);
                        
                        if (newState == SSEConnectionState.CONNECTED) {
                            connectedState.set(newState);
                        }
                        if (newState == SSEConnectionState.CLOSED) {
                            finalState.set(newState);
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

        // 验证初始状态
        assertTrue(controller.getConnectionState() == SSEConnectionState.INITIAL || 
                   controller.getConnectionState() == SSEConnectionState.CONNECTING,
                   "初始状态应该是INITIAL或CONNECTING");

        // 等待连接关闭
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 验证状态变化
        assertNotNull(connectedState.get(), "应该到达CONNECTED状态");
        assertEquals(SSEConnectionState.CONNECTED, connectedState.get(), "应该到达CONNECTED状态");
        assertNotNull(finalState.get(), "应该到达CLOSED状态");
        assertEquals(SSEConnectionState.CLOSED, finalState.get(), "应该到达CLOSED状态");
        assertTrue(stateChangeCount.get() >= 2, "应该有至少2次状态变化");

        log.info("连接状态测试完成 - 状态变化次数: {}", stateChangeCount.get());
    }

    /**
     * 测试 SSEController 功能
     * 
     * <p>验证 SSEController 的各种功能，包括状态查询、关闭连接等。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testSSEController() throws InterruptedException {
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
                        // 验证连接状态
                        SSEController ctrl = controllerRef.get();
                        if (ctrl != null) {
                            assertTrue(ctrl.isOpen(), "连接应该已打开");
                            assertEquals(SSEConnectionState.CONNECTED, ctrl.getConnectionState(), "状态应该是CONNECTED");
                        }
                    }

                    @Override
                    public void onEvent(EventMessage<JSONObject> msg) {
                        int count = eventCount.incrementAndGet();
                        
                        // 验证Last-Event-ID更新
                        SSEController ctrl = controllerRef.get();
                        if (msg.getId() != null && ctrl != null) {
                            assertEquals(msg.getId(), ctrl.getLastEventId(), "Last-Event-ID应该已更新");
                        }
                        
                        // 接收5条消息后手动关闭
                        if (count >= 5 && ctrl != null) {
                            ctrl.close();
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onStateChanged(SSEController controller, SSEConnectionState oldState, SSEConnectionState newState) {
                        log.info("状态变化: {} -> {}", oldState, newState);
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

        // 等待关闭完成
        assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");

        // 验证关闭后的状态
        assertFalse(controller.isOpen(), "连接应该已关闭");
        assertEquals(SSEConnectionState.CLOSED, controller.getConnectionState(), "状态应该是CLOSED");
        assertTrue(controller.isDestroyed(), "控制器应该已销毁");
        assertTrue(eventCount.get() >= 5, "应该接收到至少5条消息");

        log.info("SSEController测试完成 - 接收消息数: {}", eventCount.get());
    }

    /**
     * 测试 try-with-resources 自动关闭
     * 
     * <p>验证使用 try-with-resources 时连接是否正确关闭。</p>
     */
    @Test
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testAutoClose() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicReference<SSEConnectionState> finalState = new AtomicReference<>();

        try (SSEController controller = Null.ofHttp(BASE_URL + "/sse")
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
                    public void onStateChanged(SSEController controller, SSEConnectionState oldState, SSEConnectionState newState) {
                        if (newState == SSEConnectionState.CLOSED) {
                            finalState.set(newState);
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onInterrupt() {
                        log.info("SSE连接被用户终止");
                    }
                })) {
            
            // 等待接收消息
            assertTrue(latch.await(15, TimeUnit.SECONDS), "应该在15秒内完成");
            
            // 验证状态
            assertEquals(SSEConnectionState.CLOSED, finalState.get(), "状态应该是CLOSED");
            assertTrue(eventCount.get() >= 3, "应该接收到至少3条消息");
        } // 自动调用 close()

        log.info("try-with-resources测试完成");
    }
}
