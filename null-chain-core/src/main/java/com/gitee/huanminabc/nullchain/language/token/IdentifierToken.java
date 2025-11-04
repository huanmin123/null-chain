package com.gitee.huanminabc.nullchain.language.token;

import com.gitee.huanminabc.nullchain.common.NullConstants;

import java.util.List;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class IdentifierToken {
    // 处理标识符、关键字和常量
    public static int identifier(String input, char currentChar, int i, List<Token> tokens, Integer line) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        sb.append(currentChar);
        i++;
        while (i < input.length() && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
            sb.append(input.charAt(i));
            i++;
        }
        String value = sb.toString();
        switch (value) {
            case "import":
                tokens.add(new Token(TokenType.IMPORT, value, line));
                break;
            case "task":
                tokens.add(new Token(TokenType.TASK, value, line));
                break;
            case "as":
                tokens.add(new Token(TokenType.AS, value, line));
                break;
            case "run":
                tokens.add(new Token(TokenType.RUN, value, line));
                break;
            case "if":
                tokens.add(new Token(TokenType.IF, value, line));
                break;
            case "else":
                tokens.add(new Token(TokenType.ELSE, value, line));
                break;
            case "switch":
                tokens.add(new Token(TokenType.SWITCH, value, line));
                break;
            case "case":
                tokens.add(new Token(TokenType.CASE, value, line));
                break;
            case "default":
                tokens.add(new Token(TokenType.DEFAULT, value, line));
                break;
            case "while":
                tokens.add(new Token(TokenType.WHILE, value, line));
                break;
            case "for":
                tokens.add(new Token(TokenType.FOR, value, line));
                break;
            case "in":
                tokens.add(new Token(TokenType.IN, value, line));
                break;
            case "range":
                tokens.add(new Token(TokenType.RANGE, value, line));
                break;
            case "export":
                tokens.add(new Token(TokenType.EXPORT, value, line));
                break;
            case "true":
                tokens.add(new Token(TokenType.TRUE, value, line));
                break;
            case "false":
                tokens.add(new Token(TokenType.FALSE, value, line));
                break;
            case "and":
                tokens.add(new Token(TokenType.AND, value, line));
                break;
            case "or":
                tokens.add(new Token(TokenType.OR, value, line));
                break;
            case "break":
                tokens.add(new Token(TokenType.BREAK, value, line));
                break;
            case "breakall":
                tokens.add(new Token(TokenType.BREAK_ALL, value, line));
                break;
            case "continue":
                tokens.add(new Token(TokenType.CONTINUE, value, line));
                break;
            case "echo":
                tokens.add(new Token(TokenType.ECHO, value, line));
                break;
            case "new":
                tokens.add(new Token(TokenType.NEW, value, line));
                break;
            default:
                tokens.add(new Token(TokenType.IDENTIFIER, value, line));
        }
        return i;
    }
}
