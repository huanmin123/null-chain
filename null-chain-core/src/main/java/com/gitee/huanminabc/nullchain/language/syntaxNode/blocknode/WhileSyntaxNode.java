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
 * while表达式
 * while condition { ... }
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class WhileSyntaxNode extends BlockSyntaxNode {

    public WhileSyntaxNode() {
        super(SyntaxNodeType.WHILE_EXP);
    }

    public WhileSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.WHILE;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.WHILE) {
                int endIndex = skipWhileEnd(tokens, i);
                List<Token> whileTokens = new ArrayList<>(tokens.subList(i, endIndex));

                if (whileTokens.isEmpty()) {
                    throw new NfSyntaxException(
                        token.getLine(),
                        "while表达式语法错误",
                        "while语句的tokens为空，无法解析",
                        token.getValue(),
                        "请检查while语句的语法格式：while condition { ... }"
                    );
                }

                tokens.subList(i, endIndex).clear();

                WhileSyntaxNode whileStatement = new WhileSyntaxNode(SyntaxNodeType.WHILE_EXP);
                whileStatement.setValue(whileTokens);
                whileStatement.setLine(token.getLine());

                if (!buildChildStatement(whileStatement)) {
                    return false;
                }
                syntaxNodeList.add(whileStatement);
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
                "while表达式tokens为空",
                "while表达式的tokens列表不能为空",
                "",
                "请检查while语句的语法格式"
            );
        }

        tokenList.remove(0);

        int endIndex = 0;
        int tokenListSize = tokenList.size();
        for (int i = 0; i < tokenListSize - 1; i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LBRACE && tokenList.get(i + 1).type == TokenType.LINE_END) {
                endIndex = i;
                break;
            }
        }

        List<Token> conditionTokens = new ArrayList<>(tokenList.subList(0, endIndex));

        if (conditionTokens.isEmpty()) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "while表达式语法错误",
                "while语句的条件不能为空",
                "",
                "请检查while语句的语法格式：while condition { ... }"
            );
        }

        tokenList.subList(0, endIndex).clear();
        tokenList.remove(0);
        if (!tokenList.isEmpty() && tokenList.get(0).type == TokenType.LINE_END) {
            tokenList.remove(0);
        }
        if (!tokenList.isEmpty() && tokenList.get(tokenList.size() - 1).type == TokenType.RBRACE) {
            tokenList.remove(tokenList.size() - 1);
        }

        syntaxNode.setValue(conditionTokens);

        // while循环体创建新作用域
        ParseScopeTracker tracker = NfSynta.getCurrentTracker();
        if (tracker != null) {
            tracker.enterScope(ParseScopeTracker.ScopeType.BLOCK); // 进入while循环体作用域
        }
        try {
            List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokenList, tracker);
            if (!(syntaxNode instanceof WhileSyntaxNode)) {
                throw new NfException("Line:{} ,语法节点类型错误，期望WhileSyntaxNode，实际:{}",
                    syntaxNode.getLine(), syntaxNode.getClass().getName());
            }
            ((WhileSyntaxNode) syntaxNode).setChildSyntaxNodeList(syntaxNodes);
        } finally {
            if (tracker != null) {
                tracker.exitScope(); // 退出while循环体作用域
            }
        }
        return true;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        if (!(syntaxNode instanceof WhileSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望WhileSyntaxNode，实际:{}",
                syntaxNode.getLine(), syntaxNode.getClass().getName());
        }

        WhileSyntaxNode whileSyntaxNode = (WhileSyntaxNode) syntaxNode;
        List<SyntaxNode> childList = whileSyntaxNode.getChildSyntaxNodeList();
        List<Token> condition = whileSyntaxNode.getValue();

        if (condition == null || condition.isEmpty()) {
            throw new NfException("Line:{} ,while表达式条件为空", whileSyntaxNode.getLine());
        }

        String currentScopeId = context.getCurrentScopeId();

        StringBuilder conditionBuilder = TokenUtil.mergeToken(condition);

        while (true) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            if (context.isGlobalBreakAll()) {
                break;
            }

            boolean isTrue = false;
            try {
                Object result = NfCalculator.arithmetic(conditionBuilder.toString(), context);
                if (result instanceof Boolean) {
                    isTrue = (Boolean) result;
                }
            } catch (Exception e) {
                throw new NfException(e, "Line:{}  while表达式计算错误: {} ",
                    whileSyntaxNode.getLine(), conditionBuilder.toString());
            }

            if (!isTrue) {
                break;
            }

            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 设置当前作用域为新创建的子作用域
            context.setCurrentScopeId(newScope.getScopeId());

            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }

            // 恢复原作用域
            context.setCurrentScopeId(currentScopeId);
            context.removeScope(newScope.getScopeId());

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
                newScope.setContinue(false);
                continue;
            }
        }

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
     * 跳过while块的结束位置
     * 从第一个 LBRACE + LINE_END 的位置开始调用 skipBlockEnd，
     * 这样 depth 可以被正确初始化，避免在条件中的 RBRACE 处提前返回
     *
     * @param tokens token列表
     * @param startIndex WHILE token的起始位置
     * @return while块结束后的位置
     */
    private int skipWhileEnd(List<Token> tokens, int startIndex) {
        // 找到第一个 LBRACE + LINE_END 的位置作为实际起始位置
        // 这样 skipBlockEnd 可以正确计算 depth
        int braceStartIndex = startIndex;
        for (int i = startIndex; i < tokens.size(); i++) {
            if (i + 1 < tokens.size() && tokens.get(i).type == TokenType.LBRACE && tokens.get(i + 1).type == TokenType.LINE_END) {
                braceStartIndex = i;
                break;
            }
        }
        return BlockSyntaxNode.skipBlockEnd(tokens, braceStartIndex, false);
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
