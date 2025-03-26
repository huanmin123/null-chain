package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Data
public  abstract class SyntaxNodeAbs {
    public SyntaxNodeType type;
    public List<Token> value;
    //行号, 用于控制执行顺序
    public Integer line;

    //节点自己的子节点,  比如if的执行体
    public List<SyntaxNode> childSyntaxNodeList;
    //结构类型 是行节点还是块节点
    public SyntaxNodeStructType structType;

    public SyntaxNodeAbs() {
    }

    public SyntaxNodeAbs(SyntaxNodeType type) {
        this.type = type;
    }

    public void  addChild(SyntaxNode syntaxNode){
        if (childSyntaxNodeList == null) {
            childSyntaxNodeList = Lists.newArrayList();
        }
        childSyntaxNodeList.add(syntaxNode);
    }


}
