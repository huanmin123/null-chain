package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.alibaba.fastjson.JSON;
import com.gitee.huanminabc.jcommon.str.StringUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * JSON对象响应处理策略
 * 
 * <p>处理HTTP响应为JSON对象，先将响应转换为字符串，再通过FastJSON反序列化为指定类型的对象。
 * 适用于处理JSON格式的API响应。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class JsonResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        // 参数已在调用方校验，直接使用
        @SuppressWarnings("unchecked")
        Class<Object> clazz = (Class<Object>) params[0];
        
        // 先获取字符串响应
        String str = (String) OkHttpBuild.retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().string();
            }
        });
        
        if (StringUtil.isEmpty(str)) {
            return null;
        }
        
        // 使用 FastJSON 将字符串转换为对象
        Object result = JSON.parseObject(str, clazz);
        if (Null.is(result)) {
            return null;
        }
        
        return result;
    }
    
    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.JSON;
    }
}

