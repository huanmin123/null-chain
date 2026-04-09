package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.jcommon.multithreading.context.AsyncTaskContext;
import com.gitee.huanminabc.nullchain.Null;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NullChainAsyncTracePropagationTest {
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
    public void testDefaultAsyncChainShouldPropagateTraceIdAndKeepParentStable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> mapObservedTid = new AtomicReference<>();
        AtomicReference<String> consumerObservedTid = new AtomicReference<>();
        AtomicReference<String> result = new AtomicReference<>();

        TEST_TID.set("parent-null-chain");
        Null.of("value")
                .async()
                .map(value -> {
                    mapObservedTid.set(TEST_TID.get());
                    TEST_TID.set("worker-mutated-map");
                    return value + "-mapped";
                })
                .ifPresent(value -> {
                    consumerObservedTid.set(TEST_TID.get());
                    result.set(value);
                    TEST_TID.set("worker-mutated-consumer");
                    latch.countDown();
                });

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS), "null-chain 默认 async 链未按预期执行");
        Assertions.assertEquals("parent-null-chain", mapObservedTid.get());
        Assertions.assertEquals("parent-null-chain", consumerObservedTid.get());
        Assertions.assertEquals("value-mapped", result.get());
        Assertions.assertEquals("parent-null-chain", TEST_TID.get());
    }

    private static void setTid(String tid) {
        if (tid == null) {
            TEST_TID.remove();
        } else {
            TEST_TID.set(tid);
        }
    }
}
