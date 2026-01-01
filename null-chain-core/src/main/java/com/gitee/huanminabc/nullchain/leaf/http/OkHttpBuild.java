package com.gitee.huanminabc.nullchain.leaf.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.gitee.huanminabc.jcommon.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.function.NullHttpSupplierEx;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.dto.FileBinary;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.*;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.request.FormRequestStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.request.JsonRequestStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.request.MultipartRequestStrategy;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.response.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpBuild {
    public final static ConcurrentHashMap<String, OkHttpClient> okHttpClientConcurrentHashMap = new ConcurrentHashMap<>();
    public final static int LIMIT_MAXIMUM_POOL_SIZE = 1000;
    
    
    /**
     * 请求体构建策略注册表
     */
    private static final List<RequestStrategy> strategies = new ArrayList<>();
    
    /**
     * 响应处理策略注册表
     */
    private static final List<ResponseStrategy> responseStrategies = new ArrayList<>();
    
    static {
        // 注册默认请求体构建策略
        strategies.add(new JsonRequestStrategy());
        strategies.add(new FormRequestStrategy());
        strategies.add(new MultipartRequestStrategy());
        
        // 注册默认响应处理策略
        responseStrategies.add(new StringResponseStrategy());
        responseStrategies.add(new BytesResponseStrategy());
        responseStrategies.add(new InputStreamResponseStrategy());
        responseStrategies.add(new FileDownloadResponseStrategy());
        responseStrategies.add(new JsonResponseStrategy());
        responseStrategies.add(new SSEResponseStrategy());
        responseStrategies.add(new WebSocketResponseStrategy());
    }

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
                .writeTimeout(30, TimeUnit.SECONDS)
                //读取超时时间 默认是10s (一般不调整)
                .readTimeout(30, TimeUnit.SECONDS)
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
     * <p>提供统一的HTTP请求重试逻辑，支持所有响应处理策略共享使用。</p>
     * 
     * @param url 请求的URL
     * @param retryCount 重试次数
     * @param retryInterval 基础重试间隔（毫秒），实际间隔为：retryInterval * 当前重试次数
     * @param runnable 执行的HTTP请求操作
     * @param <T> 返回类型
     * @return 请求结果
     * @throws NullChainException 当所有重试都失败时抛出异常
     */
    public static <T> T retry(String url, int retryCount, long retryInterval, NullHttpSupplierEx<T> runnable) {
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

    /**
     * 构建请求体（使用策略模式）
     * 
     * @param type 请求类型
     * @param value__ 请求数据对象
     * @param requestBuilder 请求构建器，用于添加请求头等
     * @return 构建好的请求体
     * @throws Exception 构建过程中可能出现的异常
     */
    public static RequestBody requestBodyHandel(OkHttpPostEnum type, Object value__, Request.Builder requestBuilder) throws Exception {
        // 查找支持该类型的策略
        RequestStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new NullChainException("不支持的请求类型: " + type));
        
        // 如果 value__ 是 Void.TYPE，返回空请求体
        if (value__ == Void.TYPE) {
            return RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        }
        
        // 使用策略构建请求体
        return strategy.build(value__, requestBuilder);
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
     * 获取字段名：优先使用 @JSONField 的 name，否则使用字段名
     *
     * @param field 字段
     * @return 字段名
     */
    public static String getFieldName(Field field) {
        JSONField jsonField = field.getAnnotation(JSONField.class);
        if (jsonField != null && jsonField.name() != null && !jsonField.name().isEmpty()) {
            return jsonField.name();
        }
        return field.getName();
    }

    /**
     * 判断值是否是文件类型
     * 
     * <p>支持的文件类型包括：</p>
     * <ul>
     *   <li>{@link FileBinary} - 文件二进制数据传输对象</li>
     *   <li>{@link File} - Java文件对象</li>
     *   <li>{@link File}[] - 文件数组</li>
     *   <li>{@link Collection}&lt;{@link File}&gt; - 文件集合</li>
     *   <li>{@link Collection}&lt;{@link FileBinary}&gt; - 文件二进制对象集合</li>
     * </ul>
     * 
     * @param value 要判断的值
     * @return 如果是文件类型返回 true，否则返回 false
     */
    public static boolean isFileType(Object value) {
        if (value == null) {
            return false;
        }
        
        // 支持 FileBinaryDTO
        if (value instanceof FileBinary) {
            return true;
        }
        
        // 支持 File
        if (value instanceof File) {
            return true;
        }
        
        // 支持 File[]
        if (value instanceof File[]) {
            return true;
        }
        
        // 支持 Collection<File> 或 Collection<FileBinaryDTO>
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return false;
            }
            Object firstItem = collection.iterator().next();
            return firstItem instanceof File || firstItem instanceof FileBinary;
        }
        
        return false;
    }

    /**
     * 处理HTTP响应（使用策略模式）
     * 
     * @param type 响应类型
     * @param url 请求URL
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param params 额外参数（如文件路径、Class类型、SSE监听器等）
     * @return 处理结果
     * @throws Exception 处理过程中可能出现的异常
     */
    public static Object handleResponse(OkHttpResponseEnum type, String url, OkHttpClient okHttpClient, 
                                       Request.Builder request, int retryCount, long retryInterval, 
                                       Object... params) throws Exception {
        // 查找支持该类型的策略
        ResponseStrategy strategy = responseStrategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new NullChainException("不支持的响应类型: " + type));
        
        // 使用策略处理响应
        return strategy.handle(url, okHttpClient, request, retryCount, retryInterval, params);
    }


}
