package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/*
  赋值表达式 例如: int a=1
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AssignSyntaxNode extends LineSyntaxNode {
    public AssignSyntaxNode() {
        super(SyntaxNodeType.ASSIGN_EXP);
    }

    public AssignSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        // AssignSyntaxNode重写了analystToken方法，此方法不会被调用
        // 但为了满足抽象方法要求，返回null
        return null;
    }

    /**
     * 分析Token是否可以解析为赋值表达式
     * 支持以下格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
     * 3. Fun类型赋值（简化版）：FUN_TYPE IDENTIFIER ASSIGN ... (例如：Fun func = add)
     * 4. Fun类型赋值（完整版）：FUN_TYPE ... GT IDENTIFIER ASSIGN ... (例如：Fun<Integer, Integer : Integer> func = add)
     *
     * @param tokens Token列表
     * @return 是否可以解析为赋值表达式
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        int size = tokens.size();
        if (size < 2) {
            return false;
        }

        // 查找 ASSIGN 的位置
        int assignIndex = -1;
        for (int i = 0; i < size; i++) {
            if (tokens.get(i).type == TokenType.ASSIGN) {
                assignIndex = i;
                break;
            }
        }

        if (assignIndex == -1) {
            return false;
        }

        // 检查是否是已存在变量的重新赋值：IDENTIFIER ASSIGN
        if (assignIndex == 1 && tokens.get(0).type == TokenType.IDENTIFIER) {
            return true;
        }

        // 检查是否是普通类型声明：IDENTIFIER IDENTIFIER ASSIGN
        if (assignIndex == 2 &&
            tokens.get(0).type == TokenType.IDENTIFIER &&
            tokens.get(1).type == TokenType.IDENTIFIER) {
            return true;
        }

        // 检查是否是 Fun 类型声明（简化版）：FUN_TYPE IDENTIFIER ASSIGN
        if (assignIndex == 2 &&
            tokens.get(0).type == TokenType.FUN_TYPE &&
            tokens.get(1).type == TokenType.IDENTIFIER) {
            return true;
        }

        // 检查是否是 Fun<> 类型声明（完整版）：FUN_TYPE ... GT IDENTIFIER ASSIGN
        if (assignIndex >= 2 && tokens.get(assignIndex - 1).type == TokenType.IDENTIFIER) {
            // 检查 ASSIGN 前两个位置是否是 GT（表示可能是 Fun<> 类型）
            if (tokens.get(assignIndex - 2).type == TokenType.GT) {
                // 向前查找对应的 FUN_TYPE
                int angleBracketDepth = 0;
                for (int j = assignIndex - 2; j >= 0; j--) {
                    Token t = tokens.get(j);
                    if (t.type == TokenType.GT) {
                        angleBracketDepth++;
                    } else if (t.type == TokenType.LT) {
                        angleBracketDepth--;
                        if (angleBracketDepth == 0 && j > 0 && tokens.get(j - 1).type == TokenType.FUN_TYPE) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    /**
     * 构建赋值语句
     * 支持以下格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
     * 3. Fun类型赋值（简化版）：FUN_TYPE IDENTIFIER ASSIGN ... (例如：Fun func = add)
     * 4. Fun类型赋值（完整版）：FUN_TYPE ... GT IDENTIFIER ASSIGN ... (例如：Fun<Integer, Integer : Integer> func = add)
     *
     * @param tokens Token列表
     * @param syntaxNodeList 语法节点列表
     * @return 是否成功构建
     */
    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.ASSIGN) {
                // 判断是哪种格式的赋值
                // 1. 检查是否是 Fun 类型声明（简化版）：FUN_TYPE IDENTIFIER ASSIGN
                boolean isSimpleFunTypeDeclaration = i == 2 &&
                    tokens.get(0).type == TokenType.FUN_TYPE &&
                    tokens.get(1).type == TokenType.IDENTIFIER;

                // 2. 检查是否是 Fun<> 类型声明（完整版）：FUN_TYPE ... GT IDENTIFIER ASSIGN
                boolean isFullFunTypeDeclaration = false;
                int funTypeStart = -1;

                if (i >= 2 && !isSimpleFunTypeDeclaration) {
                    // 从 ASSIGN 向前查找，看是否是 Fun<> 类型
                    // 格式：Fun<ParamTypes... : ReturnType> varName =
                    // ASSIGN 前面应该是 IDENTIFIER (varName)
                    if (tokens.get(i - 1).type == TokenType.IDENTIFIER) {
                        // 从 varName 向前查找 Fun<> 类型的开始
                        int typeEndIndex = i - 2;
                        if (typeEndIndex >= 0 && tokens.get(typeEndIndex).type == TokenType.GT) {
                            // 找到了 GT，现在向前查找对应的 FUN_TYPE
                            int angleBracketDepth = 0;
                            for (int j = typeEndIndex; j >= 0; j--) {
                                Token t = tokens.get(j);
                                if (t.type == TokenType.GT) {
                                    angleBracketDepth++;
                                } else if (t.type == TokenType.LT) {
                                    angleBracketDepth--;
                                    if (angleBracketDepth == 0 && j > 0 && tokens.get(j - 1).type == TokenType.FUN_TYPE) {
                                        isFullFunTypeDeclaration = true;
                                        funTypeStart = j - 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                boolean isFunTypeDeclaration = isSimpleFunTypeDeclaration || isFullFunTypeDeclaration;

                // 3. 检查是否是普通类型声明：IDENTIFIER IDENTIFIER ASSIGN
                boolean isNormalTypeDeclaration = i == 2 &&
                    tokens.get(0).type == TokenType.IDENTIFIER &&
                    tokens.get(1).type == TokenType.IDENTIFIER;

                boolean hasTypeDeclaration = isFunTypeDeclaration || isNormalTypeDeclaration;

                int startIndex;
                if (hasTypeDeclaration) {
                    if (isFullFunTypeDeclaration) {
                        // 完整版 Fun<> 类型声明：从 FUN_TYPE 开始
                        startIndex = funTypeStart;
                    } else if (isSimpleFunTypeDeclaration) {
                        // 简化版 Fun 类型声明：FUN_TYPE IDENTIFIER ASSIGN，从开始位置截取
                        startIndex = 0;
                    } else {
                        // 普通类型声明：下标向前移动2位（类型 + 变量名）
                        startIndex = i - 2;
                    }
                } else {
                    // 已存在变量的重新赋值：下标向前移动1位（变量名）
                    // 确保 startIndex >= 0
                    if (i < 1) {
                        // 如果 i < 1，说明 ASSIGN 前面没有足够的 token，这是语法错误
                        continue;
                    }
                    startIndex = i - 1;
                }

                //记录结束下标, 用于截取和删除
                // 对于包含花括号的语句（如Lambda表达式），需要使用支持括号嵌套追踪的方法
                int endIndex;
                if (isFunTypeDeclaration) {
                    // Fun类型声明可能包含Lambda表达式，使用支持括号嵌套的方法
                    endIndex = SyntaxNodeUtil.findLineEndIndexWithBraceTracking(tokens, startIndex);
                } else {
                    // 普通赋值语句，使用原来的方法
                    endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, startIndex);
                }
                //截取赋值语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(startIndex, endIndex));
                //删除已经解析的标记
                tokens.subList(startIndex, endIndex).clear();

                //拿到变量名称（根据是否有类型声明，位置不同）
                Token varName;
                if (isFullFunTypeDeclaration) {
                    // 完整版 Fun<> 类型声明：变量名在 ASSIGN 前面一个 token
                    varName = newToken.get(i - 1 - startIndex);
                } else if (isSimpleFunTypeDeclaration) {
                    // 简化版 Fun 类型声明：FUN_TYPE IDENTIFIER ASSIGN，变量名在位置 1
                    varName = newToken.get(1);
                } else if (hasTypeDeclaration) {
                    // 普通类型声明：IDENTIFIER(varName) IDENTIFIER(varType) ASSIGN
                    varName = newToken.get(1);
                } else {
                    // 已存在变量的重新赋值：IDENTIFIER(varName) ASSIGN
                    varName = newToken.get(0);
                }
                // 禁止用户定义以 $ 开头的变量（$ 前缀保留给系统变量使用）
                if (varName.value != null && varName.value.startsWith("$")) {
                    throw new NfException("Line:{} ,变量名 {} 不能以 $ 开头，$ 前缀保留给系统变量使用, syntax: {}", 
                        varName.line, varName.value, printExp(newToken));
                }
                boolean forbidKeyword = KeywordUtil.isForbidKeyword(varName.value);
                if (forbidKeyword) {
                    throw new NfException("Line:{} ,变量名 {} 不能是禁用的关键字: {}",varName.line,varName.value,printExp(newToken));
                }

                // 解析时检查变量名重复（仅对带类型声明的赋值进行检查，因为这是新变量声明）
                if (hasTypeDeclaration) {
                    ParseScopeTracker tracker = NfSynta.getCurrentTracker();
                    if (tracker != null) {
                        String syntaxStr = printExp(newToken);
                        try {
                            tracker.checkDuplicateVariable(varName.value, varName.line, syntaxStr);
                            tracker.addVariable(varName.value, varName.line);
                        } catch (NfSyntaxException e) {
                            // 重新抛出NfSyntaxException
                            throw e;
                        }
                    }
                }

                //去掉注释
                SyntaxNodeUtil.removeComments(newToken);
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

    /**
     * 执行赋值语句
     * 支持以下格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
     * 3. Fun类型赋值（简化版）：FUN_TYPE IDENTIFIER ASSIGN ... (例如：Fun func = add)
     * 4. Fun类型赋值（完整版）：FUN_TYPE ... GT IDENTIFIER ASSIGN ... (例如：Fun<Integer, Integer : Integer> func = add)
     *
     * @param context 上下文
     * @param syntaxNode 语法节点
     */
    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> valueTokens = syntaxNode.getValue();
        if (valueTokens == null || valueTokens.isEmpty()) {
            throw new NfException("Line:{} ,赋值表达式tokens不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 查找 ASSIGN 的位置
        int assignIndex = -1;
        for (int i = 0; i < valueTokens.size(); i++) {
            if (valueTokens.get(i).type == TokenType.ASSIGN) {
                assignIndex = i;
                break;
            }
        }

        // 检查是否是简化版 Fun 类型声明：FUN_TYPE IDENTIFIER ASSIGN
        boolean isSimpleFunTypeDeclaration = assignIndex == 2 &&
            valueTokens.get(0).type == TokenType.FUN_TYPE &&
            valueTokens.get(1).type == TokenType.IDENTIFIER;

        // 检查是否是完整版 Fun<> 类型声明：FUN_TYPE ... GT IDENTIFIER ASSIGN
        boolean isFullFunTypeDeclaration = false;
        if (assignIndex != -1 && assignIndex >= 2 && !isSimpleFunTypeDeclaration) {
            // 检查是否是 Fun<> 类型
            if (valueTokens.get(assignIndex - 1).type == TokenType.IDENTIFIER &&
                valueTokens.get(assignIndex - 2).type == TokenType.GT) {
                // 向前查找对应的 FUN_TYPE
                int angleBracketDepth = 0;
                for (int j = assignIndex - 2; j >= 0; j--) {
                    Token t = valueTokens.get(j);
                    if (t.type == TokenType.GT) {
                        angleBracketDepth++;
                    } else if (t.type == TokenType.LT) {
                        angleBracketDepth--;
                        if (angleBracketDepth == 0 && j > 0 && valueTokens.get(j - 1).type == TokenType.FUN_TYPE) {
                            isFullFunTypeDeclaration = true;
                            break;
                        }
                    }
                }
            }
        }

        boolean isFunTypeDeclaration = isSimpleFunTypeDeclaration || isFullFunTypeDeclaration;

        // 检查是否是普通类型声明：IDENTIFIER IDENTIFIER ASSIGN ...
        boolean isNormalTypeDeclaration = valueTokens.size() >= 3 &&
            valueTokens.get(0).type == TokenType.IDENTIFIER &&
            valueTokens.get(1).type == TokenType.IDENTIFIER &&
            valueTokens.get(2).type == TokenType.ASSIGN;

        boolean hasTypeDeclaration = isFunTypeDeclaration || isNormalTypeDeclaration;

        String importType;
        String varName;
        List<Token> expTokens;

        if (hasTypeDeclaration) {
            if (isSimpleFunTypeDeclaration) {
                // 简化版 Fun 类型声明：FUN_TYPE IDENTIFIER ASSIGN xxx
                // 获取类型
                String type = "Fun";
                // 转化为 java 类型
                importType = context.getImportType(type);
                if (importType == null) {
                    throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",
                        valueTokens.get(0).line, type, syntaxNode);
                }
                // 获取赋值的变量名
                varName = valueTokens.get(1).value;
                // 获取赋值的表达式
                expTokens = valueTokens.subList(3, valueTokens.size());
            } else if (isFullFunTypeDeclaration) {
                // 完整版 Fun<> 类型声明：FUN_TYPE ... GT IDENTIFIER ASSIGN xxx
                // 提取 Fun<> 类型字符串
                StringBuilder typeBuilder = new StringBuilder();
                int typeEndIndex = assignIndex - 1; // IDENTIFIER (varName) 的位置
                // Fun<> 类型从开头到 GT
                for (int j = 0; j <= typeEndIndex; j++) {
                    typeBuilder.append(valueTokens.get(j).value);
                }
                String type = typeBuilder.toString();

                // 转化为 java 类型
                importType = context.getImportType(type);
                if (importType == null) {
                    // 如果找不到完整类型，尝试使用 "Fun" 作为类型名
                    importType = context.getImportType("Fun");
                    if (importType == null) {
                        throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",
                            valueTokens.get(0).line, type, syntaxNode);
                    }
                }

                // 获取赋值的变量名（ASSIGN 前面一个 token）
                varName = valueTokens.get(assignIndex - 1).value;
                // 获取赋值的表达式（ASSIGN 后面）
                expTokens = valueTokens.subList(assignIndex + 1, valueTokens.size());
            } else {
                // 普通类型声明：IDENTIFIER IDENTIFIER ASSIGN xxx
                // 获取类型
                Token token = valueTokens.get(0);
                String type = token.value;
                // 转化为 java 类型
                importType = context.getImportType(type);
                if (importType == null) {
                    throw new NfException("Line:{} ,未找到类型 {} , syntax: {}", token.line, type, syntaxNode);
                }
                // 获取赋值的变量名
                varName = valueTokens.get(1).value;
                // 获取赋值的表达式
                expTokens = valueTokens.subList(3, valueTokens.size());
            }
        } else {
            // 已存在变量的重新赋值：IDENTIFIER ASSIGN xxx
            // 获取变量名
            varName = valueTokens.get(0).value;
            // 获取赋值的表达式
            expTokens = valueTokens.subList(2, valueTokens.size());
            // 从上下文获取变量的类型
            NfVariableInfo variableInfo = context.getVariable(varName);
            if (variableInfo == null) {
                throw new NfException("Line:{} ,变量 {} 不存在，无法重新赋值 , syntax: {}",
                    valueTokens.get(0).line, varName, syntaxNode);
            }
            importType = variableInfo.getType().getName();
        }
        //如果是空的那么就报错
        if (expTokens.isEmpty()) {
            int line = valueTokens.get(0).line;
            throw new NfException("Line:{} ,赋值表达式为空 , syntax: {}", line, syntaxNode);
        }
        //取出来上下文
        NfContextScope currentScope = context.getCurrentScope();
        //获取第一个token类型如果是New那么就是创建对象（仅支持带类型声明的赋值）
        if (hasTypeDeclaration && expTokens.get(0).type == TokenType.NEW) {
            // 检查是否有参数列表：new(参数1, 参数2, ...)
            boolean hasArgs = expTokens.size() >= 3 && expTokens.get(1).type == TokenType.LPAREN;

            try {
                //创建对象
                Class<?> declaredType = Class.forName(importType);
                Class<?> actualType = declaredType;

                //如果声明的类型是接口，从上下文获取默认实现类
                if (declaredType.isInterface()) {
                    actualType = context.getInterfaceDefaultImpl(declaredType);
                    if (actualType == null) {
                        int line = valueTokens.get(0).line;
                        throw new NfException("Line:{} ,接口 {} 没有默认实现类，无法创建实例 , syntax: {}",
                            line, importType, syntaxNode);
                    }
                }

                Object instance;
                if (hasArgs) {
                    // 解析参数列表：new(参数1, 参数2, ...)
                    // 找到匹配的右括号
                    int parenEnd = -1;
                    int depth = 0;
                    for (int i = 1; i < expTokens.size(); i++) {
                        if (expTokens.get(i).type == TokenType.LPAREN) {
                            depth++;
                        } else if (expTokens.get(i).type == TokenType.RPAREN) {
                            depth--;
                            if (depth == 0) {
                                parenEnd = i;
                                break;
                            }
                        }
                    }
                    if (parenEnd == -1) {
                        throw new NfException("Line:{} ,new() 参数列表括号不匹配 , syntax: {}",
                            valueTokens.get(0).line, syntaxNode);
                    }

                    // 提取括号内的参数 tokens
                    List<Token> paramTokens = expTokens.subList(2, parenEnd);

                    // 解析参数表达式（按逗号分隔）
                    List<Object> args = new ArrayList<>();
                    List<Class<?>> argTypes = new ArrayList<>();

                    if (!paramTokens.isEmpty()) {
                        List<List<Token>> paramExprs = splitByComma(paramTokens);
                        for (List<Token> paramExpr : paramExprs) {
                            Object argValue = NfCalculator.arithmetic(TokenUtil.mergeToken(paramExpr).toString(), context);
                            args.add(argValue);
                            argTypes.add(argValue != null ? argValue.getClass() : null);
                        }
                    }

                    // 根据参数类型找到匹配的构造函数
                    Constructor<?> matchedConstructor = findMatchingConstructor(actualType, argTypes);
                    instance = matchedConstructor.newInstance(args.toArray());
                } else {
                    // 注意：重复变量检查已在解析阶段完成，此处不再检查
                    // 获取无参构造函数
                    Constructor<?> constructor = actualType.getConstructor();
                    instance = constructor.newInstance();
                }

                //将对象放入上下文，类型保持为声明的接口类型，这样类型检查时可以支持所有实现类
                currentScope.addVariable(new NfVariableInfo(varName, instance, declaredType));
            } catch (ClassNotFoundException e) {
                int line = valueTokens.get(0).line;
                throw new NfException(e, "Line:{} ,未找到类型 {} , syntax: {}", line, importType, syntaxNode);
            } catch (NoSuchMethodException e) {
                int line = valueTokens.get(0).line;
                throw new NfException(e, "Line:{} ,类型 {} 没有匹配的构造函数 , syntax: {}", line, importType, syntaxNode);
            } catch (Exception e) {
                int line = valueTokens.get(0).line;
                throw new NfException(e, "Line:{} ,创建{}对象失败 , syntax: {}", line, importType, syntaxNode);
            }
            return;
        }
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
        
        //计算表达式
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
            throw new NfException(e, "Line:{} , syntax: {}", line, syntaxNode);
        }
        //判断类型值和类型是否一致（支持接口类型兼容性）
        try {
            Class<?> declaredType = Class.forName(importType);
            Class<?> actualType = arithmetic.getClass();
            if (!declaredType.isAssignableFrom(actualType)) {
                int line = valueTokens.get(0).line;
                throw new NfException("Line:{} ,变量 {} 值类型和声明的型不匹配 {} vs {} ,syntax: {}", 
                    line, varName, importType, arithmetic.getClass(), syntaxNode);
            }
            //将计算的值放入上下文，类型保持为声明的类型（如果是接口，保持接口类型）
            if (hasTypeDeclaration) {
                // 注意：重复变量检查已在解析阶段完成，此处不再检查
                // 新变量声明：添加到当前作用域
                currentScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));

                // 特殊处理：如果是 FunRefInfo 类型，还需要注册到 context 的 funRefMap 中
                // 这样 preProcessFunctionCalls 才能识别函数引用变量的调用
                if (arithmetic instanceof com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) {
                    com.gitee.huanminabc.nullchain.language.internal.FunRefInfo funRef =
                        (com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) arithmetic;
                    context.addFunRef(varName, funRef);
                }
            } else {
                // 已存在变量的重新赋值：更新变量所在的作用域，而不是当前作用域
                // 这样可以确保在循环中对父作用域变量的修改能够持久化
                NfContextScope variableScope = context.findVariableScope(varName);
                if (variableScope != null) {
                    variableScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));
                    // 特殊处理：如果是 FunRefInfo 类型，还需要更新 context 的 funRefMap
                    if (arithmetic instanceof com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) {
                        com.gitee.huanminabc.nullchain.language.internal.FunRefInfo funRef =
                            (com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) arithmetic;
                        context.addFunRef(varName, funRef);
                    }
                } else {
                    // 如果找不到变量所在的作用域（理论上不应该发生），则添加到当前作用域
                    currentScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));
                    // 特殊处理：如果是 FunRefInfo 类型，还需要注册到 context 的 funRefMap
                    if (arithmetic instanceof com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) {
                        com.gitee.huanminabc.nullchain.language.internal.FunRefInfo funRef =
                            (com.gitee.huanminabc.nullchain.language.internal.FunRefInfo) arithmetic;
                        context.addFunRef(varName, funRef);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            int line = valueTokens.get(0).line;
            throw new NfException("Line:{} ,未找到类型 {} , syntax: {}", line, importType, syntaxNode);
        }
    }


    /**
     * 打印表达式
     * 根据是否有类型声明，格式不同：
     * 1. 带类型声明：类型 变量名 = 表达式
     * 2. 已存在变量重新赋值：变量名 = 表达式
     *
     * @param tokens Token列表
     * @return 表达式字符串
     */
    private String printExp(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);

        // 查找 ASSIGN 的位置
        int assignIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.ASSIGN) {
                assignIndex = i;
                break;
            }
        }

        if (assignIndex == -1) {
            // 没有找到 ASSIGN，直接拼接所有 tokens
            for (Token token : tokens) {
                sb.append(token.value);
            }
            return sb.toString();
        }

        // 检查是否是 Fun<> 类型声明
        boolean isFunTypeDeclaration = false;
        if (assignIndex >= 2 && tokens.get(assignIndex - 1).type == TokenType.IDENTIFIER &&
            tokens.get(assignIndex - 2).type == TokenType.GT) {
            // 向前查找对应的 FUN_TYPE
            int angleBracketDepth = 0;
            for (int j = assignIndex - 2; j >= 0; j--) {
                Token t = tokens.get(j);
                if (t.type == TokenType.GT) {
                    angleBracketDepth++;
                } else if (t.type == TokenType.LT) {
                    angleBracketDepth--;
                    if (angleBracketDepth == 0 && j > 0 && tokens.get(j - 1).type == TokenType.FUN_TYPE) {
                        isFunTypeDeclaration = true;
                        break;
                    }
                }
            }
        }

        // 检查是否是普通类型声明
        boolean isNormalTypeDeclaration = assignIndex == 2 &&
            tokens.get(0).type == TokenType.IDENTIFIER &&
            tokens.get(1).type == TokenType.IDENTIFIER;

        boolean hasTypeDeclaration = isFunTypeDeclaration || isNormalTypeDeclaration;

        if (hasTypeDeclaration) {
            // 类型 + 变量名 + 赋值符号需要空格
            for (int i = 0; i <= assignIndex; i++) {
                sb.append(tokens.get(i).value).append(" ");
            }
            // 后面的是表达式
            for (int i = assignIndex + 1; i < tokens.size(); i++) {
                sb.append(tokens.get(i).value);
            }
        } else {
            // 变量名 + 赋值符号需要空格
            for (int i = 0; i <= assignIndex; i++) {
                sb.append(tokens.get(i).value).append(" ");
            }
            // 后面的是表达式
            for (int i = assignIndex + 1; i < tokens.size(); i++) {
                sb.append(tokens.get(i).value);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return printExp(getValue());
    }

    /**
     * 按逗号分割参数表达式，支持嵌套括号
     * 例如：a, b, c 会分割成 [a], [b], [c]
     *
     * @param tokens Token列表
     * @return 分割后的参数表达式列表
     */
    private List<List<Token>> splitByComma(List<Token> tokens) {
        List<List<Token>> result = new ArrayList<>();
        if (tokens.isEmpty()) {
            return result;
        }

        List<Token> current = new ArrayList<>();
        int depth = 0; // 括号嵌套深度

        for (Token token : tokens) {
            if (token.type == TokenType.LPAREN) {
                depth++;
                current.add(token);
            } else if (token.type == TokenType.RPAREN) {
                depth--;
                current.add(token);
            } else if (token.type == TokenType.COMMA && depth == 0) {
                // 逗号且不在括号内，分割
                if (!current.isEmpty()) {
                    result.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(token);
            }
        }

        // 添加最后一个参数
        if (!current.isEmpty()) {
            result.add(current);
        }

        return result;
    }

    /**
     * 根据参数类型查找匹配的构造函数
     * 支持类型兼容性检查（如 int 可以匹配 Integer，null 可以匹配任何引用类型）
     *
     * @param clazz 目标类
     * @param argTypes 参数类型列表（可能包含 null 表示 null 值）
     * @return 匹配的构造函数
     * @throws NoSuchMethodException 如果找不到匹配的构造函数
     */
    private Constructor<?> findMatchingConstructor(Class<?> clazz, List<Class<?>> argTypes)
            throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();

            // 参数数量必须匹配
            if (paramTypes.length != argTypes.size()) {
                continue;
            }

            // 检查每个参数类型是否兼容
            boolean match = true;
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> expectedType = paramTypes[i];
                Class<?> actualType = argTypes.get(i);

                if (actualType == null) {
                    // null 值只能匹配引用类型，不能匹配基本类型
                    if (expectedType.isPrimitive()) {
                        match = false;
                        break;
                    }
                } else {
                    // 检查类型兼容性
                    if (!isTypeCompatible(actualType, expectedType)) {
                        match = false;
                        break;
                    }
                }
            }

            if (match) {
                return constructor;
            }
        }

        throw new NoSuchMethodException("找不到匹配的构造函数，参数类型: " + argTypes);
    }

    /**
     * 检查类型是否兼容
     * 支持基本类型和包装类型的自动转换
     *
     * @param actualType 实际类型
     * @param expectedType 期望类型
     * @return 是否兼容
     */
    private boolean isTypeCompatible(Class<?> actualType, Class<?> expectedType) {
        // 如果类型完全相同
        if (expectedType.equals(actualType)) {
            return true;
        }

        // 处理基本类型和包装类型的兼容性
        if (expectedType.isPrimitive()) {
            // 基本类型需要匹配对应的包装类型
            return isWrapperType(actualType, expectedType);
        }

        if (actualType.isPrimitive()) {
            // 实际是基本类型，期望是包装类型
            return isWrapperType(expectedType, actualType);
        }

        // 检查是否是子类
        return expectedType.isAssignableFrom(actualType);
    }

    /**
     * 检查包装类型是否对应指定的基本类型
     *
     * @param wrapperType 包装类型
     * @param primitiveType 基本类型
     * @return 是否对应
     */
    private boolean isWrapperType(Class<?> wrapperType, Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return wrapperType == Integer.class;
        }
        if (primitiveType == long.class) {
            return wrapperType == Long.class;
        }
        if (primitiveType == double.class) {
            return wrapperType == Double.class;
        }
        if (primitiveType == float.class) {
            return wrapperType == Float.class;
        }
        if (primitiveType == boolean.class) {
            return wrapperType == Boolean.class;
        }
        if (primitiveType == byte.class) {
            return wrapperType == Byte.class;
        }
        if (primitiveType == short.class) {
            return wrapperType == Short.class;
        }
        if (primitiveType == char.class) {
            return wrapperType == Character.class;
        }
        return false;
    }


}
