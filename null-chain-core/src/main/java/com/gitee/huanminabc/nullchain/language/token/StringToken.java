package com.gitee.huanminabc.nullchain.language.token;

import java.util.List;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class StringToken {

    // 处理字符串
    public static int string(String input, int i, List<Token> tokens, Integer line) {
        StringBuilder sb = new StringBuilder();
        i++;
        while (i < input.length() && input.charAt(i) != '"') {
            sb.append(input.charAt(i));
            i++;
        }
        i++;
        tokens.add(new Token(TokenType.STRING, "\""+ sb +"\"", line));
        return i;
    }
}
