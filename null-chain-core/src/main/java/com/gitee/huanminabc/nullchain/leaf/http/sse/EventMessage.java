package com.gitee.huanminabc.nullchain.leaf.http.sse;

import lombok.Data;

/**
 * SSE 事件消息封装类
 * 
 * <p>封装了 SSE 协议中的一条完整事件消息，包括消息 ID、事件类型、
 * 原始数据、解码后的数据以及重试间隔等信息。</p>
 * 
 * @param <T> 解码后的数据类型
 * @author huanmin
 * @since 1.0.0
 */
@Data
public class EventMessage<T> {
    /** 消息ID（对应SSE的id字段） */
    private String id;
    
    /** 事件类型（对应SSE的event字段） */
    private String event;
    
    /** 原始消息内容（原始SSE的data文本） */
    private String dataRaw;
    
    /** 已转换的消息内容（泛型） */
    private T data;
    
    /** 重连间隔（对应SSE的retry字段，可选） */
    private Long retry;
    
    /** SSE 流控制器，用于控制流的终止 */
    private SSEStreamController controller;

    /**
     * 请求终止 SSE 流
     * 
     * <p>调用此方法后，流会在读取完当前行后停止，并触发 {@link SSEEventListener#onInterrupt()} 回调。</p>
     * 
     * <p>如果控制器未设置，此方法不会产生任何效果。</p>
     * 
     * @example
     * <pre>{@code
     * public void onEvent(EventMessage<JSONObject> msg) {
     *     JSONObject data = msg.getData();
     *     // 处理数据...
     *     
     *     // 如果发现结果不对，主动终止
     *     if (shouldStop(data)) {
     *         msg.terminate();  // 终止流
     *     }
     * }
     * }</pre>
     */
    public void terminate() {
        if (controller != null) {
            controller.terminate();
        }
    }
}

