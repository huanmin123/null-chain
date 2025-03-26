package com.gitee.huanminabc.test.nullchain.task;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

public class Test1Task implements NullTask<Object,String> {

    @Override
    public String run(Object preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setAge(30);
        userEntity.setName("张三12312312");
        System.out.println(userEntity);
        return "33333333";
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
