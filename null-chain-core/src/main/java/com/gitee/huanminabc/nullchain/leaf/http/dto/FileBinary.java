package com.gitee.huanminabc.nullchain.leaf.http.dto;

import lombok.ToString;

/**
 * 文件二进制数据传输对象
 * 
 * <p>用于封装文件上传所需的二进制数据、文件名和内容类型信息。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@ToString(exclude = {"content"})
public class FileBinary {
    /** 文件名 */
    public final String fileName;
    /** 文件二进制内容 */
    public final byte[] content;
    /** 文件内容类型（MIME类型） */
    public final String contentType;

    /**
     * 构造函数
     * 
     * @param fileName 文件名
     * @param content 文件二进制内容
     * @param contentType 文件内容类型（MIME类型），可为 null
     */
    public FileBinary(String fileName, byte[] content, String contentType) {
        this.fileName = fileName;
        this.content = content;
        this.contentType = contentType;
    }
}

