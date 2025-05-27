package com.gitee.huanminabc.nullchain.member.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.gitee.huanminabc.common.reflect.AnnotationUtil;
import com.gitee.huanminabc.common.reflect.FieldUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.function.NullHttpSupplierEx;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
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
        OkHttpBuild.okHttpClientConcurrentHashMap.put(OkHttp.DEFAULT_THREAD_FACTORY_NAME, createOkHttpClient(LIMIT_MAXIMUM_POOL_SIZE));
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

    public static boolean downloadFile(String url, String filePath, OkHttpClient okHttpClient, Request.Builder request) {
        return retry(url, () -> {
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

    public static String toStr(String url, OkHttpClient okHttpClient, Request.Builder request) {
        return retry(url, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().string();
            }

        });
    }

    public static InputStream toInputStream(String url, OkHttpClient okHttpClient, Request.Builder request) {
        return retry(url, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().byteStream();
            }
        });

    }

    public static byte[] toBytes(String url, OkHttpClient okHttpClient, Request.Builder request) {
        return retry(url, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return null;
                }
                return response.body().bytes();
            }
        });
    }

    //重试3次,不行就拉倒, 就剩网络波动也不可能连续3次失败
    private static <T> T retry(String url, NullHttpSupplierEx<T> runnable) {
        for (int i = 1; i <= 3; i++) {
            try {
                return runnable.get();
            } catch (IOException e) {
                log.error("{}请求失败开始重试次数：{}", url, i, e);
                try {
                    Thread.sleep(100 * i);
                } catch (InterruptedException e1) {
                    log.warn("重试线程被中断", e1);
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new NullChainException(url + "重试" + 3 + "次还是请求失败");
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
                    if (value1 instanceof String) {
                        throw new NullChainException("FORM类型value值只能是字符串");
                    }
                    formBody.add(key, String.valueOf(value1));
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
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                        append("=").
                        append(URLEncoder.encode((String) entry.getValue(), "utf-8")).
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

}
