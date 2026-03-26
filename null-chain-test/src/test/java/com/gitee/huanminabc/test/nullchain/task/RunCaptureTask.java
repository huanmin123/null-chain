package com.gitee.huanminabc.test.nullchain.task;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.task.NullTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RunCaptureTask implements NullTask<Object, String> {
    private static final AtomicInteger RUN_COUNT = new AtomicInteger();
    private static volatile List<Object> lastParams = Collections.emptyList();

    public static void reset() {
        RUN_COUNT.set(0);
        lastParams = Collections.emptyList();
    }

    public static int getRunCount() {
        return RUN_COUNT.get();
    }

    public static List<Object> getLastParams() {
        return lastParams;
    }

    @Override
    public String run(Object preValue, NullChain<?>[] params, Map<String, Object> context) {
        List<Object> captured = new ArrayList<>(params.length);
        for (NullChain<?> param : params) {
            captured.add(param.get());
        }
        lastParams = Collections.unmodifiableList(captured);
        RUN_COUNT.incrementAndGet();
        return "captured:" + captured.size();
    }
}
