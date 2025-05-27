package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeStructType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * export语句 列如: export e
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ExportSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public static  final String EXPORT = "$$nextTaskValue$$";

    public ExportSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.EXPORT) {
                //记录结束下标, 用于截取和删除
                int endIndex = 0;
                //遇到LINE_END结束
                for (int j = i; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.LINE_END) {
                        endIndex = j;
                        break;
                    }
                }
                //截取Export语句的标记序列 不包含Export和LINE_END
                List<Token> newToken = new ArrayList(tokens.subList(i + 1, endIndex));
                //去掉注释
                newToken.removeIf(t -> t.type == TokenType.COMMENT);

                //如果是空的export, 抛出异常
                if (newToken.isEmpty()) {
                    throw new NfException("Line:{} , export 语句不能为空,请给出需要导出的变量名",tokens.get(0).line);
                }

                ExportSyntaxNode exportExpNode = new ExportSyntaxNode(SyntaxNodeType.EXPORT_EXP);
                exportExpNode.setValue(newToken);
                //设置行号
                exportExpNode.setLine(token.getLine());
                syntaxNodeList.add(exportExpNode);
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return  syntaxNode instanceof ExportSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        // 表达式中就一个IDENTIFIER, 取出来
        List<Token> tokens = syntaxNode.getValue();
        Token token = tokens.get(0);
        NfContextScope mainScope = context.getMainScope();
        NfVariableInfo variable = mainScope.getVariable(token.value);
        if (variable == null) {
            throw new NfException("Line:{} ,export 变量 {} 未定义, syntax: {}",token.line,token.value,syntaxNode);
        }
        mainScope.addVariable(new NfVariableInfo(EXPORT, variable.getValue(), variable.getType()));
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token.type == TokenType.EXPORT) {
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return "export "+getValue().get(0).value;
    }

}
