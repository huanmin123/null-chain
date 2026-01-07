package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfReturnException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunTypeInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
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
 * return语句语法节点
 * 
 * <p>支持return语句语法：return 表达式1,表达式2,...（支持多返回值）</p>
 * <p>示例：return a + b</p>
 * <p>示例：return name, age</p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReturnSyntaxNode extends LineSyntaxNode {

    public ReturnSyntaxNode() {
        super(SyntaxNodeType.RETURN_EXP);
    }

    public ReturnSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.RETURN;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        if (tokensSize < 1) {
            return false;
        }

        // 检查第一个token是否是RETURN
        if (tokens.get(0).type != TokenType.RETURN) {
            return false;
        }

        // 确定结束位置
        int startIndex = 0;
        // 使用支持花括号嵌套的方法，以正确处理包含 Lambda 表达式的 return 语句
        int endIndex = SyntaxNodeUtil.findLineEndIndexWithBraceTracking(tokens, startIndex);

        // 截取return语句的标记序列,不包含LINE_END
        List<Token> newToken = new ArrayList<>(tokens.subList(startIndex, endIndex));
        // 删除已经解析的标记
        tokens.subList(startIndex, endIndex).clear();

        // 去掉注释
        SyntaxNodeUtil.removeComments(newToken);
        ReturnSyntaxNode returnSyntaxNode = new ReturnSyntaxNode(SyntaxNodeType.RETURN_EXP);
        returnSyntaxNode.setValue(newToken);
        // 设置行号
        returnSyntaxNode.setLine(newToken.get(0).getLine());
        syntaxNodeList.add(returnSyntaxNode);

        return true;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> valueTokens = syntaxNode.getValue();
        if (valueTokens == null || valueTokens.isEmpty()) {
            throw new NfException("Line:{} ,return语句tokens不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 去掉RETURN关键字
        if (valueTokens.get(0).type != TokenType.RETURN) {
            throw new NfException("Line:{} ,return语句语法错误，缺少return关键字 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }
        List<Token> expTokens = valueTokens.subList(1, valueTokens.size());

        if (expTokens.isEmpty()) {
            // 没有返回值，返回null
            // 找到函数作用域（ALL类型）而不是当前作用域（可能是IF作用域）
            NfContextScope functionScope = context.findByTypeScope(NfContextScopeType.ALL);
            if (functionScope == null) {
                functionScope = context.getCurrentScope();
            }
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", null, Object.class));
            return;
        }

        // 解析返回值（支持多返回值，用逗号分割）
        List<Object> returnValues = new ArrayList<>();
        List<Token> currentExp = new ArrayList<>();
        int parenDepth = 0;      // 括号深度
        int braceDepth = 0;      // 大括号深度

        for (Token token : expTokens) {
            // 更新深度计数器
            if (token.type == TokenType.LPAREN) {
                parenDepth++;
            } else if (token.type == TokenType.RPAREN) {
                parenDepth--;
            } else if (token.type == TokenType.LBRACE) {
                braceDepth++;
            } else if (token.type == TokenType.RBRACE) {
                braceDepth--;
            }

            // 只有在顶层（深度为0）的逗号才是返回值分隔符
            if (token.type == TokenType.COMMA && parenDepth == 0 && braceDepth == 0) {
                if (!currentExp.isEmpty()) {
                    // 计算当前表达式
                    Object value = evaluateReturnValue(currentExp, context, syntaxNode.getLine());
                    returnValues.add(value);
                    currentExp.clear();
                }
            } else {
                currentExp.add(token);
            }
        }

        // 处理最后一个表达式
        if (!currentExp.isEmpty()) {
            Object value = evaluateReturnValue(currentExp, context, syntaxNode.getLine());
            returnValues.add(value);
        }

        // 将返回值存储到函数作用域的特定变量中
        // 必须存储到函数作用域（ALL类型）而不是当前作用域（可能是IF作用域）
        NfContextScope functionScope = context.findByTypeScope(NfContextScopeType.ALL);
        if (functionScope == null) {
            functionScope = context.getCurrentScope();
        }
        if (returnValues.size() == 1) {
            // 单返回值
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", returnValues.get(0), returnValues.get(0).getClass()));
        } else {
            // 多返回值，使用List存储
            functionScope.addVariable(new com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo(
                "$__return__", returnValues, java.util.List.class));
        }

        // 抛出返回异常，提前终止函数体的执行
        throw new NfReturnException(syntaxNode.getLine());
    }

    /**
     * 计算返回值表达式
     * 支持普通表达式和 Lambda 表达式
     *
     * @param expTokens 表达式 token 列表
     * @param context 上下文
     * @param line 行号
     * @return 计算结果
     */
    private Object evaluateReturnValue(List<Token> expTokens, NfContext context, int line) {
        // 检查是否是 Lambda 表达式 (params) -> { body }
        boolean hasLambda = false;
        int lparenIndex = -1;
        int rparenIndex = -1;
        int arrowIndex = -1;
        int lbraceIndex = -1;
        int rbraceIndex = -1;

        for (int i = 0; i < expTokens.size(); i++) {
            Token token = expTokens.get(i);
            if (token.type == TokenType.LPAREN && lparenIndex == -1) {
                lparenIndex = i;
            } else if (token.type == TokenType.RPAREN && lparenIndex != -1 && rparenIndex == -1) {
                rparenIndex = i;
            } else if (token.type == TokenType.ARROW && rparenIndex != -1 && arrowIndex == -1) {
                arrowIndex = i;
            } else if (token.type == TokenType.LBRACE && arrowIndex != -1 && lbraceIndex == -1) {
                lbraceIndex = i;
            } else if (token.type == TokenType.RBRACE && lbraceIndex != -1) {
                rbraceIndex = i;
                // 找到完整的 Lambda 表达式
                hasLambda = true;
                break;
            }
        }

        if (hasLambda && lparenIndex >= 0 && rparenIndex >= 0 && arrowIndex > 0 && lbraceIndex >= 0 && rbraceIndex >= 0) {
            // 是完整的 Lambda 表达式：(params) -> { body }
            // 使用 LambdaSyntaxNode 来解析
            return parseLambdaInReturn(expTokens, lparenIndex, rparenIndex, lbraceIndex, rbraceIndex, context, line);
        }

        // 检查是否是单个标识符（可能是函数名或函数引用变量）
        if (expTokens.size() == 1 && expTokens.get(0).type == TokenType.IDENTIFIER) {
            String funcName = expTokens.get(0).value;
            if (context.hasFunction(funcName)) {
                // 返回已定义函数的引用
                return createFunctionRef(funcName, context);
            }
            if (context.hasFunRef(funcName)) {
                // 返回已存在的函数引用变量
                return context.getFunRef(funcName);
            }
        }

        // 普通表达式，使用 NfCalculator 计算
        StringBuilder exp = TokenUtil.mergeToken(expTokens);
        return NfCalculator.arithmetic(exp.toString(), context);
    }

    /**
     * 解析 Return 语句中的 Lambda 表达式
     * 例如：return (x) -> { return x * 2 }
     */
    private Object parseLambdaInReturn(List<Token> expTokens, int lparenIndex, int rparenIndex,
                                       int lbraceIndex, int rbraceIndex, NfContext context, int line) {
        // 提取 Lambda 参数列表
        List<Token> lambdaParamsTokens = new ArrayList<>(expTokens.subList(lparenIndex + 1, rparenIndex));

        // 提取 Lambda 体
        List<Token> lambdaBodyTokens = new ArrayList<>(expTokens.subList(lbraceIndex + 1, rbraceIndex));

        // 生成唯一的 Lambda 函数名
        String lambdaFuncName = "__lambda_return_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);

        // 推断参数和返回类型（简化处理：都使用 Object）
        List<FunDefInfo.FunParameter> parameters = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();

        for (Token token : lambdaParamsTokens) {
            if (token.type == TokenType.IDENTIFIER) {
                FunDefInfo.FunParameter param = new FunDefInfo.FunParameter();
                param.setName(token.value);
                param.setType("Object");  // 使用 Object 作为默认类型
                parameters.add(param);
                paramNames.add(token.value);
            }
        }

        List<String> returnTypes = new ArrayList<>();
        returnTypes.add("Object");

        // 解析 Lambda 体为语法节点
        List<SyntaxNode> lambdaBodyNodes = NfSynta.buildMainStatement(new ArrayList<>(lambdaBodyTokens));

        // 创建函数定义信息
        FunDefInfo funDefInfo = new FunDefInfo();
        funDefInfo.setFunctionName(lambdaFuncName);
        funDefInfo.setParameters(parameters);
        funDefInfo.setReturnTypes(returnTypes);
        funDefInfo.setBodyNodes(lambdaBodyNodes);

        // 注册函数定义到上下文
        context.addFunction(lambdaFuncName, funDefInfo);

        // 创建函数引用
        FunTypeInfo funTypeInfo = new FunTypeInfo();
        List<String> paramTypes = new ArrayList<>();
        for (FunDefInfo.FunParameter param : parameters) {
            paramTypes.add(param.getType());
        }
        funTypeInfo.setParameterTypes(paramTypes);
        funTypeInfo.setReturnType(returnTypes.get(0));

        // 捕获当前作用域的所有变量（闭包支持）
        java.util.Map<String, Object> capturedVariables = captureCurrentScopeVariables(context, paramNames);

        // 创建函数引用（支持闭包）
        FunRefInfo funRef = FunRefInfo.createLambda(funDefInfo, capturedVariables, context.getCurrentScopeId(), funTypeInfo);

        return funRef;
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
     * 创建函数引用
     */
    private Object createFunctionRef(String funcName, NfContext context) {
        return NfCalculator.arithmetic(funcName, context);
    }

    @Override
    public String toString() {
        return TokenUtil.mergeToken(getValue()).toString();
    }
}



