package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.token.*;

import java.util.ArrayList;
import java.util.List;

/*
  解析Token (词法分析器)
 */

/**
 * NF脚本词法分析器
 * 将源代码解析为Token序列
 *
 * @author huanmin
 * @date 2024/11/22
 */
public class NfToken {

    /**
     * 括号匹配检测
     * 只检查结构性括号 {}[]() 是否匹配
     * 注：字符串和注释中的引号由词法分析器正确处理，无需在此处检查
     *
     * @param input 输入的脚本内容
     * @throws NfException 如果括号不匹配
     */
    public static void checkBrackets(String input) {
        int length = input.length();
        int count1 = 0; // {}
        int count2 = 0; // []
        int count3 = 0; // ()

        for (int i = 0; i < length; i++) {
            char currentChar = input.charAt(i);

            // 跳过字符串和注释中的字符（简单处理）
            // 注意：这只是预检，完整的字符串处理在词法分析阶段
            if (currentChar == '"') {
                // 跳过双引号字符串内容（处理转义）
                i++;
                while (i < length) {
                    char c = input.charAt(i);
                    if (c == '\\' && i + 1 < length) {
                        // 转义字符，跳过下一个字符
                        i += 2;
                        continue;
                    }
                    if (c == '"') {
                        break;
                    }
                    i++;
                }
                continue;
            }
            if (currentChar == '\'') {
                // 跳过单引号字符串内容（处理转义）
                i++;
                while (i < length) {
                    char c = input.charAt(i);
                    if (c == '\\' && i + 1 < length) {
                        // 转义字符，跳过下一个字符
                        i += 2;
                        continue;
                    }
                    if (c == '\'') {
                        break;
                    }
                    i++;
                }
                continue;
            }

            // 检查结构性括号
            if (currentChar == '{') {
                count1++;
            } else if (currentChar == '}') {
                count1--;
            } else if (currentChar == '[') {
                count2++;
            } else if (currentChar == ']') {
                count2--;
            } else if (currentChar == '(') {
                count3++;
            } else if (currentChar == ')') {
                count3--;
            }
        }

        if (count1 != 0 || count2 != 0 || count3 != 0) {
            throw new NfException("括号不匹配: {}[]()");
        }
    }


    /**
     * 构建Token序列
     *
     * @param input 输入的脚本内容
     * @return Token列表
     * @throws NfException 如果语法错误
     */
    public static List<Token> tokens(String input) {
        //检查{} [] () 是否匹配, 不然会出现死循环
        checkBrackets(input);
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int length = input.length();
        //行号
        int line = 1;
        while (i < length) {
            char currentChar = input.charAt(i);
            // 记录行号（只有换行符才增加行号）
            if (currentChar == '\n') {
                line++;
                i++;
                tokens.add(new Token(TokenType.LINE_END, "", line));
                continue;
            }
            // 分号单独处理，不增加行号
            if (currentChar == ';') {
                i++;
                tokens.add(new Token(TokenType.LINE_END, "", line));
                continue;
            }

            // 判断是否是空白字符,是的话直接跳过
            if (Character.isWhitespace(currentChar)) {
                i++;
                continue;
            }
            //如果是注释（//）
            if (currentChar == '/' && i + 1 < length && input.charAt(i + 1) == '/') {
                i = CommentToken.comment(input, i, tokens, line);
                continue;
            }
            // 处理标识符、关键字和常量
            // 支持 $ 前缀的系统变量（如 $preValue、$params、$threadFactoryName）
            if (Character.isLetter(currentChar) || currentChar == '_' || currentChar == '$') {
                i = IdentifierToken.identifier(input, currentChar, i, tokens, line);
                continue;
            }
            // 处理数字
            if (Character.isDigit(currentChar)) {
                i = NumberToken.number(input, currentChar, i, tokens, line);
                continue;
            }
            // 处理模板字符串（``` 开头，必须在普通字符串之前检测）
            if (currentChar == '`' && i + 2 < length &&
                input.charAt(i + 1) == '`' && input.charAt(i + 2) == '`') {
                i = StringToken.templateString(input, i, tokens, line);
                continue;
            }
            // 处理字符串（支持双引号和单引号）
            if (currentChar == '"' || currentChar == '\'') {
                i = StringToken.string(input, i, tokens, line);
                continue;
            }
            // 处理运算符
            i = OperatorToken.operator(input, length, currentChar, i, tokens, line);
        }
        return tokens;
    }


    /**
     * 去掉开头的换行
     * 如果列表为空，直接返回，避免IndexOutOfBoundsException
     *
     * @param tokens Token列表
     */
    public static void skipLineEnd(List<Token> tokens) {
        // 如果列表为空，直接返回，避免IndexOutOfBoundsException
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        // 循环移除开头的换行符
        while (!tokens.isEmpty() && tokens.get(0).type == TokenType.LINE_END) {
            tokens.remove(0);
        }
    }

}
