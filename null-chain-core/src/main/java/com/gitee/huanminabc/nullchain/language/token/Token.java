package com.gitee.huanminabc.nullchain.language.token;

import lombok.Data;

// 词法单元类

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Data
public class Token {
    public TokenType type;
    public String value;
    //行号
    public Integer line;

    public Token(TokenType type, String value, Integer line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

}