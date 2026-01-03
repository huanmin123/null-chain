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
 * @author huanmin
 * @date 2024/01/04
 */
public class ParseScopeTracker {
    
    /**
     * 作用域栈，每个作用域包含一个变量名集合
     * 栈顶表示当前作用域
     */
    private final Stack<Set<String>> variableScopeStack = new Stack<>();
    
    /**
     * 全局函数名集合（函数名在同一脚本作用域内是全局的）
     * key: 函数名, value: 首次定义的行号
     */
    private final Map<String, Integer> functionNames = new HashMap<>();
    
    /**
     * 当前作用域的变量名集合（用于记录变量名和行号的映射，用于错误提示）
     * key: 变量名, value: 首次定义的行号
     */
    private final List<Map<String, Integer>> variableScopeInfo = new ArrayList<>();
    
    /**
     * 构造函数
     * 初始化时创建全局作用域
     */
    public ParseScopeTracker() {
        enterScope(); // 创建全局作用域
    }
    
    /**
     * 进入新作用域
     * 块节点（if/for/switch/while/function等）会创建新作用域
     */
    public void enterScope() {
        variableScopeStack.push(new HashSet<>());
        variableScopeInfo.add(new HashMap<>());
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
        if (!variableScopeInfo.isEmpty()) {
            variableScopeInfo.remove(variableScopeInfo.size() - 1);
        }
    }
    
    /**
     * 检查变量名是否在当前作用域中重复
     * 
     * @param varName 变量名
     * @param line 当前行号
     * @param syntax 语法信息（用于错误提示）
     * @throws NfSyntaxException 如果变量名在当前作用域中已存在
     */
    public void checkDuplicateVariable(String varName, int line, String syntax) {
        if (variableScopeStack.isEmpty()) {
            throw new IllegalStateException("作用域栈为空");
        }
        
        Set<String> currentScope = variableScopeStack.peek();
        if (currentScope.contains(varName)) {
            // 查找首次定义的行号
            int firstLine = line;
            if (!variableScopeInfo.isEmpty()) {
                Map<String, Integer> currentScopeInfo = variableScopeInfo.get(variableScopeInfo.size() - 1);
                Integer firstDefinedLine = currentScopeInfo.get(varName);
                if (firstDefinedLine != null) {
                    firstLine = firstDefinedLine;
                }
            }
            throw new NfSyntaxException(
                line,
                "变量重复声明",
                String.format("变量 '%s' 在当前作用域中已声明（首次声明在第 %d 行），不能重复声明", varName, firstLine),
                syntax,
                "请修改变量名或删除重复声明"
            );
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
        
        Set<String> currentScope = variableScopeStack.peek();
        currentScope.add(varName);
        
        // 记录变量名和行号的映射
        if (!variableScopeInfo.isEmpty()) {
            Map<String, Integer> currentScopeInfo = variableScopeInfo.get(variableScopeInfo.size() - 1);
            if (!currentScopeInfo.containsKey(varName)) {
                currentScopeInfo.put(varName, line);
            }
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

