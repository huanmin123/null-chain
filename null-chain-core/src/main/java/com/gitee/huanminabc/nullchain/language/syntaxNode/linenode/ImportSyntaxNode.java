package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入语句节点
 *
 * 支持两种导入方式:
 * 1. 类型导入: import type com.xxx.Type 或 import type com.xxx.Type as Alias
 * 2. 任务导入: import task com.xxx.Task 或 import task com.xxx.Task as Alias
 *
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ImportSyntaxNode extends LineSyntaxNode {

    /**
     * 导入类型枚举
     */
    public enum ImportType {
        TYPE,   // 类型导入
        TASK    // 任务导入
    }

    /**
     * 导入类型（type 或 task）
     */
    private ImportType importType;

    /**
     * 类全路径
     */
    private String classPath;

    /**
     * 导入的名称（类名或别名）
     */
    private String name;

    public ImportSyntaxNode() {
        super(SyntaxNodeType.IMPORT_EXP);
    }

    public ImportSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.IMPORT;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.IMPORT) {
                int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, i);
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                tokens.subList(i, endIndex).clear();
                SyntaxNodeUtil.removeComments(newToken);

                // 至少需要: import type com.xxx.ClassName (3个token)
                if (newToken.size() < 2) {
                    throw new NfException("Line:{} ,import 语句错误,语法格式应为: import type/task 类路径 [as 别名] , syntax: import {}",
                        token.getLine(), TokenUtil.mergeToken(newToken).toString());
                }

                // 检查第二个token是 type 还是 task
                Token secondToken = newToken.get(0);
                ImportType importKind;
                if (secondToken.type == TokenType.TYPE) {
                    importKind = ImportType.TYPE;
                } else if (secondToken.type == TokenType.TASK) {
                    importKind = ImportType.TASK;
                } else {
                    throw new NfException("Line:{} ,import 语句错误,import 后必须跟 type 或 task 关键字 , syntax: import {}",
                        token.getLine(), TokenUtil.mergeToken(newToken).toString());
                }

                // 移除 type/task 关键字，获取剩余部分
                List<Token> classPathTokens = new ArrayList<>(newToken.subList(1, newToken.size()));

                // 查找 as 关键字位置
                int asIndex = -1;
                for (int j = 0; j < classPathTokens.size(); j++) {
                    if (classPathTokens.get(j).type == TokenType.AS) {
                        asIndex = j;
                        break;
                    }
                }

                String classPath;
                String alias = null;

                if (asIndex >= 0) {
                    // 有 as 别名
                    if (asIndex == 0) {
                        throw new NfException("Line:{} ,import 语句错误,as 前缺少类路径 , syntax: import {}",
                            token.getLine(), TokenUtil.mergeToken(newToken).toString());
                    }
                    if (classPathTokens.size() <= asIndex + 1) {
                        throw new NfException("Line:{} ,import 语句错误,as 后缺少别名 , syntax: import {}",
                            token.getLine(), TokenUtil.mergeToken(newToken).toString());
                    }
                    if (classPathTokens.get(asIndex + 1).type != TokenType.IDENTIFIER) {
                        throw new NfException("Line:{} ,import 语句错误,as 后必须是标识符 , syntax: import {}",
                            token.getLine(), TokenUtil.mergeToken(newToken).toString());
                    }

                    // 类路径部分（as 之前）
                    List<Token> pathTokens = new ArrayList<>(classPathTokens.subList(0, asIndex));
                    classPath = TokenUtil.mergeToken(pathTokens).toString();

                    // 别名（as 之后）
                    alias = classPathTokens.get(asIndex + 1).value;
                } else {
                    // 无 as 别名，使用类名
                    classPath = TokenUtil.mergeToken(classPathTokens).toString();
                }

                // 验证类是否存在
                try {
                    Class.forName(classPath);
                } catch (ClassNotFoundException e) {
                    throw new NfException("Line:{} ,import {} 语句错误,找不到类", token.getLine(), classPath);
                }

                // 解析类名作为默认名称
                String simpleName = classPath.substring(classPath.lastIndexOf('.') + 1);
                String finalName = alias != null ? alias : simpleName;

                ImportSyntaxNode importNode = new ImportSyntaxNode(SyntaxNodeType.IMPORT_EXP);
                importNode.setValue(newToken);
                importNode.setImportType(importKind);
                importNode.setClassPath(classPath);
                importNode.setName(finalName);
                importNode.setLine(token.getLine());
                syntaxNodeList.add(importNode);

                return true;
            }
        }
        return false;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        if (!(syntaxNode instanceof ImportSyntaxNode)) {
            throw new NfException("Line:{} ,import 节点类型错误", syntaxNode.getLine());
        }

        ImportSyntaxNode importNode = (ImportSyntaxNode) syntaxNode;
        String name = importNode.getName();
        String classPath = importNode.getClassPath();

        if (name == null || name.isEmpty()) {
            throw new NfException("Line:{} ,import 表达式名称不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }
        if (classPath == null || classPath.isEmpty()) {
            throw new NfException("Line:{} ,import 表达式类路径不能为空 , syntax: {}",
                syntaxNode.getLine(), syntaxNode);
        }

        // 根据导入类型处理
        if (importNode.getImportType() == ImportType.TYPE) {
            // 类型导入 - 检查冲突
            String existingPath = context.getImportType(name);
            if (existingPath != null) {
                if (existingPath.equals(classPath)) {
                    // 重复导入同一个类，忽略
                    return;
                }
                throw new NfException("Line:{} ,import 类型冲突: 名称 '{}' 已被 {} 使用, 不能重复导入",
                    syntaxNode.getLine(), name, existingPath);
            }
            context.addImport(name, classPath);
        } else {
            // 任务导入 - 检查冲突
            String existingPath = context.getTask(name);
            if (existingPath != null) {
                if (existingPath.equals(classPath)) {
                    // 重复导入同一个任务，忽略
                    return;
                }
                throw new NfException("Line:{} ,import 任务冲突: 名称 '{}' 已被 {} 使用, 不能重复导入",
                    syntaxNode.getLine(), name, existingPath);
            }
            context.addTask(name, classPath);

            // 尝试注册任务到工厂
            try {
                NullTask task = NullTaskFactory.getTask(classPath);
                if (task == null) {
                    Class<?> aClass = Class.forName(classPath);
                    if (NullTask.class.isAssignableFrom(aClass)) {
                        NullTaskFactory.registerTask((Class)aClass);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new NfException(e, "Line:{} ,import 任务错误,找不到任务类: {}",
                    syntaxNode.getLine(), classPath);
            }
        }
    }
}
