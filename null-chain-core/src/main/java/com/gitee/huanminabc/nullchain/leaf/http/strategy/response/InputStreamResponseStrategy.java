package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 输入流响应处理策略
 * 
 * <p>处理HTTP响应为输入流格式，适用于大文件或流式数据。
 * 注意：返回的 InputStream 必须由调用方负责关闭，否则会造成资源泄漏。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class InputStreamResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        return OkHttpBuild.retry(url, retryCount, retryInterval, () -> {
            Response response = okHttpClient.newCall(request.build()).execute();
            if (response.body() == null) {
                // 如果没有响应体,需要关闭response避免资源泄漏
                response.close();
                return null;
            }
            // 返回流,由调用方负责关闭(关闭流会自动关闭response)
            return response.body().byteStream();
        });
    }
    
    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.INPUT_STREAM;
    }
}

