package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.token.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析Token (词法分析器)
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfToken {

    //括号检测  {} [] () "" 个数是否是偶数
    public static void checkBrackets(String input) {
        int length = input.length();
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        for (int i = 0; i < length; i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '{') {
                count1++;
            }
            if (currentChar == '}') {
                count1--;
            }
            if (currentChar == '[') {
                count2++;
            }
            if (currentChar == ']') {
                count2--;
            }
            if (currentChar == '(') {
                count3++;
            }
            if (currentChar == ')') {
                count3--;
            }
            if (currentChar == '"') {
                count4++;
            }
        }
        if (count1 != 0 || count2 != 0 || count3 != 0 || count4 % 2 != 0) {
            throw new NfException("{}不匹配","{} [] () \"\" ");
        }
    }


    //构建Token
    public static List<Token> tokens(String input) {
        //检查{} [] () "" 是否匹配, 不然会出现死循环
        checkBrackets(input);
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int length = input.length();
        //行号
        int line = 1;
        while (i < length) {
            char currentChar = input.charAt(i);
            // 记录行号
            if (currentChar == '\n') {
                line++;
                i++;
                tokens.add(new Token(TokenType.LINE_END, "", line));
                continue;
            }
            // 判断是否是空白字符,是的话直接跳过
            if (Character.isWhitespace(currentChar)) {
                i++;
                continue;
            }
            //如果是注释
            if (currentChar == '/') {
                i = CommentToken.comment(input, i, tokens, line);
                continue;
            }
            // 处理标识符、关键字和常量
            if (Character.isLetter(currentChar) || currentChar == '_') {
                i = IdentifierToken.identifier(input, currentChar, i, tokens, line);
                continue;
            }
            // 处理数字
            if (Character.isDigit(currentChar)) {
                i = NumberToken.number(input, currentChar, i, tokens, line);
                continue;
            }
            // 处理字符串
            if (currentChar == '"') {
                i = StringToken.string(input, i, tokens, line);
                continue;
            }
            // 处理运算符
            i = OperatorToken.operator(input, length, currentChar, i, tokens, line);
        }
        return tokens;
    }


    //去掉开头的换行
    public static void skipLineEnd(List<Token> tokens) {
        while (tokens.get(0).type == TokenType.LINE_END) {
            tokens.remove(0);
        }
    }

}
