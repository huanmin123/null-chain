package com.gitee.huanminabc.nullchain.language.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

/**
 * 函数引用信息
 * 用于存储函数引用变量和 Lambda 表达式的信息
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunRefInfo {
    /**
     * 引用的函数名（对于函数引用）
     * Lambda 表达式此字段为 null
     */
    private String functionName;

    /**
     * 函数定义信息（复用 FunDefInfo）
     * 对于函数引用：指向已定义的函数
     * 对于 Lambda：包含 Lambda 的函数体和参数
     */
    private FunDefInfo funDefInfo;

    /**
     * 是否是 Lambda 表达式
     */
    private boolean isLambda;

    /**
     * Lambda 捕获的外部变量（闭包支持）
     * key: 变量名, value: 变量值
     * 对于函数引用，此字段为 null
     */
    private Map<String, Object> capturedVariables;

    /**
     * 捕获变量时的作用域 ID（用于查找捕获变量）
     * 对于函数引用，此字段为 null
     */
    private String captureScopeId;

    /**
     * 函数类型信息
     * 包含参数类型列表和返回值类型
     */
    private FunTypeInfo funTypeInfo;

    /**
     * 判断两个函数引用是否相等
     * 相等条件：函数名相同或函数定义信息相同
     *
     * @param o 另一个函数引用
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunRefInfo that = (FunRefInfo) o;
        return isLambda == that.isLambda &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(funDefInfo, that.funDefInfo);
    }

    /**
     * 计算哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(functionName, funDefInfo, isLambda);
    }

    /**
     * 转换为字符串表示
     *
     * @return 字符串形式的函数引用
     */
    @Override
    public String toString() {
        if (isLambda) {
            return "Lambda" + (funTypeInfo != null ? funTypeInfo : "");
        } else {
            return "FunRef[" + functionName + "]" + (funTypeInfo != null ? funTypeInfo : "");
        }
    }

    /**
     * 创建函数引用
     *
     * @param functionName  函数名
     * @param funDefInfo    函数定义信息
     * @param funTypeInfo   函数类型信息
     * @return 函数引用信息
     */
    public static FunRefInfo createFunRef(String functionName, FunDefInfo funDefInfo, FunTypeInfo funTypeInfo) {
        return new FunRefInfo(functionName, funDefInfo, false, null, null, funTypeInfo);
    }

    /**
     * 创建 Lambda 表达式
     *
     * @param funDefInfo         函数定义信息
     * @param capturedVariables  捕获的外部变量
     * @param captureScopeId     捕获时的作用域 ID
     * @param funTypeInfo        函数类型信息
     * @return Lambda 表达式信息
     */
    public static FunRefInfo createLambda(FunDefInfo funDefInfo,
                                          Map<String, Object> capturedVariables,
                                          String captureScopeId,
                                          FunTypeInfo funTypeInfo) {
        return new FunRefInfo(null, funDefInfo, true, capturedVariables, captureScopeId, funTypeInfo);
    }
}
