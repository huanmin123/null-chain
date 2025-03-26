package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeStructType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@NoArgsConstructor
public class FunExeSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public FunExeSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }


    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            //判断是否是函数执行 IDENTIFIER+DOT+IDENTIFIER+LPAREN
            if (token.type == TokenType.IDENTIFIER&&tokens.get(i+1).type==TokenType.DOT&&tokens.get(i+2).type==TokenType.IDENTIFIER&&tokens.get(i+3).type==TokenType.LPAREN) {
                //记录结束下标, 用于截取和删除
                int endIndex = 0;
                //遇到LINE_END结束
                for (int j = i; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.LINE_END) {
                        endIndex = j;
                        break;
                    }
                }
                //截取函数执行语句的标记序列,不包含LINE_END
                List<Token> newToken = Lists.newArrayList(tokens.subList(i , endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                newToken.removeIf(t -> t.type == TokenType.COMMENT);
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
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return syntaxNode instanceof FunExeSyntaxNode;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> value = syntaxNode.getValue();

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
