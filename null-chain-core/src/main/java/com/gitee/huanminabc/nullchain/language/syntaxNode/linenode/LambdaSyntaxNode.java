package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.*;
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
 * Lambda 表达式语法节点
 * 处理 Lambda 表达式: Fun<ParamTypes... : ReturnType> lambdaName = (param1, param2) -> { body }
 *
 * @author huanmin
 * @date 2025/01/06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LambdaSyntaxNode extends LineSyntaxNode {
    public LambdaSyntaxNode() {
        super(SyntaxNodeType.LAMBDA_EXP);
    }

    @Override
    protected TokenType getTargetTokenType() {
        // LambdaSyntaxNode 重写了 analystToken 方法，此方法不会被调用
        return null;
    }

    /**
     * 识别 Lambda 表达式:
     * Fun<Types... : ReturnType> lambdaName = (params) -> { body }
     *
     * 注意：Lambda 表达式可能跨多行，需要检查完整的 token 序列
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        if (tokens == null || tokens.size() < 6) {
            return false;
        }

        // 检查是否有 Fun 开头
        Token first = tokens.get(0);
        if (first.type != TokenType.FUN_TYPE) {
            return false;
        }

        // 查找 GT 位置（Fun<> 类型的结束）
        int gtIndex = -1;
        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.GT) {
                gtIndex = i;
                break;
            }
        }
        if (gtIndex == -1) {
            return false; // 不是显式类型声明，可能是函数引用
        }

        // 检查是否有变量名
        if (gtIndex + 1 >= tokens.size()) {
            return false;
        }
        Token varNameToken = tokens.get(gtIndex + 1);
        if (varNameToken.type != TokenType.IDENTIFIER) {
            return false;
        }

        // 检查是否有赋值符
        if (gtIndex + 2 >= tokens.size()) {
            return false;
        }
        if (tokens.get(gtIndex + 2).type != TokenType.ASSIGN) {
            return false;
        }

        // 检查是否有左括号（Lambda 参数列表开始）
        if (gtIndex + 3 >= tokens.size()) {
            return false;
        }
        if (tokens.get(gtIndex + 3).type != TokenType.LPAREN) {
            return false; // 不是 Lambda，可能是函数引用
        }

        // 查找右括号（Lambda 参数列表结束）
        int rparenIndex = -1;
        for (int i = gtIndex + 4; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.RPAREN) {
                rparenIndex = i;
                break;
            }
        }
        if (rparenIndex == -1) {
            return false;
        }

        // 检查是否有 Lambda 箭头 ->
        if (rparenIndex + 1 >= tokens.size()) {
            return false;
        }
        if (tokens.get(rparenIndex + 1).type != TokenType.ARROW) {
            return false;
        }

        // 检查是否有左大括号（Lambda 体开始）
        if (rparenIndex + 2 >= tokens.size()) {
            return false;
        }
        if (tokens.get(rparenIndex + 2).type != TokenType.LBRACE) {
            return false;
        }

        // 关键修改：查找匹配的右大括号（可能跨多行）
        int lbraceIndex = rparenIndex + 2;
        int rbraceIndex = findMatchingRBrace(tokens, lbraceIndex);
        if (rbraceIndex == -1) {
            return false; // 找不到匹配的右大括号，不是完整的 Lambda 表达式
        }

        // 找到了完整的 Lambda 表达式（从开头到匹配的右大括号）
        return true;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);

            // 检查是否是 Fun 开头
            if (token.type == TokenType.FUN_TYPE) {
                // 查找 GT 位置
                int gtIndex = -1;
                for (int j = i + 1; j < tokensSize; j++) {
                    if (tokens.get(j).type == TokenType.GT) {
                        gtIndex = j;
                        break;
                    }
                }
                if (gtIndex == -1 || gtIndex + 3 >= tokensSize) {
                    continue;
                }

                // 检查后续 token 类型
                if (tokens.get(gtIndex + 1).type != TokenType.IDENTIFIER ||
                    tokens.get(gtIndex + 2).type != TokenType.ASSIGN ||
                    tokens.get(gtIndex + 3).type != TokenType.LPAREN) {
                    continue;
                }

                // 查找右括号和箭头
                int rparenIndex = -1;
                for (int j = gtIndex + 4; j < tokensSize; j++) {
                    if (tokens.get(j).type == TokenType.RPAREN) {
                        rparenIndex = j;
                        break;
                    }
                }
                if (rparenIndex == -1 || rparenIndex + 2 >= tokensSize) {
                    continue;
                }

                if (tokens.get(rparenIndex + 1).type != TokenType.ARROW ||
                    tokens.get(rparenIndex + 2).type != TokenType.LBRACE) {
                    continue;
                }

                // 提取各个部分
                List<Token> funTypeTokens = new ArrayList<>(tokens.subList(i, gtIndex + 1));
                Token varNameToken = tokens.get(gtIndex + 1);
                List<Token> lambdaParamsTokens = new ArrayList<>(tokens.subList(gtIndex + 4, rparenIndex));

                // 查找 Lambda 体结束位置（匹配的右大括号）
                int lbraceIndex = rparenIndex + 2;
                int rbraceIndex = findMatchingRBrace(tokens, lbraceIndex);
                if (rbraceIndex == -1) {
                    throw new NfException("Line:{}, Lambda 表达式缺少右大括号 '}}', syntax: {}",
                        token.getLine(), TokenUtil.mergeToken(tokens).toString());
                }

                // 提取 Lambda 体
                List<Token> lambdaBodyTokens = new ArrayList<>(tokens.subList(lbraceIndex + 1, rbraceIndex));

                // 检查是否以 LINE_END 结尾
                int endIndex = rbraceIndex + 1;
                if (endIndex < tokensSize && tokens.get(endIndex).type != TokenType.LINE_END) {
                    continue;
                }

                // 删除已解析的标记
                if (endIndex < tokensSize) {
                    tokens.subList(i, endIndex + 1).clear();
                } else {
                    tokens.subList(i, endIndex).clear();
                }

                // 创建 Lambda 语法节点
                LambdaSyntaxNode lambdaSyntaxNode = new LambdaSyntaxNode();
                lambdaSyntaxNode.setLine(token.getLine());
                lambdaSyntaxNode.setFunTypeTokens(funTypeTokens);
                lambdaSyntaxNode.setVarName(varNameToken.value);
                lambdaSyntaxNode.setLambdaParamsTokens(lambdaParamsTokens);
                lambdaSyntaxNode.setLambdaBodyTokens(lambdaBodyTokens);

                syntaxNodeList.add(lambdaSyntaxNode);
                return true;
            }
        }
        return false;
    }

    /**
     * 查找匹配的右大括号
     */
    private int findMatchingRBrace(List<Token> tokens, int lbraceIndex) {
        int depth = 1;
        for (int i = lbraceIndex + 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.type == TokenType.LBRACE) {
                depth++;
            } else if (t.type == TokenType.RBRACE) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        LambdaSyntaxNode lambdaNode = (LambdaSyntaxNode) syntaxNode;

        // 1. 解析 Fun 类型声明
        List<Token> funTypeTokens = lambdaNode.getFunTypeTokens();
        String funTypeStr = buildFunTypeString(funTypeTokens);
        FunTypeInfo funTypeInfo = FunTypeParser.parse(funTypeStr);

        // 2. 解析 Lambda 参数列表（只包含参数名）
        List<Token> lambdaParamsTokens = lambdaNode.getLambdaParamsTokens();
        List<String> paramNames = extractParamNames(lambdaParamsTokens);

        // 验证参数数量
        if (paramNames.size() != funTypeInfo.getParameterTypes().size()) {
            throw new NfException("Line:{}, Lambda 参数数量不匹配, 期望: {}, 实际: {}, syntax: {}",
                syntaxNode.getLine(), funTypeInfo.getParameterTypes().size(), paramNames.size(), syntaxNode);
        }

        // 3. 生成唯一的 Lambda 函数名
        String lambdaFuncName = generateLambdaFunctionName(context);

        // 4. 构建 FunParameter 列表
        List<FunDefInfo.FunParameter> parameters = new ArrayList<>();
        List<String> paramTypes = funTypeInfo.getParameterTypes();
        for (int i = 0; i < paramNames.size(); i++) {
            FunDefInfo.FunParameter param = new FunDefInfo.FunParameter();
            param.setName(paramNames.get(i));
            param.setType(paramTypes.get(i));
            parameters.add(param);
        }

        // 5. 构建返回值类型列表
        List<String> returnTypes = new ArrayList<>();
        returnTypes.add(funTypeInfo.getReturnType());

        // 6. 解析 Lambda 体为语法节点
        List<Token> lambdaBodyTokens = lambdaNode.getLambdaBodyTokens();
        List<SyntaxNode> lambdaBodyNodes = NfSynta.buildMainStatement(new ArrayList<>(lambdaBodyTokens));

        // 7. 创建函数定义信息
        FunDefInfo funDefInfo = new FunDefInfo();
        funDefInfo.setFunctionName(lambdaFuncName);
        funDefInfo.setParameters(parameters);
        funDefInfo.setReturnTypes(returnTypes);
        funDefInfo.setBodyNodes(lambdaBodyNodes); // 设置 Lambda 函数体

        // 8. 注册函数定义到上下文
        context.addFunction(lambdaFuncName, funDefInfo);

        // 9. 创建函数引用指向 Lambda 函数
        // 支持闭包（变量捕获）
        java.util.Map<String, Object> capturedVariables = captureCurrentScopeVariables(context, paramNames);
        FunRefInfo funRef = FunRefInfo.createLambda(funDefInfo, capturedVariables, context.getCurrentScopeId(), funTypeInfo);

        // 10. 将函数引用存储到变量
        String varName = lambdaNode.getVarName();
        context.addFunRef(varName, funRef);

        // 创建变量信息
        NfVariableInfo varInfo = new NfVariableInfo();
        varInfo.setName(varName);
        varInfo.setValue(funRef);
        varInfo.setType(FunRefInfo.class);
        varInfo.setFunctionReference(true);
        varInfo.setFunRefInfo(funRef);

        // 将变量添加到当前作用域
        NfContextScope currentScope = context.getCurrentScope();
        currentScope.addVariable(varInfo);
    }

    /**
     * 从 token 列表构建函数类型字符串
     */
    private String buildFunTypeString(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        for (Token token : tokens) {
            if (token.type == TokenType.LINE_END || token.type == TokenType.COMMENT) {
                break;
            }
            sb.append(token.value);
        }
        return sb.toString();
    }

    /**
     * 从 Lambda 参数 token 列表中提取参数名
     */
    private List<String> extractParamNames(List<Token> tokens) {
        List<String> paramNames = new ArrayList<>();
        for (Token token : tokens) {
            if (token.type == TokenType.IDENTIFIER) {
                paramNames.add(token.value);
            }
            // 忽略逗号和空格
        }
        return paramNames;
    }

    /**
     * 捕获当前作用域的所有变量（用于闭包）
     *
     * @param context 上下文
     * @param lambdaParamNames Lambda 参数名列表（需要排除）
     * @return 捕获的变量映射
     */
    private java.util.Map<String, Object> captureCurrentScopeVariables(NfContext context, List<String> lambdaParamNames) {
        java.util.Map<String, Object> capturedVars = new java.util.HashMap<>();

        // 获取当前作用域
        NfContextScope currentScope = context.getCurrentScope();
        if (currentScope == null) {
            return capturedVars;
        }

        // 使用 toMap() 方法获取所有变量
        java.util.Map<String, Object> allVars = currentScope.toMap();
        if (allVars != null) {
            for (java.util.Map.Entry<String, Object> entry : allVars.entrySet()) {
                String varName = entry.getKey();

                // 排除 Lambda 参数和系统内部变量
                if (!lambdaParamNames.contains(varName) && !varName.startsWith("$__")) {
                    Object varValue = entry.getValue();
                    if (varValue != null) {
                        // 只捕获非 null 的值
                        capturedVars.put(varName, varValue);
                    }
                }
            }
        }

        return capturedVars;
    }

    /**
     * 生成唯一的 Lambda 函数名
     */
    private String generateLambdaFunctionName(NfContext context) {
        int counter = context.getLambdaCounter();
        context.setLambdaCounter(counter + 1);
        return "__lambda_" + counter + "_" + System.currentTimeMillis();
    }

    // Lambda 表达式的各个组成部分
    private List<Token> funTypeTokens;      // Fun<> 类型声明
    private String varName;                 // Lambda 变量名
    private List<Token> lambdaParamsTokens;  // Lambda 参数列表
    private List<Token> lambdaBodyTokens;    // Lambda 体
}
