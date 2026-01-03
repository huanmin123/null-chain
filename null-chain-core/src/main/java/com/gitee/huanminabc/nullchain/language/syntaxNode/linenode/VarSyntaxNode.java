package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.internal.ParseScopeTracker;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.DataType;
import com.gitee.huanminabc.nullchain.language.utils.KeywordUtil;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.EchoSyntaxNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * var变量声明表达式节点
 *
 * <p>支持两种格式：
 * <ul>
 *   <li>自动类型推导：var name="12312"</li>
 *   <li>手动指定类型：var name:String="1231"</li>
 * </ul>
 * </p>
 *
 * <p>语法格式：
 * <ul>
 *   <li>VAR IDENTIFIER ASSIGN ...（自动推导）</li>
 *   <li>VAR IDENTIFIER COLON IDENTIFIER ASSIGN ...（手动指定类型）</li>
 * </ul>
 * </p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VarSyntaxNode extends LineSyntaxNode {

    public VarSyntaxNode() {
        super(SyntaxNodeType.VAR_EXP);
    }

    public VarSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        // VarSyntaxNode重写了analystToken方法，此方法不会被调用
        // 但为了满足抽象方法要求，返回null
        return null;
    }

    /**
     * 分析Token是否可以解析为var变量声明表达式
     * 支持多种格式：
     * 1. 自动类型推导：VAR IDENTIFIER ASSIGN ... (例如：var name="12312")
     * 2. 手动指定类型：VAR IDENTIFIER COLON IDENTIFIER ASSIGN ... (例如：var name:String="1231")
     * 3. 多返回值函数调用（支持混合类型声明）：
     *    - var name, age = fun() (全部自动推导)
     *    - var name, age:Integer = fun() (混合：name自动推导，age指定类型)
     *    - var name:String, age = fun() (混合：name指定类型，age自动推导)
     *    - var name:String, age:Integer = fun() (全部指定类型)
     *
     * @param tokens Token列表
     * @return 是否可以解析为var变量声明表达式
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        int size = tokens.size();
        if (size < 3) {
            return false;
        }
        // 检查第一个token是否是VAR
        if (tokens.get(0).type != TokenType.VAR) {
            return false;
        }
        // 检查第二个token是否是IDENTIFIER（变量名）
        if (tokens.get(1).type != TokenType.IDENTIFIER) {
            return false;
        }
        // 检查是否是自动推导格式：VAR IDENTIFIER ASSIGN ...
        if (tokens.get(2).type == TokenType.ASSIGN) {
            return true;
        }
        // 检查是否是手动指定类型格式：VAR IDENTIFIER COLON IDENTIFIER ASSIGN ...
        if (size >= 5 && tokens.get(2).type == TokenType.COLON &&
            tokens.get(3).type == TokenType.IDENTIFIER &&
            tokens.get(4).type == TokenType.ASSIGN) {
            return true;
        }
        // 检查是否是多返回值函数调用格式（支持混合类型声明，支持N个变量）
        // 格式：VAR IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)+ ASSIGN ...
        // 至少需要：VAR IDENTIFIER COMMA IDENTIFIER ASSIGN (5个token)
        if (size >= 5) {
            int i = 1; // 跳过VAR
            boolean hasComma = false; // 必须至少有一个逗号才算多返回值
            // 循环解析变量：IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)*
            while (i < size) {
                // 必须有变量名
                if (i >= size || tokens.get(i).type != TokenType.IDENTIFIER) {
                    break;
                }
                i++;
                // 可选的类型声明
                if (i < size && tokens.get(i).type == TokenType.COLON) {
                    i += 2; // 跳过 COLON 和类型名
                }
                // 如果是逗号，继续解析下一个变量
                if (i < size && tokens.get(i).type == TokenType.COMMA) {
                    hasComma = true; // 标记至少有一个逗号
                    i++;
                    continue; // 继续下一个变量
                }
                // 如果是赋值符号，必须有逗号才算多返回值
                if (i < size && tokens.get(i).type == TokenType.ASSIGN) {
                    return hasComma; // 至少有一个逗号才是多返回值
                }
                // 其他情况，语法错误
                break;
            }
        }
        return false;
    }
    /**
     * 构建var变量声明语句
     * 支持多种格式：
     * 1. 自动类型推导：VAR IDENTIFIER ASSIGN ... (例如：var name="12312")
     * 2. 手动指定类型：VAR IDENTIFIER COLON IDENTIFIER ASSIGN ... (例如：var name:String="1231")
     * 3. 多返回值函数调用（支持混合类型声明）：
     *    - var name, age = fun() (全部自动推导)
     *    - var name, age:Integer = fun() (混合：name自动推导，age指定类型)
     *    - var name:String, age = fun() (混合：name指定类型，age自动推导)
     *    - var name:String, age:Integer = fun() (全部指定类型)
     *
     * @param tokens Token列表
     * @param syntaxNodeList 语法节点列表
     * @return 是否成功构建
     */
    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        if (tokensSize < 3) {
            return false;
        }

        // 检查第一个token是否是VAR
        if (tokens.get(0).type != TokenType.VAR) {
            return false;
        }

        // 检查是否是自动推导格式：VAR IDENTIFIER ASSIGN ...
        boolean isAutoInfer = tokensSize >= 3 &&
            tokens.get(1).type == TokenType.IDENTIFIER &&
            tokens.get(2).type == TokenType.ASSIGN;

        // 检查是否是手动指定类型格式：VAR IDENTIFIER COLON IDENTIFIER ASSIGN ...
        boolean isManualType = tokensSize >= 5 &&
            tokens.get(1).type == TokenType.IDENTIFIER &&
            tokens.get(2).type == TokenType.COLON &&
            tokens.get(3).type == TokenType.IDENTIFIER &&
            tokens.get(4).type == TokenType.ASSIGN;

        // 检查是否是多返回值函数调用格式（支持混合类型声明，支持N个变量）
        // 格式：VAR IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)+ ASSIGN ...
        boolean isMultiReturn = false;
        if (!isAutoInfer && !isManualType && tokensSize >= 5) {
            int i = 1; // 跳过VAR
            boolean hasComma = false; // 必须至少有一个逗号才算多返回值
            // 循环解析变量：IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)*
            while (i < tokensSize) {
                // 必须有变量名
                if (i >= tokensSize || tokens.get(i).type != TokenType.IDENTIFIER) {
                    break;
                }
                i++;
                // 可选的类型声明
                if (i < tokensSize && tokens.get(i).type == TokenType.COLON) {
                    i += 2; // 跳过 COLON 和类型名
                }
                // 如果是逗号，继续解析下一个变量
                if (i < tokensSize && tokens.get(i).type == TokenType.COMMA) {
                    hasComma = true;
                    i++;
                    continue;
                }
                // 如果是赋值符号，必须有逗号才算多返回值
                if (i < tokensSize && tokens.get(i).type == TokenType.ASSIGN) {
                    isMultiReturn = hasComma;
                    break;
                }
                // 其他情况，语法错误
                break;
            }
        }

        if (!isAutoInfer && !isManualType && !isMultiReturn) {
            return false;
        }

        // 确定结束位置
        int startIndex = 0;
        int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, startIndex);

        // 截取var语句的标记序列,不包含LINE_END
        List<Token> newToken = new ArrayList<>(tokens.subList(startIndex, endIndex));
        // 删除已经解析的标记
        tokens.subList(startIndex, endIndex).clear();

        // 解析时检查变量名重复
        ParseScopeTracker tracker = NfSynta.getCurrentTracker();
        String syntaxStr = printExp(newToken);
        
        if (isMultiReturn) {
            // 多返回值函数调用：解析所有变量名并检查
            int i = 1; // 跳过VAR
            while (i < newToken.size()) {
                if (newToken.get(i).type == TokenType.IDENTIFIER) {
                    Token varNameToken = newToken.get(i);
                    String varName = varNameToken.value;
                    // 禁止用户定义以 $ 开头的变量
                    if (varName != null && varName.startsWith("$")) {
                        throw new NfException("Line:{} ,变量名 {} 不能以 $ 开头，$ 前缀保留给系统变量使用, syntax: {}",
                            varNameToken.line, varName, syntaxStr);
                    }
                    boolean forbidKeyword = KeywordUtil.isForbidKeyword(varName);
                    if (forbidKeyword) {
                        throw new NfException("Line:{} ,变量名 {} 不能是禁用的关键字, syntax: {}",
                            varNameToken.line, varName, syntaxStr);
                    }
                    // 检查重复
                    if (tracker != null) {
                        tracker.checkDuplicateVariable(varName, varNameToken.line, syntaxStr);
                        tracker.addVariable(varName, varNameToken.line);
                    }
                    i++;
                    // 可选的类型声明
                    if (i < newToken.size() && newToken.get(i).type == TokenType.COLON) {
                        i += 2; // 跳过 COLON 和类型名
                    }
                    // 如果是逗号，继续解析下一个变量
                    if (i < newToken.size() && newToken.get(i).type == TokenType.COMMA) {
                        i++;
                        continue;
                    }
                    // 如果是赋值符号，结束
                    if (i < newToken.size() && newToken.get(i).type == TokenType.ASSIGN) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else {
            // 单变量声明：检查第一个变量名
            Token varName = newToken.get(1);
            // 禁止用户定义以 $ 开头的变量（$ 前缀保留给系统变量使用）
            if (varName.value != null && varName.value.startsWith("$")) {
                throw new NfException("Line:{} ,变量名 {} 不能以 $ 开头，$ 前缀保留给系统变量使用, syntax: {}",
                    varName.line, varName.value, syntaxStr);
            }
            boolean forbidKeyword = KeywordUtil.isForbidKeyword(varName.value);
            if (forbidKeyword) {
                throw new NfException("Line:{} ,变量名 {} 不能是禁用的关键字, syntax: {}",
                    varName.line, varName.value, syntaxStr);
            }
            // 检查重复
            if (tracker != null) {
                tracker.checkDuplicateVariable(varName.value, varName.line, syntaxStr);
                tracker.addVariable(varName.value, varName.line);
            }
        }

        // 去掉注释
        SyntaxNodeUtil.removeComments(newToken);
        VarSyntaxNode varSyntaxNode = new VarSyntaxNode(SyntaxNodeType.VAR_EXP);
        varSyntaxNode.setValue(newToken);
        // 设置行号
        varSyntaxNode.setLine(newToken.get(0).getLine());
        syntaxNodeList.add(varSyntaxNode);

        return true;
    }

    /**
     * 执行var变量声明语句
     * 支持多种格式：
     * 1. 自动类型推导：VAR IDENTIFIER ASSIGN ... (例如：var name="12312")
     * 2. 手动指定类型：VAR IDENTIFIER COLON IDENTIFIER ASSIGN ... (例如：var name:String="1231")
     * 3. 多返回值函数调用（支持混合类型声明）：
     *    - var name, age = fun() (全部自动推导)
     *    - var name, age:Integer = fun() (混合：name自动推导，age指定类型)
     *    - var name:String, age = fun() (混合：name指定类型，age自动推导)
     *    - var name:String, age:Integer = fun() (全部指定类型)
     *
     * @param context 上下文
     * @param syntaxNode 语法节点
     */
    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> valueTokens = syntaxNode.getValue();
        if (valueTokens == null || valueTokens.isEmpty()) {
            throw new NfException("Line:{} ,var变量声明表达式tokens不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 首先判断是否是多返回值函数调用（支持混合类型声明，支持N个变量）
        // 格式：VAR IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)+ ASSIGN ...
        // 必须在 hasManualType 检查之前，因为混合类型声明可能包含 COLON IDENTIFIER
        boolean isMultiReturn = false;
        if (valueTokens.size() >= 5) {
            int i = 1; // 跳过VAR
            boolean hasComma = false; // 必须至少有一个逗号才算多返回值
            // 循环解析变量：IDENTIFIER (COLON IDENTIFIER)? (COMMA IDENTIFIER (COLON IDENTIFIER)?)*
            while (i < valueTokens.size()) {
                // 必须有变量名
                if (i >= valueTokens.size() || valueTokens.get(i).type != TokenType.IDENTIFIER) {
                    break;
                }
                i++;
                // 可选的类型声明
                if (i < valueTokens.size() && valueTokens.get(i).type == TokenType.COLON) {
                    i += 2; // 跳过 COLON 和类型名
                }
                // 如果是逗号，继续解析下一个变量
                if (i < valueTokens.size() && valueTokens.get(i).type == TokenType.COMMA) {
                    hasComma = true;
                    i++;
                    continue;
                }
                // 如果是赋值符号，必须有逗号才算多返回值
                if (i < valueTokens.size() && valueTokens.get(i).type == TokenType.ASSIGN) {
                    isMultiReturn = hasComma;
                    break;
                }
                // 其他情况，语法错误
                break;
            }
        }

        // 判断是否有手动指定类型（单变量）
        // 只有在不是多返回值的情况下才检查
        boolean hasManualType = !isMultiReturn &&
            valueTokens.size() >= 5 &&
            valueTokens.get(0).type == TokenType.VAR &&
            valueTokens.get(1).type == TokenType.IDENTIFIER &&
            valueTokens.get(2).type == TokenType.COLON &&
            valueTokens.get(3).type == TokenType.IDENTIFIER &&
            valueTokens.get(4).type == TokenType.ASSIGN;

        String importType;
        String varName;
        List<Token> expTokens;

        if (isMultiReturn) {
            // 多返回值函数调用（支持混合类型声明）
            // 解析多个变量名和类型（类型声明是可选的）
            List<String> varNames = new ArrayList<>();
            List<String> varTypes = new ArrayList<>(); // null表示自动推导

            int i = 1; // 跳过VAR
            while (i < valueTokens.size() && valueTokens.get(i).type != TokenType.ASSIGN) {
                if (valueTokens.get(i).type == TokenType.IDENTIFIER) {
                    varNames.add(valueTokens.get(i).value);
                    i++;
                    // 可选的类型声明
                    if (i < valueTokens.size() && valueTokens.get(i).type == TokenType.COLON) {
                        i++;
                        if (i < valueTokens.size() && valueTokens.get(i).type == TokenType.IDENTIFIER) {
                            varTypes.add(valueTokens.get(i).value); // 指定了类型
                            i++;
                        } else {
                            throw new NfException("Line:{} ,多返回值函数调用语法错误，冒号后缺少类型 , syntax: {}",
                                syntaxNode.getLine(), syntaxNode);
                        }
                    } else {
                        varTypes.add(null); // 没有指定类型，使用自动推导
                    }
                } else if (valueTokens.get(i).type == TokenType.COMMA) {
                    i++;
                } else {
                    throw new NfException("Line:{} ,多返回值函数调用语法错误，意外的token {} , syntax: {}",
                        syntaxNode.getLine(), valueTokens.get(i).type, syntaxNode);
                }
            }

            if (i >= valueTokens.size() || valueTokens.get(i).type != TokenType.ASSIGN) {
                throw new NfException("Line:{} ,多返回值函数调用语法错误，缺少赋值符号 , syntax: {}",
                    syntaxNode.getLine(), syntaxNode);
            }

            // 获取函数调用表达式
            expTokens = valueTokens.subList(i + 1, valueTokens.size());

            if (expTokens.isEmpty()) {
                throw new NfException("Line:{} ,多返回值函数调用表达式为空 , syntax: {}",
                    syntaxNode.getLine(), syntaxNode);
            }

            // 计算函数调用表达式
            StringBuilder exp = TokenUtil.mergeToken(expTokens);
            Object returnValue;
            try {
                returnValue = NfCalculator.arithmetic(exp.toString(), context);
            } catch (Exception e) {
                int line = valueTokens.get(0).line;
                throw new NfException(e, "Line:{} ,函数调用表达式计算错误 , syntax: {}", line, syntaxNode);
            }

            // 验证返回值是List类型
            if (!(returnValue instanceof List)) {
                throw new NfException("Line:{} ,函数返回值不是List类型，无法进行多返回值赋值 , syntax: {}",
                    syntaxNode.getLine(), syntaxNode);
            }

            List<?> returnValues = (List<?>) returnValue;
            if (returnValues.size() != varNames.size()) {
                throw new NfException("Line:{} ,函数返回值数量 {} 与变量数量 {} 不匹配 , syntax: {}",
                    syntaxNode.getLine(), returnValues.size(), varNames.size(), syntaxNode);
            }

            // 将返回值赋值给各个变量
            NfContextScope currentScope = context.getCurrentScope();
            for (int j = 0; j < varNames.size(); j++) {
                String name = varNames.get(j);
                String typeName = varTypes.get(j); // 可能为null
                Object value = returnValues.get(j);

                Class<?> declaredType;
                if (typeName == null) {
                    // 没有指定类型，使用实际值的类型
                    declaredType = value.getClass();
                } else {
                    // 指定了类型，需要验证类型兼容性
                    String importTypeName = context.getImportType(typeName);
                    if (importTypeName == null) {
                        throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",
                            syntaxNode.getLine(), typeName, syntaxNode);
                    }

                    try {
                        declaredType = Class.forName(importTypeName);
                        // 验证类型兼容性
                        if (value != null && !declaredType.isAssignableFrom(value.getClass())) {
                            throw new NfException("Line:{} ,变量 {} 值类型和声明的类型不匹配 {} vs {} ,syntax: {}",
                                syntaxNode.getLine(), name, importTypeName, value.getClass(), syntaxNode);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",
                            syntaxNode.getLine(), importTypeName, syntaxNode);
                    }
                }

                // 检查当前作用域中是否已存在同名变量
                // 注意：重复变量检查已在解析阶段完成，此处不再检查
                currentScope.addVariable(new NfVariableInfo(name, value, declaredType));
            }
            
            return; // 多返回值处理完成，直接返回
        } else if (hasManualType) {
            // 手动指定类型：VAR IDENTIFIER COLON IDENTIFIER ASSIGN xxx
            // 获取变量名
            varName = valueTokens.get(1).value;
            // 获取类型
            Token typeToken = valueTokens.get(3);
            String type = typeToken.value;
            // 转化为java类型
            importType = context.getImportType(type);
            if (importType == null) {
                throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",
                    typeToken.line, type, syntaxNode);
            }
            // 获取赋值的表达式
            expTokens = valueTokens.subList(5, valueTokens.size());
        } else {
            // 自动类型推导：VAR IDENTIFIER ASSIGN xxx
            // 获取变量名
            varName = valueTokens.get(1).value;
            // 获取赋值的表达式
            expTokens = valueTokens.subList(3, valueTokens.size());
            // importType将在计算表达式后通过arithmetic.getClass()获取
            importType = null;
        }

        // 如果是空的那么就报错
        if (expTokens.isEmpty()) {
            int line = valueTokens.get(0).line;
            throw new NfException("Line:{} ,var变量声明表达式为空 , syntax: {}", line, syntaxNode);
        }

        // 取出来上下文
        NfContextScope currentScope = context.getCurrentScope();

        // 检查表达式中是否包含模板字符串，如果有则先处理模板字符串
        boolean hasTemplateString = false;
        String templateStringValue = null;
        for (Token expToken : expTokens) {
            if (expToken.type == TokenType.TEMPLATE_STRING) {
                hasTemplateString = true;
                // 去除首尾的 ```，并处理占位符
                templateStringValue = (String) DataType.realType(TokenType.TEMPLATE_STRING, expToken.value);
                templateStringValue = EchoSyntaxNode.replaceTemplate(templateStringValue, context);
                break;
            }
        }

        // 计算表达式
        StringBuilder exp = TokenUtil.mergeToken(expTokens);
        Object arithmetic;
        try {
            // 如果表达式只包含模板字符串，直接使用处理后的值
            if (hasTemplateString && expTokens.size() == 1 && expTokens.get(0).type == TokenType.TEMPLATE_STRING) {
                arithmetic = templateStringValue;
            } else {
                arithmetic = NfCalculator.arithmetic(exp.toString(), context);
                // 如果计算结果是字符串且包含占位符，进行替换
                if (arithmetic instanceof String && ((String) arithmetic).contains("{") && ((String) arithmetic).contains("}")) {
                    arithmetic = EchoSyntaxNode.replaceTemplate((String) arithmetic, context);
                }
            }
        } catch (Exception e) {
            int line = valueTokens.get(0).line;
            throw new NfException(e, "Line:{} ,表达式计算错误 , syntax: {}", line, syntaxNode);
        }

        // 确定最终类型
        Class<?> declaredType;
        try {
            if (hasManualType) {
                // 手动指定类型：使用指定的类型
                declaredType = Class.forName(importType);
                // 验证类型兼容性
                Class<?> actualType = arithmetic.getClass();
                if (!declaredType.isAssignableFrom(actualType)) {
                    int line = valueTokens.get(0).line;
                    throw new NfException("Line:{} ,变量 {} 值类型和声明的类型不匹配 {} vs {} ,syntax: {}",
                        line, varName, importType, arithmetic.getClass(), syntaxNode);
                }
            } else {
                // 自动类型推导：使用表达式计算结果的类型
                declaredType = arithmetic.getClass();
            }

            // 检查当前作用域中是否已存在同名变量
            // 注意：重复变量检查已在解析阶段完成，此处不再检查
            // 将变量添加到当前作用域
            currentScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));
        } catch (ClassNotFoundException e) {
            int line = valueTokens.get(0).line;
            throw new NfException("Line:{} ,未找到类型 {} , syntax: {}", line, importType, syntaxNode);
        }
    }

    /**
     * 打印表达式
     * 根据是否有手动指定类型，格式不同：
     * 1. 自动推导：var 变量名 = 表达式
     * 2. 手动指定类型：var 变量名 : 类型 = 表达式
     *
     * @param tokens Token列表
     * @return 表达式字符串
     */
    private String printExp(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        boolean hasManualType = tokens.size() >= 5 &&
            tokens.get(0).type == TokenType.VAR &&
            tokens.get(1).type == TokenType.IDENTIFIER &&
            tokens.get(2).type == TokenType.COLON &&
            tokens.get(3).type == TokenType.IDENTIFIER &&
            tokens.get(4).type == TokenType.ASSIGN;

        if (hasManualType) {
            // 前5个是var,变量名,冒号,类型,赋值符号 需要空格
            sb.append(tokens.get(0).value).append(" "); // var
            sb.append(tokens.get(1).value).append(" "); // 变量名
            sb.append(tokens.get(2).value).append(" "); // :
            sb.append(tokens.get(3).value).append(" "); // 类型
            sb.append(tokens.get(4).value).append(" "); // =
            // 后面的是表达式
            for (int i = 5; i < tokens.size(); i++) {
                sb.append(tokens.get(i).value);
            }
        } else {
            // 前3个是var,变量名,赋值符号 需要空格
            sb.append(tokens.get(0).value).append(" "); // var
            sb.append(tokens.get(1).value).append(" "); // 变量名
            sb.append(tokens.get(2).value).append(" "); // =
            // 后面的是表达式
            for (int i = 3; i < tokens.size(); i++) {
                sb.append(tokens.get(i).value);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return printExp(getValue());
    }
}
