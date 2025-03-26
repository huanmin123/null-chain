package com.gitee.huanminabc.nullchain.language.token;

// 枚举类型表示词法单元的种类
/**
 * @author huanmin
 * @date 2024/11/22
 */
public enum TokenType {
    IMPORT,  // 导入
    AS, // as 别名
    TASK, // 导入任务
    IDENTIFIER, // 标识符
    STRING, // 字符串
    INTEGER, // 整数
    FLOAT, // 浮点数
    BOOLEAN, // 布尔
    ASSIGN, // 赋值
    ADD, // 加
    SUB, // 减
    MUL, // 乘
    DIV, // 除
    RUN, // 运行
    ARROW_ASSIGN, // 箭头赋值
    COLON, // 冒号
    COMMA, // 逗号
    DOT, // 点
    DOT2, // 2点  和数字一起表示范围 1..10
    LPAREN, // (左括号
    RPAREN, // )右括号
    LBRACE, // {左大括号
    RBRACE, // }右大括号
    IF, // if
    ELSE, // else
    SWITCH, // switch
    CASE, // case
    DEFAULT, // default
    WHILE, // while
    FOR, // for
    IN, // in
    RANGE, // range
    EXPORT, // export
    TRUE, // true
    FALSE, // false
    AND, // and  &&
    OR, // or  ||
    BREAK, // break
    BREAK_ALL, // break
    CONTINUE, // continue
    COMMENT, // 注释
    ECHO, //打印
    LINE_END,//代码中的行结束
    NEW, // new


    LINE_END_SYMBOL, //\n 换行符号
    TAB_SYMBOL, ////\t 制表符号
}