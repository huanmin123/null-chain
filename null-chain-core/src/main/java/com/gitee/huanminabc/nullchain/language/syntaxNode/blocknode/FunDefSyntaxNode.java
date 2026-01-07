package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.utils.KeywordUtil;
import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunTypeInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.internal.ParseScopeTracker;
import com.gitee.huanminabc.nullchain.language.syntaxNode.BlockSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数定义语法节点
 * 
 * <p>支持函数定义语法：fun 函数名称(参数列表)返回值类型列表{函数体}</p>
 * <p>示例：fun add(int a, int b)Integer{ return a + b }</p>
 * <p>示例：fun getName(String name, int age)String,Integer{ return name, age }</p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class FunDefSyntaxNode extends BlockSyntaxNode {

    public FunDefSyntaxNode() {
        super(SyntaxNodeType.FUN_DEF_EXP);
    }

    public FunDefSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.FUN;
    }

    /**
     * 跳到函数定义结束位置获取结束下标
     * 函数定义的格式：fun 函数名(参数列表)返回值类型列表{函数体}
     */
    private int skipFunDefEnd(List<Token> tokens, int i) {
        // 找到函数体的大括号结束位置
        return BlockSyntaxNode.skipBlockEnd(tokens, i, false);
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.FUN) {
                // 获取语句结束下标, 用于截取和删除
                int endIndex = skipFunDefEnd(tokens, i);
                // 截取函数定义的标记序列
                List<Token> funTokens = new ArrayList<>(tokens.subList(i, endIndex));
                // 如果是空那么就是语法有问题
                if (funTokens.isEmpty()) {
                    // 检查是否因为使用了关键字作为函数名
                    if (i + 1 < tokensSize) {
                        Token nextToken = tokens.get(i + 1);
                        if (KeywordUtil.isForbidKeyword(nextToken.value)) {
                            throw new NfSyntaxException(
                                nextToken.getLine(),
                                "函数名不能使用关键字",
                                String.format("'%s' 是系统关键字，不能用作函数名", nextToken.value),
                                nextToken.value,
                                "请使用其他函数名"
                            );
                        }
                    }
                    throw new NfSyntaxException(
                        token.getLine(),
                        "函数定义语法错误",
                        "函数定义的tokens为空，无法解析",
                        token.getValue(),
                        "请检查函数定义的语法格式：fun 函数名(参数列表)返回值类型列表{函数体}"
                    );
                }
                // 删除已解析的tokens
                tokens.subList(i, endIndex).clear();

                FunDefSyntaxNode funDefNode = new FunDefSyntaxNode(SyntaxNodeType.FUN_DEF_EXP);
                funDefNode.setValue(funTokens);
                funDefNode.setLine(token.getLine());
                // 构建子节点
                if (!buildChildStatement(funDefNode)) {
                    return false;
                }
                syntaxNodeList.add(funDefNode);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建函数定义的子节点
     * 
     * <p>此方法会解析函数定义的tokens，提取函数名、参数列表、返回值类型列表和函数体。</p>
     * 
     * @param syntaxNode 函数定义节点（会被修改）
     * @return 如果成功构建返回 true，否则返回 false
     */
    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        List<Token> originalTokenList = syntaxNode.getValue();
        if (originalTokenList == null || originalTokenList.isEmpty()) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义tokens为空",
                "函数定义的tokens列表不能为空",
                "",
                "请检查函数定义的语法格式"
            );
        }

        // 创建tokens的副本用于解析，保留原始tokens给run方法使用
        List<Token> tokenList = new ArrayList<>(originalTokenList);

        // 去掉FUN关键字
        tokenList.remove(0);

        // 解析函数名（应该是IDENTIFIER）
        if (tokenList.isEmpty() || tokenList.get(0).type != TokenType.IDENTIFIER) {
            // 检查是否因为使用了关键字而导致不是标识符
            if (!tokenList.isEmpty()) {
                Token nameToken = tokenList.get(0);
                // 直接检查 token 类型是否是已知的关键字类型
                if (nameToken.type == TokenType.IF || nameToken.type == TokenType.FOR ||
                    nameToken.type == TokenType.WHILE || nameToken.type == TokenType.RETURN ||
                    nameToken.type == TokenType.GLOBAL || nameToken.type == TokenType.FUN ||
                    nameToken.type == TokenType.ELSE || nameToken.type == TokenType.SWITCH ||
                    nameToken.type == TokenType.CASE || nameToken.type == TokenType.DEFAULT ||
                    nameToken.type == TokenType.BREAK || nameToken.type == TokenType.CONTINUE ||
                    nameToken.type == TokenType.TRUE || nameToken.type == TokenType.FALSE ||
                    nameToken.type == TokenType.AND || nameToken.type == TokenType.OR ||
                    nameToken.type == TokenType.NEW || nameToken.type == TokenType.INSTANCEOF ||
                    nameToken.type == TokenType.ECHO || nameToken.type == TokenType.EXPORT ||
                    nameToken.type == TokenType.IMPORT || nameToken.type == TokenType.VAR) {
                    throw new NfSyntaxException(
                        nameToken.line,
                        "函数名不能使用关键字",
                        String.format("'%s' 是系统关键字，不能用作函数名", nameToken.value),
                        nameToken.value,
                        "请使用其他函数名"
                    );
                }
                // 对于其他情况，也检查值是否是关键字
                if (KeywordUtil.isForbidKeyword(nameToken.value)) {
                    throw new NfSyntaxException(
                        nameToken.line,
                        "函数名不能使用关键字",
                        String.format("'%s' 是系统关键字，不能用作函数名", nameToken.value),
                        nameToken.value,
                        "请使用其他函数名"
                    );
                }
            }
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义语法错误",
                "函数名必须是标识符",
                TokenUtil.mergeToken(tokenList).toString(),
                "请检查函数定义的语法格式：fun 函数名(参数列表)返回值类型列表{函数体}"
            );
        }
        Token functionNameToken = tokenList.get(0);
        String functionName = functionNameToken.value;

        // 检查函数名是否是关键字
        if (KeywordUtil.isForbidKeyword(functionName)) {
            throw new NfSyntaxException(
                functionNameToken.line,
                "函数名不能使用关键字",
                String.format("'%s' 是系统关键字，不能用作函数名", functionName),
                functionName,
                "请使用其他函数名"
            );
        }

        // 解析时检查函数名重复
        ParseScopeTracker tracker = NfSynta.getCurrentTracker();
        if (tracker != null) {
            String syntaxStr = TokenUtil.mergeToken(originalTokenList).toString();
            tracker.checkDuplicateFunction(functionName, functionNameToken.line, syntaxStr);
            tracker.addFunction(functionName, functionNameToken.line);
        }
        
        tokenList.remove(0); // 移除函数名

        // 解析参数列表：找到左括号和右括号
        if (tokenList.isEmpty() || tokenList.get(0).type != TokenType.LPAREN) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义语法错误",
                "函数名后必须是左括号'('",
                TokenUtil.mergeToken(tokenList).toString(),
                "请检查函数定义的语法格式：fun 函数名(参数列表)返回值类型列表{函数体}"
            );
        }
        tokenList.remove(0); // 移除左括号

        // 解析参数列表，直到找到右括号
        List<FunDefInfo.FunParameter> parameters = new ArrayList<>();
        List<Token> paramTokens = new ArrayList<>();
        int parenDepth = 1; // 括号深度，用于处理嵌套括号
        int paramEndIndex = -1;
        
        for (int i = 0; i < tokenList.size(); i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LPAREN) {
                parenDepth++;
            } else if (token.type == TokenType.RPAREN) {
                parenDepth--;
                if (parenDepth == 0) {
                    paramEndIndex = i;
                    break;
                }
            }
            if (parenDepth > 0) {
                paramTokens.add(token);
            }
        }

        if (paramEndIndex == -1) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义语法错误",
                "参数列表缺少右括号')'",
                TokenUtil.mergeToken(tokenList).toString(),
                "请检查函数定义的语法格式"
            );
        }

        // 解析参数列表
        if (!paramTokens.isEmpty()) {
            // 直接在参数列表中检查关键字（作为额外保护）
            // 参数格式：类型 参数名，类型 参数名，...
            // 类型是 IDENTIFIER（如 int, String 等），参数名可能是 IDENTIFIER 或关键字
            // 遍历所有 tokens，检查是否有关键字
            for (int i = 0; i < paramTokens.size(); i++) {
                Token token = paramTokens.get(i);
                // 跳过已知的非参数名 token 类型
                // IDENTIFIER 可能是类型名（在偶数位置）或普通参数名（在奇数位置）
                // INTEGER, FLOAT 是数字字面量
                // COMMA, DOT, DOT2 是分隔符
                if (token.type == TokenType.IDENTIFIER || token.type == TokenType.INTEGER ||
                    token.type == TokenType.FLOAT || token.type == TokenType.COMMA ||
                    token.type == TokenType.DOT || token.type == TokenType.DOT2) {
                    // 对于 IDENTIFIER，还需要检查是否是关键字
                    if (token.type == TokenType.IDENTIFIER && KeywordUtil.isForbidKeyword(token.value)) {
                        throw new NfSyntaxException(
                            syntaxNode.getLine(),
                            "参数名不能使用关键字",
                            String.format("'%s' 是系统关键字，不能用作参数名", token.value),
                            token.value,
                            "请使用其他参数名"
                        );
                    }
                    continue;
                }
                // 其他类型的 token（如 IF, FOR, WHILE 等关键字）需要检查
                if (KeywordUtil.isForbidKeyword(token.value)) {
                    throw new NfSyntaxException(
                        syntaxNode.getLine(),
                        "参数名不能使用关键字",
                        String.format("'%s' 是系统关键字，不能用作参数名", token.value),
                        token.value,
                        "请使用其他参数名"
                    );
                }
            }
            parseParameters(paramTokens, parameters, syntaxNode.getLine());
        }

        // 注意：函数参数允许与全局变量重名（遮蔽），不需要检查
        // 只有函数体内声明的变量才需要检查与全局变量是否重复

        // 移除参数列表的tokens
        tokenList.subList(0, paramEndIndex + 1).clear();

        // 解析返回值类型列表（在右括号后，大括号前）
        List<String> returnTypes = new ArrayList<>();
        List<Token> returnTypeTokens = new ArrayList<>();
        int braceIndex = -1;
        
        for (int i = 0; i < tokenList.size(); i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LBRACE) {
                braceIndex = i;
                break;
            }
            returnTypeTokens.add(token);
        }

        if (braceIndex == -1) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义语法错误",
                "函数定义缺少左大括号'{'",
                TokenUtil.mergeToken(tokenList).toString(),
                "请检查函数定义的语法格式"
            );
        }

        // 解析返回值类型列表（用逗号分割）
        if (!returnTypeTokens.isEmpty()) {
            parseReturnTypes(returnTypeTokens, returnTypes, syntaxNode.getLine());
        }

        // 移除返回值类型列表的tokens
        tokenList.subList(0, braceIndex + 1).clear();

        // 解析函数体（剩余部分，去掉最后的右大括号）
        if (tokenList.isEmpty() || tokenList.get(tokenList.size() - 1).type != TokenType.RBRACE) {
            throw new NfSyntaxException(
                syntaxNode.getLine(),
                "函数定义语法错误",
                "函数定义缺少右大括号'}'",
                TokenUtil.mergeToken(tokenList).toString(),
                "请检查函数定义的语法格式"
            );
        }
        tokenList.remove(tokenList.size() - 1); // 移除右大括号

        // 构建函数体语法节点（用于子节点）
        // 函数体创建新作用域（重用之前获取的tracker）
        if (tracker != null) {
            tracker.enterScope(ParseScopeTracker.ScopeType.FUNCTION); // 进入函数体作用域
            // 将函数参数添加到函数作用域的变量列表中，这样函数体内不能声明与参数同名的变量
            for (FunDefInfo.FunParameter param : parameters) {
                tracker.addVariable(param.getName(), syntaxNode.getLine());
            }
        }

        try {
            List<Token> bodyTokens = new ArrayList<>(tokenList);
            // 传入tracker，避免创建新的tracker
            List<SyntaxNode> bodyNodes = NfSynta.buildMainStatement(bodyTokens, tracker);
            
            // 将函数体的子节点添加到当前节点
            for (SyntaxNode bodyNode : bodyNodes) {
                syntaxNode.addChild(bodyNode);
            }
        } finally {
            if (tracker != null) {
                tracker.exitScope(); // 退出函数体作用域
            }
        }

        // 注意：不清除父节点的value，保留原始tokens用于run方法中解析函数定义信息
        // 函数定义信息（函数名、参数列表、返回值类型）将在run方法中解析并存储到context
        
        return true;
    }

    /**
     * 清理父节点的value（未使用，保留用于未来可能的需求）
     */
    @SuppressWarnings("unused")
    private void clearParentNodeValue(SyntaxNode syntaxNode) {
        syntaxNode.setValue(null);
    }

    /**
     * 解析参数列表
     * 格式：类型1 参数名1, 类型2 参数名2, 类型3... 参数名3
     * 支持多行参数定义和 Fun<> 类型
     */
    private void parseParameters(List<Token> paramTokens, List<FunDefInfo.FunParameter> parameters, int line) {
        // 去掉注释
        SyntaxNodeUtil.removeComments(paramTokens);

        if (paramTokens.isEmpty()) {
            return;
        }

        List<Token> currentParam = new ArrayList<>();
        int angleBracketDepth = 0; // 跟踪尖括号深度，用于处理 Fun<> 类型

        for (Token token : paramTokens) {
            // 跳过换行token
            if (token.type == TokenType.LINE_END) {
                continue;
            }

            // 更新尖括号深度
            if (token.type == TokenType.LT) {
                angleBracketDepth++;
            } else if (token.type == TokenType.GT) {
                angleBracketDepth--;
            }

            // 只有在不在 Fun<> 类型内部时才按逗号分割
            if (token.type == TokenType.COMMA && angleBracketDepth == 0) {
                if (!currentParam.isEmpty()) {
                    FunDefInfo.FunParameter param = parseSingleParameter(currentParam, line);
                    parameters.add(param);
                    currentParam.clear();
                }
            } else {
                currentParam.add(token);
            }
        }

        // 处理最后一个参数
        if (!currentParam.isEmpty()) {
            FunDefInfo.FunParameter param = parseSingleParameter(currentParam, line);
            parameters.add(param);
        }
    }

    /**
     * 解析单个参数
     * 格式：类型 参数名 或 类型... 参数名（可变参数）
     */
    private FunDefInfo.FunParameter parseSingleParameter(List<Token> tokens, int line) {
        if (tokens.isEmpty()) {
            throw new NfSyntaxException(
                line,
                "函数参数语法错误",
                "参数格式错误，参数为空",
                TokenUtil.mergeToken(tokens).toString(),
                "请检查参数格式"
            );
        }

        // 检查是否是可变参数（类型... 参数名）
        // tokenizer 将 ... 处理为 DOT2(..) + DOT(.)，而不是三个 DOT
        boolean varArgs = false;
        int typeEndIndex = 0;

        // 查找类型结束位置，检查是否有 ... 模式（IDENTIFIER + DOT2 + DOT）
        for (int i = 0; i < tokens.size() - 2; i++) {
            if (tokens.get(i).type == TokenType.IDENTIFIER &&
                tokens.get(i + 1).type == TokenType.DOT2 &&
                tokens.get(i + 2).type == TokenType.DOT) {
                // 找到 ... 模式（实际是 DOT2 + DOT）
                varArgs = true;
                typeEndIndex = i;
                break;
            }
        }

        // 获取类型和参数名
        String type;
        Token nameToken;
        int nameIndex;

        if (varArgs) {
            // 可变参数：类型... 参数名
            if (tokens.size() < 4) {
                throw new NfSyntaxException(
                    line,
                    "函数参数语法错误",
                    "可变参数格式错误，应为：类型... 参数名",
                    TokenUtil.mergeToken(tokens).toString(),
                    "请检查参数格式"
                );
            }
            // 检查类型部分是否是 Fun<> 类型
            if (tokens.get(typeEndIndex).type == TokenType.FUN_TYPE) {
                type = extractFunType(tokens, typeEndIndex);
                nameIndex = typeEndIndex + 3; // 跳过类型和 DOT2+DOT
            } else {
                type = tokens.get(typeEndIndex).value;
                nameIndex = typeEndIndex + 3;
            }
            nameToken = tokens.get(nameIndex);
        } else {
            // 普通参数：类型 参数名（或 Fun<...> 参数名）
            if (tokens.size() < 2) {
                throw new NfSyntaxException(
                    line,
                    "函数参数语法错误",
                    "参数格式错误，应为：类型 参数名",
                    TokenUtil.mergeToken(tokens).toString(),
                    "请检查参数格式"
                );
            }
            // 检查类型部分是否是 Fun<> 类型
            if (tokens.get(0).type == TokenType.FUN_TYPE) {
                type = extractFunType(tokens, 0);
                // 找到 Fun<> 类型结束位置，参数名在之后
                int funTypeEnd = findFunTypeEnd(tokens);
                if (funTypeEnd == -1) {
                    throw new NfSyntaxException(
                        line,
                        "函数参数语法错误",
                        "Fun<> 类型格式错误，找不到匹配的 '>'",
                        TokenUtil.mergeToken(tokens).toString(),
                        "请检查 Fun<> 类型格式"
                    );
                }
                nameIndex = funTypeEnd + 1;
                if (nameIndex >= tokens.size()) {
                    throw new NfSyntaxException(
                        line,
                        "函数参数语法错误",
                        "Fun<> 类型后面缺少参数名",
                        TokenUtil.mergeToken(tokens).toString(),
                        "请检查参数格式"
                    );
                }
                nameToken = tokens.get(nameIndex);
            } else {
                // 普通类型：类型是第一个 token，参数名是第二个 token
                type = tokens.get(0).value;
                nameIndex = 1;
                nameToken = tokens.get(nameIndex);
            }
        }

        String name = nameToken.value;

        // 检查参数名 token 的类型是否是 IDENTIFIER（如果不是，说明是关键字）
        // Fun_TYPE 也可以作为参数名的一部分（虽然不推荐）
        if (nameToken.type != TokenType.IDENTIFIER && nameToken.type != TokenType.FUN_TYPE) {
            throw new NfSyntaxException(
                line,
                "参数名不能使用关键字",
                String.format("'%s' 是系统关键字，不能用作参数名", name),
                name,
                "请使用其他参数名"
            );
        }

        // 检查参数名是否是关键字（通过 token 的值判断）
        if (KeywordUtil.isForbidKeyword(name)) {
            throw new NfSyntaxException(
                line,
                "参数名不能使用关键字",
                String.format("'%s' 是系统关键字，不能用作参数名", name),
                name,
                "请使用其他参数名"
            );
        }

        FunDefInfo.FunParameter param = new FunDefInfo.FunParameter();
        param.setName(name);
        param.setType(type);
        param.setVarArgs(varArgs);

        return param;
    }

    /**
     * 提取 Fun<> 类型字符串
     * 例如：Fun<Integer, Integer : Integer>
     */
    private String extractFunType(List<Token> tokens, int startIndex) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (int i = startIndex; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            sb.append(t.value);
            if (t.type == TokenType.LT) {
                depth++;
            } else if (t.type == TokenType.GT) {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 查找 Fun<> 类型的结束位置（最后一个 > 的位置）
     */
    private int findFunTypeEnd(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.get(0).type != TokenType.FUN_TYPE) {
            return -1;
        }
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.type == TokenType.LT) {
                depth++;
            } else if (t.type == TokenType.GT) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 解析返回值类型列表
     * 格式：类型1,类型2（多个返回值用逗号分割）
     * 支持Fun<>类型
     */
    private void parseReturnTypes(List<Token> returnTypeTokens, List<String> returnTypes, int line) {
        // 去掉注释
        SyntaxNodeUtil.removeComments(returnTypeTokens);

        if (returnTypeTokens.isEmpty()) {
            return;
        }

        List<Token> currentType = new ArrayList<>();
        int angleBracketDepth = 0; // 跟踪尖括号深度，用于处理 Fun<> 类型

        for (Token token : returnTypeTokens) {
            // 跳过换行token
            if (token.type == TokenType.LINE_END) {
                continue;
            }

            // 更新尖括号深度
            if (token.type == TokenType.LT) {
                angleBracketDepth++;
            } else if (token.type == TokenType.GT) {
                angleBracketDepth--;
            }

            // 只有在不在 Fun<> 类型内部时才按逗号分割
            if (token.type == TokenType.COMMA && angleBracketDepth == 0) {
                if (!currentType.isEmpty()) {
                    String type = TokenUtil.mergeToken(currentType).toString().trim();
                    if (!type.isEmpty()) {
                        returnTypes.add(type);
                    }
                    currentType.clear();
                }
            } else {
                currentType.add(token);
            }
        }

        // 处理最后一个类型
        if (!currentType.isEmpty()) {
            String type = TokenUtil.mergeToken(currentType).toString().trim();
            if (!type.isEmpty()) {
                returnTypes.add(type);
            }
        }
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        // 函数定义只在定义时执行一次，不重复定义
        // buildChildStatement 已经在解析阶段构建了函数体的子节点
        // 这里只需要解析函数签名（函数名、参数、返回类型）并存储到 context

        // 获取原始tokens
        List<Token> tokenList = syntaxNode.getValue();
        if (tokenList == null || tokenList.isEmpty()) {
            throw new NfException("Line:{} ,函数定义的tokens为空，无法解析",
                syntaxNode.getLine());
        }

        // 如果函数已经定义，直接返回（防止递归函数重复定义导致的问题）
        // 提前解析函数名以检查是否已定义
        List<Token> tokens = new ArrayList<>(tokenList);
        if (tokens.isEmpty() || tokens.get(0).type != TokenType.FUN) {
            throw new NfException("Line:{} ,函数定义语法错误，缺少fun关键字",
                syntaxNode.getLine());
        }
        tokens.remove(0); // 移除FUN关键字

        if (tokens.isEmpty() || tokens.get(0).type != TokenType.IDENTIFIER) {
            throw new NfException("Line:{} ,函数定义语法错误，函数名必须是标识符",
                syntaxNode.getLine());
        }
        String functionName = tokens.get(0).value;
        tokens.remove(0); // 移除函数名

        // 检查函数是否已经定义，如果已定义则跳过
        if (context.hasFunction(functionName)) {
            return;
        }

        // 重置tokens用于完整解析
        tokens = new ArrayList<>(tokenList);
        tokens.remove(0); // 移除FUN关键字
        tokens.remove(0); // 移除函数名

        // 解析参数列表
        if (tokens.isEmpty() || tokens.get(0).type != TokenType.LPAREN) {
            throw new NfException("Line:{} ,函数定义语法错误，函数名后必须是左括号'(",
                syntaxNode.getLine());
        }
        tokens.remove(0); // 移除左括号

        List<FunDefInfo.FunParameter> parameters = new ArrayList<>();
        List<Token> paramTokens = new ArrayList<>();
        int parenDepth = 1;
        int paramEndIndex = -1;
        
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.LPAREN) {
                parenDepth++;
            } else if (token.type == TokenType.RPAREN) {
                parenDepth--;
                if (parenDepth == 0) {
                    paramEndIndex = i;
                    break;
                }
            }
            if (parenDepth > 0) {
                paramTokens.add(token);
            }
        }

        if (paramEndIndex == -1) {
            throw new NfException("Line:{} ,函数定义语法错误，参数列表缺少右括号')'",
                syntaxNode.getLine());
        }

        // 解析参数列表
        if (!paramTokens.isEmpty()) {
            // 直接在参数列表中检查关键字（作为额外保护）
            // 参数格式：类型 参数名，类型 参数名，...
            // 类型是 IDENTIFIER（如 int, String 等），参数名可能是 IDENTIFIER 或关键字
            // 遍历所有 tokens，检查是否有关键字
            for (int i = 0; i < paramTokens.size(); i++) {
                Token token = paramTokens.get(i);
                // 跳过已知的非参数名 token 类型
                // IDENTIFIER 可能是类型名（在偶数位置）或普通参数名（在奇数位置）
                // INTEGER, FLOAT 是数字字面量
                // COMMA, DOT, DOT2 是分隔符
                if (token.type == TokenType.IDENTIFIER || token.type == TokenType.INTEGER ||
                    token.type == TokenType.FLOAT || token.type == TokenType.COMMA ||
                    token.type == TokenType.DOT || token.type == TokenType.DOT2) {
                    // 对于 IDENTIFIER，还需要检查是否是关键字
                    if (token.type == TokenType.IDENTIFIER && KeywordUtil.isForbidKeyword(token.value)) {
                        throw new NfSyntaxException(
                            syntaxNode.getLine(),
                            "参数名不能使用关键字",
                            String.format("'%s' 是系统关键字，不能用作参数名", token.value),
                            token.value,
                            "请使用其他参数名"
                        );
                    }
                    continue;
                }
                // 其他类型的 token（如 IF, FOR, WHILE 等关键字）需要检查
                if (KeywordUtil.isForbidKeyword(token.value)) {
                    throw new NfSyntaxException(
                        syntaxNode.getLine(),
                        "参数名不能使用关键字",
                        String.format("'%s' 是系统关键字，不能用作参数名", token.value),
                        token.value,
                        "请使用其他参数名"
                    );
                }
            }
            parseParameters(paramTokens, parameters, syntaxNode.getLine());
        }

        // 移除参数列表的tokens
        tokens.subList(0, paramEndIndex + 1).clear();

        // 解析返回值类型列表
        List<String> returnTypes = new ArrayList<>();
        List<Token> returnTypeTokens = new ArrayList<>();
        int braceIndex = -1;
        
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.LBRACE) {
                braceIndex = i;
                break;
            }
            returnTypeTokens.add(token);
        }

        if (braceIndex == -1) {
            throw new NfException("Line:{} ,函数定义语法错误，函数定义缺少左大括号'{'", 
                syntaxNode.getLine());
        }

        // 解析返回值类型列表
        if (!returnTypeTokens.isEmpty()) {
            parseReturnTypes(returnTypeTokens, returnTypes, syntaxNode.getLine());
        }

        // 移除返回值类型列表的tokens
        tokens.subList(0, braceIndex + 1).clear();

        // 移除最后的右大括号
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == TokenType.RBRACE) {
            tokens.remove(tokens.size() - 1);
        }

        // 从子节点列表获取函数体（buildChildStatement 在解析阶段已经构建了）
        List<SyntaxNode> bodyNodes = null;
        if (syntaxNode instanceof SyntaxNodeAbs) {
            SyntaxNodeAbs nodeAbs = (SyntaxNodeAbs) syntaxNode;
            bodyNodes = nodeAbs.getChildSyntaxNodeList();
        }

        // 确保bodyNodes不为null
        if (bodyNodes == null) {
            bodyNodes = new ArrayList<>();
        }

        // 创建函数定义信息
        FunDefInfo funDefInfo = new FunDefInfo();
        funDefInfo.setFunctionName(functionName);
        funDefInfo.setParameters(parameters);
        funDefInfo.setReturnTypes(returnTypes);
        funDefInfo.setBodyNodes(bodyNodes);

        // 将函数定义存储到context中
        context.addFunction(functionName, funDefInfo);

        // 创建函数引用变量，让函数名可以在表达式中被引用（如 return add）
        // 构建 FunType 信息
        FunTypeInfo funTypeInfo = new FunTypeInfo();
        List<String> paramTypes = new ArrayList<>();
        for (FunDefInfo.FunParameter param : parameters) {
            paramTypes.add(param.getType());
        }
        funTypeInfo.setParameterTypes(paramTypes);

        // 设置返回类型
        if (returnTypes.size() == 1) {
            funTypeInfo.setReturnType(returnTypes.get(0));
        } else {
            // 多返回值，使用 List 作为返回类型
            funTypeInfo.setReturnType("List");
        }

        // 创建 FunRefInfo 并注册为变量
        FunRefInfo funRef = FunRefInfo.createFunRef(functionName, funDefInfo, funTypeInfo);

        // 将函数引用添加到当前作用域，使其可以作为值在表达式中使用
        context.getCurrentScope().addVariable(new NfVariableInfo(
            functionName,
            funRef,
            FunRefInfo.class,
            true,  // isFinal
            funRef
        ));

        // 同时注册到 FunRef Map 中
        context.addFunRef(functionName, funRef);
    }
}

