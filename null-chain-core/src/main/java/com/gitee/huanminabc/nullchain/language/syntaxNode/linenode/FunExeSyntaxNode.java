package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行函数表达式: a.b()
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FunExeSyntaxNode extends LineSyntaxNode {
    public FunExeSyntaxNode() {
        super(SyntaxNodeType.FUN_EXE_EXP);
    }
    
    public FunExeSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        // FunExeSyntaxNode重写了analystToken方法，此方法不会被调用
        // 但为了满足抽象方法要求，返回null
        return null;
    }


    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);

            //判断是否是函数执行 IDENTIFIER+DOT+IDENTIFIER+LPAREN
            if (i + 3 < tokensSize && token.type == TokenType.IDENTIFIER&&tokens.get(i+1).type==TokenType.DOT&&tokens.get(i+2).type==TokenType.IDENTIFIER&&tokens.get(i+3).type==TokenType.LPAREN) {
                //记录结束下标, 用于截取和删除
                int endIndex = findLineEndIndex(tokens, i);
                //截取函数执行语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i , endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                removeComments(newToken);
                FunExeSyntaxNode runSyntaxNode = new FunExeSyntaxNode(SyntaxNodeType.FUN_EXE_EXP);
                runSyntaxNode.setValue(newToken);
                //设置行号
                runSyntaxNode.setLine(token.getLine());
                syntaxNodeList.add(runSyntaxNode);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        //IDENTIFIER+DOT+IDENTIFIER+LPAREN
        if (tokens.get(0).type == TokenType.IDENTIFIER&&tokens.get(1).type==TokenType.DOT&&tokens.get(2).type==TokenType.IDENTIFIER&&tokens.get(3).type==TokenType.LPAREN) {
            return true;
        }
        return false;
    }



    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> value = syntaxNode.getValue();
        if (value == null || value.isEmpty()) {
            throw new NfException("Line:{} ,函数执行表达式tokens不能为空 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }

        try {
            StringBuilder stringBuilder = TokenUtil.mergeToken(value);

            NfCalculator.arithmetic(stringBuilder.toString(), context);
        } catch (Exception e) {
            throw new NfException("Line:{} ,运行函数错误: {}",syntaxNode.getLine(),syntaxNode);
        }
    }

    @Override
    public String toString() {
        return  TokenUtil.mergeToken(getValue()).toString();
    }
}
