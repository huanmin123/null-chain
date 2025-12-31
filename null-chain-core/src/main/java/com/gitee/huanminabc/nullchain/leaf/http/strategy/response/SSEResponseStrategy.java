package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.sse.DataDecoder;
import com.gitee.huanminabc.nullchain.leaf.http.sse.EventMessage;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEEventListener;
import com.gitee.huanminabc.nullchain.leaf.http.sse.SSEStreamController;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * SSE流式响应处理策略
 * 
 * <p>处理HTTP响应为SSE流的情况。如果响应Content-Type为text/event-stream，
 * 则按照SSE协议解析并触发相应的事件回调；如果响应不是SSE格式，则触发非SSE响应回调。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class SSEResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        // 参数已在调用方校验，直接使用
        @SuppressWarnings("unchecked")
        SSEEventListener<Object> listener = (SSEEventListener<Object>) params[0];
        
        @SuppressWarnings("unchecked")
        DataDecoder<Object> decoder = (DataDecoder<Object>) params[1];
        
        // 处理 SSE 流式响应
        toSSE(url, okHttpClient, request, retryCount, retryInterval, listener, decoder);
        
        // SSE是异步处理，不返回结果
        return null;
    }

    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.SSE;
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
    private <T> void toSSE(String url, OkHttpClient okHttpClient, Request.Builder request, 
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
     * <p>SSE是长连接流式响应，需要将读取超时设置为无限，避免读取过程中超时中断。</p>
     * 
     * @param okHttpClient OkHttp客户端
     * @param request 请求构建器
     * @param listener SSE 事件监听器
     * @param decoder 数据解码器
     * @param attempt 当前尝试次数
     * @param <T> SSE 数据类型
     * @throws IOException 网络异常
     */
    private <T> void executeSseRequest(OkHttpClient okHttpClient, Request.Builder request,
                                      SSEEventListener<T> listener, DataDecoder<T> decoder, int attempt) throws IOException {
        // SSE是长连接流式响应，需要将读取超时设置为无限（0表示无限）
        OkHttpClient sseClient = okHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
        
        Response response = null;
        ResponseBody body = null;
        try {
            response = sseClient.newCall(request.build()).execute();
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
    private <T> void handleSseStream(Response response, ResponseBody body, 
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
    private void closeResponse(Response response, ResponseBody body) {
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

