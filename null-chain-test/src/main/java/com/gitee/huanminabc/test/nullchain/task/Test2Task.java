package com.gitee.huanminabc.test.nullchain.task;

import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

public class Test2Task implements NullTask<Object,String> {


    @Override
    public String run(Object preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return "2222222222";
    }

    @Override
    public void init(Object preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        NullTask.super.init(preValue, params, context);
    }

    @Override
    public NullType checkTypeParams() {
        return NullTask.super.checkTypeParams();
    }
}
