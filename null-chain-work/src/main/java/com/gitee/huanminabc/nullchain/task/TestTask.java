package com.gitee.huanminabc.nullchain.task;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullType;

import java.util.Map;
/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 **/
public class TestTask implements NullTask<String,String> {

    @Override
    public NullType checkTypeParams() {
        //这个的意思是params必须有一个参数, 这个参数可以是字符串或者布尔值
        return NullType.params(1,NullType.of("str",String.class),NullType.of("error",Boolean.class));
    }

    @Override
    public void init(String preValue, NullChain<?>[] params,Map<String, Object> context) throws Exception {
        //这里可以做一些在run之前的操作
    }

    @Override
    public String run(String preValue, NullChain<?>[] params,Map<String, Object> context) throws Exception {
        System.out.println("TestTask run :" + preValue);

        Null.of(context.get("str")).type(String.class).ifPresent((str)->{
            System.out.println("TestTask run str:" + str);
        });

        Null.of(context.get("error")).type(Boolean.class).ifPresent((bool)-> {
            if (bool){
                throw new NullChainException("TestTask run error");
            }
        });

      return "11111111111";
    }
}
