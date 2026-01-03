package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;

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
 *   <li>提供公共的analystToken实现</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public abstract class BlockSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {

    /**
     * 默认构造函数
     * 用于支持 Lombok 生成子类的无参构造函数
     * 注意：使用此构造函数时，type 需要后续手动设置
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
     * 查找LINE_END的位置
     * 从指定位置开始查找，直到找到LINE_END或到达列表末尾
     * 
     * @param tokens Token列表
     * @param startIndex 开始查找的位置
     * @return LINE_END的位置，如果未找到则返回tokens.size()
     * @deprecated 使用 {@link SyntaxNodeUtil#findLineEndIndex(List, int)} 替代
     */
    @Deprecated
    protected static int findLineEndIndex(List<Token> tokens, int startIndex) {
        return SyntaxNodeUtil.findLineEndIndex(tokens, startIndex);
    }
    
    /**
     * 删除Token列表中的注释
     * 
     * @param tokens Token列表
     * @deprecated 使用 {@link SyntaxNodeUtil#removeComments(List)} 替代
     */
    @Deprecated
    protected static void removeComments(List<Token> tokens) {
        SyntaxNodeUtil.removeComments(tokens);
    }
    
    /**
     * 构建块语句的公共模板方法
     * 
     * <p>提供块节点 buildStatement 的公共实现模板，减少代码重复。
     * 子类可以通过重写特定方法来定制行为。</p>
     * 
     * @param tokens Token列表
     * @param syntaxNodeList 语法节点列表
     * @param targetTokenType 目标Token类型
     * @param nodeFactory 节点工厂方法，用于创建节点实例
     * @param skipEndFunction 计算结束位置的函数
     * @return 如果成功构建返回 true，否则返回 false
     */
    protected boolean buildBlockStatementTemplate(
        List<Token> tokens,
        List<SyntaxNode> syntaxNodeList,
        TokenType targetTokenType,
        java.util.function.Function<Token, ? extends BlockSyntaxNode> nodeFactory,
        java.util.function.BiFunction<List<Token>, Integer, Integer> skipEndFunction
    ) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == targetTokenType) {
                // 计算结束位置
                int endIndex = skipEndFunction.apply(tokens, i);
                // 截取tokens
                List<Token> blockTokens = new java.util.ArrayList<>(tokens.subList(i, endIndex));
                
                // 验证tokens不为空（子类可以重写 validateTokens 方法自定义验证）
                if (!validateTokens(blockTokens, token)) {
                    return false;
                }
                
                // 删除已解析的tokens
                tokens.subList(i, endIndex).clear();
                
                // 创建节点
                BlockSyntaxNode blockNode = nodeFactory.apply(token);
                blockNode.setValue(blockTokens);
                blockNode.setLine(token.getLine());
                
                // 构建子节点
                if (!buildChildStatement(blockNode)) {
                    return false;
                }
                
                syntaxNodeList.add(blockNode);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证tokens是否有效
     * 子类可以重写此方法进行自定义验证
     * 
     * @param tokens 要验证的tokens
     * @param firstToken 第一个token
     * @return 如果有效返回 true，否则返回 false（会抛出异常）
     */
    protected boolean validateTokens(List<Token> tokens, Token firstToken) {
        // 默认实现：检查tokens不为空
        if (tokens.isEmpty()) {
            throw new com.gitee.huanminabc.nullchain.language.NfSyntaxException(
                firstToken.getLine(),
                "块语句语法错误",
                "块语句的tokens为空，无法解析",
                firstToken.getValue(),
                "请检查块语句的语法格式"
            );
        }
        return true;
    }
    
    /**
     * 跳到块结束位置获取结束下标
     * 通过深度计算找到匹配的块结束位置（RBRACE）
     * 
     * @param tokens Token列表
     * @param startIndex 开始查找的位置
     * @param checkElse 是否检查 ELSE 关键字（用于 if 语句）
     * @return 块结束位置的下标（包含 RBRACE），如果未找到则返回 tokens.size()
     */
    protected static int skipBlockEnd(List<Token> tokens, int startIndex, boolean checkElse) {
        //记录结束下标, 用于截取和删除
        int endIndex = 0;
        //记录深度  每次遇到 LBRACE + LINE_END 深度+1, 遇到 RBRACE 深度-1
        int depth = 0;
        int tokensSize = tokens.size();
        if (tokensSize < 2 || startIndex >= tokensSize - 1) {
            return tokensSize; // 如果tokens不足或起始位置无效，返回列表末尾
        }
        //遇到RBRACE + LINE_END结束
        for (int j = startIndex; j < tokensSize - 1; j++) {
            Token currentToken = tokens.get(j);
            Token nextToken = tokens.get(j + 1);
            if (currentToken.type == TokenType.LBRACE && nextToken.type == TokenType.LINE_END) {
                depth++;
            }
            //}  || } else (如果checkElse为true)
            if (currentToken.type == TokenType.RBRACE) {
                if (checkElse) {
                    // 只有当后面是LINE_END或ELSE时，才减少深度
                    if (nextToken.type == TokenType.LINE_END || nextToken.type == TokenType.ELSE) {
                        depth--;
                    }
                } else {
                    // 不检查ELSE时，遇到RBRACE就减少深度
                    depth--;
                }
            }
            //当深度为0且遇到RBRACE时, 说明块表达式结束
            if (depth == 0 && currentToken.type == TokenType.RBRACE) {
                // 如果checkElse为true，需要确保后面是LINE_END（if语句的结束必须是 } + 换行）
                if (checkElse) {
                    if (nextToken.type == TokenType.LINE_END) {
                        endIndex = j + 1;
                        break;
                    }
                } else {
                    // 对于switch和for，只需要深度为0且遇到RBRACE即可
                    endIndex = j + 1;
                    break;
                }
            }
        }
        return endIndex;
    }

}

