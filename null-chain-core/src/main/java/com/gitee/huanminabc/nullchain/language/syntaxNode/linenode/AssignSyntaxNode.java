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
     * 支持两种格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
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
        // 检查是否是已存在变量的重新赋值：IDENTIFIER ASSIGN ...
        if (tokens.get(0).type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.ASSIGN) {
            return true;
        }
        // 检查是否是带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ...
        return size >= 3 && tokens.get(0).type == TokenType.IDENTIFIER &&
                tokens.get(1).type == TokenType.IDENTIFIER &&
                tokens.get(2).type == TokenType.ASSIGN;
    }


    /**
     * 构建赋值语句
     * 支持两种格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
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
                boolean hasTypeDeclaration = i >= 2 && 
                    tokens.get(i - 2).type == TokenType.IDENTIFIER && 
                    tokens.get(i - 1).type == TokenType.IDENTIFIER;
                
                int startIndex;
                if (hasTypeDeclaration) {
                    // 带类型声明的赋值：下标向前移动2位（类型 + 变量名）
                    startIndex = i - 2;
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
                int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, startIndex);
                //截取赋值语句的标记序列,不包含LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(startIndex, endIndex));
                //删除已经解析的标记
                tokens.subList(startIndex, endIndex).clear();

                //拿到变量名称（根据是否有类型声明，位置不同）
                Token varName = hasTypeDeclaration ? newToken.get(1) : newToken.get(0);
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
     * 支持两种格式：
     * 1. 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN ... (例如：Integer factorial = 1)
     * 2. 已存在变量的重新赋值：IDENTIFIER ASSIGN ... (例如：factorial = factorial * i)
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
        boolean hasTypeDeclaration = valueTokens.size() >= 3 && 
            valueTokens.get(0).type == TokenType.IDENTIFIER && 
            valueTokens.get(1).type == TokenType.IDENTIFIER && 
            valueTokens.get(2).type == TokenType.ASSIGN;
        
        String importType;
        String varName;
        List<Token> expTokens;
        
        if (hasTypeDeclaration) {
            // 带类型声明的赋值：IDENTIFIER IDENTIFIER ASSIGN xxx
            //获取类型
            Token token = valueTokens.get(0);
            String type = token.value;
            //转化为java类型
            importType = context.getImportType(type);
            if (importType == null) {
                throw new NfException("Line:{} ,未找到类型 {} , syntax: {}",token.line, type, syntaxNode);
            }
            //获取赋值的变量名
            varName = valueTokens.get(1).value;
            //获取赋值的表达式
            expTokens = valueTokens.subList(3, valueTokens.size());
        } else {
            // 已存在变量的重新赋值：IDENTIFIER ASSIGN xxx
            //获取变量名
            varName = valueTokens.get(0).value;
            //获取赋值的表达式
            expTokens = valueTokens.subList(2, valueTokens.size());
            //从上下文获取变量的类型
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
                
                // 注意：重复变量检查已在解析阶段完成，此处不再检查
                // 获取无参构造函数
                Constructor<?> constructor = actualType.getConstructor();
                Object o = constructor.newInstance();
                //将对象放入上下文，类型保持为声明的接口类型，这样类型检查时可以支持所有实现类
                currentScope.addVariable(new NfVariableInfo(varName, o, declaredType));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
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
            } else {
                // 已存在变量的重新赋值：更新变量所在的作用域，而不是当前作用域
                // 这样可以确保在循环中对父作用域变量的修改能够持久化
                NfContextScope variableScope = context.findVariableScope(varName);
                if (variableScope != null) {
                    System.out.println("[DEBUG] Update variable " + varName + " in scope " + variableScope.getScopeId() + " to value " + arithmetic);
                    variableScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));
                } else {
                    // 如果找不到变量所在的作用域（理论上不应该发生），则添加到当前作用域
                    System.out.println("[DEBUG] WARNING: Variable " + varName + " scope not found, adding to current scope " + currentScope.getScopeId());
                    currentScope.addVariable(new NfVariableInfo(varName, arithmetic, declaredType));
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
        boolean hasTypeDeclaration = tokens.size() >= 3 && 
            tokens.get(0).type == TokenType.IDENTIFIER && 
            tokens.get(1).type == TokenType.IDENTIFIER && 
            tokens.get(2).type == TokenType.ASSIGN;
        
        if (hasTypeDeclaration) {
            //前3个是类型,变量名,赋值符号 需要空格
            for (int i = 0; i < 3; i++) {
                sb.append(tokens.get(i).value).append(" ");
            }
            //后面的是表达式
            for (int i = 3; i < tokens.size(); i++) {
                sb.append(tokens.get(i).value);
            }
        } else {
            //前2个是变量名,赋值符号 需要空格
            for (int i = 0; i < 2; i++) {
                sb.append(tokens.get(i).value).append(" ");
            }
            //后面的是表达式
            for (int i = 2; i < tokens.size(); i++) {
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
