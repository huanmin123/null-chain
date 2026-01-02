package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;

import com.gitee.huanminabc.nullchain.language.syntaxNode.BlockSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * for表达式: for i in 1..10 {}
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ForSyntaxNode extends BlockSyntaxNode {
    public ForSyntaxNode() {
        super(SyntaxNodeType.FOR_EXP);
    }
    
    public ForSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.FOR;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.FOR) {
                //记录结束下标, 用于截取和删除
                int endIndex =skipForEnd(tokens, i);
                //截取for表达式的标记序列
                List<Token> forTokens = new ArrayList<>(tokens.subList(i, endIndex));
                //删除
                tokens.subList(i, endIndex).clear();

                ForSyntaxNode forStatement = new ForSyntaxNode(SyntaxNodeType.FOR_EXP);
                forStatement.setValue(forTokens);
                forStatement.setLine(token.getLine());
                //构建子节点
                if (!buildChildStatement(forStatement)) {
                    return false;
                }
                syntaxNodeList.add(forStatement);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建for语句的子节点
     * 
     * <p>此方法会解析for语句的tokens，提取循环条件和循环体，构建子节点。
     * 
     * <p><b>副作用说明</b>：此方法会修改传入的 syntaxNode 节点：
     * <ul>
     *   <li>会修改 syntaxNode.getValue() 返回的 tokens 列表（移除已解析的 tokens）</li>
     *   <li>会将原始的 tokens 分解，条件部分保留在父节点的 value 中，循环体部分构建为子节点</li>
     * </ul>
     * 
     * @param syntaxNode for语句节点（会被修改）
     * @return 如果成功构建返回 true，否则返回 false
     */
    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        //for的token
        List<Token> tokenList = syntaxNode.getValue();
        //将条件提取出来
        //去掉开头的FOR
        tokenList.remove(0);
        //一直找到第一个{+LINE_END
        int endIndex=0;
        //优化：缓存size，避免在循环中重复调用
        int tokenListSize = tokenList.size();
        for (int i = 0; i < tokenListSize; i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LBRACE && tokenList.get(i + 1).type == TokenType.LINE_END) {
                endIndex = i;
                break;
            }
        }
        //截取for表达式条件
        List<Token> forTokens = new ArrayList<>(tokenList.subList(0, endIndex));

        //校验变量名必须存在（第一个token必须是IDENTIFIER）
        if (forTokens.isEmpty() || forTokens.get(0).type != TokenType.IDENTIFIER) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.isEmpty() ? 0 : forTokens.get(0).line,
                "for表达式语法错误",
                "for表达式缺少循环变量名",
                context,
                "正确的格式：for variable in start..end { ... }，variable 必须是有效的变量名"
            );
        }

        //必须存在In
        if (forTokens.stream().noneMatch(t -> t.type == TokenType.IN)) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.get(0).line,
                "for表达式语法错误",
                "for表达式必须包含 'in' 关键字",
                context,
                "正确的格式：for variable in start..end { ... }"
            );
        }

        //判断必须存在DOT2
        if (forTokens.stream().noneMatch(t -> t.type == TokenType.DOT2)) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.get(0).line,
                "for表达式语法错误",
                "for表达式必须包含范围操作符 '..'",
                context,
                "正确的格式：for variable in start..end { ... }"
            );
        }
        //DOT2前后必须是INTEGER
        //找到DOT2的位置
        int dot2Index = 0;
        //优化：缓存size，避免在循环中重复调用
        int forTokensSize = forTokens.size();
        for (int i = 0; i < forTokensSize; i++) {
            if (forTokens.get(i).type == TokenType.DOT2) {
                dot2Index = i;
                break;
            }
        }
        //取出DOT2前后的token
        Token startToken = forTokens.get(dot2Index - 1);
        //如果长度不够那么就是语法错误
        if (dot2Index + 1 >= forTokens.size()) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.get(0).line,
                "for表达式语法错误",
                "范围操作符 '..' 后面缺少结束值",
                context,
                "正确的格式：for variable in start..end { ... }，其中 start 和 end 必须是整数"
            );
        }
        Token endToken = forTokens.get(dot2Index + 1);
        //判断是否是整数
        if (startToken.type != TokenType.INTEGER || endToken.type != TokenType.INTEGER) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.get(0).line,
                "for表达式类型错误",
                "范围操作符 '..' 前后必须是整数",
                context,
                "正确的格式：for variable in start..end { ... }，其中 start 和 end 必须是整数"
            );
        }
        //校验第一个整数必须小于或者等于第二个
        if (Integer.parseInt(startToken.value) > Integer.parseInt(endToken.value)) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                forTokens.get(0).line,
                "for表达式范围错误",
                "范围操作符 '..' 前面的整数必须小于等于后面的整数",
                context,
                "请确保起始值 <= 结束值，例如：for i in 1..10 { ... }"
            );
        }
        //删除
        tokenList.subList(0, endIndex).clear();
        //删除{+LINE_END
        tokenList.remove(0);
        tokenList.remove(0);
        //删除最后的}
        tokenList.remove(tokenList.size()-1);
        //设置for条件
        syntaxNode.setValue(forTokens);
        //构建子节点
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokenList);
        if (!(syntaxNode instanceof ForSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望ForSyntaxNode，实际:{}", 
                syntaxNode.getLine(), syntaxNode.getClass().getName());
        }
        ((ForSyntaxNode)syntaxNode).setChildSyntaxNodeList(syntaxNodes);
        return true;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        if (!(syntaxNode instanceof ForSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望ForSyntaxNode，实际:{}", 
                syntaxNode.getLine(), syntaxNode.getClass().getName());
        }
        ForSyntaxNode forSyntaxNode = (ForSyntaxNode) syntaxNode;
        List<Token> forValue = forSyntaxNode.getValue();
        if (forValue == null || forValue.size() < 5) {
            throw new NfException("Line:{} ,for表达式格式错误，必须包含变量名、in关键字和范围值 , syntax:{} ", 
                forSyntaxNode.getLine(), syntaxNode);
        }
        //i in 1..10
        //取出i
        String i = forValue.get(0).value;
        //取出start
        String start = forValue.get(2).value;
        //取出end
        String end = forValue.get(4).value;
        //保留当前作用域id
        String currentScopeId = context.getCurrentScopeId();
        //优化：提前解析start和end，避免在循环中重复调用parseInt
        int startInt = Integer.parseInt(start);
        int endInt = Integer.parseInt(end);
        //循环
        List<SyntaxNode> childList = forSyntaxNode.getChildSyntaxNodeList();
        if (childList == null) {
            // for循环体为空，直接返回
            return;
        }
        for (int j = startInt; j <= endInt; j++) {
            //检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }
            //创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            //将i的值赋值
            newScope.addVariable(new NfVariableInfo(i,j, Integer.class));
            //执行子节点
            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }
            //移除子作用域
            context.removeScope(newScope.getScopeId());

            //执行子节点后再次检查globalBreakAll标志（breakall可能在子节点中被触发）
            //如果globalBreakAll被设置，立即跳出循环，不要执行后续的清除操作
            if (context.isGlobalBreakAll()) {
                break;
            }
            //如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            //如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                //传播breakAll标志到所有祖先FOR作用域
                //这样可以处理嵌套场景，例如：FOR -> FOR -> IF，IF中的breakall需要传播到外层FOR
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }
        }
        //循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        //只有当父作用域不是FOR类型时，才清除全局breakAll标志
        //这样可以确保嵌套的FOR循环也能正确响应breakall
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    //跳到For结束位置获取结束下标
    private int skipForEnd(List<Token> tokens, int i) {
        return BlockSyntaxNode.skipBlockEnd(tokens, i, false);
    }

    //打印for 的token
    private String printFor(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            //遇到LINE_END换行
            if (token.type == TokenType.LINE_END) {
                sb.append("\n");
                continue;
            }
            //遇到DOT2跳过
            if (token.type == TokenType.DOT2) {
                sb.append(token.value);
                continue;
            }
            //如果是INTEGER,判断前一个如果是DOT2
            if (token.type == TokenType.INTEGER) {
                if (i > 0 && tokens.get(i - 1).type == TokenType.DOT2) {
                    sb.append(token.value);
                    continue;
                }
            }
            sb.append(" ").append(token.value);
        }
        return sb.toString();
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