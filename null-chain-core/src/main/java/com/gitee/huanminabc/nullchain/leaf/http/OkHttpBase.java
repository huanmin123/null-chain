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
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEController;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketController;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.websocket.WebSocketHeartbeatHandler;
import lombok.extern.slf4j.Slf4j;

import static com.gitee.huanminabc.nullchain.common.NullLog.*;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    
    /**
     * 心跳处理器
     */
    private WebSocketHeartbeatHandler heartbeatHandler;
    
    /**
     * 心跳间隔时间（毫秒），默认30000毫秒（30秒）
     */
    private long heartbeatInterval = 30000;
    
    /**
     * 心跳超时时间（毫秒），默认10000毫秒（10秒）
     */
    private long heartbeatTimeout = 10000;
    
    /**
     * WebSocket 子协议列表
     */
    private List<String> subprotocols;

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
            if (retryCount < 0) {
                linkLog.append("->retryCount(").append(retryCount).append(")?").append(" 重试次数不能小于0");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("->retryCount(").append(retryCount).append(")->");
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
            if (retryInterval < 0) {
                linkLog.append("->retryInterval(").append(retryInterval).append("ms)?").append(" 重试间隔时间不能小于0");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("->retryInterval(").append(retryInterval).append("ms)->");
            this.retryInterval = retryInterval;
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    /**
     * 配置 WebSocket 心跳检测（使用默认间隔和超时时间）
     *
     * <p>设置心跳处理器，使用默认的心跳间隔（30秒）和超时时间（10秒）。
     * 心跳检测会在连接建立后自动启动。</p>
     *
     * @param handler 心跳处理器，用户实现心跳消息生成和回复判断逻辑
     * @return OkHttp对象，以便链式调用
     */
    public OkHttp heartbeat(WebSocketHeartbeatHandler handler) {
        return heartbeat(handler, 30000, 10000);
    }

    /**
     * 配置 WebSocket 心跳检测
     *
     * <p>设置心跳处理器、心跳间隔和超时时间。心跳检测会在连接建立后自动启动。</p>
     *
     * @param handler 心跳处理器，用户实现心跳消息生成和回复判断逻辑
     * @param interval 心跳间隔时间（毫秒），默认30000（30秒）
     * @param timeout 心跳超时时间（毫秒），默认10000（10秒）
     * @return OkHttp对象，以便链式调用
     */
    public OkHttp heartbeat(WebSocketHeartbeatHandler handler, long interval, long timeout) {
        this.taskList.add((preValue) -> {
            if (handler == null) {
                linkLog.append("heartbeat?").append(" 心跳处理器不能为空");
                throw new NullChainException(linkLog.toString());
            }
            if (interval <= 0) {
                linkLog.append("heartbeat?").append(" 心跳间隔时间必须大于0");
                throw new NullChainException(linkLog.toString());
            }
            if (timeout <= 0) {
                linkLog.append("heartbeat?").append(" 心跳超时时间必须大于0");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("->heartbeat(interval:").append(interval).append("ms, timeout:").append(timeout).append("ms)->");
            this.heartbeatHandler = handler;
            this.heartbeatInterval = interval;
            this.heartbeatTimeout = timeout;
            return NullBuild.noEmpty(preValue);
        });
        return this;
    }

    /**
     * 配置 WebSocket 子协议
     *
     * <p>设置支持的子协议列表。服务器会从列表中选择一个支持的协议返回。
     * 如果服务器返回的协议不在列表中，连接将失败。</p>
     *
     * @param protocols 支持的子协议列表
     * @return OkHttp对象，以便链式调用
     */
    public OkHttp subprotocol(String... protocols) {
        this.taskList.add((preValue) -> {
            linkLog.append("->subprotocol(");
            if (protocols != null && protocols.length > 0) {
                for (int i = 0; i < protocols.length; i++) {
                    if (i > 0) {
                        linkLog.append(", ");
                    }
                    linkLog.append(protocols[i]);
                }
            }
            linkLog.append(")");
            
            if (protocols == null || protocols.length == 0) {
                this.subprotocols = null;
            } else {
                this.subprotocols = new ArrayList<>();
                for (String protocol : protocols) {
                    if (protocol != null && !protocol.trim().isEmpty()) {
                        this.subprotocols.add(protocol.trim());
                    }
                }
                if (this.subprotocols.isEmpty()) {
                    this.subprotocols = null;
                }
            }
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
    public SSEController toSSEText(SSEEventListener<String> listener) {
        return toSSE(listener, DataDecoder.stringDecoder());
    }

    public SSEController toSSEJson(SSEEventListener<JSONObject> listener) {
        return toSSE(listener, DataDecoder.jsonDecoder());
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
     * @return SSE 控制器，可用于管理连接状态和关闭连接
     */
    public <R> SSEController toSSE(SSEEventListener<R> listener, DataDecoder<R> decoder) {
        if (listener == null) {
            linkLog.append(HTTP_TO_SSE_Q).append("监听器不能为空");
            throw new NullChainException(linkLog.toString());
        }
        if (decoder == null) {
            linkLog.append(HTTP_TO_SSE_Q).append("解码器不能为空");
            throw new NullChainException(linkLog.toString());
        }
        // 先执行taskList中的所有任务，初始化request等字段
        NullTaskList.NullNode<Object> nullNode = taskList.runTaskAll();
        if (nullNode.isNull) {
            linkLog.append(HTTP_TO_SSE_Q).append("任务链执行返回空值");
            throw new NullChainException(linkLog.toString());
        }

        try {
            OkHttpBuild.setHeader(headerMap, request);
            SSEController controller = (SSEController) OkHttpBuild.handleResponse(
                    OkHttpResponseEnum.SSE, url, okHttpClient, request,
                    retryCount, retryInterval, listener, decoder);
            if (controller == null) {
                linkLog.append(HTTP_TO_SSE_Q).append("SSEController创建失败，返回null");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(HTTP_TO_SSE_ARROW);
            return controller;
        } catch (NullChainException e) {
            // 如果是NullChainException，直接抛出，不重复包装
            throw e;
        } catch (Exception e) {
            linkLog.append(HTTP_TO_SSE_Q).append(e.getMessage());
            throw new NullChainException(e, linkLog.toString());
        }
    }

    /**
     * 处理 WebSocket 连接
     *
     * <p>该方法用于建立 WebSocket 连接，提供自定义事件监听器接口。
     * 支持智能重连机制：区分消息重发（有消息队列时）和连接重连（无消息队列时）。</p>
     *
     * <h3>使用说明：</h3>
     * <ul>
     *   <li>连接建立：会调用 {@link WebSocketEventListener#onOpen(WebSocketController)}</li>
     *   <li>接收消息：会调用 {@link WebSocketEventListener#onMessage(WebSocketController, String)} 或
     *       {@link WebSocketEventListener#onMessage(WebSocketController, byte[])}</li>
     *   <li>错误处理：通过 {@link WebSocketEventListener#onError(WebSocketController, Throwable, String)} 回调</li>
     *   <li>连接关闭：通过 {@link WebSocketEventListener#onClose(WebSocketController, int, String)} 回调</li>
     *   <li>自动重连：使用全局的 retryCount 和 retryInterval 配置</li>
     * </ul>
     *
     * @param listener WebSocket 事件监听器，处理各种 WebSocket 事件
     * @return WebSocketController 实例，可用于发送消息和关闭连接
     */
    public WebSocketController toWebSocket(WebSocketEventListener listener) {
        if (listener == null) {
            linkLog.append(HTTP_TO_WEBSOCKET_Q).append("监听器不能为空");
            throw new NullChainException(linkLog.toString());
        }
        // 先执行taskList中的所有任务，初始化request等字段
        NullTaskList.NullNode<Object> nullNode = taskList.runTaskAll();
        if (nullNode.isNull) {
            linkLog.append(HTTP_TO_WEBSOCKET_Q).append("任务链执行返回空值");
            throw new NullChainException(linkLog.toString());
        }

        try {
            //因为不需要get post ... 协议所以创建无协议的request
            request = new Request.Builder();
            OkHttpBuild.setHeader(headerMap, request);
            if (url == null || url.isEmpty()) {
                linkLog.append(HTTP_TO_WEBSOCKET_Q).append("URL不能为空");
                throw new NullChainException(linkLog.toString());
            }
            WebSocketController controller = (WebSocketController) OkHttpBuild.handleResponse(
                    OkHttpResponseEnum.WEBSOCKET, url, okHttpClient, request,
                    retryCount, retryInterval, listener, heartbeatHandler, heartbeatInterval, heartbeatTimeout, subprotocols);
            if (controller == null) {
                linkLog.append(HTTP_TO_WEBSOCKET_Q).append("WebSocketController创建失败，返回null");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(HTTP_TO_WEBSOCKET_ARROW);
            return controller;
        } catch (NullChainException e) {
            // 如果是NullChainException，直接抛出，不重复包装
            throw e;
        } catch (Exception e) {
            linkLog.append(HTTP_TO_WEBSOCKET_Q).append( e.getMessage());
            throw new NullChainException(e, linkLog.toString());
        }
    }

}

