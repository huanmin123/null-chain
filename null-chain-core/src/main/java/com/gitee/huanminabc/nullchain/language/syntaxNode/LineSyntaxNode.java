package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.util.List;

/**
 * 行节点抽象基类
 * 
 * <p>所有行节点（如ASSIGN、ECHO、RUN等）的基类，提供公共的实现逻辑。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>统一设置structType为LINE_NODE</li>
 *   <li>提供默认的buildChildStatement实现（行节点不需要构建子节点）</li>
 *   <li>提供公共的analystToken和analystSyntax实现</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public abstract class LineSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    
    /**
     * 默认构造函数（Lombok要求）
     * 注意：使用此构造函数后需要手动设置type和structType
     */
    protected LineSyntaxNode() {
        super();
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }
    
    /**
     * 构造函数
     * 
     * @param type 语法节点类型
     */
    protected LineSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }
    
    /**
     * 行节点不需要构建子节点，直接返回true
     * 
     * @param syntaxNode 语法节点
     * @return 总是返回true
     */
    @Override
    public final boolean buildChildStatement(SyntaxNode syntaxNode) {
        return true; // 行节点不需要构建子节点
    }
    
    /**
     * 获取目标Token类型，子类需要实现此方法指定要识别的TokenType
     * 
     * @return 目标Token类型
     */
    protected abstract TokenType getTargetTokenType();
    
    /**
     * 分析Token是否可以解析
     * 默认实现：检查第一个token是否匹配目标TokenType
     * 如果getTargetTokenType()返回null，子类需要重写此方法
     * 
     * @param tokens Token列表
     * @return 如果可以解析返回true，否则返回false
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        TokenType targetType = getTargetTokenType();
        if (targetType == null) {
            // 如果getTargetTokenType()返回null，说明需要子类重写此方法
            throw new UnsupportedOperationException("子类必须重写analystToken方法或实现getTargetTokenType方法");
        }
        return !tokens.isEmpty() && tokens.get(0).type == targetType;
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

