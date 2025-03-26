package com.gitee.huanminabc.nullchain.rpc.filters;

import com.gitee.huanminabc.nullchain.Null;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Arrays;

/**
 * 拦截prc接口请求的参数不能是空否则放弃, 避免错误请求增加服务器的压力
 */
@Slf4j
@Activate(order = Integer.MIN_VALUE, group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER})
public class ProviderConsumerDubboNullCheckFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (invocation.getArguments() != null) {
            Object[] arguments = invocation.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                Object arg = arguments[i];
                if (Null.is(arg)) {
                    //获取调用的接口
                    String interfaceName = invoker.getInterface().getName() + "." + invocation.getMethodName() + "(" + Arrays.toString(invocation.getParameterTypes()) + ")";
                    //抛出异常
                    throw new RpcException(interfaceName + "第" + i + "个参数为空, 已被拦截!");
                }
            }
        }

        return invoker.invoke(invocation);
    }
}