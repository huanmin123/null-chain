package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.token.Token;

import java.util.List;

//需要继承SyntaxNodeAbs来使用
/**
 * @author huanmin
 * @date 2024/11/22
 */
public interface SyntaxNode {
    SyntaxNodeType getType();

    List<Token> getValue();

    void setValue(List<Token> value);

    Integer getLine();  //获取行号

    //获取结构类型, 行结构还是块结构
    SyntaxNodeStructType getStructType();

    void addChild(SyntaxNode syntaxNode);

    //分析token是否可以解析,如果可以解析就会调用buildStatement
    boolean analystToken(List<Token> tokens);

    boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList);

    //部分语法节点需要构建子节点或者平级节点 , 特别是块结构的语法基本都需要
    default boolean buildChildStatement(SyntaxNode syntaxNode) {
        return true;
    }


    //分析语法节点是否可以执行如果可以执行就会调用run
     boolean analystSyntax(SyntaxNode syntaxNode);

    //运行语法节点
     void run(NfContext context, SyntaxNode syntaxNode) ;
}
