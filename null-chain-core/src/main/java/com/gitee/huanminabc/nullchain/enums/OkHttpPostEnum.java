package com.gitee.huanminabc.nullchain.enums;
/**
 * @author huanmin
 * @date 2024/11/30
 */
public enum OkHttpPostEnum {
    JSON, //post 请求 上传json字符串格式的数据
    FORM, // post 请求 上传form表单格式的数据      数据格式必须是对象 或者是Map
    FILE, // post 请求 上传文件,可以和表单一起上传  数据格式必须是对象 或者是Map
}
