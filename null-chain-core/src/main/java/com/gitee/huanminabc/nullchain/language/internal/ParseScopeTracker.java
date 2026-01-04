package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.language.NfSyntaxException;

import java.util.*;

/**
 * 解析时作用域和名称追踪器
 *
 * <p>用于在解析阶段追踪变量名和函数名，检测重复声明。
 * 变量名需要考虑作用域层级（块节点创建新作用域），
 * 函数名在同一脚本作用域内是全局的。</p>
 *
 * <p>作用域规则：
 * <ul>
 *   <li>块级作用域（if/for/switch/while）内的变量不能和父作用域重复</li>
 *   <li>函数内的变量不能和全局变量重复</li>
 *   <li>函数内的块级变量也不能和函数作用域内的变量重复</li>
 *   <li>不同函数之间的变量可以重名（函数作用域是独立的）</li>
 * </ul>
 * </p>
 *
 * @author huanmin
 * @date 2024/01/04
 */
public class ParseScopeTracker {

    /**
     * 作用域类型枚举
     */
    public enum ScopeType {
        /** 全局作用域（脚本最外层） */
        GLOBAL,
        /** 函数作用域（函数定义内部） */
        FUNCTION,
        /** 块级作用域（if/for/switch/while等） */
        BLOCK
    }

    /**
     * 作用域信息类
     */
    private static class ScopeInfo {
        /** 变量名集合 */
        Set<String> variables = new HashSet<>();
        /** 作用域类型 */
        ScopeType type;
        /** 变量名和行号的映射（用于错误提示） */
        Map<String, Integer> variableLines = new HashMap<>();

        ScopeInfo(ScopeType type) {
            this.type = type;
        }
    }

    /**
     * 作用域栈，每个作用域包含变量名集合和作用域类型
     * 栈顶表示当前作用域
     */
    private final Stack<ScopeInfo> variableScopeStack = new Stack<>();

    /**
     * 全局函数名集合（函数名在同一脚本作用域内是全局的）
     * key: 函数名, value: 首次定义的行号
     */
    private final Map<String, Integer> functionNames = new HashMap<>();

    /**
     * 构造函数
     * 初始化时创建全局作用域
     */
    public ParseScopeTracker() {
        enterScope(ScopeType.GLOBAL); // 创建全局作用域
    }

    /**
     * 进入新作用域（默认为块级作用域）
     * 块节点（if/for/switch/while等）会创建新作用域
     *
     * @deprecated 使用 {@link #enterScope(ScopeType)} 明确指定作用域类型
     */
    @Deprecated
    public void enterScope() {
        enterScope(ScopeType.BLOCK);
    }

    /**
     * 进入新作用域
     *
     * @param scopeType 作用域类型
     */
    public void enterScope(ScopeType scopeType) {
        variableScopeStack.push(new ScopeInfo(scopeType));
    }

    /**
     * 退出当前作用域
     * 块节点解析完成后退出作用域
     */
    public void exitScope() {
        if (variableScopeStack.isEmpty()) {
            throw new IllegalStateException("作用域栈为空，无法退出作用域");
        }
        variableScopeStack.pop();
    }

    /**
     * 检查变量名是否在父作用域中重复
     *
     * <p>检查规则：
     * <ul>
     *   <li>遍历所有父作用域检查是否有同名变量</li>
     *   <li>遇到 FUNCTION 类型作用域时停止向上查找（实现函数间变量隔离）</li>
     * </ul>
     * </p>
     *
     * @param varName 变量名
     * @param line 当前行号
     * @param syntax 语法信息（用于错误提示）
     * @throws NfSyntaxException 如果变量名在父作用域中已存在
     */
    public void checkDuplicateVariable(String varName, int line, String syntax) {
        if (variableScopeStack.isEmpty()) {
            throw new IllegalStateException("作用域栈为空");
        }

        // 从当前作用域开始向上遍历检查
        for (int i = variableScopeStack.size() - 1; i >= 0; i--) {
            ScopeInfo scopeInfo = variableScopeStack.get(i);

            // 检查当前作用域是否已包含该变量
            if (scopeInfo.variables.contains(varName)) {
                Integer firstDefinedLine = scopeInfo.variableLines.get(varName);
                int firstLine = firstDefinedLine != null ? firstDefinedLine : line;
                String scopeTypeDesc = getScopeTypeDescription(scopeInfo.type);

                throw new NfSyntaxException(
                    line,
                    "变量重复声明",
                    String.format("变量 '%s' 在%s已声明（首次声明在第 %d 行），不能重复声明", varName, scopeTypeDesc, firstLine),
                    syntax,
                    "请修改变量名或删除重复声明"
                );
            }

            // 如果遇到函数作用域，停止向上查找（函数作用域会遮蔽外部作用域）
            // 这样实现：函数内的变量可以遮蔽全局变量，不同函数之间可以重名
            if (scopeInfo.type == ScopeType.FUNCTION) {
                break;
            }
        }
    }

    /**
     * 获取作用域类型的描述文字
     *
     * @param type 作用域类型
     * @return 描述文字
     */
    private String getScopeTypeDescription(ScopeType type) {
        switch (type) {
            case GLOBAL:
                return "全局作用域";
            case FUNCTION:
                return "函数作用域";
            case BLOCK:
                return "当前作用域";
            default:
                return "上层作用域";
        }
    }

    /**
     * 添加变量名到当前作用域
     *
     * @param varName 变量名
     * @param line 行号
     */
    public void addVariable(String varName, int line) {
        if (variableScopeStack.isEmpty()) {
            throw new IllegalStateException("作用域栈为空");
        }

        ScopeInfo currentScope = variableScopeStack.peek();
        currentScope.variables.add(varName);
        // 只记录首次定义的行号
        if (!currentScope.variableLines.containsKey(varName)) {
            currentScope.variableLines.put(varName, line);
        }
    }

    /**
     * 检查函数名是否重复
     *
     * @param functionName 函数名
     * @param line 当前行号
     * @param syntax 语法信息（用于错误提示）
     * @throws NfSyntaxException 如果函数名已存在
     */
    public void checkDuplicateFunction(String functionName, int line, String syntax) {
        if (functionNames.containsKey(functionName)) {
            int firstLine = functionNames.get(functionName);
            throw new NfSyntaxException(
                line,
                "函数重复定义",
                String.format("函数 '%s' 已定义（首次定义在第 %d 行），不能重复定义", functionName, firstLine),
                syntax,
                "请修改函数名或删除重复定义"
            );
        }
    }

    /**
     * 添加函数名到全局集合
     *
     * @param functionName 函数名
     * @param line 行号
     */
    public void addFunction(String functionName, int line) {
        if (!functionNames.containsKey(functionName)) {
            functionNames.put(functionName, line);
        }
    }

    /**
     * 获取当前作用域深度（用于调试）
     *
     * @return 作用域深度
     */
    public int getScopeDepth() {
        return variableScopeStack.size();
    }
}
