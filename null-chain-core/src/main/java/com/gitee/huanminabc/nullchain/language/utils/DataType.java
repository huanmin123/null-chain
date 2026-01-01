package com.gitee.huanminabc.nullchain.language.utils;

import com.gitee.huanminabc.nullchain.language.token.TokenType;

public class DataType {

    public  static Object realType(TokenType type, Object value){
        switch (type) {
            case STRING:
                String string = value.toString();
                //判断前后是否有引号（支持双引号和单引号）
                if ((string.startsWith("\"") && string.endsWith("\"")) ||
                    (string.startsWith("'") && string.endsWith("'"))) {
                    return string.substring(1, string.length() - 1);
                }
                return string;
            case INTEGER:
                return Integer.parseInt(value.toString());
            case BOOLEAN:
                return Boolean.parseBoolean(value.toString());
            case FLOAT:
                //判断是Float还是Double
                if (value.toString().contains("f") || value.toString().contains("F")) {
                    return Float.parseFloat(value.toString());
                }
                return Double.parseDouble(value.toString());
            default:
               return value;
        }
    }

}
