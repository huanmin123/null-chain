package com.gitee.huanminabc.nullchain.language.token;

import com.gitee.huanminabc.nullchain.common.NullConstants;

import java.util.List;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class CommentToken {
    //注释处理
    public static int comment(String input, int i, List<Token> tokens, Integer line) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        if (i + 1 < input.length() && input.charAt(i + 1) == '/') {
            i += 2;
            while (i < input.length() && input.charAt(i) != '\n') {
                sb.append(input.charAt(i));
                i++;
            }
            tokens.add(new Token(TokenType.COMMENT, sb.toString(), line));
        }else{
            //不支持
            throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + input.charAt(i)+input.charAt(i+1));
        }
        return i;
    }
}
