package com.gitee.huanminabc.nullchain.leaf.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.gitee.huanminabc.jcommon.reflect.AnnotationUtil;
import com.gitee.huanminabc.jcommon.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.jcommon.reflect.FieldUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.function.NullHttpSupplierEx;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEStreamController;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpBuild {
    public final static ConcurrentHashMap<String, OkHttpClient> okHttpClientConcurrentHashMap = new ConcurrentHashMap<>();
    public final static int LIMIT_MAXIMUM_POOL_SIZE = 1000;

    static {
        //默认的OkHttp 实例
        OkHttpBuild.okHttpClientConcurrentHashMap.put(OkHttpBase.DEFAULT_THREAD_FACTORY_NAME, createOkHttpClient(LIMIT_MAXIMUM_POOL_SIZE));
    }

    private static OkHttpClient createOkHttpClient(int maxPoolSize) {
        TrustManager[] trustManagers = OkHttpBuild.buildTrustManagers();
        return new OkHttpClient.Builder()
                //设置连接超时时间 默认是10s
                .connectTimeout(10, TimeUnit.SECONDS)
                //写入超时时间 默认是10s (一般不调整)
                .writeTimeout(10, TimeUnit.SECONDS)
                //读取超时时间 默认是10s (一般不调整)
                .readTimeout(10, TimeUnit.SECONDS)
                //跳过ssl认证(https)
                .sslSocketFactory(OkHttpBuild.createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                .hostnameVerifier((hostName, session) -> true)
                //这个必须开启, 否则在并发的时候就会出现  java.io.IOException: unexpected end of stream on
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(maxPoolSize, 10, TimeUnit.MINUTES))
                .build();
    }

    //获取指定的 OkHttpClient
    public static OkHttpClient getOkHttp(String httpName) {
        return okHttpClientConcurrentHashMap.computeIfAbsent(httpName, k -> createOkHttpClient(100));
    }

    public static boolean downloadFile(String url, String filePath, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval) {
        return retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return false;
                }
                FileOutputStream fileOutputStream = null;
                try (InputStream inputStream = response.body().byteStream()) {
                    File file = new File(filePath);
                    //判断文件目录
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    return true;
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                }
            }
        });
    }

    public static String toStr(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval) {
        return retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().string();
            }

        });
    }

    /**
     * 获取响应的输入流
     * 注意: 返回的 InputStream 必须由调用方负责关闭,否则会造成资源泄漏
     * 
     * @param url HTTP请求的URL
     * @param okHttpClient OkHttp客户端实例
     * @param request 请求构建器
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 响应体的输入流,如果响应体为空则返回null
     */
    public static InputStream toInputStream(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval) {
        return retry(url, retryCount, retryInterval, () -> {
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

    public static byte[] toBytes(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval) {
        return retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().bytes();
            }
        });
    }

    /**
     * HTTP请求重试机制
     * 
     * @param url 请求的URL
     * @param retryCount 重试次数
     * @param retryInterval 基础重试间隔（毫秒），实际间隔为：retryInterval * 当前重试次数
     * @param runnable 执行的HTTP请求操作
     * @return 请求结果
     * @throws NullChainException 当所有重试都失败时抛出异常
     */
    private static <T> T retry(String url, int retryCount, long retryInterval, NullHttpSupplierEx<T> runnable) {
        // 如果重试次数为0，直接执行一次
        if (retryCount == 0) {
            try {
                return runnable.get();
            } catch (IOException e) {
                log.error("{}请求失败，未配置重试", url, e);
                throw new NullChainException(url + "请求失败: " + e.getMessage());
            }
        }
        
        // 执行重试逻辑
        for (int i = 1; i <= retryCount; i++) {
            try {
                return runnable.get();
            } catch (IOException e) {
                log.error("{}请求失败开始重试次数：{}", url, i, e);
                // 如果还有重试机会，则等待后继续
                if (i < retryCount) {
                    try {
                        Thread.sleep(retryInterval * i);
                    } catch (InterruptedException e1) {
                        log.warn("重试线程被中断", e1);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new NullChainException(url + "重试" + retryCount + "次还是请求失败");
    }


    /**
     * 为request添加请求头
     */
    public static void setHeader(Map<String, String> headerMap, Request.Builder request) {
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    public static RequestBody requestBodyHandel(OkHttpPostEnum type, Object value__) throws Exception {
        RequestBody requestBody = null;
        switch (type) {
            case JSON:
                String json = JSON.toJSONString(value__);
                requestBody = RequestBody.create( MediaType.parse("application/json; charset=utf-8"),json);
                break;
            case FORM:
                //默认是application/x-www-form-urlencoded协议
                FormBody.Builder formBody = new FormBody.Builder();
                Map<String, Object> formMap = OkHttpBuild.valuetoMap(value__);
                for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                    String key = entry.getKey();
                    Object value1 = entry.getValue();
                    //必须是字符串
                    if (!(value1 instanceof String)) {
                        throw new NullChainException("FORM类型value值只能是字符串");
                    }
                    formBody.add(key, (String) value1);
                }
                requestBody = formBody.build();
                break;
            case FILE:
                //multipart/form-data 协议
                Map<String, Object> fileMap = OkHttpBuild.fileValuetoMap(value__);
                Object fileName = fileMap.get("fileName");
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                for (Map.Entry<String, Object> field : fileMap.entrySet()) { // 添加表单信息
                    String key = field.getKey();
                    Object value = field.getValue();
                    //判断是 File
                    if (value instanceof File) {
                        File file = (File) field.getValue();
                        if (!file.exists()) {
                            throw new NullChainException("文件路径不存在:" + file.getAbsolutePath());
                        }
                        builder.addFormDataPart(key, file.getName(), RequestBody.create( MediaType.parse("application/octet-stream"),file));
                    }

                    if (value instanceof File[]) {  //判断是 File[]
                        File[] values = (File[]) field.getValue();
                        for (File file : values) {
                            if (!file.exists()) {
                                throw new NullChainException("文件路径不存在:" + file.getAbsolutePath());
                            }
                            builder.addFormDataPart(key, file.getName(), RequestBody.create( MediaType.parse("application/octet-stream"),file));
                        }
                    }
                    if (value instanceof Collection) {
                        //取出第一个值判断是否为File
                        Object o = ((Collection) value).iterator().next();
                        if (o instanceof File) {
                            for (Object o1 : (Collection) value) {
                                File file = (File) o1;
                                //判断是否存在
                                if (!file.exists()) {
                                    throw new NullChainException("文件路径不存在:" + file.getAbsolutePath());
                                }
                                builder.addFormDataPart(key, file.getName(), RequestBody.create( MediaType.parse("application/octet-stream"),file));
                            }
                            continue;
                        }
                        if (o instanceof byte[]) {
                            for (Object o1 : (Collection) value) {
                                byte[] bytes = (byte[]) o1;
                                builder.addFormDataPart(key, (String) fileName, RequestBody.create( MediaType.parse("application/octet-stream"),bytes));
                            }
                            continue;
                        }
                    }

                    if (value instanceof byte[]) {
                        if (Null.is(fileName)) {
                            throw new NullChainException("参数错误,你使用的是字节上传,需要添加fileName参数,指定文件名");
                        }
                        byte[] fileByte = (byte[]) field.getValue();
                        builder.addFormDataPart(key, (String) fileName, RequestBody.create(MediaType.parse("application/octet-stream"),fileByte));
                        continue;
                    }

                    if (value instanceof byte[][]) {
                        if (Null.is(fileName)) {
                            throw new NullChainException("参数错误,你使用的是字节上传,需要添加fileName参数,指定文件名");
                        }
                        byte[][] fileByte = (byte[][]) field.getValue();
                        for (byte[] bytes : fileByte) {
                            builder.addFormDataPart(key, (String) fileName, RequestBody.create( MediaType.parse("application/octet-stream"),bytes));
                        }
                        continue;
                    }
                    builder.addFormDataPart(field.getKey(), String.valueOf(field.getValue()));
                }
                requestBody = builder.build();

                break;
        }
        return requestBody;
    }


    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     */
    public static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            throw new NullChainException(e);
        }
        return ssfFactory;
    }

    public static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }


    /**
     * 将value转化为map
     */
    public static Map<String, Object> valuetoMap(Object v) {
        //如果value 是Void.TYPE
        if (v == Void.TYPE) {
            return new HashMap<>();
        }
        if (!(v instanceof Map)) {
            //如果是数组和集合那么不支持
            if (v instanceof Collection || v.getClass().isArray()) {
                throw new NullChainException("不支持的类型:" + v.getClass().getName());
            }
            //默认会排除掉null的node
            String jsonString = JSON.toJSONString(v);
            //转化为map
            return JSON.parseObject(jsonString);
        }
        return (Map<String, Object>) v;
    }

    public static Map<String, Object> fileValuetoMap(Object v) throws IllegalAccessException {
        if (v == Void.TYPE) {
            return new HashMap<>();
        }
        // 如果v本身就是Map类型，直接返回，不需要特殊处理
        if (v instanceof Map) {
            return (Map<String, Object>) v;
        }
        //这个会把类中的字节给转化为字符串了,需要单独取出来
        Map<String, Object> map = valuetoMap(v);
        Set<String> strings = map.keySet();
        Class<?> aClass = v.getClass();
        for (String field : strings) {
            //去类中找对应的属性把值取出来
            Field field1 = FieldUtil.getField(aClass, field);
            //如果是空的,那么就看看这个字段上是否有@JSONField(name="file")这个注解 ,如果name和field一样,那么就是这个字段
            if (Null.is(field1)) {
                Field[] fields = AnnotationUtil.getAnnotationsFields(aClass, JSONField.class).toArray(new Field[0]);
                //找到符合的
                for (Field field2 : fields) {
                    JSONField annotation = field2.getAnnotation(JSONField.class);
                    if (annotation.name().equals(field)) {
                        field2.setAccessible(true);
                        field1 = field2;
                        break;
                    }
                }
            }
            // 如果还是找不到字段，跳过（可能是Map中的key，但对象中没有对应字段）
            if (Null.is(field1)) {
                continue;
            }
            field1.setAccessible(true);
            Object o = field1.get(v);
            //获取类型,如果是byte[] 或者File... 那么就处理
            if (
                    o instanceof File ||
                            o instanceof File[] ||
                            o instanceof Collection ||
                            o instanceof byte[] ||
                            o instanceof byte[][]
            ) {
                if (o instanceof Collection) {
                    //获取第一个数据判断是File类型或者byte[]类型
                    Object o1 = ((Collection) o).iterator().next();
                    if (o1 instanceof File) {
                        map.put(field, o);
                        continue;
                    }
                    if (o1 instanceof byte[]) {
                        map.put(field, o);
                    }
                } else {
                    map.put(field, o);
                }
            }

        }
        return map;
    }


    public static StringBuilder valueToUrl(String url, Object value) throws UnsupportedEncodingException {
        //判断是否有?
        String params = "";
        int index = url.indexOf("?");
        if (index != -1) {
            params = url.substring(index + 1);
            url = url.substring(0, index);
        }

        //如果value 是Void.TYPE
        if (value == Void.TYPE) {
            return new StringBuilder(url);
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        //如果是字符串
        if (value instanceof String) {
            String paramValue = (String) value;
            //去掉多余的 比如开头的?或者&符号,结尾的&符号
            if (paramValue.startsWith("?") || paramValue.startsWith("&")) {
                paramValue = paramValue.substring(1);
            }
            if (paramValue.endsWith("&")) {
                paramValue = paramValue.substring(0, paramValue.length() - 1);
            }
            //对内容进行编码,避免特殊字符导致请求失败
            String[] split = paramValue.split("&");
            urlBuilder.append("?");
            for (String s : split) {
                String[] split1 = s.split("=");
                //如果长度不是2,那么就没有值
                if (split1.length != 2) {
                    urlBuilder.append(URLEncoder.encode(split1[0], "utf-8")).append("&");
                } else {
                    urlBuilder.append(URLEncoder.encode(split1[0], "utf-8")).append("=").append(URLEncoder.encode(split1[1], "utf-8")).append("&");
                }
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        } else {
            Map<String, Object> formMap = valuetoMap(value);
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                Object value1 = entry.getValue();
                //校验value必须是基本数据类型和字符串
                if (!ClassIdentifyUtil.isPrimitiveOrWrapperOrString(value1.getClass())){
                    throw new NullChainException(value.getClass()+"内的值类型必须是基本数据类型(包装)或者字符串,不支持:" + value1.getClass().getName());
                }
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                        append("=").
                        append(URLEncoder.encode(value1.toString(), "utf-8")).
                        append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        //如果有参数,那么拼接上去
        if (Null.non(params)) {
            //识别是否有参数
            if (urlBuilder.indexOf("?") != -1) {
                urlBuilder.append("&").append(params);
            } else {
                urlBuilder.append("?").append(params);
            }
        }
        return urlBuilder;
    }

    /**
     * 处理 SSE 流式响应
     * 
     * <p>该方法用于处理 HTTP 响应为 SSE 流的情况。如果响应 Content-Type 为 text/event-stream，
     * 则按照 SSE 协议解析并触发相应的事件回调；如果响应不是 SSE 格式，则触发非 SSE 响应回调。</p>
     * 
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>执行 HTTP 请求（支持重试）</li>
     *   <li>检查 Content-Type 是否为 text/event-stream</li>
     *   <li>如果是 SSE：按照 SSE 协议解析流，触发相应回调</li>
     *   <li>如果不是 SSE：读取响应体，调用 onNonSseResponse</li>
     *   <li>错误处理：非 2xx 响应或异常时调用 onError</li>
     * </ol>
     * 
     * @param url HTTP请求的URL
     * @param okHttpClient OkHttp客户端实例
     * @param request 请求构建器
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param <T> SSE 数据类型
     */
    public static <T> void toSSE(String url, OkHttpClient okHttpClient, Request.Builder request, 
                                 int retryCount, long retryInterval, 
                                 SSEEventListener<T> listener, DataDecoder<T> decoder) {
        // SSE 处理使用自定义重试逻辑，错误通过回调处理，不抛出异常
        int attempt = 0;
        IOException lastException = null;
        
        // 如果重试次数为0，直接执行一次
        if (retryCount == 0) {
            attempt = 1;
            try {
                executeSseRequest(okHttpClient, request, listener, decoder, attempt);
                return;
            } catch (IOException e) {
                log.error("{}请求失败，未配置重试", url, e);
                listener.onError(attempt, null, "请求失败: " + e.getMessage(), e);
                return;
            }
        }
        
        // 执行重试逻辑
        for (int i = 1; i <= retryCount; i++) {
            attempt = i;
            try {
                executeSseRequest(okHttpClient, request, listener, decoder, attempt);
                return; // 成功则返回
            } catch (IOException e) {
                lastException = e;
                log.error("{}请求失败开始重试次数：{}", url, i, e);
                // 如果还有重试机会，则等待后继续
                if (i < retryCount) {
                    try {
                        Thread.sleep(retryInterval * i);
                    } catch (InterruptedException e1) {
                        log.warn("重试线程被中断", e1);
                        Thread.currentThread().interrupt();
                        listener.onError(attempt, null, "重试被中断: " + e1.getMessage(), e1);
                        return;
                    }
                }
            }
        }
        
        // 所有重试都失败，通过回调处理
        if (lastException != null) {
            listener.onError(attempt, null, "重试" + retryCount + "次后仍然失败: " + lastException.getMessage(), lastException);
        }
    }

    /**
     * 执行 SSE 请求
     * 
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param attempt 当前尝试次数
     * @param <T> SSE 数据类型
     * @throws IOException 网络异常
     */
    private static <T> void executeSseRequest(OkHttpClient okHttpClient, Request.Builder request,
                                              SSEEventListener<T> listener, DataDecoder<T> decoder, int attempt) throws IOException {
        Response response = null;
        ResponseBody body = null;
        try {
            response = okHttpClient.newCall(request.build()).execute();
            body = response.body();
            
            // 检查响应状态
            if (!response.isSuccessful()) {
                String errorBody = body != null ? body.string() : "无响应体";
                closeResponse(response, body);
                listener.onError(attempt, response.code(), "HTTP请求失败: " + errorBody, null);
                return;
            }
            
            if (body == null) {
                closeResponse(response, null);
                listener.onError(attempt, response.code(), "响应体为空", null);
                return;
            }
            
            // 检查 Content-Type，判断是否为 SSE 流
            String contentType = response.header("Content-Type");
            boolean isSseStream = contentType != null && contentType.toLowerCase().contains("text/event-stream");
            
            if (isSseStream) {
                // 处理 SSE 流
                handleSseStream(response, body, listener, decoder);
            } else {
                // 处理非 SSE 响应
                String responseBody = body.string();
                listener.onOpen();
                listener.onNonSseResponse(responseBody, contentType);
                listener.onComplete();
                closeResponse(response, body);
            }
        } catch (IOException e) {
            closeResponse(response, body);
            throw e; // 重新抛出，让重试逻辑处理
        } catch (Exception e) {
            closeResponse(response, body);
            listener.onError(attempt, null, "处理异常: " + e.getMessage(), e);
            throw new IOException("SSE处理异常", e);
        }
    }

    /**
     * 处理 SSE 流响应
     * 按照 SSE 协议解析流式数据，逐帧分发事件
     * 
     * @param response HTTP 响应对象
     * @param body 响应体对象
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param <T> SSE 数据类型
     */
    private static <T> void handleSseStream(Response response, ResponseBody body, 
                                          SSEEventListener<T> listener, DataDecoder<T> decoder) {
        // 创建流控制器，用于支持用户主动终止
        SSEStreamController controller = SSEStreamController.create();
        boolean finished = false;
        boolean interrupted = false;
        try (BufferedSource source = body.source()) {
            listener.onOpen();
            
            String id = null;
            String event = null;
            StringBuilder dataBuilder = new StringBuilder();
            Long retry = null;

            while (true) {
                // 每次读取一行后检查终止标志
                if (controller.isTerminated()) {
                    interrupted = true;
                    break;
                }
                
                String line = source.readUtf8Line();
                if (line == null) {
                    // 流结束
                    break;
                }
                
                // 读取完当前行后再次检查终止标志
                if (controller.isTerminated()) {
                    interrupted = true;
                    break;
                }
                
                if (line.isEmpty()) {
                    // 空行表示一个完整事件帧的结束
                    EventMessage<T> msg = new EventMessage<>();
                    msg.setId(id);
                    msg.setEvent(event);
                    String dataRaw = dataBuilder.length() > 0 ? dataBuilder.toString() : null;
                    // 移除末尾的换行符
                    if (dataRaw != null && dataRaw.endsWith("\n")) {
                        dataRaw = dataRaw.substring(0, dataRaw.length() - 1);
                    }
                    msg.setDataRaw(dataRaw);
                    
                    // 使用 decoder 解码数据
                    T parsed = null;
                    if (dataRaw != null) {
                        try {
                            parsed = decoder.decode(dataRaw);
                        } catch (Exception e) {
                            log.warn("SSE数据解码失败: {}", e.getMessage());
                            // 解码失败时保持 parsed 为 null
                        }
                    }
                    msg.setData(parsed);
                    msg.setRetry(retry);
                    // 设置控制器引用，允许用户通过 msg.terminate() 主动终止
                    msg.setController(controller);
                    
                    // 判断是否终止（通过 shouldTerminate 方法）
                    boolean terminate;
                    try {
                        terminate = listener.shouldTerminate(msg);
                    } catch (Exception e) {
                        log.warn("shouldTerminate 判断异常: {}", e.getMessage());
                        terminate = false;
                    }
                    
                    if (terminate) {
                        listener.onComplete();
                        finished = true;
                        break;
                    }
                    
                    // 检查用户是否主动终止（通过 msg.terminate() 调用）
                    if (controller.isTerminated()) {
                        interrupted = true;
                        break;
                    }
                    
                    // 分发事件
                    try {
                        listener.onEvent(msg);
                    } catch (Exception e) {
                        log.warn("onEvent 处理异常: {}", e.getMessage());
                    }
                    
                    // 检查用户是否在 onEvent 中调用了 terminate()
                    if (controller.isTerminated()) {
                        interrupted = true;
                        break;
                    }
                    
                    // 重置状态，准备下一个事件帧
                    id = null;
                    event = null;
                    dataBuilder.setLength(0);
                    retry = null;
                    continue;
                }
                
                // 跳过注释行（以冒号开头）
                if (line.startsWith(":")) {
                    continue;
                }
                
                // 解析字段：field: value
                int idx = line.indexOf(":");
                if (idx <= 0) {
                    continue;
                }
                
                String field = line.substring(0, idx);
                String value = line.substring(idx + 1);
                // 移除值前面的空格
                if (value.startsWith(" ")) {
                    value = value.substring(1);
                }
                
                switch (field) {
                    case "id":
                        id = value;
                        break;
                    case "event":
                        event = value;
                        break;
                    case "data":
                        // 支持多行 data，用换行符连接
                        dataBuilder.append(value).append("\n");
                        break;
                    case "retry":
                        try {
                            retry = Long.parseLong(value);
                        } catch (NumberFormatException e) {
                            log.warn("SSE retry 字段格式错误: {}", value);
                            retry = null;
                        }
                        break;
                    default:
                        // 忽略未知字段
                        break;
                }
            }

            // 根据结束方式调用不同的回调
            if (interrupted) {
                // 用户主动终止，调用 onInterrupt
                listener.onInterrupt();
            } else if (!finished) {
                // 流正常结束，调用 onComplete
                listener.onComplete();
            }
        } catch (Exception e) {
            log.error("SSE流处理异常", e);
            listener.onError(0, null, "SSE流处理异常: " + e.getMessage(), e);
        } finally {
            // 确保响应资源被关闭
            closeResponse(response, body);
        }
    }

    /**
     * 安全关闭响应资源
     * 
     * @param response HTTP响应对象
     * @param body 响应体对象
     */
    private static void closeResponse(Response response, ResponseBody body) {
        if (body != null) {
            try {
                body.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
        if (response != null) {
            try {
                response.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
    }

}
