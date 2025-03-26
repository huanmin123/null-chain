package com.gitee.huanminabc.nullchain.language.token;

import java.util.List;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public  class NumberToken {
    // 处理数字
    public static int number(String input, char currentChar, int i, List<Token> tokens, Integer line) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentChar);
        i++;
        while (i < input.length() && Character.isDigit(input.charAt(i))) {
            sb.append(input.charAt(i));
            i++;
        }
        if (i < input.length() && input.charAt(i) == '.') {
            //如果点后面还有点 那么1.. 这种情况 后面2个点是一个token
            if (i + 1 < input.length() && input.charAt(i + 1) == '.') {
                tokens.add(new Token(TokenType.INTEGER, sb.toString(), line));
                tokens.add(new Token(TokenType.DOT2, "..", line));
                return i + 2;
            }

            sb.append(input.charAt(i));
            i++;
            while (i < input.length() && Character.isDigit(input.charAt(i))) {
                sb.append(input.charAt(i));
                i++;
            }
            tokens.add(new Token(TokenType.FLOAT, sb.toString(), line));
        } else {
            tokens.add(new Token(TokenType.INTEGER, sb.toString(), line));
        }
        return i;
    }
}
