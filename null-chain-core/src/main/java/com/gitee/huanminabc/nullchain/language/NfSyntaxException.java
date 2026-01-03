package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.token.Token;

/**
 * NF语法异常类
 * 
 * <p>专门用于语法解析阶段的异常，提供更详细的错误信息，包括：
 * <ul>
 *   <li>行号：错误发生的行号</li>
 *   <li>错误类型：语法错误的具体类型</li>
 *   <li>错误描述：详细的错误描述</li>
 *   <li>错误上下文：错误位置的代码片段</li>
 *   <li>建议信息：可能的修复建议</li>
 * </ul>
 * </p>
 * 
 * @author huanmin
 * @date 2024/11/22
 * @since 1.1.4
 */
public class NfSyntaxException extends NfException {
    
    /** 错误发生的行号 */
    private final Integer line;
    
    /** 错误类型 */
    private final String errorType;
    
    /** 错误上下文（错误位置的代码片段） */
    private final String context;
    
    /** 建议信息 */
    private final String suggestion;
    
    /**
     * 构造函数
     *
     * @param line 行号
     * @param errorType 错误类型
     * @param message 错误描述
     * @param context 错误上下文
     * @param suggestion 建议信息
     */
    public NfSyntaxException(Integer line, String errorType, String message, String context, String suggestion) {
        // 注意：这里传递空数组避免格式化处理，因为suggestion中可能包含代码示例如"for i in 1..10 {}"
        // 如果进行格式化会导致MissingFormatArgumentException
        super(buildErrorMessage(line, errorType, message, context, suggestion), new Object[0]);
        this.line = line;
        this.errorType = errorType;
        this.context = context;
        this.suggestion = suggestion;
    }
    
    /**
     * 构造函数（从Token构建）
     * 
     * @param token 发生错误的Token
     * @param errorType 错误类型
     * @param message 错误描述
     * @param suggestion 建议信息
     */
    public NfSyntaxException(Token token, String errorType, String message, String suggestion) {
        this(token != null ? token.getLine() : null, errorType, message, 
             token != null ? token.getValue() : "", suggestion);
    }
    
    /**
     * 构造函数（简化版，只有行号和消息）
     * 
     * @param line 行号
     * @param message 错误描述
     */
    public NfSyntaxException(Integer line, String message) {
        this(line, "语法错误", message, "", "");
    }
    
    /**
     * 构建完整的错误消息
     * 
     * @param line 行号
     * @param errorType 错误类型
     * @param message 错误描述
     * @param context 错误上下文
     * @param suggestion 建议信息
     * @return 格式化的错误消息
     */
    private static String buildErrorMessage(Integer line, String errorType, String message, 
                                           String context, String suggestion) {
        StringBuilder sb = new StringBuilder();
        
        // 行号信息
        if (line != null) {
            sb.append("Line ").append(line).append(": ");
        }
        
        // 错误类型
        if (errorType != null && !errorType.isEmpty()) {
            sb.append("[").append(errorType).append("] ");
        }
        
        // 错误描述
        sb.append(message);
        
        // 错误上下文
        if (context != null && !context.isEmpty()) {
            sb.append("\n  位置: ").append(context);
        }
        
        // 建议信息
        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append("\n  建议: ").append(suggestion);
        }
        
        return sb.toString();
    }
    
    /**
     * 获取行号
     * 
     * @return 行号
     */
    public Integer getLine() {
        return line;
    }
    
    /**
     * 获取错误类型
     * 
     * @return 错误类型
     */
    public String getErrorType() {
        return errorType;
    }
    
    /**
     * 获取错误上下文
     * 
     * @return 错误上下文
     */
    public String getContext() {
        return context;
    }
    
    /**
     * 获取建议信息
     * 
     * @return 建议信息
     */
    public String getSuggestion() {
        return suggestion;
    }
}

