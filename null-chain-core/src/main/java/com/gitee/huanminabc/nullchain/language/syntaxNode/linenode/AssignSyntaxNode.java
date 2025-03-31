package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
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
import com.gitee.huanminabc.nullchain.language.utils.KeywordUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * 赋值表达式 例如: int a=1
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AssignSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {


    public AssignSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }

    @Override
    public boolean analystToken(List<Token> tokens) {
        return tokens.get(0).type == TokenType.IDENTIFIER &&
                tokens.get(1).type == TokenType.IDENTIFIER &&
                tokens.get(2).type == TokenType.ASSIGN;
    }


    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.ASSIGN) {
                //下标像前移动2位
                i -= 2;
                //记录结束下标, 用于截取和删除
                int endIndex = 0;
                //遇到LINE_END结束
                for (int j = i; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.LINE_END) {
                        endIndex = j;
                        break;
                    }
                }
                //截取赋值语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //拿到第1个变量名称
                Token varName = newToken.get(1);
                boolean forbidKeyword = KeywordUtil.isForbidKeyword(varName.value);
                if (forbidKeyword) {
                    throw new NfException("Line:{} ,变量名 {} 不能是禁用的关键字: {}",varName.line,varName.value,printExp(newToken));
                }

                //去掉注释
                newToken.removeIf(t -> t.type == TokenType.COMMENT);
                AssignSyntaxNode assignSyntaxNode = new AssignSyntaxNode(SyntaxNodeType.ASSIGN_EXP);
                assignSyntaxNode.setValue(newToken);
                //设置行号
                assignSyntaxNode.setLine(token.getLine());
                syntaxNodeList.add(assignSyntaxNode);

                return true;
            }

        }
        return false;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        // IDENTIFIER IDENTIFIER ASSIGN xxx
        //获取类型
        Token token = syntaxNode.getValue().get(0);
        String type = token.value;
        //转化为java类型
        String importType = context.getImportType(type);
        if (importType == null) {
            throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",token.line, type, syntaxNode);
        }

        //获取赋值的变量名
        String varName = syntaxNode.getValue().get(1).value;
        //获取赋值的表达式
        List<Token> expTokens = syntaxNode.getValue().subList(3, syntaxNode.getValue().size());
        //如果是空的那么就报错
        if (expTokens.isEmpty()) {
            throw new NfException("Line:{} ,赋值表达式为空 , syntax: {}", token.line,syntaxNode);
        }
        //取出来上下文
        NfContextScope currentScope = context.getCurrentScope();
        //获取第一个token类型如果是New那么就是创建对象
        if (expTokens.get(0).type == TokenType.NEW) {
            try {
                //创建对象
                Class<?> aClass = Class.forName(importType);
                // 获取 HashMap 的无参构造函数
                Constructor<?> constructor = aClass.getConstructor();
                Object o = constructor.newInstance();
                //将对象放入上下文
                currentScope.addVariable(new NfVariableInfo(varName, o, aClass));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new NfException(e, "Line:{} ,创建{}对象失败 , syntax: {}", token.line , importType, syntaxNode);
            }
            return;
        }
        //计算表达式
        StringBuilder exp = TokenUtil.mergeToken(expTokens);
        Object arithmetic = null;
        try {
            arithmetic = NfCalculator.arithmetic(exp.toString(), context);
        } catch (Exception e) {
            throw new NfException(e, "Line:{} , syntax: {}", token.line , syntaxNode);
        }
        //判断类型值和类型是否一致
        if (!importType.equals(arithmetic.getClass().getName())) {
            throw new NfException("Line:{} ,变量 {} 值类型和声明的型不匹配 {} vs {} ,syntax: {}", token.line ,varName, importType, arithmetic.getClass(), syntaxNode);
        }
        //将计算的值放入上下文
        currentScope.addVariable(new NfVariableInfo(varName, arithmetic, arithmetic.getClass()));
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        //判断语法是否是赋值表达式
        return syntaxNode instanceof AssignSyntaxNode;
    }

    //打印表达式
    private String printExp(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        //前3个是类型,变量名,赋值符号 需要空格
        for (int i = 0; i < 3; i++) {
            sb.append(tokens.get(i).value).append(" ");
        }
        //后面的是表达式
        for (int i = 3; i < tokens.size(); i++) {
            sb.append(tokens.get(i).value);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return printExp(getValue());
    }


}
