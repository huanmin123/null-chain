package com.gitee.huanminabc.nullchain.leaf.http;

import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.jcommon.str.StringUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import lombok.extern.slf4j.Slf4j;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.InputStream;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author huanmin
 * @date 2024/11/30
 */
@Slf4j
public class OkHttpBase<T> extends NullKernelAbstract implements OkHttp<T> {
    public static final String DEFAULT_THREAD_FACTORY_NAME = "$$$--NULL_DEFAULT_OKHTTP_SYNC--$$$";
    private OkHttpClient okHttpClient;
    private Map<String, String> headerMap;
    private Request.Builder request;
    private String url;
    /**
     * 重试次数，默认3次
     */
    private int retryCount = 3;
    /**
     * 重试间隔时间（毫秒），默认100毫秒
     */
    private long retryInterval = 100;

    private void setUrl(String url) {
        //加工url,如果结尾是/或者?那么去掉
        if (url.endsWith("/") || url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    public OkHttpBase(String url, StringBuilder linkLog, NullTaskList taskList) {
        this(DEFAULT_THREAD_FACTORY_NAME, url, linkLog, taskList);
    }

    public OkHttpBase(String httpName, String url, StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
        this.okHttpClient = OkHttpBuild.getOkHttp(httpName);
        setUrl(url);
    }


    //设置连接超时时间(建议比系统最大超时时间低一些,比如rpc设置的15s断开,那么这里设置14s)
    public OkHttp connectTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value) -> {
            linkLog.append(HTTP_CONNECT_TIMEOUT_ARROW);
            okHttpClient = okHttpClient.newBuilder().connectTimeout(time, timeUnit).build();
            return NullBuild.noEmpty(value);
        });
        return this;
    }

    //设置写入超时时间(一般不调整)
    public OkHttp writeTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value) -> {
            linkLog.append(HTTP_WRITE_TIMEOUT_ARROW);
            okHttpClient = okHttpClient.newBuilder().writeTimeout(time, timeUnit).build();
            return NullBuild.noEmpty(value);
        });
        return this;
    }

    //设置读取超时时间(一般不调整)
    public OkHttp readTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value) -> {
            linkLog.append(HTTP_READ_TIMEOUT_ARROW);
            okHttpClient = okHttpClient.newBuilder().readTimeout(time, timeUnit).build();
            return NullBuild.noEmpty(value);
        });
        return this;
    }

    //设置代理
    //设置代理方式
    //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
    public OkHttp proxy(Proxy proxy) {
        this.taskList.add((value) -> {
            linkLog.append(HTTP_PROXY_ARROW);
            okHttpClient = okHttpClient.newBuilder().proxy(proxy).build();
            return NullBuild.noEmpty(value);
        });
        return this;
    }

    //自定义线程池
    //new ConnectionPool(500, 10, TimeUnit.MINUTES)
    private OkHttp connectionPool(ConnectionPool connectionPool) {
        this.taskList.add((value) -> {
            linkLog.append(HTTP_CONNECTION_POOL_ARROW);
            okHttpClient = okHttpClient.newBuilder().connectionPool(connectionPool).build();
            return NullBuild.noEmpty(value);
        });
        return this;
    }


    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttp addHeader(String key, String value) {
        this.taskList.add((preValue) -> {
            linkLog.append(HTTP_ADD_HEADER_ARROW);
            if (headerMap == null) {
                headerMap = new LinkedHashMap<>(16);
            }
            headerMap.put(key, value);
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    /**
     * 设置请求失败时的重试次数
     *
     * @param retryCount 重试次数，必须大于等于0
     * @return OkHttp对象，以便链式调用
     */
    public OkHttp retryCount(int retryCount) {
        this.taskList.add((preValue) -> {
            linkLog.append("->retryCount(").append(retryCount).append(")");
            if (retryCount < 0) {
                throw new NullChainException("重试次数不能小于0");
            }
            this.retryCount = retryCount;
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    /**
     * 设置请求失败时的重试间隔时间
     *
     * @param retryInterval 重试间隔时间（毫秒），必须大于等于0
     * @return OkHttp对象，以便链式调用
     */
    public OkHttp retryInterval(long retryInterval) {
        this.taskList.add((preValue) -> {
            linkLog.append("->retryInterval(").append(retryInterval).append("ms)");
            if (retryInterval < 0) {
                throw new NullChainException("重试间隔时间不能小于0");
            }
            this.retryInterval = retryInterval;
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    @Override
    public OkHttp async() {
        NullKernelAsyncAbstract.async(linkLog, taskList);
        return this;
    }

    @Override
    public OkHttp async(String threadFactoryName) {
        NullKernelAsyncAbstract.async(linkLog, taskList, threadFactoryName);
        return this;
    }


    public OkHttp get() {
        this.taskList.add((preValue) -> {
            request = new Request.Builder().get();
            try {
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, preValue);
                request.url(valueToUrl.toString());
                linkLog.append(HTTP_GET_ARROW);
            } catch (Exception e) {
                linkLog.append(HTTP_GET_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }


    public OkHttp post(OkHttpPostEnum type) {
        this.taskList.add((preValue) -> {
            if (preValue == Void.TYPE) {
                return NullBuild.empty();
            }
            try {
                // 创建请求构建器
                Request.Builder requestBuilder = new Request.Builder().url(url);
                // 使用策略模式构建请求体（会自动提取并添加请求头）
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, preValue, requestBuilder);
                request = requestBuilder.post(requestBody);
                linkLog.append(HTTP_POST_ARROW);
            } catch (Exception e) {
                linkLog.append(HTTP_POST_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }


    public OkHttp put(OkHttpPostEnum type) {
        this.taskList.add((preValue) -> {
            if (preValue == Void.TYPE) {
                return NullBuild.empty();
            }
            try {
                // 创建请求构建器
                Request.Builder requestBuilder = new Request.Builder().url(url);
                // 使用策略模式构建请求体（会自动提取并添加请求头）
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, preValue, requestBuilder);
                request = requestBuilder.put(requestBody);
                linkLog.append(HTTP_PUT_ARROW);
            } catch (Exception e) {
                linkLog.append(HTTP_PUT_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    public OkHttp del() {
        this.taskList.add((preValue) -> {
            try {
                request = new Request.Builder().delete();
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, preValue);
                request.url(valueToUrl.toString());
                linkLog.append(HTTP_DEL_ARROW);
            } catch (Exception e) {
                linkLog.append(HTTP_DEL_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }


    /**
     * 下载文件到指定路径
     *
     * @param filePath 下载内容存储的路径
     * @return true表示下载成功，false表示下载失败
     */
    public NullChain<Boolean> downloadFile(String filePath) {
        this.taskList.add((__) -> {
            if (StringUtil.isEmpty(filePath)) {
                linkLog.append(HTTP_DOWNLOAD_FILE_PATH_NULL);
                throw new NullChainException(linkLog.toString());
            }
            try {
                OkHttpBuild.setHeader(headerMap, request);
                Boolean result = (Boolean) OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.FILE_DOWNLOAD, url, okHttpClient, request,
                        retryCount, retryInterval, filePath);
                linkLog.append(HTTP_DOWNLOAD_FILE_ARROW);
                return NullBuild.noEmpty(result);
            } catch (Exception e) {
                linkLog.append(HTTP_DOWNLOAD_FILE_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy(linkLog, taskList);

    }

    /**
     * 获取返回的字节数组
     *
     * @return 包含字节数组的Null链，如果响应为空则返回空链
     */
    public NullChain<byte[]> toBytes() {
        this.taskList.add((__) -> {
            try {
                OkHttpBuild.setHeader(headerMap, request);
                byte[] bytes = (byte[]) OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.BYTES, url, okHttpClient, request,
                        retryCount, retryInterval);
                if (bytes == null) {
                    linkLog.append(HTTP_TO_BYTES_ARROW).append("(empty)");
                    return NullBuild.empty();
                }
                linkLog.append(HTTP_TO_BYTES_ARROW);
                return NullBuild.noEmpty(bytes);
            } catch (Exception e) {
                linkLog.append(HTTP_TO_BYTES_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy(linkLog, taskList);
    }

    /**
     * 获取返回的输入流
     * 注意: 调用者需要负责关闭返回的输入流
     *
     * @return 包含输入流的Null链，如果响应为空则返回空链
     */
    public NullChain<InputStream> toInputStream() {
        this.taskList.add((__) -> {
            try {
                OkHttpBuild.setHeader(headerMap, request);
                InputStream inputStream = (InputStream) OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.INPUT_STREAM, url, okHttpClient, request,
                        retryCount, retryInterval);
                if (inputStream == null) {
                    linkLog.append(HTTP_TO_INPUTSTREAM_ARROW).append("(null)");
                    return NullBuild.empty();
                }
                linkLog.append(HTTP_TO_INPUTSTREAM_ARROW);
                return NullBuild.noEmpty(inputStream);
            } catch (Exception e) {
                linkLog.append(HTTP_TO_INPUTSTREAM_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy(linkLog, taskList);
    }

    /**
     * 获取返回的字符串
     *
     * @return 包含字符串的Null链
     */
    public NullChain<String> toSTR() {
        this.taskList.add((__) -> {
            try {
                OkHttpBuild.setHeader(headerMap, request);
                String str = (String) OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.STRING, url, okHttpClient, request,
                        retryCount, retryInterval);
                if (StringUtil.isEmpty(str)) {
                    linkLog.append(HTTP_TO_STR_Q).append("返回值为空");
                    return NullBuild.empty();
                }
                linkLog.append(HTTP_TO_STR_ARROW);
                return NullBuild.noEmpty(str);
            } catch (Exception e) {
                linkLog.append(HTTP_TO_STR_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy(linkLog, taskList);
    }

    /**
     * 获取返回的字符串并转换为指定类型的对象
     *
     * @param <R>   目标对象类型
     * @param clazz 目标类型的Class对象
     * @return 包含目标类型对象的Null链
     */
    public <R> NullChain<R> toFromJson(Class<R> clazz) {
        this.taskList.add((__) -> {
            try {
                OkHttpBuild.setHeader(headerMap, request);
                @SuppressWarnings("unchecked")
                R result = (R) OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.JSON, url, okHttpClient, request,
                        retryCount, retryInterval, clazz);
                if (Null.is(result)) {
                    linkLog.append(HTTP_TO_STR_Q).append("返回值为空");
                    return NullBuild.empty();
                }
                linkLog.append(HTTP_TO_STR_ARROW).append("->toJson(").append(clazz.getSimpleName()).append(")");
                return NullBuild.noEmpty(result);
            } catch (Exception e) {
                linkLog.append(HTTP_TO_STR_Q).append("转换为").append(clazz.getSimpleName()).append("失败:").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy(linkLog, taskList);
    }

    /**
     * 处理 SSE 流式响应（使用默认字符串解码器）
     *
     * <p>该方法用于处理 HTTP 响应为 SSE 流的情况。如果响应 Content-Type 为 text/event-stream，
     * 则按照 SSE 协议解析并触发相应的事件回调；如果响应不是 SSE 格式，则触发非 SSE 响应回调。</p>
     *
     * <p>此方法使用默认的字符串解码器，直接将原始数据作为字符串返回。</p>
     *
     * @param listener SSE 事件监听器，处理各种 SSE 事件（数据类型为 String）
     */
    public void toSSEText(SSEEventListener<String> listener) {
        toSSE(listener, DataDecoder.stringDecoder());
    }

    public void toSSEJson(SSEEventListener<JSONObject> listener) {
        toSSE(listener, DataDecoder.jsonDecoder());
    }

    /**
     * 处理 SSE 流式响应（自定义解码器）
     *
     * <p>该方法用于处理 HTTP 响应为 SSE 流的情况。如果响应 Content-Type 为 text/event-stream，
     * 则按照 SSE 协议解析并触发相应的事件回调；如果响应不是 SSE 格式，则触发非 SSE 响应回调。</p>
     *
     * @param <R>      SSE 数据解码后的类型
     * @param listener SSE 事件监听器，处理各种 SSE 事件
     * @param decoder  数据解码器，将原始字符串解码为泛型对象
     */
    public <R> void toSSE(SSEEventListener<R> listener, DataDecoder<R> decoder) {
        taskList.runTaskAll((nullChainBase) -> {
            if (listener == null) {
                linkLog.append(HTTP_TO_SSE_Q).append("监听器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            if (decoder == null) {
                linkLog.append(HTTP_TO_SSE_Q).append("解码器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            try {
                // 先执行taskList中的所有任务，初始化request等字段
                taskList.runTaskAll();
                OkHttpBuild.setHeader(headerMap, request);
                OkHttpBuild.handleResponse(
                        OkHttpResponseEnum.SSE, url, okHttpClient, request,
                        retryCount, retryInterval, listener, decoder);
                linkLog.append(HTTP_TO_SSE_ARROW);
            } catch (Exception e) {
                linkLog.append(HTTP_TO_SSE_Q).append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        }, null);

    }

}

