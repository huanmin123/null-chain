package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.token.Token;

import java.util.List;

/**
 * 语法节点接口 - 定义语法节点的基本结构
 * 
 * <p>该接口定义了语法节点的基本结构，用于构建语法树。
 * 需要继承SyntaxNodeAbs来使用。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>语法节点：定义语法节点的基本结构</li>
 *   <li>类型获取：获取语法节点的类型</li>
 *   <li>上下文处理：处理语法节点的上下文</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>构建语法树</li>
 *   <li>语法分析</li>
 *   <li>语言处理</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see SyntaxNodeAbs 语法节点抽象类
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
