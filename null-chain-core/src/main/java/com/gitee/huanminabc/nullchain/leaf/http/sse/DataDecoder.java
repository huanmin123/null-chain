package com.gitee.huanminabc.nullchain.leaf.http.sse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * SSE 数据解码器接口
 * 
 * <p>用于将 SSE 事件中的原始字符串数据解码为业务需要的泛型对象。
 * 例如：将 JSON 字符串解码为 JSONObject，或将纯文本保持为 String。</p>
 * 
 * @param <T> 解码后的数据类型
 * @author huanmin
 * @since 1.0.0
 */
@FunctionalInterface
public interface DataDecoder<T> {
    /**
     * 解码原始字符串数据
     * 
     * @param raw 原始字符串数据
     * @return 解码后的对象，如果解码失败可返回 null
     */
    T decode(String raw);

    /**
     * 默认的字符串解码器
     * 直接返回原始字符串，不做任何转换
     * 
     * @return 字符串解码器实例
     */
    static DataDecoder<String> stringDecoder() {
        return raw -> raw;
    }

    /**
     * JSON 解码器
     * 将 JSON 字符串解码为 JSONObject 对象
     * 
     * <p>如果原始字符串为空或解析失败，返回 null。解析失败时不会抛出异常，
     * 而是静默返回 null，由调用方处理。</p>
     * 
     * @return JSON 解码器实例
     */
    static DataDecoder<JSONObject> jsonDecoder() {
        return raw -> {
            if (raw == null || raw.trim().isEmpty()) {
                return null;
            }
            try {
                return JSON.parseObject(raw);
            } catch (Exception e) {
                // 解析失败时返回 null，不抛出异常
                return null;
            }
        };
    }

}

