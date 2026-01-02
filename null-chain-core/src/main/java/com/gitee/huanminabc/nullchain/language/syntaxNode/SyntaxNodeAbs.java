package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.token.Token;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法节点抽象基类
 * 
 * <p>所有语法节点的基类，提供公共的字段和方法。
 * 使用Lombok的@Data注解自动生成getter/setter方法。</p>
 * 
 * @author huanmin
 * @date 2024/11/22
 * @since 1.1.4
 */
@Data
public abstract class SyntaxNodeAbs {
    /** 语法节点类型 */
    private SyntaxNodeType type;
    
    /** Token值列表 */
    private List<Token> value;
    
    /** 行号, 用于控制执行顺序 */
    private Integer line;

    /** 节点自己的子节点, 比如if的执行体 */
    private List<SyntaxNode> childSyntaxNodeList;
    
    /** 结构类型 是行节点还是块节点 */
    private SyntaxNodeStructType structType;

    public SyntaxNodeAbs() {
    }

    public SyntaxNodeAbs(SyntaxNodeType type) {
        this.type = type;
    }

    public void  addChild(SyntaxNode syntaxNode){
        if (childSyntaxNodeList == null) {
            childSyntaxNodeList = new ArrayList<>();
        }
        childSyntaxNodeList.add(syntaxNode);
    }

    /**
     * 生成树形结构的字符串表示
     * 用于可视化 AST（抽象语法树）
     * 
     * @return 树形结构的字符串
     */
    public String toTreeString() {
        return toTreeString(0);
    }
    
    /**
     * 生成树形结构的字符串表示（带缩进）
     * 
     * @param indent 当前缩进级别
     * @return 树形结构的字符串
     */
    private String toTreeString(int indent) {
        StringBuilder sb = new StringBuilder();
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentBuilder.append("  ");
        }
        String indentStr = indentBuilder.toString();
        
        // 节点信息
        sb.append(indentStr).append("├─ ").append(type != null ? type.name() : "UNKNOWN");
        if (line != null) {
            sb.append(" (Line: ").append(line).append(")");
        }
        sb.append("\n");
        
        // 值信息（如果有）
        if (value != null && !value.isEmpty()) {
            String valueStr = value.stream()
                .map(t -> t.value != null ? t.value : t.type.name())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
            if (valueStr.length() > 50) {
                valueStr = valueStr.substring(0, 47) + "...";
            }
            sb.append(indentStr).append("│  Value: ").append(valueStr).append("\n");
        }
        
        // 子节点
        if (childSyntaxNodeList != null && !childSyntaxNodeList.isEmpty()) {
            for (int i = 0; i < childSyntaxNodeList.size(); i++) {
                SyntaxNode child = childSyntaxNodeList.get(i);
                boolean isLast = (i == childSyntaxNodeList.size() - 1);
                if (child instanceof SyntaxNodeAbs) {
                    SyntaxNodeAbs childAbs = (SyntaxNodeAbs) child;
                    String childStr = childAbs.toTreeString(indent + 1);
                    // 调整最后子节点的前缀
                    if (isLast) {
                        childStr = childStr.replaceFirst("├─", "└─");
                        childStr = childStr.replaceAll("│  ", "   ");
                    }
                    sb.append(childStr);
                } else {
                    sb.append(indentStr).append(isLast ? "└─ " : "├─ ")
                      .append(child.getType() != null ? child.getType().name() : "UNKNOWN").append("\n");
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 生成 JSON 格式的字符串表示
     * 用于外部工具可视化 AST
     * 
     * @return JSON 格式的字符串
     */
    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        toJsonString(sb, 0);
        return sb.toString();
    }
    
    /**
     * 生成 JSON 格式的字符串表示（递归）
     * 
     * @param sb StringBuilder
     * @param indent 当前缩进级别
     */
    private void toJsonString(StringBuilder sb, int indent) {
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentBuilder.append("  ");
        }
        String indentStr = indentBuilder.toString();
        sb.append(indentStr).append("{\n");
        
        // type
        sb.append(indentStr).append("  \"type\": \"").append(type != null ? type.name() : "UNKNOWN").append("\",\n");
        
        // line
        if (line != null) {
            sb.append(indentStr).append("  \"line\": ").append(line).append(",\n");
        }
        
        // structType
        if (structType != null) {
            sb.append(indentStr).append("  \"structType\": \"").append(structType.name()).append("\",\n");
        }
        
        // value
        if (value != null && !value.isEmpty()) {
            sb.append(indentStr).append("  \"value\": [");
            for (int i = 0; i < value.size(); i++) {
                if (i > 0) sb.append(", ");
                Token token = value.get(i);
                sb.append("{\"type\":\"").append(token.type.name())
                  .append("\",\"value\":\"").append(escapeJson(token.value)).append("\"");
                if (token.line != null) {
                    sb.append(",\"line\":").append(token.line);
                }
                sb.append("}");
            }
            sb.append("],\n");
        }
        
        // children
        if (childSyntaxNodeList != null && !childSyntaxNodeList.isEmpty()) {
            sb.append(indentStr).append("  \"children\": [\n");
            for (int i = 0; i < childSyntaxNodeList.size(); i++) {
                SyntaxNode child = childSyntaxNodeList.get(i);
                if (child instanceof SyntaxNodeAbs) {
                    SyntaxNodeAbs childAbs = (SyntaxNodeAbs) child;
                    childAbs.toJsonString(sb, indent + 2);
                } else {
                    sb.append(indentStr).append("    {\"type\":\"")
                      .append(child.getType() != null ? child.getType().name() : "UNKNOWN").append("\"}");
                }
                if (i < childSyntaxNodeList.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(indentStr).append("  ]\n");
        }
        
        sb.append(indentStr).append("}");
    }
    
    /**
     * 转义 JSON 字符串中的特殊字符
     * 
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

}
