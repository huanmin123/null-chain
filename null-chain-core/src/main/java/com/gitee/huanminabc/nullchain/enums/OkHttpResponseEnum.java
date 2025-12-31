package com.gitee.huanminabc.nullchain.enums;

/**
 * HTTP响应处理类型枚举
 * 
 * <p>定义不同的HTTP响应处理方式，用于策略模式选择对应的响应处理策略。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public enum OkHttpResponseEnum {
    /** 字符串响应（对应 toStr/toSTR 方法） */
    STRING,
    
    /** 字节数组响应（对应 toBytes 方法） */
    BYTES,
    
    /** 输入流响应（对应 toInputStream 方法） */
    INPUT_STREAM,
    
    /** 文件下载响应（对应 downloadFile 方法） */
    FILE_DOWNLOAD,
    
    /** JSON对象响应（对应 toFromJson 方法） */
    JSON,
    
    /** SSE流式响应（对应 toSSE 方法） */
    SSE
}

