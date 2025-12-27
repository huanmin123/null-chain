package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class NullHttpSmokeTest {

    @Test
    public void testOfHttpBuildChainNoNetwork() {
        // 仅构建，不触发网络 IO（不调用 toStr/toBytes/downloadFile 等）
        Assertions.assertDoesNotThrow(() -> {
            Null.ofHttp("https://example.com")
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .addHeader("X-Demo", "1")
                    .get();
        });
    }

    @Test
    public void testOfHttpWithBodyBuildChainNoNetwork() {
        Assertions.assertDoesNotThrow(() -> {
            HashMap<String, Object> body = new HashMap<>();
            body.put("a", 1);
            Null.ofHttp("https://example.com", body)
                    .post(OkHttpPostEnum.JSON);
        });
    }
}


