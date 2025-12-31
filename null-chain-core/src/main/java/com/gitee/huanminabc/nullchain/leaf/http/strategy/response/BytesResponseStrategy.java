package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 字节数组响应处理策略
 * 
 * <p>处理HTTP响应为字节数组格式，适用于二进制数据、图片、文件等。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class BytesResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        return OkHttpBuild.retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().bytes();
            }
        });
    }
    
    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.BYTES;
    }
}

