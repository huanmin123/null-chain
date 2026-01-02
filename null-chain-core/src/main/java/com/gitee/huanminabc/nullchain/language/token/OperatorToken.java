package com.gitee.huanminabc.nullchain.language.token;

import com.gitee.huanminabc.nullchain.language.NfException;

import java.util.List;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class OperatorToken {

    public static int operator(String input, int length, char currentChar, int i, List<Token> tokens, Integer line) {
        // 处理各种符号
        switch (currentChar) {
            case '=':
                // 检查是否是 ==
                if (i + 1 < length && input.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.EQ, "==", line));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", line));
                    i++;
                }
                break;
            case '>':
                // 检查是否是 >=
                if (i + 1 < length && input.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.GE, ">=", line));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.GT, ">", line));
                    i++;
                }
                break;
            case '<':
                // 检查是否是 <=
                if (i + 1 < length && input.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.LE, "<=", line));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.LT, "<", line));
                    i++;
                }
                break;
            case '!':
                // 检查是否是 !=
                if (i + 1 < length && input.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.NE, "!=", line));
                    i += 2;
                } else {
                    throw new NfException("非法字符  行: {} 字符: {}", line, currentChar);
                }
                break;
            case '-':
                if (i + 1 < length && input.charAt(i + 1) == '>') {
                    tokens.add(new Token(TokenType.ARROW_ASSIGN, "->", line));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.SUB, "-", line));
                    i++;
                }
                break;
            case '+':
                tokens.add(new Token(TokenType.ADD, "+", line));
                i++;
                break;
            case '*':
                tokens.add(new Token(TokenType.MUL, "*", line));
                i++;
                break;
            case '/':
                tokens.add(new Token(TokenType.DIV, "/", line));
                i++;
                break;
            case '%':
                tokens.add(new Token(TokenType.MOD, "%", line));
                i++;
                break;
            case ':':
                tokens.add(new Token(TokenType.COLON, ":", line));
                i++;
                break;
            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", line));
                i++;
                break;
            case '.':
                //判断后一个如果也是.那么就表示.. 应该识别为DOT2 token（范围操作符）
                //语法检查会在语法分析阶段进行，这里只负责词法分析
                if (i + 1 < length && input.charAt(i + 1) == '.') {
                    tokens.add(new Token(TokenType.DOT2, "..", line));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.DOT, ".", line));
                    i++;
                }
                break;
            case '(':
                tokens.add(new Token(TokenType.LPAREN, "(", line));
                i++;
                break;
            case ')':
                tokens.add(new Token(TokenType.RPAREN, ")", line));
                i++;
                break;
            case '{':
                tokens.add(new Token(TokenType.LBRACE, "{", line));
                i++;
                break;
            case '}':
                tokens.add(new Token(TokenType.RBRACE, "}", line));
                i++;
                break;
            case '&':
                //&&
                if (i + 1 < length && input.charAt(i + 1) == '&') {
                    tokens.add(new Token(TokenType.AND, "&&", line));
                    i += 2;
                } else {
                    throw new NfException("非法字符  行: {} 字符: {}", line, currentChar);
                }
                break;
            case '|':
                //||
                if (i + 1 < length && input.charAt(i + 1) == '|') {
                    tokens.add(new Token(TokenType.OR, "||", line));
                    i += 2;
                } else {
                    throw new NfException("非法字符  行: {} 字符: {}", line, currentChar);
                }
                break;
            case '\\':
                if (i + 1 < length && input.charAt(i + 1) == 'n') { //换行符号
                    tokens.add(new Token(TokenType.LINE_END_SYMBOL, "\\n", line));
                    i += 2;
                } else if (i + 1 < length && input.charAt(i + 1) == 't') { //\t
                    tokens.add(new Token(TokenType.TAB_SYMBOL, "\\t", line));
                    i += 2;
                } else {
                    throw new NfException("非法字符  行: {} 字符: {}", line, currentChar);
                }
                break;
            case '`':
                // 单个反引号不被支持，需要使用三个反引号 ``` 来表示模板字符串
                throw new NfException("非法字符  行: {} 字符: {} (单个反引号不被支持，请使用三个反引号 ``` 来表示模板字符串)", line, currentChar);
            default:
                throw new NfException("非法字符  行: {} 字符: {}", line, currentChar);
        }
        return i;
    }
}
