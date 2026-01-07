package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfReturnException;
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
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * if表达式
 *     //识别语句的类型
 *     // if a > b {  a = 1; }
 *     // if a > b {  a = 1; } else {  a = 2; }
 *     // if a > b {  a = 1; } else if a < b {  a = 2; }
 *     // if a > b {  a = 1; } else if a < b {  a = 2; } else {  a = 3; }
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class IFSyntaxNode extends BlockSyntaxNode {
    //if表达式类型
    private IFType ifType;
    
    public IFSyntaxNode() {
        super(SyntaxNodeType.IF_EXP);
    }
    
    public IFSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.IF;
    }
    public enum IFType {
        IF,
        ELSE_IF,
        ELSE,
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.IF) {
                //获取语句结束下标, 用于截取和删除
                int endIndex = skipIfEnd(tokens, i);
                //截取if表达式的标记序列
                List<Token> ifTokens = new ArrayList<>(tokens.subList(i, endIndex));
                //如果是0那么就是语法有问题
                if (ifTokens.isEmpty()) {
                    throw new NfSyntaxException(
                        token.getLine(),
                        "if表达式语法错误",
                        "if语句的tokens为空，无法解析",
                        token.getValue(),
                        "请检查if语句的语法格式：if condition { ... }"
                    );
                }
                //删除
                tokens.subList(i, endIndex).clear();

                IFSyntaxNode ifStatement = new IFSyntaxNode(SyntaxNodeType.IF_EXP);
                ifStatement.setValue(ifTokens);
                ifStatement.setLine(token.getLine());
                //构建子节点
                if (!buildChildStatement(ifStatement)) {
                    return false;
                }
                syntaxNodeList.add(ifStatement);
                return true;
            }
        }
        return false;
    }

    //跳到if结束位置获取结束下标
    private int skipIfEnd(List<Token> tokens, int i) {
        return BlockSyntaxNode.skipBlockEnd(tokens, i, true);
    }
    //只跳到下一个块if位置
    private int skipIf1Block(List<Token> tokens) {
        int tokensSize = tokens.size();
        if (tokensSize < 2) {
            return tokensSize > 0 ? tokensSize - 1 : 0;
        }

        int depth = 0;
        int endIndex = -1; // if 块的 } 位置

        // 完整扫描所有 tokens
        for (int j = 0; j < tokensSize; j++) {
            Token token = tokens.get(j);
            if (token.type == TokenType.LBRACE) {
                depth++;
            } else if (token.type == TokenType.RBRACE) {
                depth--;
                // 找到第一个深度为 0 的 }，这就是块结束位置
                if (depth == 0) {
                    endIndex = j;
                    break;
                }
            }
        }

        // 如果没有找到 depth 回到 0 的 }，返回最后一个位置
        if (endIndex == -1) {
            return tokensSize > 0 ? tokensSize - 1 : 0;
        }

        // 返回 endIndex + 1（包含 }）
        return endIndex + 1;
    }


    /**
     * 构建if语句的子节点
     * 
     * <p>此方法会解析if语句的tokens，将其分解为 if、else if、else 等子节点。
     * 
     * <p><b>副作用说明</b>：此方法会修改传入的 syntaxNode 节点：
     * <ul>
     *   <li>会修改 syntaxNode.getValue() 返回的 tokens 列表（移除已解析的 tokens）</li>
     *   <li>构建完成后会清空父节点的 value（调用 clearParentNodeValue）</li>
     *   <li>原始的 tokens 会被分解到各个子节点中</li>
     * </ul>
     * 
     * @param syntaxNode if语句节点（会被修改）
     * @return 如果成功构建返回 true，否则返回 false
     */
    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        //第一步需要先将全部的if else if else 语句分割出来
        List<Token> tokenList = syntaxNode.getValue();
        if (tokenList == null || tokenList.isEmpty()) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "if表达式tokens为空",
                "if表达式的tokens列表不能为空",
                "",
                "请检查if语句的语法格式"
            );
        }
        //去掉IF
        tokenList.remove(0);
        IFSyntaxNode ifSyntaxNode = splitIf(tokenList,IFType.IF);
        syntaxNode.addChild(ifSyntaxNode);
        while (true) {
            IFSyntaxNode elseIfSyntaxNode = splitElseIf(tokenList);
            if (elseIfSyntaxNode == null) {
                break;
            }
            syntaxNode.addChild(elseIfSyntaxNode);
        }
        IFSyntaxNode elseSyntaxNode = splitElse(tokenList);
        if (elseSyntaxNode != null) {
            syntaxNode.addChild(elseSyntaxNode);
        }
        //清除原有的标记序列（因为tokens已经被分解到子节点中，父节点不再需要保留原始tokens）
        clearParentNodeValue(syntaxNode);
        return true;
    }
    
    /**
     * 清理父节点的value
     * 在构建完子节点后，原始的tokens已经被分解到各个子节点中，父节点不再需要保留原始tokens
     * 
     * @param syntaxNode 语法节点
     */
    private void clearParentNodeValue(SyntaxNode syntaxNode) {
        syntaxNode.setValue(null);
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //取出子节点
        if (!(syntaxNode instanceof IFSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望IFSyntaxNode，实际:{}", 
                syntaxNode.getLine(), syntaxNode.getClass().getName());
        }
        IFSyntaxNode ifSyntaxNode = (IFSyntaxNode)syntaxNode;
        List<SyntaxNode> childSyntaxNodeList = ifSyntaxNode.getChildSyntaxNodeList();
        if (childSyntaxNodeList == null || childSyntaxNodeList.isEmpty()) {
            // if语句没有子节点，直接返回
            return;
        }
        for (SyntaxNode node : childSyntaxNodeList) {
            if (!(node instanceof IFSyntaxNode)) {
                throw new NfException("Line:{} ,if子节点类型错误，期望IFSyntaxNode，实际:{}", 
                    node.getLine(), node.getClass().getName());
            }
            IFSyntaxNode nodeIf = (IFSyntaxNode) node;
            //判断是否是else,那么就没有条件直接执行
            if (nodeIf.ifType==IFType.ELSE){
                //创建子作用域
                NfContextScope childScope = context.createChildScope(context.getCurrentScopeId(), NfContextScopeType.IF);
                //执行else if代码块内部的语句
                List<SyntaxNode> elseChildList = nodeIf.getChildSyntaxNodeList();
                if (elseChildList != null) {
                    try {
                        SyntaxNodeFactory.executeAll(elseChildList, context);
                    } catch (NfReturnException e) {
                        // return语句需要穿透if块，传播到函数调用处
                        throw e;
                    }
                }
                //检查并传播breakAll标志到父作用域
                if (childScope.isBreakAll()) {
                    propagateBreakAllToParentFors(context, childScope.getParentScopeId());
                }
                //删除子作用域
                context.removeScope(childScope.getScopeId());
                break;
            }
            //条件
            List<Token> ifValue = node.getValue();
            if (ifValue == null || ifValue.isEmpty()) {
                continue;
            }
            StringBuilder ifBuilder = TokenUtil.mergeToken(ifValue);
            try {
                Object arithmetic = NfCalculator.arithmetic(ifBuilder.toString(), context);
                //判断是否是true
                if (arithmetic instanceof Boolean && (Boolean) arithmetic) {
                    //创建子作用域
                    NfContextScope childScope = context.createChildScope(context.getCurrentScopeId(), NfContextScopeType.IF);
                    //执行if代码块内部的语句
                    List<SyntaxNode> ifChildList = nodeIf.getChildSyntaxNodeList();
                    if (ifChildList != null) {
                        try {
                            SyntaxNodeFactory.executeAll(ifChildList, context);
                        } catch (NfReturnException e) {
                            // return语句需要穿透if块，传播到函数调用处
                            throw e;
                        }
                    }
                    //检查并传播breakAll标志到父作用域
                    if (childScope.isBreakAll()) {
                        propagateBreakAllToParentFors(context, childScope.getParentScopeId());
                    }
                    //删除子作用域
                    context.removeScope(childScope.getScopeId());
                    break;
                }
            } catch (NfReturnException e) {
                // return语句需要穿透if块，传播到函数调用处
                throw e;
            } catch (Exception e) {
                throw new NfException(e,"Line:{}  if表达式计算错误:{} ", ifSyntaxNode.getLine(), ifBuilder.toString());
            }
        }
    }

    //识别if表达式
    public IFSyntaxNode splitIf(List<Token> tokens,IFType ifType) {
        if (tokens == null || tokens.isEmpty()) {
            throw new NfException("if表达式tokens不能为空");
        }

        IFSyntaxNode ifStatement = new IFSyntaxNode(SyntaxNodeType.IF_EXP);
        ifStatement.setIfType(ifType);
        ifStatement.setLine(tokens.get(0).getLine());

        //记录结束下标, 用于截取和删除
        int endIndex = skipIf1Block(tokens);
        if (endIndex <= 0 || endIndex > tokens.size()) {
            throw new NfException("Line:{} ,if表达式格式错误，无法找到结束位置", tokens.get(0).getLine());
        }
        //截取if表达式的标记序列（手动创建副本，避免subList视图的clear影响原始数据）
        List<Token> ifTokens = new ArrayList<>();
        for (int k = 0; k < endIndex; k++) {
            ifTokens.add(tokens.get(k));
        }
        //删除（skipIf1Block 返回 endIndex + 1，已包含 }，无需再删除）
        tokens.subList(0, endIndex).clear();

        //找到第一个{+LINE_END的位置
        int endIndex2 = 0;
        int ifTokensSize = ifTokens.size();
        for (int j = 0; j < ifTokensSize - 1; j++) {
            if (ifTokens.get(j).type == TokenType.LBRACE && ifTokens.get(j + 1).type == TokenType.LINE_END) {
                endIndex2 = j;
                break;
            }
        }

        //验证条件不能为空（else语句除外，else没有条件）
        //必须在截取conditionTokens之前校验，避免endIndex2=0导致的空条件
        if ((ifType == IFType.IF || ifType == IFType.ELSE_IF) && endIndex2 == 0) {
            throw new NfSyntaxException(
                ifStatement.getLine(),
                "if表达式语法错误",
                "if语句的条件不能为空",
                "",
                "请检查if语句的语法格式：if condition { ... }"
            );
        }

        //截取if表达式的条件
        List<Token> conditionTokens = new ArrayList<>(ifTokens.subList(0, endIndex2));
        //删除条件
        ifTokens.subList(0, endIndex2).clear();
        //去掉{
        ifTokens.remove(0);
        //去掉第一个换行
        if(ifTokens.get(0).type == TokenType.LINE_END){
            ifTokens.remove(0);
        }
        //去掉末尾的}
        if(!ifTokens.isEmpty() && ifTokens.get(ifTokens.size() - 1).type == TokenType.RBRACE){
            ifTokens.remove(ifTokens.size() - 1);
        }
        //if表达式的条件
        ifStatement.setValue(conditionTokens);

        //继续构建代码体
        // if块创建新作用域
        ParseScopeTracker tracker = NfSynta.getCurrentTracker();
        if (tracker != null) {
            tracker.enterScope(ParseScopeTracker.ScopeType.BLOCK); // 进入if块作用域
        }
        try {
            List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(ifTokens, tracker);
            ifStatement.setChildSyntaxNodeList(syntaxNodes);
        } finally {
            if (tracker != null) {
                tracker.exitScope(); // 退出if块作用域
            }
        }
        return ifStatement;
    }


    //识别else if表达式
    public IFSyntaxNode splitElseIf(List<Token> tokens) {
        //判断长度
        if (tokens.size() < 2) {
            return null;
        }
        //识别是否是else if
        //拿第一个token 和第二个token
        Token token1 = tokens.get(0);
        Token token2 = tokens.get(1);
        if (token1.type==TokenType.ELSE&&token2.type== TokenType.IF){
            //删除
            tokens.remove(0);
            tokens.remove(0);
            return splitIf(tokens,IFType.ELSE_IF);
        }
        return null;
    }


    //识别else表达式
    public IFSyntaxNode splitElse(List<Token> tokens) {
        //判断长度
        if (tokens.isEmpty()) {
            return null;
        }
        //识别是否是else
        Token token1 = tokens.get(0);
        if (token1.type==TokenType.ELSE){
            IFSyntaxNode ifStatement = new IFSyntaxNode(SyntaxNodeType.IF_EXP);
            ifStatement.setIfType(IFType.ELSE);
            ifStatement.setLine(token1.getLine());

            //删除ELSE
            tokens.remove(0);

            // 检查剩余tokens是否以{开头
            if (tokens.isEmpty() || tokens.get(0).type != TokenType.LBRACE) {
                throw new NfSyntaxException(
                    token1.getLine(),
                    "else表达式语法错误",
                    "else后面缺少{",
                    "",
                    "请检查else语句的语法格式：else { ... }"
                );
            }

            //删除{
            tokens.remove(0);

            //删除第一个换行
            if(!tokens.isEmpty() && tokens.get(0).type == TokenType.LINE_END){
                tokens.remove(0);
            }

            //去掉末尾的}
            if(!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == TokenType.RBRACE){
                tokens.remove(tokens.size() - 1);
            }

            // 继续构建代码体前，检查tokens是否为空
            if (tokens.isEmpty()) {
                // else块为空，返回空节点
                return ifStatement;
            }

            //继续构建代码体（tokens已被修改，直接使用）
            // else块创建新作用域
            ParseScopeTracker tracker = NfSynta.getCurrentTracker();
            if (tracker != null) {
                tracker.enterScope(ParseScopeTracker.ScopeType.BLOCK); // 进入else块作用域
            }
            try {
                List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens, tracker);
                ifStatement.setChildSyntaxNodeList(syntaxNodes);
            } finally {
                if (tracker != null) {
                    tracker.exitScope(); // 退出else块作用域
                }
            }
            return ifStatement;
        }
        return null;
    }

    /**
     * 将breakAll标志传播到所有祖先FOR作用域
     * 这个方法处理breakall在IF语句中触发的场景，确保能够跳出所有层级的FOR循环
     *
     * @param context 上下文
     * @param scopeId 起始作用域ID（子作用域的父作用域）
     */
    private void propagateBreakAllToParentFors(NfContext context, String scopeId) {
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
