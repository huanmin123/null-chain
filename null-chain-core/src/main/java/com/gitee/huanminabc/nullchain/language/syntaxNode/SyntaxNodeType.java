package com.gitee.huanminabc.nullchain.language.syntaxNode;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public enum SyntaxNodeType {
    IMPORT_EXP, // import语句（支持 type 和 task 导入）
    ASSIGN_EXP, // 赋值表达式
    DECLARE_EXP, // 声明表达式
    VAR_EXP, // var变量声明表达式
    RUN_EXP, // 运行任务表达式
    EXPORT_EXP, // export语句
    IF_EXP, // if语句
    SWITCH_EXP, // export语句
    WHILE_EXP, // while语句
    DO_WHILE_EXP, // do while语句
    FOR_EXP, // for语句
    ECHO_EXP, // echo语句
    FUN_EXE_EXP, // 函数调用
    BREAK_EXP, // break语句
    BREAK_ALL_EXP, // break语句
    CONTINUE_EXP, // continue语句
    FUN_DEF_EXP, // 函数定义表达式
    FUN_CALL_EXP, // 函数调用表达式
    FUN_REF_EXP, // 函数引用表达式（Fun<...> varName = functionName）
    LAMBDA_EXP, // Lambda 表达式（(参数列表) -> { 函数体 }）
    RETURN_EXP, // return语句表达式


}