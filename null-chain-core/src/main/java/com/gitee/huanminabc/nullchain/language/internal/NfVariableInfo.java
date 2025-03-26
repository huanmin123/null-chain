package com.gitee.huanminabc.nullchain.language.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量信息:
 * 1. 用于存储变量相关信息 例如: 变量名, 变量值, 变量类型等
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NfVariableInfo {
    //变量名
    private String name;
    //变量值
    private Object value;
    //变量类型 用的是java的类型
    private Class<?> type;
}
