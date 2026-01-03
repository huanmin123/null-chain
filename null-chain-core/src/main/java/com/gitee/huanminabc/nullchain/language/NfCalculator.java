package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.FunCallSyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.NfToken;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 懒得自己写算术表达式解析器, 直接用现成的, 后期有时间再自己写
 * 表达式计算器  2 * (3 + 4)
 * <p>
 * JexlEngine有缺陷就是第一次加载的太慢了, 如果自己写就没有这个问题, 其实也不怎么影响
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfCalculator {
    //第一次加载的时候需要100~200ms 之后就没有影响了
    private final static JexlEngine jexl = new JexlBuilder().create();

    // 临时变量存储（用于递归处理函数调用时共享临时变量）
    private static ThreadLocal<Map<String, Object>> tempVarStorage = ThreadLocal.withInitial(java.util.HashMap::new);

    // 递归深度计数器，用于判断是否是最外层调用
    private static ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);

    /**
     * instanceof 处理结果，包含转换后的表达式和需要的类型类
     */
    private static class InstanceofProcessResult {
        String processedExpression;
        Set<String> requiredTypeNames;

        InstanceofProcessResult(String expr, Set<String> types) {
            this.processedExpression = expr;
            this.requiredTypeNames = types;
        }
    }

    /**
     * 预处理表达式，将 instanceof 转换为 JEXL 支持的语法
     * instanceof 有两层含义：1.判断类型相等 2.判断是否是子类
     * 利用 importMap 中已导入的类型信息，自动解析类型名
     * 例如：value instanceof Integer -> __Integer_Class.isAssignableFrom(value.class)
     * 例如：value instanceof MyClass (已导入) -> __MyClass_Class.isAssignableFrom(value.class)
     *
     * @param expression 原始表达式
     * @param importMap   导入的类型映射表
     * @return 处理结果，包含转换后的表达式和需要的类型名称集合
     */
    private static InstanceofProcessResult preProcessExpression(String expression, Map<String, String> importMap) {
        if (expression == null || expression.isEmpty()) {
            return new InstanceofProcessResult(expression, new java.util.HashSet<>());
        }

        // 匹配 instanceof 表达式：(\w+)\s+instanceof\s+(\S+)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\w+)\\s+instanceof\\s+(\\S+)");
        java.util.regex.Matcher matcher = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        Set<String> requiredTypes = new java.util.HashSet<>();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String typeName = matcher.group(2);

            // 解析类型全限定名
            String fullClassName = resolveTypeName(typeName, importMap);

            // 生成类型变量名（避免冲突，使用 __TypeName_Class 格式）
            String typeVarName = "__" + typeName.replace(".", "_") + "_Class";
            requiredTypes.add(typeVarName + "=" + fullClassName);

            // 转换为 JEXL 表达式
            String replacement = String.format("%s.isAssignableFrom(%s.class)", typeVarName, varName);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return new InstanceofProcessResult(sb.toString(), requiredTypes);
    }

    /**
     * 解析类型名，返回完整的类路径
     * 优先级：importMap 中的导入 > 全限定名 > java.lang 包
     *
     * @param typeName   类型名称（可能是短名称或全限定名）
     * @param importMap  导入的类型映射表
     * @return 完整的类路径
     */
    private static String resolveTypeName(String typeName, Map<String, String> importMap) {
        // 1. 如果是全限定名（包含 .），直接返回
        if (typeName.contains(".")) {
            return typeName;
        }

        // 2. 在 importMap 中查找
        if (importMap != null && importMap.containsKey(typeName)) {
            return importMap.get(typeName);
        }

        // 3. 默认为 java.lang 包中的类型
        return "java.lang." + typeName;
    }

    public static Object arithmetic(String expression, Map<String, Object> params) {
        JexlContext context = new MapContext();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        return jexl.createExpression(expression).evaluate(context);
    }

    public static Object arithmetic(String expression, NfContext nfContext) {
        if (nfContext == null) {
            throw new NfException("nfContext为null，无法计算表达式: " + expression);
        }
        JexlContext context = new MapContext();
        //获取当前作用域
        NfContextScope currentScope = nfContext.getCurrentScope();
        //检查当前作用域是否为null
        if (currentScope == null) {
            throw new NfException("当前作用域为null，无法计算表达式: " + expression + "，currentScopeId: " + nfContext.getCurrentScopeId());
        }
        //合并作用域
        mergeScope(nfContext, currentScope, context);
        //获取类型导入
        Map<String, String> importMap = nfContext.getImportMap();
        if (importMap != null) {
            Set<Map.Entry<String, String>> entries = importMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                //将value转化为实际的类
                try {
                    Class<?> aClass = Class.forName(entry.getValue());
                    context.set(entry.getKey(), aClass);
                } catch (ClassNotFoundException e) {
                    throw new NfException(e, "找不到类: " + entry.getValue());
                }
            }
        }

        // 增加递归深度
        int depth = recursionDepth.get();
        recursionDepth.set(depth + 1);
        boolean isTopLevel = (depth == 0);

        try {
            // 只在最外层调用时初始化临时变量存储
            if (isTopLevel) {
                tempVarStorage.get().clear();
            }

            // 预处理表达式中的导入脚本变量访问（脚本名称.变量名）
            String processedExpr = preProcessImportedScriptAccess(expression, nfContext);

            // 预处理表达式中的函数调用
            processedExpr = preProcessFunctionCalls(processedExpr, nfContext, context);

            // 将临时变量添加到 JexlContext 中
            for (Map.Entry<String, Object> entry : tempVarStorage.get().entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }

            // 预处理表达式，转换 instanceof 等语法（传入 importMap 用于类型解析）
            InstanceofProcessResult processResult = preProcessExpression(processedExpr, importMap);
            String finalExpr = processResult.processedExpression;

            // 将需要的类型类设置到上下文中
            for (String typeDef : processResult.requiredTypeNames) {
                String[] parts = typeDef.split("=", 2);
                String varName = parts[0];
                String className = parts[1];
                try {
                    Class<?> typeClass = Class.forName(className);
                    context.set(varName, typeClass);
                } catch (ClassNotFoundException e) {
                    throw new NfException(e, "找不到类: " + className);
                }
            }

            Object result = jexl.createExpression(finalExpr).evaluate(context);
            return result;
        } catch (NfReturnException e) {
            // return语句需要穿透表达式计算，传播到函数调用处
            throw e;
        } catch (Exception e) {
            // 添加详细错误信息用于调试
            System.err.println("表达式计算错误 - 原始表达式: " + expression);
            System.err.println("表达式计算错误 - 异常信息: " + e.getMessage());
            e.printStackTrace();
            throw new NfException(e, "表达式计算错误: " + expression);
        } finally {
            // 恢复递归深度
            recursionDepth.set(depth);
            // 只在最外层调用时清理临时变量存储
            if (isTopLevel) {
                tempVarStorage.get().clear();
            }
        }
    }

    /**
     * 合并作用域到JEXL上下文（迭代实现，避免递归导致的栈溢出）
     *
     * @param nfContext NF上下文
     * @param currentScope 当前作用域
     * @param context Jexl上下文
     */
    private static void mergeScope(NfContext nfContext, NfContextScope currentScope, JexlContext context) {
        if (currentScope == null) {
            return;
        }

        // 使用迭代方式合并作用域链，避免递归导致的栈溢出
        // 首先收集所有需要合并的作用域（从当前作用域向上遍历）
        java.util.List<NfContextScope> scopesToMerge = new java.util.ArrayList<>();
        java.util.Set<String> visitedScopes = new java.util.HashSet<>();

        NfContextScope scope = currentScope;
        while (scope != null) {
            String scopeId = scope.getScopeId();

            // 检测循环引用
            if (visitedScopes.contains(scopeId)) {
                break;
            }

            visitedScopes.add(scopeId);
            scopesToMerge.add(scope);

            // 防止作用域链过长（超过1000个作用域时停止）
            if (visitedScopes.size() > 1000) {
                break;
            }

            // 获取父作用域
            String parentScopeId = scope.getParentScopeId();
            if (parentScopeId != null) {
                scope = nfContext.getScope(parentScopeId);
            } else {
                scope = null;
            }
        }

        // 按照从父作用域到当前作用域的顺序合并变量（这样当前作用域的变量会覆盖父作用域的同名变量）
        // 所以需要反向遍历
        for (int i = scopesToMerge.size() - 1; i >= 0; i--) {
            NfContextScope s = scopesToMerge.get(i);
            Map<String, Object> scopeMap = s.toMap();
            for (Map.Entry<String, Object> entry : scopeMap.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 预处理表达式中的导入脚本变量访问
     * 识别 `脚本名称.变量名` 模式，从导入脚本的作用域中获取变量值，替换为临时变量
     * 
     * <p>注意：此方法只处理变量访问，函数调用（`脚本名称.函数名()`）由 FunCallSyntaxNode 处理</p>
     *
     * @param expression 原始表达式
     * @param nfContext NF上下文
     * @return 处理后的表达式
     */
    private static String preProcessImportedScriptAccess(String expression, NfContext nfContext) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        // 匹配模式：脚本名称.变量名（但不匹配函数调用，即后面不能跟左括号）
        // 使用正则表达式：\b([a-zA-Z_$][a-zA-Z0-9_$]*)\.([a-zA-Z_$][a-zA-Z0-9_$]*)(?!\s*\()
        // (?!\s*\() 是负向前瞻，确保后面不是左括号（即不是函数调用）
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$][a-zA-Z0-9_$]*)(?!\\s*\\()"
        );
        java.util.regex.Matcher matcher = pattern.matcher(expression);
        
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        
        while (matcher.find()) {
            String scriptName = matcher.group(1);
            String varName = matcher.group(2);
            
            // 检查是否是导入的脚本
            if (nfContext.hasImportedScript(scriptName)) {
                // 获取导入脚本的作用域
                NfContextScope scriptScope = nfContext.getImportedScriptScope(scriptName);
                if (scriptScope != null) {
                    // 从脚本作用域中获取变量
                    NfVariableInfo varInfo = scriptScope.getVariable(varName);
                    if (varInfo != null) {
                        // 找到变量，生成临时变量名并存储值
                        String tempVarName = "__script_" + scriptName + "_" + varName + "_" + System.nanoTime();
                        tempVarStorage.get().put(tempVarName, varInfo.getValue());
                        
                        // 替换为临时变量
                        matcher.appendReplacement(sb, tempVarName);
                        found = true;
                        continue;
                    }
                }
            }
            
            // 不是导入脚本访问，保留原样
            matcher.appendReplacement(sb, matcher.group(0));
        }
        
        if (found) {
            matcher.appendTail(sb);
            return sb.toString();
        }
        
        return expression;
    }

    /**
     * 预处理表达式中的函数调用
     * 如果表达式中包含函数调用，先执行函数调用，将结果替换为临时变量
     * 使用递归方式处理，确保嵌套函数调用被正确处理
     *
     * @param expression 原始表达式
     * @param nfContext NF上下文
     * @param jexlContext JEXL上下文（用于合并作用域变量）
     * @return 处理后的表达式
     */
    private static String preProcessFunctionCalls(String expression, NfContext nfContext, JexlContext jexlContext) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        // 使用正则表达式匹配函数调用：函数名(参数列表) 或 脚本名称.函数名(参数列表)
        // 匹配模式1：普通函数调用 - 标识符(
        // 匹配模式2：导入脚本的函数调用 - 标识符.标识符(
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");
        
        // 先检查是否有导入脚本的函数调用
        java.util.regex.Matcher matcher2 = pattern2.matcher(expression);
        boolean hasScriptFunctionCall = false;
        while (matcher2.find()) {
            String scriptName = matcher2.group(1);
            String functionName = matcher2.group(2);
            if (nfContext.hasImportedScript(scriptName)) {
                NfContext scriptContext = nfContext.getImportedScriptContext(scriptName);
                if (scriptContext != null && scriptContext.hasFunction(functionName)) {
                    hasScriptFunctionCall = true;
                    break;
                }
            }
        }
        
        // 检查是否有当前上下文的函数调用
        java.util.regex.Matcher matcher1 = pattern1.matcher(expression);
        boolean hasFunctionCall = false;
        while (matcher1.find()) {
            String functionName = matcher1.group(1);
            if (nfContext.hasFunction(functionName)) {
                hasFunctionCall = true;
                break;
            }
        }

        // 如果没有函数调用，直接返回原表达式
        if (!hasFunctionCall && !hasScriptFunctionCall) {
            return expression;
        }

        // 找到最内层（最后一个开始的）函数调用并处理它
        // 然后递归处理剩余的函数调用
        // 先处理导入脚本的函数调用
        matcher2.reset();
        int lastScriptMatchStart = -1;
        int lastScriptMatchEnd = -1;
        String lastScriptName = null;
        String lastScriptFunctionName = null;
        
        while (matcher2.find()) {
            String scriptName = matcher2.group(1);
            String functionName = matcher2.group(2);
            if (nfContext.hasImportedScript(scriptName)) {
                NfContext scriptContext = nfContext.getImportedScriptContext(scriptName);
                if (scriptContext != null && scriptContext.hasFunction(functionName)) {
                    lastScriptMatchStart = matcher2.start();
                    lastScriptName = scriptName;
                    lastScriptFunctionName = functionName;
                }
            }
        }
        
        // 处理当前上下文的函数调用
        matcher1.reset();
        int lastMatchStart = -1;
        int lastMatchEnd = -1;
        String lastFunctionName = null;

        while (matcher1.find()) {
            String functionName = matcher1.group(1);
            // 跳过导入脚本的函数调用（已经处理过了）
            if (lastScriptMatchStart >= 0 && matcher1.start() == lastScriptMatchStart) {
                continue;
            }
            if (nfContext.hasFunction(functionName)) {
                lastMatchStart = matcher1.start();
                lastFunctionName = functionName;
            }
        }

        // 优先处理导入脚本的函数调用（如果存在）
        if (lastScriptMatchStart >= 0) {
            // 找到该函数调用的完整范围
            int startPos = lastScriptMatchStart;
            int parenDepth = 1;
            int endPos = startPos + lastScriptName.length() + 1 + lastScriptFunctionName.length() + 1; // 跳过脚本名.函数名和左括号
            
            // 找到匹配的右括号
            while (endPos < expression.length() && parenDepth > 0) {
                char c = expression.charAt(endPos);
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    parenDepth--;
                }
                endPos++;
            }
            
            // 提取函数调用表达式
            String functionCallExpr = expression.substring(startPos, endPos);
            
            // 将函数调用表达式解析为tokens
            List<Token> tokens;
            try {
                tokens = NfToken.tokens(functionCallExpr);
            } catch (Exception e) {
                // 如果解析失败，保留原表达式
                return expression;
            }
            
            // 创建函数调用语法节点
            FunCallSyntaxNode funCallNode = new FunCallSyntaxNode();
            funCallNode.setValue(tokens);
            if (!tokens.isEmpty()) {
                funCallNode.setLine(tokens.get(0).getLine());
            }
            
            // 保存当前作用域ID
            String savedScopeIdForPreprocess = nfContext.getCurrentScopeId();
            
            // 执行函数调用
            Object returnValue;
            try {
                returnValue = funCallNode.executeFunction(nfContext, funCallNode);
            } catch (Exception e) {
                throw new NfException(e, "表达式中的导入脚本函数调用执行失败: " + functionCallExpr);
            } finally {
                if (savedScopeIdForPreprocess != null) {
                    nfContext.switchScope(savedScopeIdForPreprocess);
                }
            }
            
            // 生成临时变量名并存储值
            String tempVarName = "__script_fun_call_" + System.nanoTime();
            tempVarStorage.get().put(tempVarName, returnValue);
            
            // 替换函数调用为临时变量
            String processedExpr = expression.substring(0, startPos) + tempVarName + expression.substring(endPos);
            
            // 递归处理剩余的函数调用
            return preProcessFunctionCalls(processedExpr, nfContext, jexlContext);
        }

        if (lastMatchStart == -1) {
            return expression;
        }

        // 找到该函数调用的完整范围
        int startPos = lastMatchStart;
        int parenDepth = 1;
        int endPos = startPos + lastFunctionName.length() + 1; // 跳过函数名和左括号

        // 找到匹配的右括号
        while (endPos < expression.length() && parenDepth > 0) {
            char c = expression.charAt(endPos);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            }
            endPos++;
        }

        // 提取函数调用表达式
        String functionCallExpr = expression.substring(startPos, endPos);

        // 将函数调用表达式解析为tokens
        List<Token> tokens;
        try {
            tokens = NfToken.tokens(functionCallExpr);
        } catch (Exception e) {
            // 如果解析失败，保留原表达式
            return expression;
        }

        // 创建函数调用语法节点
        FunCallSyntaxNode funCallNode = new FunCallSyntaxNode();
        funCallNode.setValue(tokens);
        if (!tokens.isEmpty()) {
            funCallNode.setLine(tokens.get(0).getLine());
        }

        // 保存当前作用域ID
        String savedScopeIdForPreprocess = nfContext.getCurrentScopeId();

        // 执行函数调用
        Object returnValue;
        try {
            returnValue = funCallNode.executeFunction(nfContext, funCallNode);
        } catch (Exception e) {
            throw new NfException(e, "表达式中的函数调用执行失败: " + functionCallExpr);
        } finally {
            if (savedScopeIdForPreprocess != null) {
                nfContext.switchScope(savedScopeIdForPreprocess);
            }
        }

        // 生成临时变量名（使用计数器确保唯一性）
        String tempVarName = "__fun_call_" + System.nanoTime();

        // 将返回值存储到临时变量存储中（ThreadLocal，递归调用间共享）
        tempVarStorage.get().put(tempVarName, returnValue);

        // 替换函数调用为临时变量
        String processedExpr = expression.substring(0, startPos) + tempVarName + expression.substring(endPos);

        // 递归处理剩余的函数调用
        return preProcessFunctionCalls(processedExpr, nfContext, jexlContext);
    }

    // 临时变量计数器（用于生成唯一变量名）
    private static int tempVarCounter = 0;

    public static void main(String[] args) {
        Object arithmetic = NfCalculator.arithmetic("2 * (3 + 4)", new HashMap<>());
        System.out.println(arithmetic);

        int a = 1;
        int b = 2;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", a);
        map.put("b", b);
        Object arithmetic1 = NfCalculator.arithmetic("a + b", map);
        System.out.println(arithmetic1);

        Object arithmetic2 = NfCalculator.arithmetic("'1'+ '2'", map);
        System.out.println(arithmetic2);


        map.put("UUID", java.util.UUID.class);
        Object arithmetic3 = NfCalculator.arithmetic("UUID.randomUUID().toString()", map);
        System.out.println(arithmetic3);
    }


}
