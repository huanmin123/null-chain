package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.FunCallSyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
@Slf4j
public class NfCalculator {
    //第一次加载的时候需要100~200ms 之后就没有影响了
    private final static JexlEngine jexl = new JexlBuilder().create();
    
    /**
     * 全局表达式缓存：缓存编译后的JEXL表达式
     * 使用Caffeine缓存，提供高性能和自动管理能力
     * <p>配置说明：
     * <ul>
     *   <li>maximumSize(10000): 最大容量10000个表达式</li>
     *   <li>expireAfterAccess(1, TimeUnit.HOURS): 1小时未访问自动过期</li>
     *   <li>线程安全：Caffeine自动保证线程安全</li>
     * </ul>
     * </p>
     * <p>设计说明：
     * <ul>
     *   <li>编译后的JEXL表达式是纯语法结构，不包含变量值，可以全局共享</li>
     *   <li>直接使用表达式字符串作为key，Caffeine内部会使用String.hashCode()处理</li>
     *   <li>同一个表达式在不同上下文、不同作用域中都可以复用编译结果</li>
     *   <li>变量值在evaluate时通过JexlContext传入，不影响编译结果</li>
     * </ul>
     * </p>
     * key: 表达式字符串, value: 编译后的表达式对象
     */
    private static final Cache<String, JexlExpression> globalExpressionCache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

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

        //检查超时（表达式计算前检查，防止复杂表达式长时间执行）
        nfContext.checkTimeout();
        
        JexlContext context = new MapContext();
        //获取当前作用域
        NfContextScope currentScope = nfContext.getCurrentScope();
        //检查当前作用域是否为null
        if (currentScope == null) {
            throw new NfException("当前作用域为null，无法计算表达式: " + expression + "，currentScopeId: " + nfContext.getCurrentScopeId());
        }
        //合并作用域
        mergeScope(nfContext, currentScope, context);

        // 特殊处理：如果表达式只是单个标识符，检查是否是函数引用变量
        // 函数引用变量不能参与表达式计算，只能作为参数传递
        String trimmedExpression = expression.trim();
        if (isValidIdentifier(trimmedExpression)) {
            // 检查是否是函数引用变量
            if (nfContext.hasFunRef(trimmedExpression)) {
                // 直接返回函数引用对象，不参与表达式计算
                return nfContext.getFunRef(trimmedExpression);
            }
            // 检查是否是已定义的函数（函数名也可以作为函数引用使用）
            if (nfContext.hasFunction(trimmedExpression)) {
                // 创建函数引用并返回
                FunDefInfo funDef = nfContext.getFunction(trimmedExpression);
                return FunRefInfo.createFunRef(
                    trimmedExpression,
                    funDef,
                    null  // funTypeInfo
                );
            }
        }

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
        int depth = nfContext.getRecursionDepth();
        nfContext.setRecursionDepth(depth + 1);
        boolean isTopLevel = (depth == 0);

        try {
            // 只在最外层调用时初始化临时变量存储
            if (isTopLevel) {
                nfContext.getTempVarStorage().clear();
            }

            // 预处理表达式中的导入脚本变量访问（脚本名称.变量名）
            String processedExpr = preProcessImportedScriptAccess(expression, nfContext);

            // 预处理表达式中的全局变量访问（global.xxx）
            processedExpr = preProcessGlobalAccess(processedExpr, nfContext);

            // 预处理表达式中的函数调用
            processedExpr = preProcessFunctionCalls(processedExpr, nfContext, context);

            // 将临时变量添加到 JexlContext 中
            for (Map.Entry<String, Object> entry : nfContext.getTempVarStorage().entrySet()) {
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

            // 使用全局缓存的表达式对象，避免重复编译
            // 编译后的表达式是纯语法结构，不依赖变量值，可以全局共享
            // 直接使用表达式字符串作为缓存key，Caffeine内部会处理哈希

            JexlExpression cachedExpression = globalExpressionCache.get(finalExpr, jexl::createExpression);
            Object evaluate = cachedExpression.evaluate(context);
//            log.info("finalExpr: {}, evaluate: {}", finalExpr, evaluate);
            return evaluate;
        } catch (NfReturnException e) {
            // return语句需要穿透表达式计算，传播到函数调用处
            throw e;
        } catch (Exception e) {
            // 记录详细错误信息用于调试
            log.error("表达式计算错误 - 原始表达式: {}, 异常信息: {}", expression, e.getMessage(), e);
            throw new NfException(e, "表达式计算错误: {}, 错误详情: {}", expression, e.getMessage());
        } finally {
            // 恢复递归深度
            nfContext.setRecursionDepth(depth);
            // 只在最外层调用时清理临时变量存储
            if (isTopLevel) {
                nfContext.getTempVarStorage().clear();
            }
        }
    }

    /**
     * 合并作用域到JEXL上下文（迭代实现，避免递归导致的栈溢出）
     * 优化：直接遍历作用域的变量Map，避免调用toMap()创建新Map
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
        // 优化：直接访问作用域的value Map，避免调用toMap()创建新Map
        for (int i = scopesToMerge.size() - 1; i >= 0; i--) {
            NfContextScope s = scopesToMerge.get(i);
            // 直接访问作用域的变量Map，避免创建新Map
            Map<String, NfVariableInfo> variables = s.getValue();
            if (variables != null) {
                for (Map.Entry<String, NfVariableInfo> entry : variables.entrySet()) {
                    // 函数引用变量也可以作为值访问（用于 return funName 等场景）
                    // JEXL3 无法直接调用 FunRefInfo 对象，但可以访问其值
                    context.set(entry.getKey(), entry.getValue().getValue());
                }
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
                        nfContext.getTempVarStorage().put(tempVarName, varInfo.getValue());
                        
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
     * 预处理表达式中的全局变量访问（global.xxx）
     * 用于在函数内部访问被遮蔽的全局变量
     *
     * 注意：目前仅支持读取，不支持赋值。赋值需要单独使用 assign 语句
     *
     * @param expression 原始表达式
     * @param nfContext NF上下文
     * @return 处理后的表达式
     */
    private static String preProcessGlobalAccess(String expression, NfContext nfContext) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        // 匹配模式：global.变量名
        // 使用正则表达式：\bglobal\.([a-zA-Z_$][a-zA-Z0-9_$]*)
        // \b 确保是完整的单词边界，避免匹配类似 myglobal.xxx 的情况
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\\bglobal\\.([a-zA-Z_$][a-zA-Z0-9_$]*)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(expression);

        StringBuffer sb = new StringBuffer();
        boolean found = false;

        while (matcher.find()) {
            String varName = matcher.group(1);

            // 获取全局作用域（最顶层作用域，没有父作用域的作用域）
            NfContextScope globalScope = findGlobalScope(nfContext);
            if (globalScope != null) {
                // 从全局作用域中获取变量
                NfVariableInfo varInfo = globalScope.getVariable(varName);
                if (varInfo != null) {
                    // 找到变量，生成临时变量名并存储值
                    String tempVarName = "__global_" + varName + "_" + System.nanoTime();
                    nfContext.getTempVarStorage().put(tempVarName, varInfo.getValue());

                    // 替换为临时变量
                    matcher.appendReplacement(sb, tempVarName);
                    found = true;
                    continue;
                }
            }

            // 没有找到全局变量，保留原样（运行时会报错）
            matcher.appendReplacement(sb, matcher.group(0));
        }

        if (found) {
            matcher.appendTail(sb);
            return sb.toString();
        }

        return expression;
    }

    /**
     * 查找全局作用域（最顶层作用域）
     *
     * @param nfContext NF上下文
     * @return 全局作用域
     */
    private static NfContextScope findGlobalScope(NfContext nfContext) {
        // 从当前作用域开始向上查找，直到找到没有父作用域的作用域
        NfContextScope scope = nfContext.getCurrentScope();
        while (scope != null) {
            String parentScopeId = scope.getParentScopeId();
            if (parentScopeId == null) {
                // 没有父作用域，这就是全局作用域
                return scope;
            }
            scope = nfContext.getScope(parentScopeId);
        }
        return null;
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

        // 检查是否有函数调用
        FunctionCallInfo callInfo = findLastFunctionCall(expression, nfContext);
        if (callInfo == null) {
            return expression;
        }

        // 提取函数调用表达式
        String functionCallExpr = extractFunctionCallExpression(expression, callInfo);
        if (functionCallExpr == null) {
            return expression;
        }

        // 执行函数调用
        Object returnValue = executeFunctionCall(functionCallExpr, nfContext, callInfo.isScriptCall());

        // 替换函数调用为临时变量
        String tempVarName = callInfo.isScriptCall() ? "__script_fun_call_" + System.nanoTime() : "__fun_call_" + System.nanoTime();
        nfContext.getTempVarStorage().put(tempVarName, returnValue);
        String processedExpr = expression.substring(0, callInfo.getStartPos()) + tempVarName + expression.substring(callInfo.getEndPos());

        // 递归处理剩余的函数调用
        return preProcessFunctionCalls(processedExpr, nfContext, jexlContext);
    }

    /**
     * 函数调用信息
     */
    private static class FunctionCallInfo {
        private final int startPos;
        private final int endPos;
        private final boolean scriptCall;
        private final String functionName;
        private final String scriptName;

        FunctionCallInfo(int startPos, int endPos, boolean scriptCall, String functionName, String scriptName) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.scriptCall = scriptCall;
            this.functionName = functionName;
            this.scriptName = scriptName;
        }

        int getStartPos() { return startPos; }
        int getEndPos() { return endPos; }
        boolean isScriptCall() { return scriptCall; }
        String getFunctionName() { return functionName; }
        String getScriptName() { return scriptName; }
    }

    /**
     * 检测字符串是否包含 Lambda 表达式
     * Lambda 格式：(params) -> { body }
     *
     * @param str 待检测的字符串
     * @return 如果包含 Lambda 表达式返回 true
     */
    private static boolean containsLambdaExpression(String str) {
        // 简单检测：是否包含 " -> {" 模式
        int arrowIndex = str.indexOf("->");
        if (arrowIndex == -1) {
            return false;
        }

        // 检查箭头后面是否有 {
        int lbraceIndex = str.indexOf('{', arrowIndex);
        if (lbraceIndex == -1) {
            return false;
        }

        // 检查箭头前面是否有 )
        int rparenIndex = str.lastIndexOf(')', arrowIndex);
        if (rparenIndex == -1) {
            return false;
        }

        // 检查 ) 前面是否有 (
        int lparenIndex = str.lastIndexOf('(', rparenIndex);
        if (lparenIndex == -1) {
            return false;
        }

        // 确认顺序：( ... ) -> { ... }
        return lparenIndex < rparenIndex && rparenIndex < arrowIndex && arrowIndex < lbraceIndex;
    }

    /**
     * 查找最后一个函数调用（最内层）
     *
     * @param expression 表达式
     * @param nfContext NF上下文
     * @return 函数调用信息，如果没有找到返回null
     */
    private static FunctionCallInfo findLastFunctionCall(String expression, NfContext nfContext) {
        // 使用正则表达式匹配函数调用：函数名(参数列表) 或 脚本名称.函数名(参数列表)
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");

        // 先查找导入脚本的函数调用
        java.util.regex.Matcher matcher2 = pattern2.matcher(expression);
        int lastScriptMatchStart = -1;
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

        // 查找当前上下文的函数调用
        java.util.regex.Matcher matcher1 = pattern1.matcher(expression);
        int lastMatchStart = -1;
        String lastFunctionName = null;

        while (matcher1.find()) {
            String functionName = matcher1.group(1);
            // 跳过导入脚本的函数调用（已经处理过了）
            if (lastScriptMatchStart >= 0 && matcher1.start() == lastScriptMatchStart) {
                continue;
            }
            // 检查是否是普通函数或函数引用变量
            boolean hasFunction = nfContext.hasFunction(functionName);
            boolean hasFunRef = nfContext.hasFunRef(functionName);
            if (hasFunction || hasFunRef) {
                lastMatchStart = matcher1.start();
                lastFunctionName = functionName;
            }
        }

        // 优先处理导入脚本的函数调用（如果存在）
        if (lastScriptMatchStart >= 0) {
            int endPos = findFunctionCallEndPos(expression, lastScriptMatchStart,
                lastScriptName.length() + 1 + lastScriptFunctionName.length() + 1);
            return new FunctionCallInfo(lastScriptMatchStart, endPos, true, lastScriptFunctionName, lastScriptName);
        }

        if (lastMatchStart >= 0) {
            int endPos = findFunctionCallEndPos(expression, lastMatchStart, lastFunctionName.length() + 1);
            return new FunctionCallInfo(lastMatchStart, endPos, false, lastFunctionName, null);
        }

        return null;
    }

    /**
     * 查找函数调用的结束位置（右括号位置）
     *
     * @param expression 表达式
     * @param startPos 开始位置
     * @param skipLength 需要跳过的长度（函数名和左括号的长度）
     * @return 结束位置（右括号后）
     */
    private static int findFunctionCallEndPos(String expression, int startPos, int skipLength) {
        int parenDepth = 1;
        int endPos = startPos + skipLength;

        // 找到匹配的右括号
        while (endPos < expression.length() && parenDepth > 0) {
            char c = expression.charAt(endPos);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            } else if (c == '-' && endPos + 1 < expression.length() && expression.charAt(endPos + 1) == '>') {
                // 检查是否是 Lambda 表达式的箭头 ->
                // 如果是，需要跳过整个 Lambda 体 { ... }
                // 找到箭头后的 {
                int lbracePos = expression.indexOf('{', endPos + 2);
                if (lbracePos != -1) {
                    // 跳过 {，并查找匹配的 }
                    int braceDepth = 1;
                    endPos = lbracePos + 1;
                    while (endPos < expression.length() && braceDepth > 0) {
                        char bc = expression.charAt(endPos);
                        if (bc == '{') {
                            braceDepth++;
                        } else if (bc == '}') {
                            braceDepth--;
                        }
                        endPos++;
                    }
                    // 继续外层循环，不要跳过 endPos 的增量
                    continue;
                }
            }
            endPos++;
        }

        return endPos;
    }

    /**
     * 提取函数调用表达式
     * 
     * @param expression 原始表达式
     * @param callInfo 函数调用信息
     * @return 函数调用表达式，如果解析失败返回null
     */
    private static String extractFunctionCallExpression(String expression, FunctionCallInfo callInfo) {
        String functionCallExpr = expression.substring(callInfo.getStartPos(), callInfo.getEndPos());
        
        // 验证表达式是否可以解析为tokens
        try {
            NfToken.tokens(functionCallExpr);
        } catch (Exception e) {
            // 如果解析失败，保留原表达式
            return null;
        }
        
        return functionCallExpr;
    }

    /**
     * 执行函数调用
     * 
     * @param functionCallExpr 函数调用表达式
     * @param nfContext NF上下文
     * @param isScriptCall 是否是脚本函数调用
     * @return 函数返回值
     */
    private static Object executeFunctionCall(String functionCallExpr, NfContext nfContext, boolean isScriptCall) {
        // 将函数调用表达式解析为tokens
        List<Token> tokens;
        try {
            tokens = NfToken.tokens(functionCallExpr);
        } catch (Exception e) {
            throw new NfException(e, "表达式中的函数调用解析失败: " + functionCallExpr);
        }
        
        // 创建函数调用语法节点
        FunCallSyntaxNode funCallNode = new FunCallSyntaxNode();
        funCallNode.setValue(tokens);
        if (!tokens.isEmpty()) {
            funCallNode.setLine(tokens.get(0).getLine());
        }
        
        // 保存当前作用域ID
        String savedScopeIdForPreprocess = nfContext.getCurrentScopeId();
        
        try {
            // 执行函数调用
            return funCallNode.executeFunction(nfContext, funCallNode);
        } catch (Exception e) {
            String errorMsg = isScriptCall ? "表达式中的导入脚本函数调用执行失败: " : "表达式中的函数调用执行失败: ";
            throw new NfException(e, errorMsg + functionCallExpr);
        } finally {
            if (savedScopeIdForPreprocess != null) {
                nfContext.switchScope(savedScopeIdForPreprocess);
            }
        }
    }

    // 临时变量计数器（用于生成唯一变量名）
    private static int tempVarCounter = 0;

    /**
     * 检查字符串是否是有效的标识符
     * 用于判断表达式是否只是单个变量名
     *
     * @param str 待检查的字符串
     * @return 如果是有效标识符返回 true
     */
    private static boolean isValidIdentifier(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 标识符必须以字母、下划线或美元符号开头
        if (!Character.isJavaIdentifierStart(str.charAt(0))) {
            return false;
        }
        // 其余字符必须是字母、数字、下划线或美元符号
        for (int i = 1; i < str.length(); i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

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
