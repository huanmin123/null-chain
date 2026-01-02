package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
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
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * echo语句 例如: echo "测试打印: ","____","123:{c}",\t,c,\t,123
 * 每次打印一行, 多个参数用逗号隔开 字符串支持模版占位符, 支持\n 和 \t
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EchoSyntaxNode extends LineSyntaxNode {
    public EchoSyntaxNode() {
        super(SyntaxNodeType.ECHO_EXP);
    }
    
    public EchoSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.ECHO;
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.ECHO) {
                //记录结束下标, 用于截取和删除
                int endIndex = findLineEndIndex(tokens, i);
                //截取ECHO语句的标记序列 不包含ECHO
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                //去掉注释
                removeComments(newToken);
                EchoSyntaxNode exportExpNode = new EchoSyntaxNode(SyntaxNodeType.ECHO_EXP);
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
        NfContextScope mainScope = context.getMainScope();
        //获取全局中是否存在log
        NfVariableInfo variable = mainScope.getVariable("log");
        StringBuilder prints = toPrint(tokens, context, syntaxNode);
        if (variable != null) {
            Object log = variable.getValue();
            //转化log对象
            if (log instanceof Logger) {
                Logger logger = (Logger) log;
                logger.info(prints.toString());
            } else {
                System.out.println(prints);
            }
        } else {
            System.out.println(prints);
        }


    }


    private  StringBuilder toPrint(List<Token> tokens,NfContext context,SyntaxNode syntaxNode){
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        StringBuilder exp= new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        //目前打印只支持字符串和变量和常亮
        for (Token token : tokens) {
            switch (token.type){
                case LINE_END_SYMBOL:
                    sb.append("\n");
                    break;
                case TAB_SYMBOL:
                    sb.append("\t");
                    break;
                case STRING:
                    //需要处理模版占位符{a}  把{a}替换成变量的值
                    String value = token.value;
                    //如果是模版占位符
                    if (value.contains("{")&&value.contains("}")) {
                        value = replaceTemplate(value, context,syntaxNode);
                    }
                    exp.append(value);
                    break;
                case TEMPLATE_STRING:
                    // 处理模板字符串，去除首尾的 ```，然后替换占位符
                    // 模板字符串应该直接输出，不通过表达式计算
                    String templateValue = token.value;
                    // 去除首尾的 ```
                    if (templateValue.startsWith("```") && templateValue.endsWith("```")) {
                        templateValue = templateValue.substring(3, templateValue.length() - 3);
                    }
                    // 替换占位符
                    if (templateValue.contains("{") && templateValue.contains("}")) {
                        templateValue = replaceTemplate(templateValue, context, syntaxNode);
                    }
                    // 如果 exp 中有内容，先计算并输出
                    if (exp.length() > 0) {
                        try {
                            Object arithmetic = NfCalculator.arithmetic(exp.toString(), context);
                            sb.append(arithmetic);
                            exp = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
                        } catch (Exception e) {
                            throw new NfException(e, "Line:{}, 表达式计算错误: {} , syntax: {}", syntaxNode.getLine(), exp, syntaxNode);
                        }
                    }
                    // 直接输出模板字符串内容
                    sb.append(templateValue);
                    break;
                case COMMA:
                    if (exp.length()==0){
                        break;
                    }
                    try {
                        Object arithmetic = NfCalculator.arithmetic(exp.toString(), context);
                        exp = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
                        sb.append(arithmetic);
                    } catch (Exception e) {
                        throw new NfException(e, "Line:{}, 表达式计算错误: {} , syntax: {}", syntaxNode.getLine(), exp, syntaxNode);
                    }
                    break;
                default:
                    exp.append(token.value);
                    break;
            }
        }
        if (exp.length() > 0) {
            try {
                Object arithmetic = NfCalculator.arithmetic(exp.toString(), context);
                sb.append(arithmetic);
            } catch (Exception e) {
                throw new NfException(e, "Line:{}, 表达式计算错误: {} , syntax: {}", syntaxNode.getLine(), exp, syntaxNode);
            }
        }
        return sb;
    }


    /**
     * 传入一个字符串"123:{c} {b}"  将全部的模版替换为实际的值
     * 公共静态方法，供其他语法节点复用
     * 
     * @param str 包含占位符的字符串
     * @param context NF上下文
     * @param syntaxNode 语法节点（用于错误信息）
     * @return 替换后的字符串
     */
    public static String replaceTemplate(String str, NfContext context, SyntaxNode syntaxNode) {
        //找到所有的模版占位符
        int start = str.indexOf("{");
        int end = str.indexOf("}");
        while (start != -1 && end != -1) {
            //取出模版占位符
            String template = str.substring(start, end + 1);
            //取出变量名
            String varName = template.substring(1, template.length() - 1);
            //取出变量的值
            NfVariableInfo variable = context.getVariable(varName);
            //如果没有可能是表达式,利用计算器计算
            if (variable == null) {
                Object arithmetic = NfCalculator.arithmetic(varName, context);
                variable = new NfVariableInfo();
                variable.setValue(arithmetic);
            }
            Object value1 = variable.getValue()==null?"":variable.getValue();
            //(对象打印导致的)如果内容中有{和} 需要替换为其他字符,否则会被当做模版占位符, 替换为中文的括号
            if (value1.toString().contains("{") && value1.toString().contains("}")) {
                value1 = value1.toString().replace("{", "【").replace("}", "】");
            }
            //替换模版占位符
            str = str.replace(template, value1.toString());
            //继续找下一个模版占位符
            start = str.indexOf("{");
            end = str.indexOf("}");
        }
        return str;
    }




    @Override
    public String toString() {
        return  "echo "+ TokenUtil.mergeToken(getValue());
    }
}
