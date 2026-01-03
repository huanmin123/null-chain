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
     * 格式：IDENTIFIER LPAREN ... RPAREN
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        int size = tokens.size();
        if (size < 3) {
            return false;
        }
        // 检查是否是函数调用：IDENTIFIER LPAREN ...
        return tokens.get(0).type == TokenType.IDENTIFIER && 
               tokens.get(1).type == TokenType.LPAREN;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);

            // 判断是否是函数调用 IDENTIFIER LPAREN
            if (i + 1 < tokensSize && 
                token.type == TokenType.IDENTIFIER && 
                tokens.get(i + 1).type == TokenType.LPAREN) {
                
                // 找到右括号的位置
                int parenDepth = 1;
                int endIndex = i + 2;
                for (int j = i + 2; j < tokensSize; j++) {
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
                int lineEndIndex = SyntaxNodeUtil.findLineEndIndex(tokens, i);
                endIndex = Math.min(endIndex, lineEndIndex);
                
                // 截取函数调用语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i, endIndex));
                // 删除已经解析的标记
                tokens.subList(i, endIndex).clear();

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

        // 解析函数名
        if (valueTokens.get(0).type != TokenType.IDENTIFIER) {
            throw new NfException("Line:{} ,函数调用语法错误，函数名必须是标识符 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }
        String functionName = valueTokens.get(0).value;

        // 获取函数定义
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

        // 解析参数列表
        if (valueTokens.get(1).type != TokenType.LPAREN) {
            throw new NfException("Line:{} ,函数调用语法错误，函数名后必须是左括号'(' , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 提取参数列表tokens（在括号内）
        List<Token> paramTokens = new ArrayList<>();
        int parenDepth = 1;
        
        for (int i = 2; i < valueTokens.size(); i++) {
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
                        throw new NfException("Line:{} ,函数 {} 参数不足，期望至少 {} 个参数，实际 {} 个 , syntax: {}",
                            syntaxNode.getLine(), functionName, i + 1, paramValues.size(), syntaxNode);
                    }
                }

                // 获取参数类型
                String paramTypeName = context.getImportType(param.getType());
                if (paramTypeName == null) {
                    throw new NfException("Line:{} ,函数 {} 参数 {} 类型 {} 未找到 , syntax: {}",
                        syntaxNode.getLine(), functionName, param.getName(), param.getType(), syntaxNode);
                }

                try {
                    Class<?> paramType = Class.forName(paramTypeName);
                    functionScope.addVariable(new NfVariableInfo(param.getName(), paramValue, paramType));
                } catch (ClassNotFoundException e) {
                    throw new NfException("Line:{} ,函数 {} 参数 {} 类型 {} 类未找到 , syntax: {}",
                        syntaxNode.getLine(), functionName, param.getName(), paramTypeName, syntaxNode);
                }
            }

            // 执行函数体
            List<SyntaxNode> bodyNodes = funDef.getBodyNodes();
            if (bodyNodes == null) {
                throw new NfException("Line:{} ,函数 {} 的函数体未定义 , syntax: {}",
                    syntaxNode.getLine(), functionName, syntaxNode);
            }

            try {
                // 执行函数体内的所有语句
                // 如果遇到return语句，会抛出NfReturnException来提前终止
                SyntaxNodeFactory.executeAll(bodyNodes, context);
            } catch (NfReturnException e) {
                // 正常的函数返回，return语句已经将返回值存储到$__return__变量中
                // 这里捕获异常后继续获取返回值
            }

            // 获取返回值
            NfVariableInfo returnVar = functionScope.getVariable("$__return__");
            if (returnVar == null) {
                // 没有return语句，返回null
                return null;
            }

            Object returnValue = returnVar.getValue();
            return returnValue;

        } finally {
            // 恢复作用域
            context.switchScope(savedScopeId);
            // 清理函数作用域
            context.removeScope(functionScopeId);
        }
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

    @Override
    public String toString() {
        return TokenUtil.mergeToken(getValue()).toString();
    }
}

