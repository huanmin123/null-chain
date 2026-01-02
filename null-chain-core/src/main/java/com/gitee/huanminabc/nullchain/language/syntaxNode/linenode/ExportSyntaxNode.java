package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.DataType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/*
  export语句
  支持两种形式：
  1. export 变量名 - 导出变量（如：export result）
  2. export 表达式 - 导出表达式计算结果（如：export $preValue + "_nf1"）
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExportSyntaxNode extends LineSyntaxNode {
    public static  final String EXPORT = "$$nextTaskValue$$";

    public ExportSyntaxNode() {
        super(SyntaxNodeType.EXPORT_EXP);
    }
    
    public ExportSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.EXPORT;
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.EXPORT) {
                //记录结束下标, 用于截取和删除
                int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, i);
                //截取Export语句的标记序列 不包含Export和LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                //去掉注释
                SyntaxNodeUtil.removeComments(newToken);

                //如果是空的export, 抛出异常
                if (newToken.isEmpty()) {
                    throw new NfException("Line:{} , export 语句不能为空,请给出需要导出的变量名或表达式",tokens.get(0).line);
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
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> tokens = syntaxNode.getValue();
        if (tokens == null || tokens.isEmpty()) {
            throw new NfException("Line:{} ,export表达式tokens不能为空 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }
        NfContextScope mainScope = context.getMainScope();
        Object exportValue;
        Class<?> exportType;
        
        // 检查是否是模板字符串（单个 TEMPLATE_STRING token）
        if (tokens.size() == 1 && tokens.get(0).type == TokenType.TEMPLATE_STRING) {
            Token token = tokens.get(0);
            // 去除首尾的 ```，并处理占位符
            String templateValue = (String) DataType.realType(TokenType.TEMPLATE_STRING, token.value);
            exportValue = EchoSyntaxNode.replaceTemplate(templateValue, context);
            exportType = String.class;
        }
        // 如果只有一个 IDENTIFIER token，优先作为变量名处理（向后兼容）
        else if (tokens.size() == 1 && tokens.get(0).type == TokenType.IDENTIFIER) {
            Token token = tokens.get(0);
            NfVariableInfo variable = mainScope.getVariable(token.value);
            if (variable != null) {
                // 变量存在，直接使用变量值
                exportValue = variable.getValue();
                exportType = variable.getType();
            } else {
                // 变量不存在，尝试作为表达式计算
                String expression = TokenUtil.mergeToken(tokens).toString();
                try {
                    exportValue = NfCalculator.arithmetic(expression, context);
                    exportType = exportValue != null ? exportValue.getClass() : null;
                    // 如果计算结果是字符串且包含占位符，进行替换
                    if (exportValue instanceof String && ((String) exportValue).contains("{") && ((String) exportValue).contains("}")) {
                        exportValue = EchoSyntaxNode.replaceTemplate((String) exportValue, context);
                    }
                } catch (Exception e) {
                    throw new NfException(e, "Line:{} ,export 变量 {} 未定义,且表达式计算失败, syntax: {}", token.line, token.value, syntaxNode);
                }
            }
        } else {
            // 多个 tokens，作为表达式计算
            String expression = TokenUtil.mergeToken(tokens).toString();
            try {
                exportValue = NfCalculator.arithmetic(expression, context);
                exportType = exportValue != null ? exportValue.getClass() : null;
                // 如果计算结果是字符串且包含占位符，进行替换
                if (exportValue instanceof String && ((String) exportValue).contains("{") && ((String) exportValue).contains("}")) {
                    exportValue = EchoSyntaxNode.replaceTemplate((String) exportValue, context);
                }
            } catch (Exception e) {
                throw new NfException(e, "Line:{} ,export 表达式计算错误: {} , syntax: {}", 
                        tokens.get(0).line, expression, syntaxNode);
            }
        }
        
        // 将计算结果存储到 EXPORT 变量中
        mainScope.addVariable(new NfVariableInfo(EXPORT, exportValue, exportType));
    }



    @Override
    public String toString() {
        if (getValue() == null || getValue().isEmpty()) {
            return "export";
        }
        return "export " + TokenUtil.mergeToken(getValue());
    }

}
