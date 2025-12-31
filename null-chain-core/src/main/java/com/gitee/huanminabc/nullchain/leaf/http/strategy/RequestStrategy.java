package com.gitee.huanminabc.nullchain.leaf.http.strategy;

import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 请求体构建策略接口
 * 
 * <p>定义了不同请求类型的请求体构建策略，支持 JSON、表单、文件上传等多种格式。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public interface RequestStrategy {
    /**
     * 构建请求体
     * 
     * @param requestData 请求数据对象
     * @param requestBuilder 请求构建器，用于添加请求头等
     * @return 构建好的请求体
     * @throws Exception 构建过程中可能出现的异常
     */
    RequestBody build(Object requestData, Request.Builder requestBuilder) throws Exception;
    
    /**
     * 判断是否支持指定的请求类型
     * 
     * @param type 请求类型枚举
     * @return 如果支持该类型返回 true，否则返回 false
     */
    boolean supports(OkHttpPostEnum type);
}

