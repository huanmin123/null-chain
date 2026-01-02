package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.KeywordUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 声明表达式 例如: int a
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeclareSyntaxNode extends LineSyntaxNode {
    public DeclareSyntaxNode() {
        super(SyntaxNodeType.DECLARE_EXP);
    }
    
    public DeclareSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        // DeclareSyntaxNode重写了analystToken方法，此方法不会被调用
        // 但为了满足抽象方法要求，返回null
        return null;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            //IDENTIFIER +IDENTIFIER +LINE_END
            if (token.type == TokenType.IDENTIFIER && tokens.get(i + 1).type == TokenType.IDENTIFIER && tokens.get(i + 2).type == TokenType.LINE_END) {
                //截取声明语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i, i + 2));
                //删除已经解析的标记
                tokens.subList(i, i + 2).clear();
                //拿到第1个变量名称
                Token varName = newToken.get(1);
                boolean forbidKeyword = KeywordUtil.isForbidKeyword(varName.value);
                if (forbidKeyword) {
                    throw new NfException("Line:{} ,变量名 {} 不能是禁用的关键字, syntax: {}",varName.line,varName.value,printExp(newToken));
                }

                //去掉注释
                removeComments(newToken);
                DeclareSyntaxNode declareSyntaxNode = new DeclareSyntaxNode(SyntaxNodeType.DECLARE_EXP);
                declareSyntaxNode.setValue(newToken);
                //设置行号
                declareSyntaxNode.setLine(token.getLine());
                syntaxNodeList.add(declareSyntaxNode);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        if ( tokens.get(0).type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.IDENTIFIER && tokens.get(2).type == TokenType.LINE_END) {
            return true;
        }
        return false;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //获取类型
        Token token = syntaxNode.getValue().get(0);
        String type = token.value;
        //转化为java类型
        String importType = context.getImportType(type);
        if (importType == null) {
            throw new NfException("Line:{} ,未找到类型: {} , syntax: {}",token.line,type,syntaxNode);
        }
        try {
            Class<?> typeClass = Class.forName(importType);

            //获取赋值的变量名
            String varName = syntaxNode.getValue().get(1).value;

            //取出来上下文
            NfContextScope currentScope = context.getCurrentScope();
            //将计算的值放入上下文
            currentScope.addVariable(new NfVariableInfo(varName, null, typeClass));
        } catch (ClassNotFoundException e) {
            throw new NfException("Line:{} ,未找到类型: {} , syntax: {}",token.line,type,syntaxNode);
        }
    }


    //打印表达式
    private String printExp(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        //前2个是类型,变量名
        for (int i = 0; i < 2; i++) {
            sb.append(tokens.get(i).value).append(" ");
        }
        //后面的是表达式
        for (int i = 2; i < tokens.size(); i++) {
            sb.append(tokens.get(i).value);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return printExp(getValue());
    }

}
