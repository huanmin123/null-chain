package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
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
public  class ImportSyntaxNode extends LineSyntaxNode {
    public ImportSyntaxNode() {
        super(SyntaxNodeType.IMPORT_EXP);
    }
    
    public ImportSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.IMPORT;
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.IMPORT) {
                //记录结束下标, 用于截取和删除
                int endIndex = findLineEndIndex(tokens, i);
                //截取import语句的标记序列 不包含import和LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                removeComments(newToken);

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
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> value = syntaxNode.getValue();
        if (value == null || value.isEmpty()) {
            throw new NfException("Line:{} ,import表达式tokens不能为空 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        for (Token token : value) {
            sb.append(token.value);
        }
        //最后一个token就是类型
        String type = value.get(value.size() - 1).value;
        context.addImport(type,sb.toString());
    }


}