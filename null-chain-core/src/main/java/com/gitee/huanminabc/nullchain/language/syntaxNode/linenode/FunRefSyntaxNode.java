package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.*;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数引用语法节点
 * 处理函数引用赋值:
 * 1. Fun funVar = functionName (省略类型，自动推导)
 * 2. Fun<T1, T2 : R> funVar = functionName (显式类型声明)
 *
 * @author huanmin
 * @date 2025/01/06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FunRefSyntaxNode extends LineSyntaxNode {
    public FunRefSyntaxNode() {
        super(SyntaxNodeType.FUN_REF_EXP);
    }

    @Override
    protected TokenType getTargetTokenType() {
        // FunRefSyntaxNode 重写了 analystToken 方法，此方法不会被调用
        return null;
    }

    /**
     * 识别函数引用:
     * 1. Fun varName = functionName (省略类型)
     * 2. Fun<类型列表> varName = functionName (显式类型)
     */
    @Override
    public boolean analystToken(List<Token> tokens) {
        if (tokens == null || tokens.size() < 4) {
            return false;
        }

        // 检查是否有 Fun 开头
        Token first = tokens.get(0);
        if (first.type != TokenType.FUN_TYPE) {
            return false;
        }

        // 模式1: Fun IDENTIFIER ASSIGN IDENTIFIER (省略类型)
        if (tokens.size() >= 4 &&
            tokens.get(0).type == TokenType.FUN_TYPE &&
            tokens.get(1).type == TokenType.IDENTIFIER &&
            tokens.get(2).type == TokenType.ASSIGN &&
            tokens.get(3).type == TokenType.IDENTIFIER) {
            return true;
        }

        // 模式2: Fun<...> IDENTIFIER ASSIGN IDENTIFIER (显式类型)
        if (tokens.size() >= 5) {
            // 检查是否有 <...> (类型参数)
            int gtIndex = -1;
            for (int i = 1; i < tokens.size(); i++) {
                if (tokens.get(i).type == TokenType.GT) {
                    gtIndex = i;
                    break;
                }
            }
            if (gtIndex == -1) {
                return false;
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

            // 检查右值是否是标识符(不带括号)
            if (gtIndex + 3 >= tokens.size()) {
                return false;
            }
            Token funcNameToken = tokens.get(gtIndex + 3);
            if (funcNameToken.type != TokenType.IDENTIFIER) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);

            // 检查是否是 Fun 开头
            if (token.type == TokenType.FUN_TYPE) {
                // 模式1: Fun IDENTIFIER ASSIGN IDENTIFIER (省略类型)
                if (i + 3 < tokensSize &&
                    tokens.get(i + 1).type == TokenType.IDENTIFIER &&
                    tokens.get(i + 2).type == TokenType.ASSIGN &&
                    tokens.get(i + 3).type == TokenType.IDENTIFIER) {

                    Token varNameToken = tokens.get(i + 1);
                    Token funcNameToken = tokens.get(i + 3);

                    // 检查是否以 LINE_END 结尾
                    int endIndex = i + 4;
                    if (endIndex < tokensSize && tokens.get(endIndex).type != TokenType.LINE_END) {
                        continue;
                    }

                    // 删除已解析的标记
                    if (endIndex < tokensSize) {
                        tokens.subList(i, endIndex + 1).clear();
                    } else {
                        tokens.subList(i, endIndex).clear();
                    }

                    // 创建函数引用语法节点（省略类型）
                    FunRefSyntaxNode funRefSyntaxNode = new FunRefSyntaxNode();
                    funRefSyntaxNode.setValue(new ArrayList<>()); // 空列表表示省略类型
                    funRefSyntaxNode.setLine(token.getLine());
                    funRefSyntaxNode.setVarName(varNameToken.value);
                    funRefSyntaxNode.setFunctionName(funcNameToken.value);
                    funRefSyntaxNode.setFunTypeSpecified(false);

                    syntaxNodeList.add(funRefSyntaxNode);
                    return true;
                }

                // 模式2: Fun<...> IDENTIFIER ASSIGN IDENTIFIER (显式类型)
                if (i + 4 < tokensSize) {
                    // 查找 GT 位置
                    int gtIndex = -1;
                    for (int j = i + 1; j < tokensSize; j++) {
                        if (tokens.get(j).type == TokenType.GT) {
                            gtIndex = j;
                            break;
                        }
                    }
                    if (gtIndex == -1 || gtIndex + 4 >= tokensSize) {
                        continue;
                    }

                    // 检查后续 token 类型
                    if (tokens.get(gtIndex + 1).type != TokenType.IDENTIFIER ||
                        tokens.get(gtIndex + 2).type != TokenType.ASSIGN ||
                        tokens.get(gtIndex + 3).type != TokenType.IDENTIFIER) {
                        continue;
                    }

                    // 提取函数类型、变量名、函数名
                    List<Token> funTypeTokens = new ArrayList<>(tokens.subList(i, gtIndex + 1));
                    Token varNameToken = tokens.get(gtIndex + 1);
                    Token funcNameToken = tokens.get(gtIndex + 3);

                    // 检查是否以 LINE_END 结尾
                    int endIndex = gtIndex + 4;
                    if (endIndex < tokensSize && tokens.get(endIndex).type != TokenType.LINE_END) {
                        continue;
                    }

                    // 删除已解析的标记
                    if (endIndex < tokensSize) {
                        tokens.subList(i, endIndex + 1).clear();
                    } else {
                        tokens.subList(i, endIndex).clear();
                    }

                    // 去掉注释
                    SyntaxNodeUtil.removeComments(funTypeTokens);

                    // 创建函数引用语法节点（显式类型）
                    FunRefSyntaxNode funRefSyntaxNode = new FunRefSyntaxNode();
                    funRefSyntaxNode.setValue(funTypeTokens);
                    funRefSyntaxNode.setLine(token.getLine());
                    funRefSyntaxNode.setVarName(varNameToken.value);
                    funRefSyntaxNode.setFunctionName(funcNameToken.value);
                    funRefSyntaxNode.setFunTypeSpecified(true);

                    syntaxNodeList.add(funRefSyntaxNode);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 变量名（用于存储额外的节点信息）
     */
    private String varName;

    /**
     * 函数名（用于存储额外的节点信息）
     */
    private String functionName;

    /**
     * 是否显式指定了函数类型
     */
    private boolean funTypeSpecified;

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        // 获取函数名
        String funcName = ((FunRefSyntaxNode) syntaxNode).getFunctionName();

        // 查找函数定义
        FunDefInfo funDefInfo = context.getFunction(funcName);
        if (funDefInfo == null) {
            throw new NfException("Line:{}, 函数 {} 不存在, syntax: {}",
                syntaxNode.getLine(), funcName, syntaxNode);
        }

        // 检查函数体是否已定义
        if (funDefInfo.getBodyNodes() == null) {
            throw new NfException("Line:{}, 函数 {} 的函数体未定义，可能函数定义还未执行, syntax: {}",
                syntaxNode.getLine(), funcName, syntaxNode);
        }

        // 获取或推导函数类型信息
        FunTypeInfo funTypeInfo;
        boolean typeSpecified = ((FunRefSyntaxNode) syntaxNode).isFunTypeSpecified();

        if (typeSpecified) {
            // 显式指定了类型，解析类型声明
            List<Token> value = syntaxNode.getValue();
            if (value == null || value.isEmpty()) {
                throw new NfException("Line:{}, 函数类型声明不能为空, syntax: {}",
                    syntaxNode.getLine(), syntaxNode);
            }

            // 提取函数类型字符串
            String funTypeStr = buildFunTypeString(value);

            // 解析函数类型
            funTypeInfo = FunTypeParser.parse(funTypeStr);

            // 验证函数签名是否匹配
            FunTypeInfo actualFunTypeInfo = FunTypeParser.fromFunDefInfo(funDefInfo);
            if (!funTypeInfo.equals(actualFunTypeInfo)) {
                throw new NfException("Line:{}, 函数签名不匹配, 期望: {}, 实际: {}, syntax: {}",
                    syntaxNode.getLine(), funTypeInfo, actualFunTypeInfo, syntaxNode);
            }
        } else {
            // 省略类型，从函数定义推导
            funTypeInfo = FunTypeParser.fromFunDefInfo(funDefInfo);
        }

        // 创建函数引用信息
        FunRefInfo funRef = FunRefInfo.createFunRef(funcName, funDefInfo, funTypeInfo);

        // 获取变量名
        String varName = ((FunRefSyntaxNode) syntaxNode).getVarName();

        // 存储函数引用到上下文
        context.addFunRef(varName, funRef);

        // 创建变量信息（用于变量查找）
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
     * 例如: [FUN_TYPE, LT, Integer, COMMA, Integer, COLON, Integer, GT] -> "Fun<Integer, Integer : Integer>"
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
}
