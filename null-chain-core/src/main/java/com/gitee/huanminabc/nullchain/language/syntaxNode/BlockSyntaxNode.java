package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.util.List;

/**
 * 块节点抽象基类
 * 
 * <p>所有块节点（如IF、FOR、SWITCH等）的基类，提供公共的实现逻辑。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>统一设置structType为BLOCK_NODE</li>
 *   <li>强制子类实现buildChildStatement方法（块节点必须构建子节点）</li>
 *   <li>提供公共的analystToken和analystSyntax实现</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public abstract class BlockSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    
    /**
     * 默认构造函数（Lombok要求）
     * 注意：使用此构造函数后需要手动设置type和structType
     */
    protected BlockSyntaxNode() {
        super();
        super.setStructType(SyntaxNodeStructType.BLOCK_NODE);
    }
    
    /**
     * 构造函数
     * 
     * @param type 语法节点类型
     */
    protected BlockSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.BLOCK_NODE);
    }
    
    /**
     * 块节点必须实现buildChildStatement方法来构建子节点
     * 
     * @param syntaxNode 语法节点
     * @return 是否成功构建子节点
     */
    @Override
    public abstract boolean buildChildStatement(SyntaxNode syntaxNode);
    
    /**
     * 获取目标Token类型，子类需要实现此方法指定要识别的TokenType
     * 
     * @return 目标Token类型
     */
    protected abstract TokenType getTargetTokenType();
    
    /**
     * 分析Token是否可以解析
     * 默认实现：检查第一个token是否匹配目标TokenType
     * 
     * @param tokens Token列表
     * @return 如果可以解析返回true，否则返回false
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        return !tokens.isEmpty() && tokens.get(0).type == getTargetTokenType();
    }
    
    /**
     * 分析语法节点是否可以执行
     * 默认实现：检查节点类型是否匹配当前类
     * 
     * @param syntaxNode 语法节点
     * @return 如果可以执行返回true，否则返回false
     */
    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return syntaxNode.getClass() == this.getClass();
    }
}

