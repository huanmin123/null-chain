package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.ParseScopeTracker;
import com.gitee.huanminabc.nullchain.language.syntaxNode.BlockSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * do-while表达式
 * do { ... } while condition
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class DoWhileSyntaxNode extends BlockSyntaxNode {

    public DoWhileSyntaxNode() {
        super(SyntaxNodeType.DO_WHILE_EXP);
    }

    public DoWhileSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.DO;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.DO) {
                int endIndex = skipDoWhileEnd(tokens, i);
                List<Token> doWhileTokens = new ArrayList<>(tokens.subList(i, endIndex));

                if (doWhileTokens.isEmpty()) {
                    throw new NfSyntaxException(
                        token.getLine(),
                        "do-while表达式语法错误",
                        "do-while语句的tokens为空，无法解析",
                        token.getValue(),
                        "请检查do-while语句的语法格式：do { ... } while condition"
                    );
                }

                tokens.subList(i, endIndex).clear();

                DoWhileSyntaxNode doWhileStatement = new DoWhileSyntaxNode(SyntaxNodeType.DO_WHILE_EXP);
                doWhileStatement.setValue(doWhileTokens);
                doWhileStatement.setLine(token.getLine());

                if (!buildChildStatement(doWhileStatement)) {
                    return false;
                }
                syntaxNodeList.add(doWhileStatement);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        List<Token> tokenList = syntaxNode.getValue();
        if (tokenList == null || tokenList.isEmpty()) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "do-while表达式tokens为空",
                "do-while表达式的tokens列表不能为空",
                "",
                "请检查do-while语句的语法格式"
            );
        }

        // 去掉开头的DO
        tokenList.remove(0);

        // 找到第一个{+LINE_END的位置（do { ... } while condition）
        int blockStartIndex = 0;
        int tokenListSize = tokenList.size();
        for (int i = 0; i < tokenListSize - 1; i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LBRACE && tokenList.get(i + 1).type == TokenType.LINE_END) {
                blockStartIndex = i;
                break;
            }
        }

        // 找到while的位置（在}之后）
        int whileIndex = -1;
        int blockEndIndex = -1;
        int depth = 0;
        for (int i = 0; i < tokenListSize; i++) {
            if (i < tokenListSize - 1 && tokenList.get(i).type == TokenType.LBRACE && tokenList.get(i + 1).type == TokenType.LINE_END) {
                depth++;
            }
            if (tokenList.get(i).type == TokenType.RBRACE) {
                depth--;
                if (depth == 0) {
                    blockEndIndex = i;
                    // 检查下一个token是否是while
                    if (i + 1 < tokenListSize && tokenList.get(i + 1).type == TokenType.WHILE) {
                        whileIndex = i + 1;
                    }
                    break;
                }
            }
        }

        if (whileIndex == -1) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "do-while表达式语法错误",
                "do-while语句缺少while关键字",
                "",
                "请检查do-while语句的语法格式：do { ... } while condition"
            );
        }

        // 截取do块中的内容（去掉do { 和 }）
        List<Token> bodyTokens = new ArrayList<>(tokenList.subList(blockStartIndex + 1, blockEndIndex));
        // 去掉开头的换行
        if (!bodyTokens.isEmpty() && bodyTokens.get(0).type == TokenType.LINE_END) {
            bodyTokens.remove(0);
        }

        // 截取while条件（去掉while）
        List<Token> conditionTokens = new ArrayList<>(tokenList.subList(whileIndex + 1, tokenListSize));

        if (conditionTokens.isEmpty()) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "do-while表达式语法错误",
                "do-while语句的while条件不能为空",
                "",
                "请检查do-while语句的语法格式：do { ... } while condition"
            );
        }

        // 设置条件
        syntaxNode.setValue(conditionTokens);

        // 构建循环体
        // do-while循环体创建新作用域
        ParseScopeTracker tracker = NfSynta.getCurrentTracker();
        if (tracker != null) {
            tracker.enterScope(ParseScopeTracker.ScopeType.BLOCK); // 进入do-while循环体作用域
        }
        try {
            List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(bodyTokens, tracker);
            ((DoWhileSyntaxNode) syntaxNode).setChildSyntaxNodeList(syntaxNodes);
        } finally {
            if (tracker != null) {
                tracker.exitScope(); // 退出do-while循环体作用域
            }
        }
        return true;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        if (!(syntaxNode instanceof DoWhileSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望DoWhileSyntaxNode，实际:{}",
                syntaxNode.getLine(), syntaxNode.getClass().getName());
        }

        DoWhileSyntaxNode doWhileSyntaxNode = (DoWhileSyntaxNode) syntaxNode;
        List<SyntaxNode> childList = doWhileSyntaxNode.getChildSyntaxNodeList();
        List<Token> condition = doWhileSyntaxNode.getValue();

        if (condition == null || condition.isEmpty()) {
            throw new NfException("Line:{} ,do-while表达式条件为空", doWhileSyntaxNode.getLine());
        }

        String currentScopeId = context.getCurrentScopeId();

        StringBuilder conditionBuilder = TokenUtil.mergeToken(condition);

        // do-while循环：先执行一次，再判断条件
        while (true) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            // 第一次执行（无条件）
            if (context.isGlobalBreakAll()) {
                break;
            }

            // 创建子作用域并执行循环体
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 设置当前作用域为新创建的子作用域
            context.setCurrentScopeId(newScope.getScopeId());

            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }

            // 恢复原作用域
            context.setCurrentScopeId(currentScopeId);
            context.removeScope(newScope.getScopeId());

            // 执行后检查控制流标志
            if (context.isGlobalBreakAll()) {
                break;
            }

            if (newScope.isBreak()) {
                break;
            }

            if (newScope.isBreakAll()) {
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }

            if (newScope.isContinue()) {
                // clear continue flag and continue to check condition
                newScope.setContinue(false);
            }

            // 检查while条件
            boolean isTrue = false;
            try {
                Object result = NfCalculator.arithmetic(conditionBuilder.toString(), context);
                if (result instanceof Boolean) {
                    isTrue = (Boolean) result;
                }
            } catch (Exception e) {
                throw new NfException(e, "Line:{}  do-while表达式计算错误: {} ",
                    doWhileSyntaxNode.getLine(), conditionBuilder.toString());
            }

            if (!isTrue) {
                break;
            }
        }

        // 循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    /**
     * 跳到do-while结束位置获取结束下标
     * do-while格式：do { ... } while condition
     *
     * @param tokens token列表
     * @param i 起始位置
     * @return 结束位置
     */
    private int skipDoWhileEnd(List<Token> tokens, int i) {
        int endIndex = 0;
        int depth = 0;
        int tokensSize = tokens.size();

        // 跳过do和{
        int startIndex = i;
        boolean foundBrace = false;

        for (int j = i; j < tokensSize; j++) {
            if (tokens.get(j).type == TokenType.LBRACE) {
                foundBrace = true;
                break;
            }
        }

        if (!foundBrace) {
            return tokensSize;
        }

        // 从do开始找匹配的}
        for (int j = startIndex; j < tokensSize; j++) {
            Token currentToken = tokens.get(j);
            Token nextToken = j + 1 < tokensSize ? tokens.get(j + 1) : null;

            if (currentToken.type == TokenType.LBRACE && nextToken != null && nextToken.type == TokenType.LINE_END) {
                depth++;
            }
            if (currentToken.type == TokenType.RBRACE) {
                depth--;
                if (depth == 0) {
                    // 找到}，继续找while条件结束
                    // while条件直到行结束或下一个语句
                    for (int k = j + 1; k < tokensSize; k++) {
                        if (tokens.get(k).type == TokenType.LINE_END) {
                            endIndex = k;
                            break;
                        }
                    }
                    if (endIndex == 0) {
                        endIndex = tokensSize - 1;
                    }
                    break;
                }
            }
        }

        return endIndex + 1;
    }

    /**
     * 将breakAll标志传播到所有祖先FOR作用域
     * 这个方法处理嵌套循环场景，确保breakall能够跳出所有层级的FOR循环
     *
     * @param context 上下文
     * @param scopeId 起始作用域ID（当前FOR作用域）
     */
    private void propagateBreakAllToAncestorFors(NfContext context, String scopeId) {
        String currentScopeId = scopeId;
        // 向上遍历作用域链，找到所有FOR类型的作用域并设置breakAll标志
        while (currentScopeId != null) {
            NfContextScope scope = context.getScope(currentScopeId);
            if (scope == null) {
                break;
            }
            // 如果是FOR作用域，设置breakAll标志
            if (scope.getType() == NfContextScopeType.FOR) {
                scope.setBreakAll(true);
            }
            // 如果到达主作用域（ALL类型），停止传播
            if (scope.getType() == NfContextScopeType.ALL) {
                break;
            }
            // 继续向上遍历
            currentScopeId = scope.getParentScopeId();
        }
    }
}
