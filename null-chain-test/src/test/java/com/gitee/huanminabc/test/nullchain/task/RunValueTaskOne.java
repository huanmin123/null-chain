package com.gitee.huanminabc.test.nullchain.task;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.task.NullTask;

import java.util.Map;

public class RunValueTaskOne implements NullTask<Object, String> {
    @Override
    public String run(Object preValue, NullChain<?>[] params, Map<String, Object> context) {
        return "33333333";
    }
}
