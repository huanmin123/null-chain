package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

/**
 * FOR循环迭代类型枚举
 * 定义FOR语句支持的两种迭代模式
 *
 * @author huanmin
 * @date 2024/11/22
 */
public enum ForLoopType {
    /**
     * 数值范围循环: for i in 1..10 { ... }
     */
    RANGE,

    /**
     * 变量迭代: for item in list 或 for k, v in map { ... }
     * 在运行时根据变量的实际类型决定是列表迭代还是Map迭代
     */
    VARIABLE_ITERATION
}

