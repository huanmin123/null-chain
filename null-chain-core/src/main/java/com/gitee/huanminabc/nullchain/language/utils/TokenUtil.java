package com.gitee.huanminabc.nullchain.language.utils;

import com.gitee.huanminabc.nullchain.language.token.Token;

import java.util.List;

public class TokenUtil {
    public  static StringBuilder mergeToken(List<Token> tokens) {
        return  mergeToken(tokens, 0, tokens.size());
    }

    public  static StringBuilder mergeToken(List<Token> tokens, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(tokens.get(i).value);
        }
        return sb;
    }
}
