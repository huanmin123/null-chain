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


}
