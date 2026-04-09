package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.jcommon.multithreading.context.AsyncTaskContext;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LongLivedAsyncTracePropagationTest {
    private static final ThreadLocal<String> TEST_TID = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        AsyncTaskContext.setDecorator(task -> {
            final String parentTid = TEST_TID.get();
            return () -> {
                String previousTid = TEST_TID.get();
                setTid(parentTid);
                try {
                    task.run();
                } finally {
                    setTid(previousTid);
                }
            };
        });
        AsyncTaskContext.setTraceIdAccessor(TEST_TID::get);
        TEST_TID.remove();
    }

    @AfterEach
    public void tearDown() {
        AsyncTaskContext.resetDecorator();
        AsyncTaskContext.resetTraceIdAccessor();
        TEST_TID.remove();
    }

    @Test
    public void testSseControllerReconnectShouldKeepCapturedTraceId() throws InterruptedException {
        SSEController controller = new SSEController();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> observedTid = new AtomicReference<>();

        TEST_TID.set("parent-sse-trace");
        controller.setMaxReconnectCount(1);
        controller.setReconnectInterval(1);
        controller.setReconnectTrigger(() -> {
            observedTid.set(TEST_TID.get());
            TEST_TID.set("worker-mutated-sse");
            latch.countDown();
        });

        TEST_TID.remove();
        Assertions.assertTrue(controller.triggerReconnect("unit-test"), "SSE 重连应被成功触发");
        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS), "SSE 重连任务未按预期执行");
        Assertions.assertEquals("parent-sse-trace", observedTid.get());
        Assertions.assertNull(TEST_TID.get(), "父线程在移除 trace 后不应被 SSE 重连任务污染");
        controller.close("test-close");
    }

    @Test
    public void testWebSocketHeartbeatShouldKeepCapturedTraceId() throws InterruptedException {
        WebSocketController controller = new WebSocketController();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> observedTid = new AtomicReference<>();

        TEST_TID.set("parent-websocket-trace");
        controller.bindAsyncContextSnapshot(AsyncTaskContext.capture());
        controller.setWebSocket(new RecordingWebSocket(latch, observedTid));
        controller.setOpen(true);

        try {
            controller.startHeartbeat(new WebSocketHeartbeatHandler() {
                @Override
                public String generateHeartbeat() {
                    return "ping";
                }

                @Override
                public boolean isHeartbeatResponse(String text) {
                    return false;
                }
            }, 10, 1000);

            TEST_TID.remove();
            Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS), "WebSocket 心跳任务未按预期执行");
            Assertions.assertEquals("parent-websocket-trace", observedTid.get());
            Assertions.assertNull(TEST_TID.get(), "父线程在移除 trace 后不应被心跳任务污染");
        } finally {
            controller.stopHeartbeat();
            controller.close();
        }
    }

    private static void setTid(String tid) {
        if (tid == null) {
            TEST_TID.remove();
        } else {
            TEST_TID.set(tid);
        }
    }

    private static final class RecordingWebSocket implements WebSocket {
        private final CountDownLatch latch;
        private final AtomicReference<String> observedTid;
        private final Request request = new Request.Builder().url("ws://localhost/test").build();

        private RecordingWebSocket(CountDownLatch latch, AtomicReference<String> observedTid) {
            this.latch = latch;
            this.observedTid = observedTid;
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public long queueSize() {
            return 0;
        }

        @Override
        public boolean send(String text) {
            observedTid.compareAndSet(null, TEST_TID.get());
            TEST_TID.set("worker-mutated-websocket-heartbeat");
            latch.countDown();
            return true;
        }

        @Override
        public boolean send(ByteString bytes) {
            observedTid.compareAndSet(null, TEST_TID.get());
            TEST_TID.set("worker-mutated-websocket-heartbeat-bytes");
            latch.countDown();
            return true;
        }

        @Override
        public boolean close(int code, String reason) {
            return true;
        }

        @Override
        public void cancel() {
        }
    }
}
