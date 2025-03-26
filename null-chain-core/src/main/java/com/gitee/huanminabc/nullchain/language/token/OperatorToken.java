package com.gitee.huanminabc.nullchain.language.token;

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
                tokens.add(new Token(TokenType.ASSIGN, "=", line));
                i++;
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
            case ':':
                tokens.add(new Token(TokenType.COLON, ":", line));
                i++;
                break;
            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", line));
                i++;
                break;
            case '.':
                //判断后一个如果也是.那么就表示.. 就是语法错误
                if (i + 1 < length && input.charAt(i + 1) == '.') {
                    throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + "..");
                }
                tokens.add(new Token(TokenType.DOT, ".", line));
                i++;
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
                    throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + currentChar);
                }
                break;
            case '|':
                //||
                if (i + 1 < length && input.charAt(i + 1) == '|') {
                    tokens.add(new Token(TokenType.OR, "||", line));
                    i += 2;
                } else {
                    throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + currentChar);
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
                    throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + currentChar);
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal character  line: " + line + " char: " + currentChar);
        }
        return i;
    }
}
