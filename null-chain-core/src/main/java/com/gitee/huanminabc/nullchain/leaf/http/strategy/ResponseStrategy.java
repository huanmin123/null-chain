package com.gitee.huanminabc.nullchain.leaf.http.strategy;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 响应处理策略接口
 * 
 * <p>定义了不同响应类型的处理策略，支持字符串、字节数组、输入流、文件下载、JSON对象、SSE流等多种响应格式。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public interface ResponseStrategy {
    /**
     * 处理HTTP响应
     * 
     * @param url 请求URL
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param params 额外参数（如文件路径、Class类型、SSE监听器等）
     * @return 处理结果
     * @throws Exception 处理过程中的异常
     */
    Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, 
                  int retryCount, long retryInterval, Object... params) throws Exception;
    
    /**
     * 判断是否支持指定的响应类型
     * 
     * @param type 响应类型枚举
     * @return 如果支持该类型返回 true，否则返回 false
     */
    boolean supports(OkHttpResponseEnum type);
}

