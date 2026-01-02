package com.gitee.huanminabc.nullchain.language.token;

import com.gitee.huanminabc.nullchain.common.NullConstants;

import java.util.List;
/**
 * 字符串Token处理类
 * 支持双引号、单引号字符串和模板字符串 ```
 * 
 * @author huanmin
 * @date 2024/11/22
 */
public class StringToken {

    /**
     * 处理字符串（支持双引号和单引号）
     * 支持转义字符：\n, \t, \r, \\, \", \'
     * 
     * @param input 输入字符串
     * @param i 当前字符位置
     * @param tokens Token列表
     * @param line 行号
     * @return 处理后的字符位置
     */
    public static int string(String input, int i, List<Token> tokens, Integer line) {
        char quoteChar = input.charAt(i); // 获取引号字符（" 或 '）
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        i++;
        
        // 查找匹配的结束引号，处理转义字符
        while (i < input.length()) {
            char c = input.charAt(i);
            
            // 检查转义字符
            if (c == '\\' && i + 1 < input.length()) {
                char nextChar = input.charAt(i + 1);
                switch (nextChar) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    default:
                        // 未知转义字符，保留原样（转义字符+下一个字符）
                        sb.append('\\').append(nextChar);
                        break;
                }
                i += 2; // 跳过转义字符和下一个字符
                continue;
            }
            
            // 检查结束引号
            if (c == quoteChar) {
                break;
            }
            
            sb.append(c);
            i++;
        }
        
        // 检查字符串是否未正确结束
        if (i >= input.length()) {
            throw new com.gitee.huanminabc.nullchain.language.NfException(
                "字符串未正确结束，缺少结束引号，行号: {}", line);
        }
        
        i++; // 跳过结束引号
        // 统一使用双引号格式存储，便于后续处理
        tokens.add(new Token(TokenType.STRING, quoteChar + sb.toString() + quoteChar, line));
        return i;
    }

    /**
     * 处理模板字符串（``` 开头和结尾）
     * 保留中间内容，包括换行符
     * 
     * @param input 输入字符串
     * @param i 当前字符位置（指向第一个 `）
     * @param tokens Token列表
     * @param line 行号
     * @return 处理后的字符位置
     */
    public static int templateString(String input, int i, List<Token> tokens, Integer line) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        // 跳过开头的 ```
        i += 3;
        
        // 查找匹配的结束 ```
        while (i < input.length()) {
            // 检查是否是结束的 ```
            if (i + 2 < input.length() && 
                input.charAt(i) == '`' && 
                input.charAt(i + 1) == '`' && 
                input.charAt(i + 2) == '`') {
                // 找到结束标记，跳过这三个字符
                i += 3;
                break;
            }
            // 记录当前字符，包括换行符
            sb.append(input.charAt(i));
            i++;
        }
        
        // 存储模板字符串内容（包含 ``` 标记，便于后续处理）
        tokens.add(new Token(TokenType.TEMPLATE_STRING, "```" + sb + "```", line));
        return i;
    }
}
