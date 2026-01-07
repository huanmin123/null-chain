package com.gitee.huanminabc.nullchain.language.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 函数类型信息
 * 用于表示 Fun<T1, T2, ..., R> 类型的解析结果
 *
 * @author huanmin
 * @date 2025/01/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunTypeInfo {
    /**
     * 参数类型列表
     * 例如 Fun<Integer, String, Boolean> 的参数类型为 [Integer, String]
     */
    private List<String> parameterTypes;

    /**
     * 返回值类型
     * 例如 Fun<Integer, String, Boolean> 的返回值类型为 Boolean
     */
    private String returnType;

    /**
     * 参数数量（函数的元数）
     */
    private int arity;

    /**
     * 判断两个函数类型是否相等
     * 相等条件：参数类型列表相同且返回值类型相同
     * 注意：原始类型和包装类型被视为相同（例如 int 和 Integer）
     *
     * @param o 另一个函数类型信息
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunTypeInfo that = (FunTypeInfo) o;
        if (arity != that.arity) return false;

        // 比较参数类型列表（统一原始类型和包装类型）
        if (parameterTypes != null && that.parameterTypes != null) {
            if (parameterTypes.size() != that.parameterTypes.size()) return false;
            for (int i = 0; i < parameterTypes.size(); i++) {
                String thisType = normalizeType(parameterTypes.get(i));
                String thatType = normalizeType(that.parameterTypes.get(i));
                if (!Objects.equals(thisType, thatType)) return false;
            }
        } else if (!Objects.equals(parameterTypes, that.parameterTypes)) {
            return false;
        }

        // 比较返回值类型（统一原始类型和包装类型）
        String thisReturnType = normalizeType(returnType);
        String thatReturnType = normalizeType(that.returnType);
        return Objects.equals(thisReturnType, thatReturnType);
    }

    /**
     * 规范化类型名称，将原始类型转换为包装类型
     * 例如：int -> Integer, boolean -> Boolean, double -> Double
     *
     * @param type 原始类型名称
     * @return 规范化后的类型名称
     */
    private String normalizeType(String type) {
        if (type == null) return null;
        switch (type) {
            case "int": return "Integer";
            case "boolean": return "Boolean";
            case "double": return "Double";
            case "float": return "Float";
            case "long": return "Long";
            case "short": return "Short";
            case "char": return "Character";
            case "byte": return "Byte";
            default: return type;
        }
    }

    /**
     * 计算哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType, arity);
    }

    /**
     * 转换为字符串表示
     *
     * @return 字符串形式的函数类型，例如 "Fun<Integer, String, Boolean>"
     */
    @Override
    public String toString() {
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return "Fun<" + returnType + ">";
        }
        StringBuilder sb = new StringBuilder("Fun<");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes.get(i));
        }
        sb.append(", ").append(returnType).append(">");
        return sb.toString();
    }
}
