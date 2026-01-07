package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.language.NfException;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数类型解析器
 * 用于解析 Fun<参数类型列表 : 返回值类型> 格式的函数类型字符串
 *
 * @author huanmin
 * @date 2025/01/06
 */
public class FunTypeParser {

    /**
     * 检查类型字符串是否是函数类型
     * 函数类型格式：Fun<参数类型列表 : 返回值类型>
     *
     * @param typeStr 类型字符串，例如 "Fun<Integer, Integer : Integer>"
     * @return 是否是函数类型
     */
    public static boolean isFunType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return false;
        }
        return typeStr.startsWith("Fun<") && typeStr.endsWith(">");
    }

    /**
     * 解析函数类型字符串
     * 格式：Fun<参数类型列表 : 返回值类型>
     * - 冒号前面是参数类型列表（用逗号分隔）
     * - 冒号后面是返回值类型
     *
     * @param typeStr 类型字符串，例如 "Fun<Integer, Integer : Integer>"
     * @return 函数类型信息
     * @throws NfException 如果类型格式不正确
     */
    public static FunTypeInfo parse(String typeStr) {
        // 检查是否是函数类型
        if (!isFunType(typeStr)) {
            throw new NfException("无效的函数类型格式: " + typeStr + ", 正确格式: Fun<参数类型列表 : 返回值类型>");
        }

        // 提取尖括号内的内容
        String content = typeStr.substring(4, typeStr.length() - 1).trim();

        if (content.isEmpty()) {
            throw new NfException("函数类型不能为空: " + typeStr);
        }

        // 查找冒号位置（考虑泛型嵌套）
        int colonIndex = findColonIndex(content);

        if (colonIndex == -1) {
            throw new NfException("函数类型必须包含冒号 ':' 分隔参数和返回值: " + typeStr);
        }

        // 分割参数类型列表和返回值类型
        String paramsPart = content.substring(0, colonIndex).trim();
        String returnPart = content.substring(colonIndex + 1).trim();

        if (returnPart.isEmpty()) {
            throw new NfException("返回值类型不能为空: " + typeStr);
        }

        // 解析参数类型列表
        List<String> parameterTypes = new ArrayList<>();
        if (!paramsPart.isEmpty()) {
            parameterTypes = splitByComma(paramsPart);
        }

        String returnType = returnPart;
        int arity = parameterTypes.size();

        return new FunTypeInfo(parameterTypes, returnType, arity);
    }

    /**
     * 查找冒号位置（考虑泛型嵌套）
     * 只在顶层查找冒号，忽略尖括号内的冒号
     *
     * @param content 类型参数字符串
     * @return 冒号位置，如果找不到返回 -1
     */
    private static int findColonIndex(String content) {
        int angleBracketDepth = 0; // 尖括号深度，处理泛型嵌套

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '<') {
                angleBracketDepth++;
            } else if (c == '>') {
                angleBracketDepth--;
            } else if (c == ':' && angleBracketDepth == 0) {
                // 在顶层找到冒号
                return i;
            }
        }

        return -1;
    }

    /**
     * 按逗号分割类型参数列表（考虑泛型嵌套）
     *
     * @param content 类型参数字符串
     * @return 类型参数列表
     */
    private static List<String> splitByComma(String content) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int angleBracketDepth = 0; // 尖括号深度，处理泛型嵌套

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '<') {
                angleBracketDepth++;
                current.append(c);
            } else if (c == '>') {
                angleBracketDepth--;
                current.append(c);
            } else if (c == ',' && angleBracketDepth == 0) {
                // 在顶层遇到逗号，分割类型参数
                String paramType = current.toString().trim();
                if (!paramType.isEmpty()) {
                    result.add(paramType);
                }
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // 添加最后一个类型参数
        String lastParam = current.toString().trim();
        if (!lastParam.isEmpty()) {
            result.add(lastParam);
        }

        // 检查尖括号是否匹配
        if (angleBracketDepth != 0) {
            throw new NfException("泛型尖括号不匹配: " + content);
        }

        return result;
    }

    /**
     * 从函数定义信息构建函数类型信息
     *
     * @param funDefInfo 函数定义信息
     * @return 函数类型信息
     */
    public static FunTypeInfo fromFunDefInfo(FunDefInfo funDefInfo) {
        if (funDefInfo == null) {
            throw new NfException("函数定义信息不能为空");
        }

        List<String> parameterTypes = new ArrayList<>();
        if (funDefInfo.getParameters() != null) {
            for (FunDefInfo.FunParameter param : funDefInfo.getParameters()) {
                parameterTypes.add(param.getType());
            }
        }

        // 获取返回值类型（支持多返回值）
        String returnType;
        if (funDefInfo.getReturnTypes() != null && !funDefInfo.getReturnTypes().isEmpty()) {
            if (funDefInfo.getReturnTypes().size() == 1) {
                returnType = funDefInfo.getReturnTypes().get(0);
            } else {
                // 多返回值：用逗号连接
                returnType = String.join(", ", funDefInfo.getReturnTypes());
            }
        } else {
            returnType = "Void";
        }

        return new FunTypeInfo(parameterTypes, returnType, parameterTypes.size());
    }
}
