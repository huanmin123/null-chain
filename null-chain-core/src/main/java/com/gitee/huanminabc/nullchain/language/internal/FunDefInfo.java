package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数定义信息
 * 用于存储函数定义的完整信息，包括函数名、参数列表、返回值类型和函数体
 *
 * @author huanmin
 * @date 2024/11/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunDefInfo {
    /**
     * 函数名
     */
    private String functionName;
    
    /**
     * 参数列表
     * 每个参数包含：参数名、参数类型、是否可变参数
     */
    private List<FunParameter> parameters = new ArrayList<>();
    
    /**
     * 返回值类型列表（支持多返回值）
     */
    private List<String> returnTypes = new ArrayList<>();
    
    /**
     * 函数体（语法节点列表）
     */
    private List<SyntaxNode> bodyNodes = new ArrayList<>();
    
    /**
     * 函数参数信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunParameter {
        /**
         * 参数名
         */
        private String name;
        
        /**
         * 参数类型（Java类型全限定名）
         */
        private String type;
        
        /**
         * 是否可变参数（如 String... names）
         */
        private boolean varArgs;
    }
}


