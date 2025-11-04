package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;

import com.gitee.huanminabc.nullchain.language.syntaxNode.*;
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
@NoArgsConstructor
public class ForSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public ForSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.BLOCK_NODE);
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
                //截取While表达式的标记序列
                List<Token> ifTokens = new ArrayList<>(tokens.subList(i, endIndex));
                //删除
                tokens.subList(i, endIndex).clear();

                ForSyntaxNode ifStatement = new ForSyntaxNode(SyntaxNodeType.FOR_EXP);
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
        List<Token> forTokens = new ArrayList(tokenList.subList(0, endIndex));

        //必须存在In
        if (forTokens.stream().noneMatch(t -> t.type == TokenType.IN)) {
            throw new NfException("Line:{} ,for表达式必须包含in, syntax: {}",forTokens.get(0).line,printFor(forTokens));
        }

        //判断必须存在DOT2
        if (forTokens.stream().noneMatch(t -> t.type == TokenType.DOT2)) {
           throw new NfException("Line:{} ,for表达式必须包含.., syntax: {}",forTokens.get(0).line,printFor(forTokens));
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
            throw new NfException("Line:{} ,for表达式的..前后必须是整数, syntax: {}",forTokens.get(0).line,printFor(forTokens));
        }
        Token endToken = forTokens.get(dot2Index + 1);
        //判断是否是整数
        if (startToken.type != TokenType.INTEGER || endToken.type != TokenType.INTEGER) {
            throw new NfException("Line:{} ,for表达式的..前后必须是整数, syntax: {}",forTokens.get(0).line,printFor(forTokens));
        }
        //校验第一个整数必须小于或者等于第二个
        if (Integer.parseInt(startToken.value) > Integer.parseInt(endToken.value)) {
            throw new NfException("Line:{} ,for表达式的..前面的整数必须小于等于后面的整数, syntax: {}",forTokens.get(0).line,printFor(forTokens));
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
        ((ForSyntaxNode)syntaxNode).setChildSyntaxNodeList(syntaxNodes);
        return true;
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token.type == TokenType.FOR) {
            return true;
        }
        return false;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return   syntaxNode instanceof ForSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        ForSyntaxNode forSyntaxNode = (ForSyntaxNode) syntaxNode;
        List<Token> ifValue = forSyntaxNode.getValue();
        //i in 1..10
        //取出i
        String i = ifValue.get(0).value;
        //取出start
        String start = ifValue.get(2).value;
        //取出end
        String end = ifValue.get(4).value;
        //保留当前作用域id
        String currentScopeId = context.getCurrentScopeId();
        //优化：提前解析start和end，避免在循环中重复调用parseInt
        int startInt = Integer.parseInt(start);
        int endInt = Integer.parseInt(end);
        //循环
        for (int j = startInt; j <= endInt; j++) {
            //创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            //将i的值赋值
            newScope.addVariable(new NfVariableInfo(i,j, Integer.class));
            //执行子节点
            SyntaxNodeFactory.executeAll(forSyntaxNode.getChildSyntaxNodeList(), context);
            //移除子作用域
            context.removeScope(newScope.getScopeId());
            //如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            //如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                context.getScope(newScope.getParentScopeId()).setBreakAll(true);
                break;
            }
        }
    }

    //跳到For结束位置获取结束下标
    private int skipForEnd(List<Token> tokens, int i) {
        //记录结束下标, 用于截取和删除
        int endIndex = 0;
        //记录深度  每次遇到 LBRACE + LINE_END 深度+1, 遇到 RBRACE 深度-1
        int depth = 0;
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        //遇到RBRACE + LINE_END结束
        for (int j = i; j < tokensSize - 1; j++) {
            if (tokens.get(j).type == TokenType.LBRACE && tokens.get(j + 1).type == TokenType.LINE_END) {
                depth++;
            }
            //}
            if (tokens.get(j).type == TokenType.RBRACE) {
                depth--;
            }
            //当结尾是RBRACE + LINE_END 且深度为0时, 说明表达式结束
            if (depth == 0 && tokens.get(j).type == TokenType.RBRACE) {
                endIndex = j + 1;
                break;
            }
        }
        return endIndex;
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

}