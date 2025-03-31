package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeStructType;
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
 * import语句 例如: import com.gitee.huanminabc.nullchain.language.syntaxNode.node.TaskNode
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public  class ImportSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public ImportSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {

        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.IMPORT) {
                //记录结束下标, 用于截取和删除
                int endIndex = 0;
                //遇到LINE_END结束
                for (int j = i; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.LINE_END) {
                        endIndex = j;
                        break;
                    }
                }
                //截取import语句的标记序列 不包含import和LINE_END
                List<Token> newToken = new ArrayList(tokens.subList(i + 1, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                newToken.removeIf(t -> t.type == TokenType.COMMENT);

                //校验import语句是否合法
                String imp = TokenUtil.mergeToken(newToken).toString();

                try {
                    Class.forName(imp);
                } catch (ClassNotFoundException e) {
                    throw new NfException("Line:{} ,import {} 语句错误,找不到类" ,token.getLine(), imp );
                }

                ImportSyntaxNode importStatement = new ImportSyntaxNode(SyntaxNodeType.IMPORT_EXP);
                importStatement.setValue(newToken);
                //设置行号
                importStatement.setLine(token.getLine());
                syntaxNodeList.add(importStatement);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return  syntaxNode instanceof ImportSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> value = syntaxNode.getValue();
        StringBuilder sb = new StringBuilder();
        for (Token token : value) {
            sb.append(token.value);
        }
        //最后一个token就是类型
        String type = value.get(value.size() - 1).value;
        context.addImport(type,sb.toString());
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token.type == TokenType.IMPORT) {
            return true;
        }
        return false;
    }

}