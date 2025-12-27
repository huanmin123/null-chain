# HTTP请求

本文档介绍 HTTP 请求操作，包括 GET、POST、PUT、DELETE 等方法。

## 创建 HTTP 请求

### 基本创建

```java
// 创建 GET 请求（无请求体）
OkHttp http = Null.ofHttp("https://api.example.com/data");

// 创建 POST 请求（带请求体）
OkHttp http = Null.ofHttp("https://api.example.com/data", requestBody);

// 使用指定的 HTTP 客户端名称
OkHttp http = Null.ofHttp("httpClientName", "https://api.example.com/data", requestBody);
```

## HTTP 配置

### 超时设置

```java
// 设置连接超时
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .connectTimeout(30, TimeUnit.SECONDS)
    .get();

// 设置写超时
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .writeTimeout(60, TimeUnit.SECONDS)
    .post(OkHttpPostEnum.JSON);

// 设置读超时
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .readTimeout(120, TimeUnit.SECONDS)
    .get();
```

### 代理设置

```java
// 设置 HTTP 代理
Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .proxy(proxy)
    .get();
```

### 请求头设置

```java
// 添加请求头
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .addHeader("Authorization", "Bearer token123")
    .addHeader("Content-Type", "application/json")
    .get();
```

### 重试设置

```java
// 设置重试次数和间隔
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .retryCount(5)           // 设置重试5次
    .retryInterval(200)      // 设置基础间隔为200毫秒
    .get();
// 实际重试间隔为：第1次200ms，第2次400ms，第3次600ms
```

### 异步请求

```java
// 异步请求（使用默认线程池）
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .async()
    .get();

// 异步请求（使用指定线程池）
OkHttp http = Null.ofHttp("https://api.example.com/data")
    .async("customThreadPool")
    .get();
```

## HTTP 方法

### GET 请求

```java
// GET 请求（参数会自动拼接到 URL）
String response = Null.ofHttp("https://api.example.com/users", params)
    .get()
    .toStr()
    .orElse("请求失败");
```

### POST 请求

```java
// POST 请求 - JSON 格式
String response = Null.ofHttp("https://api.example.com/users", requestBody)
    .post(OkHttpPostEnum.JSON)
    .toStr()
    .orElse("请求失败");

// POST 请求 - 表单格式
String response = Null.ofHttp("https://api.example.com/users", formData)
    .post(OkHttpPostEnum.FORM)
    .toStr()
    .orElse("请求失败");

// POST 请求 - 文件上传
String response = Null.ofHttp("https://api.example.com/upload", fileData)
    .post(OkHttpPostEnum.FILE)
    .toStr()
    .orElse("请求失败");
```

**文件上传说明**：
- 支持 `File`、`File[]`、`byte[]`、`byte[][]` 类型
- 文件的 key 是 Map 的 key 或对象字段名称
- 可通过 `@JSONField(name="file")` 指定 key
- 字节上传必须指定 `fileName`，可通过 `@JSONField(name="fileName")` 指定

### PUT 请求

```java
// PUT 请求
String response = Null.ofHttp("https://api.example.com/users/1", requestBody)
    .put(OkHttpPostEnum.JSON)
    .toStr()
    .orElse("请求失败");
```

### DELETE 请求

```java
// DELETE 请求（参数会自动拼接到 URL）
String response = Null.ofHttp("https://api.example.com/users/1", params)
    .del()
    .toStr()
    .orElse("请求失败");
```

## 结果处理

### 字符串结果

```java
// 获取字符串结果
String response = Null.ofHttp("https://api.example.com/users")
    .get()
    .toStr()
    .orElse("请求失败");

// 获取字符串并转换为对象
User user = Null.ofHttp("https://api.example.com/user/1")
    .get()
    .toFromJson(User.class)
    .orElseNull();
```

### 字节数组结果

```java
// 获取字节数组
byte[] data = Null.ofHttp("https://example.com/image.jpg")
    .get()
    .toBytes()
    .orElse(new byte[0]);
```

### 输入流结果

```java
// 获取输入流
InputStream stream = Null.ofHttp("https://example.com/large-file.zip")
    .get()
    .toInputStream()
    .orElseNull();

if (stream != null) {
    try (stream) {
        // 处理流数据
    }
}
```

### 文件下载

```java
// 下载文件到指定路径
Boolean success = Null.ofHttp("https://example.com/file.pdf")
    .get()
    .downloadFile("/path/to/download/file.pdf");
```

### SSE 流式响应

```java
// SSE 文本流
Null.ofHttp("https://api.example.com/sse")
    .get()
    .toSSEText(new SSEEventListener<String>() {
        @Override
        public void onEvent(EventMessage<String> msg) {
            System.out.println("收到事件: " + msg.getData());
            // 如果发现结果不对，可以主动终止
            if (shouldStop(msg.getData())) {
                msg.terminate();  // 终止流
            }
        }
        
        @Override
        public void onInterrupt() {
            System.out.println("SSE 流已被用户终止");
        }
        
        // ... 其他回调方法
    });

// SSE JSON 流
Null.ofHttp("https://api.example.com/sse")
    .get()
    .toSSEJson(new SSEEventListener<JSONObject>() {
        @Override
        public void onEvent(EventMessage<JSONObject> msg) {
            JSONObject data = msg.getData();
            if (data != null) {
                System.out.println("收到事件: " + data.getString("content"));
            }
        }
        // ... 其他回调方法
    });

// SSE 自定义解码器
Null.ofHttp("https://api.example.com/sse")
    .get()
    .toSSE(new SSEEventListener<MyData>() {
        @Override
        public void onEvent(EventMessage<MyData> msg) {
            MyData data = msg.getData();
            // 处理数据
        }
        // ... 其他回调方法
    }, DataDecoder.jsonDecoder());
```

## 完整示例

```java
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import java.util.concurrent.TimeUnit;

public class HttpExample {
    
    public void getRequest() {
        // 简单的 GET 请求
        String response = Null.ofHttp("https://api.example.com/users")
            .get()
            .toStr()
            .orElse("请求失败");
        
        // 带参数的 GET 请求
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        String response2 = Null.ofHttp("https://api.example.com/users", params)
            .get()
            .toStr()
            .orElse("请求失败");
    }
    
    public void postRequest() {
        // POST JSON 请求
        User user = new User();
        user.setName("张三");
        user.setAge(25);
        
        String response = Null.ofHttp("https://api.example.com/users", user)
            .addHeader("Authorization", "Bearer token123")
            .post(OkHttpPostEnum.JSON)
            .toStr()
            .orElse("请求失败");
        
        // 转换为对象
        User createdUser = Null.ofHttp("https://api.example.com/users", user)
            .post(OkHttpPostEnum.JSON)
            .toFromJson(User.class)
            .orElseNull();
    }
    
    public void fileUpload() {
        // 文件上传
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("file", new File("/path/to/file.jpg"));
        fileData.put("description", "图片描述");
        
        String response = Null.ofHttp("https://api.example.com/upload", fileData)
            .post(OkHttpPostEnum.FILE)
            .toStr()
            .orElse("上传失败");
    }
    
    public void downloadFile() {
        // 文件下载
        Boolean success = Null.ofHttp("https://example.com/file.pdf")
            .get()
            .downloadFile("/path/to/download/file.pdf");
    }
    
    public void withConfig() {
        // 带配置的请求
        String response = Null.ofHttp("https://api.example.com/data")
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .addHeader("Authorization", "Bearer token123")
            .retryCount(3)
            .retryInterval(200)
            .get()
            .toStr()
            .orElse("请求失败");
    }
}
```

## 注意事项

1. **参数处理**：GET 和 DELETE 请求的参数会自动拼接到 URL，POST 和 PUT 请求的参数会作为请求体
2. **空值处理**：值为 null 的参数会被自动忽略
3. **URL 参数拼接**：会自动识别 URL 中是否已有参数，如果有会自动与节点参数拼接
4. **文件上传**：需要正确设置文件字段和文件名
5. **异步请求**：异步请求不会阻塞当前线程，但需要处理回调结果
6. **SSE 流**：SSE 流可以通过 `terminate()` 方法主动终止

