package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.*;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.DataType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * if表达式
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class SwitchSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    //if表达式类型
    private SwitchType switchType;
    public enum SwitchType {
        CASE,
        DEFAULT,

    }
    public SwitchSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.BLOCK_NODE);
    }
    @Override
    public  boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.SWITCH) {
                //记录结束下标, 用于截取和删除
                int endIndex = skipSwitchEnd(tokens, i);
                //截取switch表达式的标记序列
                List<Token> switchTokens = new ArrayList(tokens.subList(i, endIndex));

                //删除
                tokens.subList(i, endIndex).clear();
                SwitchSyntaxNode switchStatement = new SwitchSyntaxNode(SyntaxNodeType.SWITCH_EXP);
                switchStatement.setValue(switchTokens);
                switchStatement.setLine(token.getLine());
                //构建子节点
                if (!buildChildStatement(switchStatement)) {
                    return false;
                }
                List<SyntaxNode> childSyntaxNodeList1 = switchStatement.getChildSyntaxNodeList();
                if (childSyntaxNodeList1 ==null) {
                    throw new NfException("问题行号:{} , switch 语法问题:{}", switchStatement.getLine(), printSwitch(switchTokens));
                }
                syntaxNodeList.add(switchStatement);
                return true;
            }
        }
        return false;
    }
    //分解case
    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        //取出来switch的标记序列
        List<Token> tokens = syntaxNode.getValue();
        //取出来switch的条件值
        List<Token> switchValue = splitSwitchValue(tokens);
        //判断是否是标识符（变量）或常量（整数、字符串、布尔值、浮点数）
        TokenType switchValueType = switchValue.get(0).type;
        if (switchValueType != TokenType.IDENTIFIER && 
            switchValueType != TokenType.INTEGER && 
            switchValueType != TokenType.STRING && 
            switchValueType != TokenType.BOOLEAN && 
            switchValueType != TokenType.FLOAT) {
            throw new NfException("问题行号:{} , switch的条件值必须是变量或常量（整数、字符串、布尔值、浮点数）", switchValue.get(0).getLine());
        }
        syntaxNode.setValue(switchValue);
        //取出来全部的case
        while (true) {
            //去掉开头换行
            NfToken.skipLineEnd(tokens);
            //取出来case
            SwitchSyntaxNode caseSyntaxNode = splitCase(tokens);
            if (caseSyntaxNode == null) {
                break;
            }
            syntaxNode.addChild(caseSyntaxNode);
        }
        //去掉开头换行
        NfToken.skipLineEnd(tokens);
        //取出来default
        SwitchSyntaxNode defaultSyntaxNode = splitDefault(tokens);
        if (defaultSyntaxNode != null) {
            syntaxNode.addChild(defaultSyntaxNode);
        }
        return true;
    }


    @Override
    public boolean analystToken(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token.type == TokenType.SWITCH) {
            return true;
        }
        return false;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return  syntaxNode instanceof SwitchSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        SwitchSyntaxNode switchSyntaxNode = (SwitchSyntaxNode) syntaxNode;
        Token switchValueToken = switchSyntaxNode.getValue().get(0);
        Object switchValue;
        
        //判断switch条件值是变量还是常量
        if (switchValueToken.type == TokenType.IDENTIFIER) {
            //是变量，从上下文中获取值
            String switchName = switchValueToken.value;
            NfVariableInfo variable = context.getVariable(switchName);
            if (variable == null) {
                throw new NfException("{}变量不存在 , syntax:{} ", switchName, syntaxNode);
            }
            switchValue = variable.getValue();
        } else {
            //是常量，直接转换为实际值
            switchValue = DataType.realType(switchValueToken.type, switchValueToken.value);
        }
        back:
        for (SyntaxNode node : switchSyntaxNode.getChildSyntaxNodeList()) {
            SwitchSyntaxNode caseNode= (SwitchSyntaxNode) node;
            if (caseNode.getSwitchType()==SwitchType.DEFAULT){
                //创建子作用域
                NfContextScope childScope = context.createChildScope(context.getCurrentScopeId(), NfContextScopeType.SWITCH);
                //执行else if代码块内部的语句
                SyntaxNodeFactory.executeAll(caseNode.getChildSyntaxNodeList(), context);
                //删除子作用域
                context.removeScope(childScope.getScopeId());
                //执行完毕后跳出switch
                break;
            }else{
                List<Token> caseValue = node.getValue();
                for (Token token : caseValue) {
                    //跳过逗号
                    if (token.type == TokenType.COMMA) {
                        continue;
                    }
                    Object caseValueIf = DataType.realType(token.type, token.value);
                    //判断类型是否一致
                    if(switchValue.getClass()==caseValueIf.getClass() && switchValue.equals(caseValueIf)){
                        //创建子作用域
                        NfContextScope childScope = context.createChildScope(context.getCurrentScopeId(), NfContextScopeType.SWITCH);
                        //执行else if代码块内部的语句
                        SyntaxNodeFactory.executeAll(caseNode.getChildSyntaxNodeList(), context);
                        //删除子作用域
                        context.removeScope(childScope.getScopeId());
                        //执行完毕后跳出switch
                        break back;
                    }
                }
            }
        }
    }

    //取出Switch条件值
    private List<Token> splitSwitchValue(List<Token> tokens){
        //去掉开头的switch
        tokens.remove(0);
        List<Token> value = new ArrayList();
        value.add(tokens.get(0));
        //去掉值
        tokens.remove(0);
        //去掉{
        tokens.remove(0);
        //去掉换行
        tokens.remove(0);
        //去掉最后的}（RBRACE），这是switch语句的结束标记
        //注意：这个RBRACE是在skipSwitchEnd中已经计算好的switch语句的结束位置
        if (tokens.size() > 0 && tokens.get(tokens.size() - 1).type == TokenType.RBRACE) {
            tokens.remove(tokens.size() - 1);
        }
        return value;
    }
    /**
     * 提取case语句
     * 如果列表为空或开头不是case，返回null
     * 
     * @param tokens Token列表
     * @return case语句节点，如果不存在则返回null
     */
    private  SwitchSyntaxNode  splitCase( List<Token> tokens){
        // 如果列表为空，直接返回null
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        //判断开头是否是case
        if (tokens.get(0).type != TokenType.CASE) {
            return null;
        }
        SwitchSyntaxNode caseStatement = new SwitchSyntaxNode(SyntaxNodeType.SWITCH_EXP);
        caseStatement.setSwitchType(SwitchType.CASE);
        caseStatement.setLine(tokens.get(0).getLine());

        //记录结束下标, 用于截取和删除
        //去掉开头的case
        tokens.remove(0);
        int endIndex = skipCase1Block(tokens);
        //截取case表达式 , 因为需要包括换行所以需要+1
        List<Token> caseTokens = new ArrayList(tokens.subList(0, endIndex+1));
        //删除
        tokens.subList(0, endIndex+1).clear();

        //提取case的条件
        List<Token> caseValue = new ArrayList();
        int endIndex2=0;
        //截取到换行
        //优化：缓存size，避免在循环中重复调用
        int caseTokensSize = caseTokens.size();
        for (int i = 0; i < caseTokensSize; i++) {
            if (caseTokens.get(i).type == TokenType.LINE_END) {
                endIndex2 = i+1;
                break;
            }
            caseValue.add(caseTokens.get(i));
        }
        caseStatement.setValue(caseValue);
        //截取case体
        List<Token> caseBody = new ArrayList(caseTokens.subList(endIndex2, caseTokens.size()));
        //构建子节点
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(caseBody);
        caseStatement.setChildSyntaxNodeList(syntaxNodes);
        return caseStatement;
    }
    /**
     * 提取default语句
     * 如果列表为空或开头不是default，返回null
     * 
     * @param tokens Token列表
     * @return default语句节点，如果不存在则返回null
     */
    private  SwitchSyntaxNode  splitDefault( List<Token> tokens){
        // 如果列表为空，直接返回null
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        //判断开头是否是default
        if (tokens.get(0).type != TokenType.DEFAULT) {
            return null;
        }
        SwitchSyntaxNode defaultStatement = new SwitchSyntaxNode(SyntaxNodeType.SWITCH_EXP);
        defaultStatement.setSwitchType(SwitchType.DEFAULT);
        defaultStatement.setLine(tokens.get(0).getLine());

        //记录结束下标, 用于截取和删除
        //去掉开头的default
        tokens.remove(0);
        //去掉换行
        tokens.remove(0);
        //剩下的就是default的表达式
        int endIndex = tokens.size();
        //截取default表达式 , 因为需要包括换行所以需要+1
        List<Token> defaultTokens = new ArrayList(tokens.subList(0, endIndex));
        //删除
        tokens.subList(0, endIndex).clear();

        //构建子节点
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(defaultTokens);
        defaultStatement.setChildSyntaxNodeList(syntaxNodes);
        return defaultStatement;
    }







    //跳到if结束位置获取结束下标
    private int skipSwitchEnd(List<Token> tokens, int i) {
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


    //只跳到下一个块Case位置
    private int skipCase1Block(List<Token> tokens) {
        //记录结束下标, 用于截取和删除
        int endIndex = -1;
        //记录深度  每次遇到 LBRACE + LINE_END 深度+1, 遇到 RBRACE 深度-1
        int depth = 0;
        //遇到RBRACE + LINE_END结束
        for (int j = 0; j < tokens.size(); j++) {
            if (j < tokens.size() - 1 && tokens.get(j).type == TokenType.LBRACE && tokens.get(j + 1).type == TokenType.LINE_END) {
                depth++;
            }
            //}
            if (tokens.get(j).type == TokenType.RBRACE ) {
                depth--;
            }
            //当深度为0时, 并且遇到case或default位置了 那么回退到上一个位置就是case的结束位置
            if (depth == 0 && (tokens.get(j).type == TokenType.CASE || tokens.get(j).type == TokenType.DEFAULT)) {
                endIndex = j - 1;
                break;
            }
            //如果深度小于0，说明遇到了switch的结束}，也应该结束
            if (depth < 0 && tokens.get(j).type == TokenType.RBRACE) {
                endIndex = j - 1;
                break;
            }
        }
        //如果没有找到下一个case/default，说明这是最后一个case，返回tokens的末尾
        if (endIndex < 0) {
            endIndex = tokens.size() - 1;
        }
        //确保endIndex至少为0
        if (endIndex < 0) {
            endIndex = 0;
        }
        return endIndex;
    }


    //打印switch 的token
    private String printSwitch(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        sb.append("\n");
        for (Token token : tokens) {
            //遇到LINE_END换行
            if (token.type == TokenType.LINE_END) {
                sb.append("\n");
                continue;
            }
            sb.append(token.value).append(" ");
        }
        return sb.toString();
    }
}
