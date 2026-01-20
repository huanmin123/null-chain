package com.gitee.huanminabc.nullchain.leaf.http.strategy.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.RequestStrategy;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * JSON 请求体构建策略
 * 
 * <p>实现 JSON 格式的请求体构建，支持：</p>
 * <p>实现 JSON 格式的请求体构建，支持：</p>
 * <ul>
 *   <li>自动排除文件类型字段（FileBinaryDTO、File、File[]、Collection&lt;File&gt; 等）</li>
 *   <li>支持 {@code @JSONField} 字段名映射</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class JsonRequestStrategy implements RequestStrategy {
    
    @Override
    public RequestBody build(Object requestData, Request.Builder requestBuilder) throws Exception {
        // 构建 JSON 请求体
        String jsonBody = buildJsonBody(requestData);
        RequestBody requestBody = RequestBody.create(Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8")), jsonBody);
        
        // 设置 Content-Type 请求头
        requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
        
        return requestBody;
    }
    
    @Override
    public boolean supports(OkHttpPostEnum type) {
        return type == OkHttpPostEnum.JSON;
    }
    
    /**
     * 构建JSON请求体
     * 将请求数据对象序列化为JSON字符串，自动排除文件类型字段
     *
     * @param requestData 请求数据对象
     * @return JSON字符串
     */
    private String buildJsonBody(Object requestData) {
        try {
            //如何是map包括JSONObject
            if (requestData instanceof Map || requestData instanceof Collection) {
                return JSON.toJSONString(requestData, SerializerFeature.DisableCircularReferenceDetect);
            }

            if (requestData instanceof String) {
                return requestData.toString();
            }
            if (requestData instanceof StringBuilder) {
                return requestData.toString();
            }
            if (requestData instanceof StringBuffer) {
                return requestData.toString();
            }


            // 创建一个Map，只包含需要序列化的字段（自动排除文件类型字段）
            java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
            Class<?> clazz = requestData.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(requestData);

                    // 跳过 null 值
                    if (value == null) {
                        continue;
                    }

                    // 检查是否是文件类型：如果是文件类型则排除
                    if (OkHttpBuild.isFileType(value)) {
                        continue; // 排除文件类型字段
                    }

                    // 确定JSON字段名：优先使用 @JSONField 的 name，否则使用字段名
                    String fieldName = OkHttpBuild.getFieldName(field);
                    jsonMap.put(fieldName, value);

                } catch (IllegalAccessException e) {
                    // 忽略无法访问的字段
                }
            }

            // 序列化为JSON字符串, 忽略循环引用加快速度
            return JSON.toJSONString(jsonMap, SerializerFeature.DisableCircularReferenceDetect);

        } catch (Exception e) {
            throw new RuntimeException("构建JSON请求体失败", e);
        }
    }
    

}

