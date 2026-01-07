package com.gitee.huanminabc.nullchain.language.internal;

import lombok.Data;

/**
 * 变量信息:
 * 1. 用于存储变量相关信息 例如: 变量名, 变量值, 变量类型等
 * 2. 支持函数引用变量（存储函数引用和 Lambda 表达式）
 *
 * @author huanmin
 * @date 2024/11/22
 */
@Data
public class NfVariableInfo {
    //变量名
    private String name;
    //变量值
    private Object value;
    //变量类型 用的是java的类型
    private Class<?> type;

    /**
     * 是否是函数引用变量
     * 标识该变量存储的是函数引用或 Lambda 表达式
     */
    private boolean isFunctionReference;

    /**
     * 函数引用信息（当 isFunctionReference=true 时使用）
     * 存储函数引用或 Lambda 表达式的详细信息
     */
    private FunRefInfo funRefInfo;

    /**
     * 无参构造函数
     */
    public NfVariableInfo() {
        this.isFunctionReference = false;
        this.funRefInfo = null;
    }

    /**
     * 三参数构造函数（向后兼容）
     * 用于普通变量
     *
     * @param name  变量名
     * @param value 变量值
     * @param type  变量类型
     */
    public NfVariableInfo(String name, Object value, Class<?> type) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isFunctionReference = false;
        this.funRefInfo = null;
    }

    /**
     * 五参数构造函数（完整构造函数）
     * 用于函数引用变量
     *
     * @param name                 变量名
     * @param value                变量值
     * @param type                 变量类型
     * @param isFunctionReference  是否是函数引用
     * @param funRefInfo           函数引用信息
     */
    public NfVariableInfo(String name, Object value, Class<?> type, boolean isFunctionReference, FunRefInfo funRefInfo) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isFunctionReference = isFunctionReference;
        this.funRefInfo = funRefInfo;
    }
}
