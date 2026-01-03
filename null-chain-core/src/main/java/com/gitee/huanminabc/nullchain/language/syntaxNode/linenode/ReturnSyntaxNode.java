package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfReturnException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * return语句语法节点
 * 
 * <p>支持return语句语法：return 表达式1,表达式2,...（支持多返回值）</p>
 * <p>示例：return a + b</p>
 * <p>示例：return name, age</p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReturnSyntaxNode extends LineSyntaxNode {

    public ReturnSyntaxNode() {
        super(SyntaxNodeType.RETURN_EXP);
    }

    public ReturnSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.RETURN;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        if (tokensSize < 1) {
            return false;
        }

        // 检查第一个token是否是RETURN
        if (tokens.get(0).type != TokenType.RETURN) {
            return false;
        }

        // 确定结束位置
        int startIndex = 0;
        int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, startIndex);

        // 截取return语句的标记序列,不包含LINE_END
        List<Token> newToken = new ArrayList<>(tokens.subList(startIndex, endIndex));
        // 删除已经解析的标记
        tokens.subList(startIndex, endIndex).clear();

        // 去掉注释
        SyntaxNodeUtil.removeComments(newToken);
        ReturnSyntaxNode returnSyntaxNode = new ReturnSyntaxNode(SyntaxNodeType.RETURN_EXP);
        returnSyntaxNode.setValue(newToken);
        // 设置行号
        returnSyntaxNode.setLine(newToken.get(0).getLine());
        syntaxNodeList.add(returnSyntaxNode);

        return true;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> valueTokens = syntaxNode.getValue();
        if (valueTokens == null || valueTokens.isEmpty()) {
            throw new NfException("Line:{} ,return语句tokens不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 去掉RETURN关键字
        if (valueTokens.get(0).type != TokenType.RETURN) {
            throw new NfException("Line:{} ,return语句语法错误，缺少return关键字 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }
        List<Token> expTokens = valueTokens.subList(1, valueTokens.size());

        if (expTokens.isEmpty()) {
            // 没有返回值，返回null
            // 找到函数作用域（ALL类型）而不是当前作用域（可能是IF作用域）
            NfContextScope functionScope = context.findByTypeScope(NfContextScopeType.ALL);
            if (functionScope == null) {
                functionScope = context.getCurrentScope();
            }
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", null, Object.class));
            return;
        }

        // 解析返回值（支持多返回值，用逗号分割）
        List<Object> returnValues = new ArrayList<>();
        List<Token> currentExp = new ArrayList<>();

        for (Token token : expTokens) {
            if (token.type == TokenType.COMMA) {
                if (!currentExp.isEmpty()) {
                    // 计算当前表达式
                    StringBuilder exp = TokenUtil.mergeToken(currentExp);
                    Object value = NfCalculator.arithmetic(exp.toString(), context);
                    returnValues.add(value);
                    currentExp.clear();
                }
            } else {
                currentExp.add(token);
            }
        }

        // 处理最后一个表达式
        if (!currentExp.isEmpty()) {
            StringBuilder exp = TokenUtil.mergeToken(currentExp);
            Object value = NfCalculator.arithmetic(exp.toString(), context);
            returnValues.add(value);
        }

        // 将返回值存储到函数作用域的特定变量中
        // 必须存储到函数作用域（ALL类型）而不是当前作用域（可能是IF作用域）
        NfContextScope functionScope = context.findByTypeScope(NfContextScopeType.ALL);
        if (functionScope == null) {
            functionScope = context.getCurrentScope();
        }
        if (returnValues.size() == 1) {
            // 单返回值
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", returnValues.get(0), returnValues.get(0).getClass()));
        } else {
            // 多返回值，使用List存储
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", returnValues, java.util.List.class));
        }

        // 抛出返回异常，提前终止函数体的执行
        throw new NfReturnException(syntaxNode.getLine());
    }

    @Override
    public String toString() {
        return TokenUtil.mergeToken(getValue()).toString();
    }
}



