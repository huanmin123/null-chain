package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfReturnException;
import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
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
 * 函数调用语法节点
 * 
 * <p>支持函数调用语法：函数名(参数列表)</p>
 * <p>函数调用可以出现在表达式中，返回值会被用于表达式计算</p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FunCallSyntaxNode extends LineSyntaxNode {

    public FunCallSyntaxNode() {
        super(SyntaxNodeType.FUN_CALL_EXP);
    }

    public FunCallSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        // FunCallSyntaxNode重写了analystToken方法，此方法不会被调用
        return null;
    }

    /**
     * 分析Token是否可以解析为函数调用
     * 格式：IDENTIFIER LPAREN ... RPAREN 或 IDENTIFIER DOT IDENTIFIER LPAREN ... RPAREN（导入脚本的函数调用）
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        int size = tokens.size();
        if (size < 3) {
            return false;
        }
        // 检查是否是函数调用：
        // 1. IDENTIFIER LPAREN ... (普通函数调用)
        // 2. IDENTIFIER DOT IDENTIFIER LPAREN ... (导入脚本的函数调用)
        if (tokens.get(0).type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.LPAREN) {
            return true;
        }
        if (size >= 4 && 
            tokens.get(0).type == TokenType.IDENTIFIER && 
            tokens.get(1).type == TokenType.DOT &&
            tokens.get(2).type == TokenType.IDENTIFIER &&
            tokens.get(3).type == TokenType.LPAREN) {
            return true;
        }
        return false;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);

            // 判断是否是函数调用：
            // 1. IDENTIFIER LPAREN (普通函数调用)
            // 2. IDENTIFIER DOT IDENTIFIER LPAREN (导入脚本的函数调用)
            boolean isFunctionCall = false;
            int functionStartIndex = i;
            
            if (i + 1 < tokensSize && 
                token.type == TokenType.IDENTIFIER && 
                tokens.get(i + 1).type == TokenType.LPAREN) {
                // 普通函数调用
                isFunctionCall = true;
            } else if (i + 3 < tokensSize &&
                token.type == TokenType.IDENTIFIER &&
                tokens.get(i + 1).type == TokenType.DOT &&
                tokens.get(i + 2).type == TokenType.IDENTIFIER &&
                tokens.get(i + 3).type == TokenType.LPAREN) {
                // 导入脚本的函数调用
                isFunctionCall = true;
            }
            
            if (isFunctionCall) {
                // 找到右括号的位置
                int parenDepth = 1;
                int endIndex = functionStartIndex + (tokens.get(functionStartIndex + 1).type == TokenType.DOT ? 4 : 2);
                for (int j = endIndex; j < tokensSize; j++) {
                    Token t = tokens.get(j);
                    if (t.type == TokenType.LPAREN) {
                        parenDepth++;
                    } else if (t.type == TokenType.RPAREN) {
                        parenDepth--;
                        if (parenDepth == 0) {
                            endIndex = j + 1;
                            break;
                        }
                    }
                }
                
                // 记录结束下标, 用于截取和删除
                int lineEndIndex = SyntaxNodeUtil.findLineEndIndex(tokens, functionStartIndex);
                endIndex = Math.min(endIndex, lineEndIndex);
                
                // 截取函数调用语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(functionStartIndex, endIndex));
                // 删除已经解析的标记
                tokens.subList(functionStartIndex, endIndex).clear();

                // 去掉注释
                SyntaxNodeUtil.removeComments(newToken);
                FunCallSyntaxNode funCallNode = new FunCallSyntaxNode(SyntaxNodeType.FUN_CALL_EXP);
                funCallNode.setValue(newToken);
                // 设置行号
                funCallNode.setLine(token.getLine());
                syntaxNodeList.add(funCallNode);
                return true;
            }
        }
        return false;
    }

    /**
     * 执行函数调用
     * 
     * @param context 上下文
     * @param syntaxNode 语法节点
     * @return 函数返回值（单值或多值List）
     */
    public Object executeFunction(NfContext context, SyntaxNode syntaxNode) {
        List<Token> valueTokens = syntaxNode.getValue();
        if (valueTokens == null || valueTokens.isEmpty()) {
            throw new NfException("Line:{} ,函数调用tokens不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 解析函数名（支持普通函数调用和导入脚本的函数调用）
        String functionName;
        if (valueTokens.get(0).type == TokenType.IDENTIFIER && 
            valueTokens.size() > 1 && 
            valueTokens.get(1).type == TokenType.DOT) {
            // 导入脚本的函数调用：脚本名称.函数名(...)
            if (valueTokens.size() < 4 || valueTokens.get(2).type != TokenType.IDENTIFIER) {
                throw new NfException("Line:{} ,函数调用语法错误，导入脚本函数调用格式应为: 脚本名称.函数名(...) , syntax: {}",
                    syntaxNode.getLine(), syntaxNode);
            }
            // 合并脚本名称和函数名为完整函数名（带点号）
            functionName = valueTokens.get(0).value + "." + valueTokens.get(2).value;
        } else if (valueTokens.get(0).type == TokenType.IDENTIFIER) {
            // 普通函数调用：函数名(...)
            functionName = valueTokens.get(0).value;
        } else {
            throw new NfException("Line:{} ,函数调用语法错误，函数名必须是标识符 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 解析参数列表（先解析，因为脚本函数调用也需要参数）
        // 确定左括号的位置（普通函数调用在第1个token后，导入脚本函数调用在第3个token后）
        int lparenIndex = 1;
        if (functionName.contains(".")) {
            lparenIndex = 3; // 导入脚本函数调用：脚本名 DOT 函数名 LPAREN
        }
        
        if (valueTokens.size() <= lparenIndex || valueTokens.get(lparenIndex).type != TokenType.LPAREN) {
            throw new NfException("Line:{} ,函数调用语法错误，函数名后必须是左括号'(' , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 提取参数列表tokens（在括号内）
        List<Token> paramTokens = new ArrayList<>();
        int parenDepth = 1;
        
        for (int i = lparenIndex + 1; i < valueTokens.size(); i++) {
            Token token = valueTokens.get(i);
            if (token.type == TokenType.LPAREN) {
                parenDepth++;
            } else if (token.type == TokenType.RPAREN) {
                parenDepth--;
                if (parenDepth == 0) {
                    break;
                }
            }
            if (parenDepth > 0) {
                paramTokens.add(token);
            }
        }

        // 检查是否是导入脚本的函数调用（脚本名称.函数名）
        // 注意：只有当点号前面的标识符是已导入的脚本名称时，才认为是导入脚本的函数调用
        // 否则，应该作为普通的对象方法调用处理（通过 JEXL 表达式计算）
        String scriptName = null;
        String actualFunctionName = functionName;
        NfContext scriptContext = null;
        
        if (functionName.contains(".")) {
            // 解析脚本名称和函数名
            int dotIndex = functionName.indexOf('.');
            scriptName = functionName.substring(0, dotIndex);
            actualFunctionName = functionName.substring(dotIndex + 1);
            
            // 检查脚本是否已导入（只有已导入的脚本才认为是导入脚本的函数调用）
            if (context.hasImportedScript(scriptName)) {
                // 获取脚本的上下文
                scriptContext = context.getImportedScriptContext(scriptName);
                if (scriptContext == null) {
                    throw new NfException("Line:{} ,脚本 '{}' 的上下文不存在 , syntax: {}",
                        syntaxNode.getLine(), scriptName, syntaxNode);
                }
                
                // 从脚本上下文中获取函数定义
                FunDefInfo funDef = scriptContext.getFunction(actualFunctionName);
                if (funDef == null) {
                    throw new NfException("Line:{} ,脚本 '{}' 中未找到函数 {} , syntax: {}",
                        syntaxNode.getLine(), scriptName, actualFunctionName, syntaxNode);
                }
                
                // 使用脚本上下文执行函数
                return executeScriptFunction(scriptContext, scriptName, funDef, paramTokens, context, syntaxNode);
            }
            // 如果不是已导入的脚本，则作为普通的对象方法调用处理（通过 JEXL 表达式计算）
            // 这种情况下，需要将整个函数调用表达式（包括参数）转换为 JEXL 表达式并计算
            return executeObjectMethodCall(functionName, paramTokens, context, syntaxNode);
        }

        // 普通函数调用（当前上下文的函数）
        FunDefInfo funDef = context.getFunction(functionName);
        if (funDef == null) {
            throw new NfException("Line:{} ,函数 {} 未定义 , syntax: {}",
                syntaxNode.getLine(), functionName, syntaxNode);
        }
        
        // 检查函数体是否已定义
        if (funDef.getBodyNodes() == null) {
            throw new NfException("Line:{} ,函数 {} 的函数体未定义，可能函数定义还未执行 , syntax: {}",
                syntaxNode.getLine(), functionName, syntaxNode);
        }

        // 在解析参数之前保存当前作用域ID，作为函数作用域的父作用域
        // 这很关键，因为参数解析过程中可能会改变currentScopeId
        String parentScopeId = context.getCurrentScopeId();

        // 解析参数值（在函数作用域创建之前，使用调用时的作用域）
        List<Object> paramValues = parseParameterValues(paramTokens, context, syntaxNode.getLine());

        // 验证参数数量和类型
        validateParameters(funDef, paramValues, syntaxNode.getLine());

        // 创建函数作用域（使用解析参数之前保存的父作用域ID）
        String functionScopeId = NfContext.generateScopeId();
        NfContextScope functionScope = context.createScope(functionScopeId, parentScopeId, NfContextScopeType.ALL);

        // 保存当前作用域ID（用于恢复）
        String savedScopeId = context.getCurrentScopeId();
        
        try {
            // 切换到函数作用域
            context.switchScope(functionScopeId);

            // 设置函数参数
            setupFunctionParameters(functionScope, funDef, paramValues, context, functionName, syntaxNode.getLine(), syntaxNode);

            // 执行函数体
            executeFunctionBody(funDef, context, functionName, syntaxNode);

            // 获取返回值
            return getReturnValue(functionScope);

        } finally {
            // 恢复作用域
            context.switchScope(savedScopeId);
            // 清理函数作用域
            context.removeScope(functionScopeId);
        }
    }

    /**
     * 执行导入脚本的函数
     * 
     * @param scriptContext 脚本的上下文
     * @param scriptName 脚本名称
     * @param funDef 函数定义
     * @param paramTokens 参数tokens
     * @param currentContext 当前上下文（用于解析参数值）
     * @param syntaxNode 语法节点
     * @return 函数返回值
     */
    private Object executeScriptFunction(NfContext scriptContext, String scriptName, FunDefInfo funDef,
                                         List<Token> paramTokens, NfContext currentContext, SyntaxNode syntaxNode) {
        // 解析参数值（使用当前上下文解析，因为参数可能引用当前脚本的变量）
        List<Object> paramValues = parseParameterValues(paramTokens, currentContext, syntaxNode.getLine());
        
        // 验证参数数量和类型
        validateParameters(funDef, paramValues, syntaxNode.getLine());
        
        // 创建函数作用域（使用脚本上下文，父作用域是脚本的全局作用域）
        String scriptMainScopeId = scriptContext.getMainScopeId();
        String functionScopeId = NfContext.generateScopeId();
        NfContextScope functionScope = scriptContext.createScope(functionScopeId, scriptMainScopeId, NfContextScopeType.ALL);
        
        // 保存脚本上下文的当前作用域ID（用于恢复）
        String savedScriptScopeId = scriptContext.getCurrentScopeId();
        
        try {
            // 切换到函数作用域
            scriptContext.switchScope(functionScopeId);
            
            // 设置函数参数（使用脚本上下文和脚本名称）
            setupFunctionParameters(functionScope, funDef, paramValues, scriptContext, 
                scriptName + "." + funDef.getFunctionName(), syntaxNode.getLine(), syntaxNode, scriptName);
            
            // 执行函数体
            executeFunctionBody(funDef, scriptContext, scriptName + "." + funDef.getFunctionName(), syntaxNode, scriptName);
            
            // 获取返回值
            return getReturnValue(functionScope);
            
        } finally {
            // 恢复脚本上下文的作用域
            scriptContext.switchScope(savedScriptScopeId);
            // 清理函数作用域
            scriptContext.removeScope(functionScopeId);
        }
    }

    /**
     * 执行对象方法调用（通过 JEXL 表达式计算）
     * 
     * <p>当函数调用包含点号但不是导入脚本的函数调用时，将其作为对象方法调用处理。
     * 例如：map.put("key", "value") 会被转换为 JEXL 表达式并计算。</p>
     * 
     * @param methodName 方法名（包含对象名和方法名，如 "map.put"）
     * @param paramTokens 参数 tokens
     * @param context 上下文
     * @param syntaxNode 语法节点
     * @return 方法调用的返回值
     */
    private Object executeObjectMethodCall(String methodName, List<Token> paramTokens, NfContext context, SyntaxNode syntaxNode) {
        // 将整个函数调用转换为表达式字符串
        // 格式：methodName(param1, param2, ...)
        StringBuilder exprBuilder = new StringBuilder();
        exprBuilder.append(methodName).append("(");
        
        // 将参数 tokens 转换为字符串表达式
        if (!paramTokens.isEmpty()) {
            StringBuilder paramExpr = TokenUtil.mergeToken(paramTokens);
            exprBuilder.append(paramExpr.toString().trim());
        }
        
        exprBuilder.append(")");
        
        // 通过 JEXL 表达式计算（arithmetic 方法会处理参数表达式）
        String expression = exprBuilder.toString();
        return NfCalculator.arithmetic(expression, context);
    }

    /**
     * 解析参数值
     */
    private List<Object> parseParameterValues(List<Token> paramTokens, NfContext context, int line) {
        List<Object> paramValues = new ArrayList<>();
        
        if (paramTokens.isEmpty()) {
            return paramValues;
        }

        // 去掉注释
        SyntaxNodeUtil.removeComments(paramTokens);

        List<Token> currentParam = new ArrayList<>();
        for (Token token : paramTokens) {
            // 跳过换行token
            if (token.type == TokenType.LINE_END) {
                continue;
            }
            if (token.type == TokenType.COMMA) {
                if (!currentParam.isEmpty()) {
                    // 计算当前参数表达式
                    StringBuilder exp = TokenUtil.mergeToken(currentParam);
                    String expStr = exp.toString().trim();
                    // 如果表达式是单个数字或字符串字面量，直接解析，避免调用arithmetic
                    Object value = parseSimpleValue(currentParam, expStr, context);
                    if (value == null) {
                        // 不是简单值，使用arithmetic计算
                        value = NfCalculator.arithmetic(expStr, context);
                    }
                    paramValues.add(value);
                    currentParam.clear();
                }
            } else {
                currentParam.add(token);
            }
        }
        
        // 处理最后一个参数
        if (!currentParam.isEmpty()) {
            StringBuilder exp = TokenUtil.mergeToken(currentParam);
            String expStr = exp.toString().trim();
            // 如果表达式是单个数字或字符串字面量，直接解析，避免调用arithmetic
            Object value = parseSimpleValue(currentParam, expStr, context);
            if (value == null) {
                // 不是简单值，使用arithmetic计算
                value = NfCalculator.arithmetic(expStr, context);
            }
            paramValues.add(value);
        }

        return paramValues;
    }

    /**
     * 解析简单值（数字、字符串字面量等）
     * 如果参数是单个token且是字面量，直接解析，避免调用arithmetic
     */
    private Object parseSimpleValue(List<Token> tokens, String expStr, NfContext context) {
        if (tokens.size() == 1) {
            Token token = tokens.get(0);
            switch (token.type) {
                case INTEGER:
                    try {
                        return Integer.parseInt(token.value);
                    } catch (NumberFormatException e) {
                        return Long.parseLong(token.value);
                    }
                case FLOAT:
                    return Double.parseDouble(token.value);
                case STRING:
                    // 字符串token的value包含引号，需要去掉
                    String value = token.value;
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        return value.substring(1, value.length() - 1);
                    }
                    return value;
                case BOOLEAN:
                    return Boolean.parseBoolean(token.value);
                default:
                    // 不是简单值，返回null让调用者使用arithmetic
                    return null;
            }
        }
        // 多个token，不是简单值
        return null;
    }

    /**
     * 验证参数数量和类型
     */
    private void validateParameters(FunDefInfo funDef, List<Object> paramValues, int line) {
        List<FunDefInfo.FunParameter> parameters = funDef.getParameters();
        
        // 检查是否有可变参数
        boolean hasVarArgs = false;
        int fixedParamCount = parameters.size();
        for (FunDefInfo.FunParameter param : parameters) {
            if (param.isVarArgs()) {
                hasVarArgs = true;
                fixedParamCount = parameters.size() - 1;
                break;
            }
        }

        // 验证参数数量
        if (!hasVarArgs) {
            // 没有可变参数，参数数量必须完全匹配
            if (paramValues.size() != parameters.size()) {
                throw new NfException("Line:{} ,函数 {} 参数数量不匹配，期望 {} 个参数，实际 {} 个",
                    line, funDef.getFunctionName(), parameters.size(), paramValues.size());
            }
        } else {
            // 有可变参数，参数数量必须 >= 固定参数数量
            if (paramValues.size() < fixedParamCount) {
                throw new NfException("Line:{} ,函数 {} 参数数量不足，期望至少 {} 个参数，实际 {} 个",
                    line, funDef.getFunctionName(), fixedParamCount, paramValues.size());
            }
        }
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        // 执行函数调用，返回值会被存储到临时变量中
        // 注意：函数调用在表达式中时，返回值会被用于表达式计算
        // 这里我们执行函数调用，但返回值需要由调用者处理
        executeFunction(context, syntaxNode);
    }

    /**
     * 设置函数参数
     * 
     * @param functionScope 函数作用域
     * @param funDef 函数定义
     * @param paramValues 参数值列表
     * @param context 上下文（用于获取类型信息）
     * @param functionName 函数名称（用于错误信息）
     * @param line 行号
     * @param syntaxNode 语法节点
     */
    private void setupFunctionParameters(NfContextScope functionScope, FunDefInfo funDef, List<Object> paramValues,
                                        NfContext context, String functionName, int line, SyntaxNode syntaxNode) {
        setupFunctionParameters(functionScope, funDef, paramValues, context, functionName, line, syntaxNode, null);
    }

    /**
     * 设置函数参数（支持脚本函数）
     * 
     * @param functionScope 函数作用域
     * @param funDef 函数定义
     * @param paramValues 参数值列表
     * @param context 上下文（用于获取类型信息）
     * @param functionName 函数名称（用于错误信息）
     * @param line 行号
     * @param syntaxNode 语法节点
     * @param scriptName 脚本名称（可选，用于脚本函数的错误信息）
     */
    private void setupFunctionParameters(NfContextScope functionScope, FunDefInfo funDef, List<Object> paramValues,
                                        NfContext context, String functionName, int line, SyntaxNode syntaxNode, String scriptName) {
        List<FunDefInfo.FunParameter> parameters = funDef.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            FunDefInfo.FunParameter param = parameters.get(i);
            Object paramValue;
            
            if (param.isVarArgs()) {
                // 可变参数：将剩余的所有参数值组合成数组
                List<Object> varArgsValues = new ArrayList<>();
                for (int j = i; j < paramValues.size(); j++) {
                    varArgsValues.add(paramValues.get(j));
                }
                paramValue = varArgsValues;
            } else {
                // 普通参数
                if (i < paramValues.size()) {
                    paramValue = paramValues.get(i);
                } else {
                    if (scriptName != null) {
                        throw new NfException("Line:{} ,脚本 '{}' 的函数 {} 参数不足，期望至少 {} 个参数，实际 {} 个 , syntax: {}",
                            line, scriptName, funDef.getFunctionName(), i + 1, paramValues.size(), syntaxNode);
                    } else {
                        throw new NfException("Line:{} ,函数 {} 参数不足，期望至少 {} 个参数，实际 {} 个 , syntax: {}",
                            line, functionName, i + 1, paramValues.size(), syntaxNode);
                    }
                }
            }

            // 获取参数类型
            String paramTypeName = context.getImportType(param.getType());
            if (paramTypeName == null) {
                if (scriptName != null) {
                    throw new NfException("Line:{} ,脚本 '{}' 的函数 {} 参数 {} 类型 {} 未找到 , syntax: {}",
                        line, scriptName, funDef.getFunctionName(), param.getName(), param.getType(), syntaxNode);
                } else {
                    throw new NfException("Line:{} ,函数 {} 参数 {} 类型 {} 未找到 , syntax: {}",
                        line, functionName, param.getName(), param.getType(), syntaxNode);
                }
            }

            try {
                Class<?> paramType = Class.forName(paramTypeName);
                functionScope.addVariable(new NfVariableInfo(param.getName(), paramValue, paramType));
            } catch (ClassNotFoundException e) {
                if (scriptName != null) {
                    throw new NfException("Line:{} ,脚本 '{}' 的函数 {} 参数 {} 类型 {} 类未找到 , syntax: {}",
                        line, scriptName, funDef.getFunctionName(), param.getName(), paramTypeName, syntaxNode);
                } else {
                    throw new NfException("Line:{} ,函数 {} 参数 {} 类型 {} 类未找到 , syntax: {}",
                        line, functionName, param.getName(), paramTypeName, syntaxNode);
                }
            }
        }
    }

    /**
     * 执行函数体
     * 
     * @param funDef 函数定义
     * @param context 上下文
     * @param functionName 函数名称（用于错误信息）
     * @param syntaxNode 语法节点
     */
    private void executeFunctionBody(FunDefInfo funDef, NfContext context, String functionName, SyntaxNode syntaxNode) {
        executeFunctionBody(funDef, context, functionName, syntaxNode, null);
    }

    /**
     * 执行函数体（支持脚本函数）
     * 
     * @param funDef 函数定义
     * @param context 上下文
     * @param functionName 函数名称（用于错误信息）
     * @param syntaxNode 语法节点
     * @param scriptName 脚本名称（可选，用于脚本函数的错误信息）
     */
    private void executeFunctionBody(FunDefInfo funDef, NfContext context, String functionName, SyntaxNode syntaxNode, String scriptName) {
        // 执行函数体
        List<SyntaxNode> bodyNodes = funDef.getBodyNodes();
        if (bodyNodes == null) {
            if (scriptName != null) {
                throw new NfException("Line:{} ,脚本 '{}' 的函数 {} 的函数体未定义 , syntax: {}",
                    syntaxNode.getLine(), scriptName, funDef.getFunctionName(), syntaxNode);
            } else {
                throw new NfException("Line:{} ,函数 {} 的函数体未定义 , syntax: {}",
                    syntaxNode.getLine(), functionName, syntaxNode);
            }
        }

        try {
            // 执行函数体内的所有语句
            // 如果遇到return语句，会抛出NfReturnException来提前终止
            SyntaxNodeFactory.executeAll(bodyNodes, context);
        } catch (NfReturnException e) {
            // 正常的函数返回，return语句已经将返回值存储到$__return__变量中
            // 这里捕获异常后继续获取返回值
        }
    }

    /**
     * 获取函数返回值
     * 
     * @param functionScope 函数作用域
     * @return 返回值，如果没有return语句返回null
     */
    private Object getReturnValue(NfContextScope functionScope) {
        // 获取返回值
        NfVariableInfo returnVar = functionScope.getVariable("$__return__");
        if (returnVar == null) {
            // 没有return语句，返回null
            return null;
        }
        return returnVar.getValue();
    }

    @Override
    public String toString() {
        return TokenUtil.mergeToken(getValue()).toString();
    }
}

