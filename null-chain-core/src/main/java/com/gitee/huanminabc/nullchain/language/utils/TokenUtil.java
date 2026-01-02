package com.gitee.huanminabc.nullchain.language.utils;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.util.List;

public class TokenUtil {
    public  static StringBuilder mergeToken(List<Token> tokens) {
        return  mergeToken(tokens, 0, tokens.size());
    }

    public  static StringBuilder mergeToken(List<Token> tokens, int start, int end) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        for (int i = start; i < end; i++) {
            Token currentToken = tokens.get(i);
            // 在非分隔符之间添加空格，避免将多个标识符合并成一个词
            // 例如：value instanceof Integer 不能变成 valueinstanceofInteger
            if (i > start) {
                Token prevToken = tokens.get(i - 1);
                // 判断是否需要添加空格
                boolean needsSpace = shouldAddSpace(prevToken, currentToken);
                if (needsSpace) {
                    sb.append(" ");
                }
            }
            sb.append(currentToken.value);
        }
        return sb;
    }

    /**
     * 判断两个token之间是否需要添加空格
     * 
     * @param prevToken 前一个token
     * @param currentToken 当前token
     * @return 如果需要添加空格返回true，否则返回false
     */
    private static boolean shouldAddSpace(Token prevToken, Token currentToken) {
        // LINE_END token 前后不需要空格
        if (prevToken.type == TokenType.LINE_END || currentToken.type == TokenType.LINE_END) {
            return false;
        }
        
        // 空字符串token不需要空格
        if (prevToken.value.isEmpty() || currentToken.value.isEmpty()) {
            return false;
        }
        
        // 左分隔符（{ (）后面不需要空格
        if (isLeftSeparator(currentToken)) {
            return false;
        }
        
        // 右分隔符（} )）前面不需要空格，但后面如果是非分隔符则需要空格
        if (isRightSeparator(prevToken)) {
            // } else 这种情况需要空格
            return !isLeftSeparator(currentToken);
        }
        
        // 运算符（如 . , : 等）前后不需要空格
        if (isOperator(prevToken) || isOperator(currentToken)) {
            return false;
        }
        
        // 其他情况都需要添加空格（保证 value instanceof Integer 之间有空格）
        return true;
    }

    /**
     * 判断token是否是左分隔符
     * 左分隔符包括：{ (
     */
    private static boolean isLeftSeparator(Token token) {
        return token.type == TokenType.LBRACE || token.type == TokenType.LPAREN;
    }

    /**
     * 判断token是否是右分隔符
     * 右分隔符包括：} )
     */
    private static boolean isRightSeparator(Token token) {
        return token.type == TokenType.RBRACE || token.type == TokenType.RPAREN;
    }

    /**
     * 判断token是否是运算符（不需要前后空格的运算符）
     * 包括：. , : = + - * / % 等
     * 注意：INSTANCEOF 不是运算符，它需要前后有空格（如 value instanceof Integer）
     */
    private static boolean isOperator(Token token) {
        return token.type == TokenType.DOT ||
               token.type == TokenType.COMMA ||
               token.type == TokenType.COLON ||
               token.type == TokenType.ASSIGN ||
               token.type == TokenType.ADD ||
               token.type == TokenType.SUB ||
               token.type == TokenType.MUL ||
               token.type == TokenType.DIV ||
               token.type == TokenType.MOD ||
               token.type == TokenType.GT ||
               token.type == TokenType.LT ||
               token.type == TokenType.GE ||
               token.type == TokenType.LE ||
               token.type == TokenType.EQ ||
               token.type == TokenType.NE ||
               token.type == TokenType.AND ||
               token.type == TokenType.OR ||
               token.type == TokenType.DOT2; // 范围运算符 .. 不需要空格
    }
}
