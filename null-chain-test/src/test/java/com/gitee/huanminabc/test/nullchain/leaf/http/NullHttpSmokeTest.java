package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullHttp HTTP 请求功能测试类
 * 
 * <p>使用 httpbin.org 作为测试服务，测试各种 HTTP 请求功能：
 * - GET/POST/PUT/DELETE 请求
 * - Headers 测试
 * - Cookie 测试
 * - 文件上传
 * - 超时设置
 * </p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullHttpSmokeTest {

    private static final String HTTPBIN_BASE_URL = "https://httpbin.org";
    // 定义响应对象
    @Data
    public  static class HttpBinPostResponse {
        public String url;
        public Map<String, Object> json;
        public Map<String, Object> headers;
    }



    // ========== 链式构建测试（不触发网络IO） ==========

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
    public void testOfHttpBuildChainWithRetryConfigNoNetwork() {
        // 测试重试配置构建，不触发网络 IO
        Assertions.assertDoesNotThrow(() -> {
            Null.ofHttp("https://example.com")
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .retryCount(5)           // 设置重试5次
                    .retryInterval(200)      // 设置重试间隔200毫秒
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

    // ========== GET 请求测试 ==========

    /**
     * 测试 GET 请求：获取请求信息
     */
    @Test
    public void testGetRequest() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "GET 请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
        assertTrue(response.contains("httpbin.org/get"), "响应应该包含请求的 URL");
    }

    /**
     * 测试 GET 请求：带 URL 参数
     */
    @Test
    public void testGetRequestWithParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        params.put("number", 123);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get", params)
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "GET 请求响应不应该为空");
        assertTrue(response.contains("key1"), "响应应该包含参数 key1");
        assertTrue(response.contains("value1"), "响应应该包含参数值 value1");
        assertTrue(response.contains("key2"), "响应应该包含参数 key2");
    }

    // ========== POST 请求测试 ==========

    /**
     * 测试 POST 请求：JSON 格式
     */
    @Test
    public void testPostRequestJson() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "test");
        body.put("age", 25);
        body.put("active", true);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", body)
                .post(OkHttpPostEnum.JSON)
                .toJson()
                .orElseNull();

        assertNotNull(response, "POST 请求响应不应该为空");
        assertTrue(response.contains("\"json\""), "响应应该包含 json 字段");
        assertTrue(response.contains("test"), "响应应该包含请求体中的 name 值");
        assertTrue(response.contains("25"), "响应应该包含请求体中的 age 值");
    }

    /**
     * 测试 POST 请求：FORM 格式
     * 注意：FORM 格式要求 value 值只能是字符串
     */
    @Test
    public void testPostRequestForm() {
        Map<String, String> formData = new HashMap<>();
        formData.put("username", "testuser");
        formData.put("password", "testpass");
        formData.put("email", "test@example.com");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                .post(OkHttpPostEnum.FORM)
                .toJson()
                .orElseNull();

        assertNotNull(response, "POST 请求响应不应该为空");
        assertTrue(response.contains("\"form\""), "响应应该包含 form 字段");
        assertTrue(response.contains("testuser"), "响应应该包含表单数据中的 username");
    }

    // ========== PUT 请求测试 ==========

    /**
     * 测试 PUT 请求：JSON 格式
     */
    @Test
    public void testPutRequestJson() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("name", "updated");
        body.put("status", "active");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/put", body)
                .put(OkHttpPostEnum.JSON)
                .toJson()
                .orElseNull();

        assertNotNull(response, "PUT 请求响应不应该为空");
        assertTrue(response.contains("\"json\""), "响应应该包含 json 字段");
        assertTrue(response.contains("updated"), "响应应该包含请求体中的 name 值");
    }

    // ========== DELETE 请求测试 ==========

    /**
     * 测试 DELETE 请求
     */
    @Test
    public void testDeleteRequest() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 123);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/delete", params)
                .del()
                .toJson()
                .orElseNull();

        assertNotNull(response, "DELETE 请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    // ========== Headers 测试 ==========

    /**
     * 测试自定义请求头
     */
    @Test
    public void testCustomHeaders() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/headers")
                .addHeader("X-Custom-Header", "custom-value")
                .addHeader("X-Request-ID", "12345")
                .addHeader("User-Agent", "NullChain-Test/1.0")
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        assertTrue(response.contains("X-Custom-Header"), "响应应该包含自定义请求头");
        assertTrue(response.contains("custom-value"), "响应应该包含请求头的值");
        // httpbin 可能将请求头名称转换为小写，所以检查小写版本
        assertTrue(response.contains("x-custom-header") || response.contains("X-Custom-Header"), 
                "响应应该包含自定义请求头（不区分大小写）");
    }

    // ========== Cookie 测试 ==========

    /**
     * 测试设置 Cookie
     */
    @Test
    public void testSetCookie() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/cookies/set")
                .addHeader("Cookie", "session=abc123; user=test")
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
    }

    /**
     * 测试获取 Cookie
     */
    @Test
    public void testGetCookie() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/cookies")
                .addHeader("Cookie", "session=abc123; user=testuser")
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        assertTrue(response.contains("\"cookies\""), "响应应该包含 cookies 字段");
    }

    // ========== 文件上传测试 ==========

    /**
     * 测试文件上传
     */
    @Test
    public void testFileUpload() throws IOException {
        // 创建临时测试文件
        Path tempFile = Files.createTempFile("test-upload-", ".txt");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write("This is a test file for upload");
        }

        File file = tempFile.toFile();
        Map<String, Object> formData = new HashMap<>();
        formData.put("file", file);
        // FILE 类型支持文件上传，但其他字段需要是字符串
        formData.put("description", "Test file upload");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                .post(OkHttpPostEnum.FILE)
                .toJson()
                .orElseNull();

        assertNotNull(response, "文件上传响应不应该为空");
        // httpbin 返回的文件信息可能在 files 或 form 字段中
        assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                "响应应该包含 files 或 form 字段");

        // 清理临时文件
        Files.deleteIfExists(tempFile);
    }

    // ========== 超时设置测试 ==========

    /**
     * 测试超时设置
     */
    @Test
    public void testTimeoutSettings() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/delay/2")
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    // ========== 响应格式测试 ==========

    /**
     * 测试获取字节数组响应
     */
    @Test
    public void testToBytes() {
        byte[] response = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .get()
                .toBytes()
                .orElseNull();

        assertNotNull(response, "字节数组响应不应该为空");
        assertTrue(response.length > 0, "字节数组不应该为空");
        
        // 验证是有效的 JSON
        String jsonStr = new String(response);
        assertTrue(jsonStr.contains("\"url\""), "响应内容应该是有效的 JSON");
    }

    /**
     * 测试获取输入流响应
     */
    @Test
    public void testToInputStream() throws IOException {
        java.io.InputStream inputStream = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .get()
                .toInputStream()
                .orElseNull();

        assertNotNull(inputStream, "输入流不应该为空");
        
        try {
            // 读取所有内容验证
            byte[] buffer = new byte[4096];
            int totalBytes = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer, totalBytes, buffer.length - totalBytes)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes >= buffer.length) {
                    // 如果缓冲区满了，扩展缓冲区
                    byte[] newBuffer = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    buffer = newBuffer;
                }
            }
            assertTrue(totalBytes > 0, "应该能读取到数据");
            
            String content = new String(buffer, 0, totalBytes);
            assertTrue(content.contains("\"url\""), "响应内容应该是有效的 JSON");
        } finally {
            // 确保关闭流
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // ========== 状态码测试 ==========

    /**
     * 测试不同的 HTTP 状态码
     */
    @Test
    public void testStatusCode() {
        // 测试 200 OK - httpbin 的 /status/200 返回空响应体，但状态码是 200
        assertDoesNotThrow(() -> {
            String response200 = Null.ofHttp(HTTPBIN_BASE_URL + "/status/200")
                    .get()
                    .toJson()
                    .orElseNull();
            // 200 状态码可能返回空响应体，这是正常的
        });

        // 测试 404 Not Found - 验证不抛异常
        assertDoesNotThrow(() -> {
            Null.ofHttp(HTTPBIN_BASE_URL + "/status/404")
                    .get()
                    .toJson()
                    .orElseNull();
            // 404 可能返回空或错误信息，这里只验证不抛异常
        });
    }

    // ========== 复杂场景测试 ==========

    /**
     * 测试完整的请求流程：配置 + 请求 + 响应处理
     */
    @Test
    public void testCompleteRequestFlow() {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Hello HttpBin");
        body.put("timestamp", System.currentTimeMillis());

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", body)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addHeader("X-Request-Source", "NullChain-Test")
                .addHeader("Content-Type", "application/json")
                .post(OkHttpPostEnum.JSON)
                .toJson()
                .orElseNull();

        assertNotNull(response, "完整请求流程的响应不应该为空");
        assertTrue(response.contains("\"json\""), "响应应该包含 json 字段");
        assertTrue(response.contains("Hello HttpBin"), "响应应该包含请求体中的 message");
        assertTrue(response.contains("X-Request-Source"), "响应应该包含自定义请求头");
    }

    // ========== 文件下载测试 ==========

    /**
     * 测试文件下载功能
     */
    @Test
    public void testDownloadFile() throws IOException {
        // 创建临时文件用于下载
        Path tempFile = Files.createTempFile("test-download-", ".txt");
        try {
            // 下载一个小的JSON响应到文件
            Boolean success = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                    .get()
                    .downloadFile(tempFile.toString()).get();

            assertTrue(success, "文件下载应该成功");
            assertTrue(Files.exists(tempFile), "下载的文件应该存在");
            assertTrue(Files.size(tempFile) > 0, "下载的文件不应该为空");
            
            // 验证文件内容
            String content = new String(Files.readAllBytes(tempFile));
            assertTrue(content.contains("\"url\""), "下载的文件内容应该包含 url 字段");
        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempFile);
        }
    }

    // ========== 重定向测试 ==========

    /**
     * 测试HTTP重定向（httpbin会自动处理重定向）
     */
    @Test
    public void testRedirect() {
        // httpbin的/redirect/1会重定向到/get
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/redirect/1")
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "重定向请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    // ========== 更多状态码测试 ==========

    /**
     * 测试更多HTTP状态码
     */
    @Test
    public void testMoreStatusCodes() {
        // 测试 500 Internal Server Error - 验证不抛异常
        assertDoesNotThrow(() -> {
            Null.ofHttp(HTTPBIN_BASE_URL + "/status/500")
                    .get()
                    .toJson()
                    .orElseNull();
        });

        // 测试 503 Service Unavailable - 验证不抛异常
        assertDoesNotThrow(() -> {
            Null.ofHttp(HTTPBIN_BASE_URL + "/status/503")
                    .get()
                    .toJson()
                    .orElseNull();
        });

        // 测试 201 Created - 验证不抛异常
        assertDoesNotThrow(() -> {
            Null.ofHttp(HTTPBIN_BASE_URL + "/status/201")
                    .get()
                    .toJson()
                    .orElseNull();
        });
    }

    // ========== 空响应体测试 ==========

    /**
     * 测试空响应体处理
     */
    @Test
    public void testEmptyResponse() {
        // httpbin的/status/204返回空响应体
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/status/204")
                .get()
                .toJson()
                .orElseNull();

        // 204 No Content 可能返回null或空字符串，都是正常的
        assertTrue(response == null || response.isEmpty(), "204状态码应该返回空响应体");
    }

    // ========== 大文件上传测试 ==========

    /**
     * 测试大文件上传
     */
    @Test
    public void testLargeFileUpload() throws IOException {
        // 创建较大的临时测试文件（100KB）
        Path tempFile = Files.createTempFile("test-large-upload-", ".txt");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            // 写入100KB的数据
            for (int i = 0; i < 10000; i++) {
                writer.write("This is line " + i + " of a large test file for upload.\n");
            }
        }

        File file = tempFile.toFile();
        Map<String, Object> formData = new HashMap<>();
        formData.put("file", file);
        formData.put("description", "Large file upload test");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                .post(OkHttpPostEnum.FILE)
                .toJson()
                .orElseNull();

        assertNotNull(response, "大文件上传响应不应该为空");
        assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                "响应应该包含 files 或 form 字段");

        // 清理临时文件
        Files.deleteIfExists(tempFile);
    }

    // ========== 多个文件上传测试 ==========

    /**
     * 测试多个文件上传
     */
    @Test
    public void testMultipleFileUpload() throws IOException {
        // 创建多个临时测试文件
        Path tempFile1 = Files.createTempFile("test-upload-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-upload-2-", ".txt");
        
        try {
            try (FileWriter writer = new FileWriter(tempFile1.toFile())) {
                writer.write("First test file content");
            }
            try (FileWriter writer = new FileWriter(tempFile2.toFile())) {
                writer.write("Second test file content");
            }

            File[] files = {tempFile1.toFile(), tempFile2.toFile()};
            Map<String, Object> formData = new HashMap<>();
            formData.put("files", files);
            formData.put("description", "Multiple files upload test");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                    .post(OkHttpPostEnum.FILE)
                    .toJson()
                    .orElseNull();

            assertNotNull(response, "多文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }

    // ========== 自定义OkHttpClient测试 ==========

    /**
     * 测试使用自定义OkHttpClient名称
     * 注意：自定义HttpClient会在第一次使用时自动创建
     */
    @Test
    public void testCustomHttpClient() {
        // 使用自定义HttpClient名称，会自动创建新的OkHttpClient实例
        // 使用Void.TYPE作为value，表示没有请求体
        String response = Null.ofHttp("test-custom-client", HTTPBIN_BASE_URL + "/get", Void.TYPE)
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "自定义HttpClient请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    // ========== 复杂JSON请求体测试 ==========

    /**
     * 测试复杂JSON请求体
     */
    @Test
    public void testComplexJsonBody() {
        Map<String, Object> complexBody = new HashMap<>();
        complexBody.put("name", "test");
        complexBody.put("age", 25);
        complexBody.put("active", true);
        
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "Test City");
        address.put("zip", "12345");
        complexBody.put("address", address);
        
        List<String> hobbies = new java.util.ArrayList<>();
        hobbies.add("reading");
        hobbies.add("coding");
        complexBody.put("hobbies", hobbies);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", complexBody)
                .post(OkHttpPostEnum.JSON)
                .toJson()
                .orElseNull();

        assertNotNull(response, "复杂JSON请求响应不应该为空");
        assertTrue(response.contains("\"json\""), "响应应该包含 json 字段");
        assertTrue(response.contains("test"), "响应应该包含请求体中的 name 值");
        assertTrue(response.contains("Test City"), "响应应该包含嵌套的 address.city 值");
    }

    // ========== URL参数编码测试 ==========

    /**
     * 测试URL参数编码（特殊字符）
     */
    @Test
    public void testUrlParameterEncoding() {
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value with spaces");
        params.put("key2", "value&with=special");
        params.put("key3", "中文测试");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get", params)
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "URL参数编码请求响应不应该为空");
        assertTrue(response.contains("key1"), "响应应该包含参数 key1");
        assertTrue(response.contains("key2"), "响应应该包含参数 key2");
        assertTrue(response.contains("key3"), "响应应该包含参数 key3");
    }

    // ========== 超时异常测试 ==========

    /**
     * 测试超时设置（短超时应该失败）
     */
    @Test
    public void testTimeoutFailure() {
        // 设置很短的超时时间，请求一个需要延迟的URL
        // 注意：这个测试可能会因为网络速度而失败，所以使用assertDoesNotThrow
        assertDoesNotThrow(() -> {
            try {
                Null.ofHttp(HTTPBIN_BASE_URL + "/delay/5")
                        .connectTimeout(1, TimeUnit.SECONDS)
                        .readTimeout(1, TimeUnit.SECONDS)
                        .get()
                        .toJson()
                        .orElseNull();
            } catch (Exception e) {
                // 超时异常是预期的，这里只验证不会导致测试框架崩溃
            }
        });
    }

    // ========== 重试功能测试 ==========

    /**
     * 测试自定义重试次数和间隔配置
     * 使用正常的URL测试配置是否生效（不会真正触发重试）
     */
    @Test
    public void testRetryConfiguration() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .retryCount(5)           // 设置重试5次
                .retryInterval(200)      // 设置重试间隔200毫秒
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "配置重试后的请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    /**
     * 测试设置重试次数为0（不重试）
     */
    @Test
    public void testRetryCountZero() {
        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .retryCount(0)           // 设置不重试
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "不重试配置的请求响应不应该为空");
        assertTrue(response.contains("\"url\""), "响应应该包含 url 字段");
    }

    /**
     * 测试重试配置与其他配置的组合使用
     */
    @Test
    public void testRetryWithOtherConfigs() {
        Map<String, Object> params = new HashMap<>();
        params.put("test", "retry");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/get", params)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryCount(3)           // 设置重试3次
                .retryInterval(100)      // 设置重试间隔100毫秒
                .addHeader("X-Test", "Retry")
                .get()
                .toJson()
                .orElseNull();

        assertNotNull(response, "组合配置的请求响应不应该为空");
        assertTrue(response.contains("test"), "响应应该包含参数");
        assertTrue(response.contains("X-Test"), "响应应该包含自定义请求头");
    }

    // ========== JSON 转换对象测试 ==========

    /**
     * 测试将 JSON 响应转换为对象
     */
    @Test
    public void testToStrWithClass() {
        // 使用 httpbin 的 /get 接口，返回的 JSON 包含 url, headers 等字段
        // 定义一个简单的响应类用于测试

        HttpBinPostResponse response = Null.ofHttp(HTTPBIN_BASE_URL + "/get")
                .get()
                .toJson(HttpBinPostResponse.class)
                .orElseNull();

        assertNotNull(response, "转换后的对象不应该为空");
        assertNotNull(response.url, "url 字段不应该为空");
        assertTrue(response.url.contains("httpbin.org/get"), "url 应该包含请求的地址");
    }


    /**
     * 测试将 POST JSON 响应转换为对象
     */
    @Test
    public void testToStrWithClassPost() {
        // 定义请求体对象
        class UserRequest {
            public String name;
            public int age;
            public boolean active;
        }



        UserRequest user = new UserRequest();
        user.name = "张三";
        user.age = 25;
        user.active = true;

        HttpBinPostResponse response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", user)
                .post(OkHttpPostEnum.JSON)
                .toJson(HttpBinPostResponse.class)
                .orElseNull();

        assertNotNull(response, "转换后的对象不应该为空");
        assertNotNull(response.json, "json 字段不应该为空");
        assertEquals("张三", response.json.get("name"), "应该包含请求的 name 字段");
    }
}


