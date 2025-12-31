package com.gitee.huanminabc.nullchain.leaf.http.strategy;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

/**
 * 表单请求体构建策略
 * 
 * <p>实现 {@code application/x-www-form-urlencoded} 格式的请求体构建，支持：</p>
 * <ul>
 *   <li>排除特殊字段（FILE）</li>
 *   <li>支持字段名映射</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class FormRequestBodyStrategy implements RequestBodyStrategy {
    
    @Override
    public RequestBody build(Object requestData, Request.Builder requestBuilder) throws Exception {
        // 构建表单请求体
        FormBody.Builder formBody = new FormBody.Builder();
        Map<String, Object> formMap = OkHttpBuild.valuetoMap(requestData);
        
        for (Map.Entry<String, Object> entry : formMap.entrySet()) {
            String key = entry.getKey();
            Object value1 = entry.getValue();
            
            // 必须是字符串
            if (!(value1 instanceof String)) {
                throw new NullChainException("FORM类型value值只能是字符串");
            }
            formBody.add(key, (String) value1);
        }
        
        return formBody.build();
    }
    
    @Override
    public boolean supports(OkHttpPostEnum type) {
        return type == OkHttpPostEnum.FORM;
    }
}

