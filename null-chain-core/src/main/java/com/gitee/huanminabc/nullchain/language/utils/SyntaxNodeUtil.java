package com.gitee.huanminabc.nullchain.language.utils;

import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.util.List;

/**
 * 语法节点工具类
 * 
 * <p>提供语法节点解析过程中使用的公共工具方法。</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public class SyntaxNodeUtil {
    
    /**
     * 查找LINE_END的位置
     * 从指定位置开始查找，直到找到LINE_END或到达列表末尾
     * 
     * @param tokens Token列表
     * @param startIndex 开始查找的位置
     * @return LINE_END的位置，如果未找到则返回tokens.size()
     */
    public static int findLineEndIndex(List<Token> tokens, int startIndex) {
        int size = tokens.size();
        for (int i = startIndex; i < size; i++) {
            if (tokens.get(i).type == TokenType.LINE_END) {
                return i;
            }
        }
        return size; // 默认到列表末尾
    }
    
    /**
     * 删除Token列表中的注释
     * 
     * @param tokens Token列表
     */
    public static void removeComments(List<Token> tokens) {
        tokens.removeIf(t -> t.type == TokenType.COMMENT);
    }
}



