package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.FunCallSyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
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
    private static final Cache<String, List<Token>> functionCallTokenCache = Caffeine.newBuilder()
        .maximumSize(5000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    private static final Map<String, Class<?>> globalClassCache = new ConcurrentHashMap<>();
    private static final java.util.regex.Pattern INSTANCEOF_PATTERN =
        java.util.regex.Pattern.compile("(\\w+)\\s+instanceof\\s+(\\S+)");
    private static final java.util.regex.Pattern IMPORTED_SCRIPT_ACCESS_PATTERN =
        java.util.regex.Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$][a-zA-Z0-9_$]*)(?!\\s*\\()");
    private static final java.util.regex.Pattern GLOBAL_ACCESS_PATTERN =
        java.util.regex.Pattern.compile("\\bglobal\\.([a-zA-Z_$][a-zA-Z0-9_$]*)");

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
     * JEXL 分层上下文：基础变量视图只读复用，表达式执行过程中的临时变量、导入类型放到 overlay。
     */
    private static class LayeredJexlContext implements JexlContext {
        private final Map<String, Object> baseValues;
        private final Map<String, Object> importedValues;
        private final Map<String, Object> overlayValues = new HashMap<>();

        LayeredJexlContext(Map<String, Object> baseValues, Map<String, Object> importedValues) {
            this.baseValues = baseValues == null ? Collections.emptyMap() : baseValues;
            this.importedValues = importedValues == null ? Collections.emptyMap() : importedValues;
        }

        @Override
        public boolean has(String name) {
            return overlayValues.containsKey(name) || importedValues.containsKey(name) || baseValues.containsKey(name);
        }

        @Override
        public Object get(String name) {
            if (overlayValues.containsKey(name)) {
                return overlayValues.get(name);
            }
            if (importedValues.containsKey(name)) {
                return importedValues.get(name);
            }
            return baseValues.get(name);
        }

        @Override
        public void set(String name, Object value) {
            overlayValues.put(name, value);
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
        java.util.regex.Matcher matcher = INSTANCEOF_PATTERN.matcher(expression);
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
        
        //获取当前作用域
        NfContextScope currentScope = nfContext.getCurrentScope();
        //检查当前作用域是否为null
        if (currentScope == null) {
            throw new NfException("当前作用域为null，无法计算表达式: " + expression + "，currentScopeId: " + nfContext.getCurrentScopeId());
        }
        JexlContext context = new LayeredJexlContext(
            nfContext.getVisibleVariables(),
            nfContext.getResolvedImportValues(NfCalculator::resolveClass)
        );

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
            processedExpr = preProcessFunctionCalls(processedExpr, nfContext);

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
                context.set(varName, resolveClass(className));
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
        } catch (NfTimeoutException e) {
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
        java.util.regex.Matcher matcher = IMPORTED_SCRIPT_ACCESS_PATTERN.matcher(expression);
        
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
                        String tempVarName = nextTempVariableName("__script_" + scriptName + "_" + varName + "_");
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
        java.util.regex.Matcher matcher = GLOBAL_ACCESS_PATTERN.matcher(expression);

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
                    String tempVarName = nextTempVariableName("__global_" + varName + "_");
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
     * @param expression 原始表达式
     * @param nfContext NF上下文
     * @return 处理后的表达式
     */
    private static String preProcessFunctionCalls(String expression, NfContext nfContext) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        String currentExpression = expression;
        while (true) {
            PreparedFunctionCall preparedCall = prepareLastFunctionCall(currentExpression, nfContext);
            if (preparedCall == null) {
                return currentExpression;
            }

            Object returnValue = executeFunctionCall(preparedCall, nfContext);
            String tempVarName = nextTempVariableName(preparedCall.callInfo.isScriptCall() ? "__script_fun_call_" : "__fun_call_");
            nfContext.getTempVarStorage().put(tempVarName, returnValue);

            currentExpression = currentExpression.substring(0, preparedCall.callInfo.getStartPos()) +
                tempVarName +
                currentExpression.substring(preparedCall.callInfo.getEndPos());
        }
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

    private static class PreparedFunctionCall {
        private final FunctionCallInfo callInfo;
        private final String functionCallExpr;
        private final List<Token> tokens;

        private PreparedFunctionCall(FunctionCallInfo callInfo, String functionCallExpr, List<Token> tokens) {
            this.callInfo = callInfo;
            this.functionCallExpr = functionCallExpr;
            this.tokens = tokens;
        }
    }

    /**
     * 查找最后一个函数调用（最内层）
     *
     * @param expression 表达式
     * @param nfContext NF上下文
     * @return 函数调用信息，如果没有找到返回null
     */
    private static FunctionCallInfo findLastFunctionCall(String expression, NfContext nfContext) {
        if (expression == null || expression.isEmpty() ||
            expression.indexOf('(') < 0 || expression.indexOf(')') < 0) {
            return null;
        }

        java.util.ArrayDeque<Integer> parenStack = new java.util.ArrayDeque<>();
        FunctionCallInfo lastCall = null;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            boolean escaped = isEscaped(expression, i);

            if (inSingleQuote) {
                if (current == '\'' && !escaped) {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                if (current == '"' && !escaped) {
                    inDoubleQuote = false;
                }
                continue;
            }
            if (current == '\'' && !escaped) {
                inSingleQuote = true;
                continue;
            }
            if (current == '"' && !escaped) {
                inDoubleQuote = true;
                continue;
            }

            if (current == '(') {
                parenStack.push(i);
                continue;
            }
            if (current != ')' || parenStack.isEmpty()) {
                continue;
            }

            int lparenIndex = parenStack.pop();
            FunctionCallInfo callInfo = resolveFunctionCall(expression, lparenIndex, i + 1, nfContext);
            if (callInfo != null &&
                (lastCall == null || callInfo.getStartPos() > lastCall.getStartPos())) {
                lastCall = callInfo;
            }
        }

        return lastCall;
    }

    private static FunctionCallInfo resolveFunctionCall(String expression, int lparenIndex, int endPos, NfContext nfContext) {
        int cursor = skipWhitespaceBackward(expression, lparenIndex - 1);
        if (cursor < 0) {
            return null;
        }

        int methodEnd = cursor + 1;
        while (cursor >= 0 && Character.isJavaIdentifierPart(expression.charAt(cursor))) {
            cursor--;
        }
        int methodStart = cursor + 1;
        if (methodStart >= methodEnd || !Character.isJavaIdentifierStart(expression.charAt(methodStart))) {
            return null;
        }

        String methodName = expression.substring(methodStart, methodEnd);
        cursor = skipWhitespaceBackward(expression, cursor);

        if (cursor >= 0 && expression.charAt(cursor) == '.') {
            int targetEnd = cursor;
            cursor = skipWhitespaceBackward(expression, cursor - 1);
            if (cursor < 0) {
                return null;
            }

            int targetIdentifierEnd = cursor + 1;
            while (cursor >= 0 && Character.isJavaIdentifierPart(expression.charAt(cursor))) {
                cursor--;
            }
            int targetStart = cursor + 1;
            if (targetStart >= targetIdentifierEnd || !Character.isJavaIdentifierStart(expression.charAt(targetStart))) {
                return null;
            }
            if (targetIdentifierEnd != targetEnd) {
                return null;
            }

            String targetName = expression.substring(targetStart, targetIdentifierEnd);
            if (nfContext.hasImportedScript(targetName)) {
                NfContext scriptContext = nfContext.getImportedScriptContext(targetName);
                if (scriptContext != null && scriptContext.hasFunction(methodName)) {
                    return new FunctionCallInfo(targetStart, endPos, true, methodName, targetName);
                }
            }

            String functionCallExpr = expression.substring(targetStart, endPos);
            if (isJavaMethodCallCandidate(functionCallExpr, targetName, nfContext)) {
                return new FunctionCallInfo(targetStart, endPos, false, targetName + "." + methodName, null);
            }
            return null;
        }

        if (nfContext.hasFunction(methodName) || nfContext.hasFunRef(methodName)) {
            return new FunctionCallInfo(methodStart, endPos, false, methodName, null);
        }

        return null;
    }

    private static int skipWhitespaceBackward(String expression, int index) {
        int cursor = index;
        while (cursor >= 0 && Character.isWhitespace(expression.charAt(cursor))) {
            cursor--;
        }
        return cursor;
    }

    private static boolean isEscaped(String expression, int index) {
        int backslashCount = 0;
        int cursor = index - 1;
        while (cursor >= 0 && expression.charAt(cursor) == '\\') {
            backslashCount++;
            cursor--;
        }
        return (backslashCount & 1) == 1;
    }

    private static boolean isJavaMethodCallCandidate(String functionCallExpr,
                                                     String targetName, NfContext nfContext) {
        if (nfContext.getImportType(targetName) == null && nfContext.getVariable(targetName) == null) {
            return false;
        }

        return containsLambdaLikeArgument(functionCallExpr, nfContext);
    }

    private static boolean containsLambdaLikeArgument(String functionCallExpr, NfContext nfContext) {
        List<String> arguments = extractTopLevelArguments(functionCallExpr);
        if (arguments.isEmpty()) {
            return false;
        }

        for (String argument : arguments) {
            String trimmedArgument = argument.trim();
            if (trimmedArgument.isEmpty()) {
                continue;
            }
            if (containsLambdaArrow(trimmedArgument)) {
                return true;
            }
            if (isValidIdentifier(trimmedArgument) &&
                (nfContext.hasFunRef(trimmedArgument) || nfContext.hasFunction(trimmedArgument))) {
                return true;
            }
        }

        return false;
    }

    private static List<String> extractTopLevelArguments(String functionCallExpr) {
        List<String> arguments = new java.util.ArrayList<>();
        int argsStart = findArgumentsStart(functionCallExpr);
        if (argsStart < 0 || argsStart + 1 >= functionCallExpr.length()) {
            return arguments;
        }

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int parenDepth = 0;
        int braceDepth = 0;
        int bracketDepth = 0;
        int argumentStart = argsStart + 1;

        for (int i = argsStart + 1; i < functionCallExpr.length(); i++) {
            char currentChar = functionCallExpr.charAt(i);
            if (currentChar == '\'' && !inDoubleQuote && !isEscaped(functionCallExpr, i)) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (currentChar == '"' && !inSingleQuote && !isEscaped(functionCallExpr, i)) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }

            if (currentChar == '(') {
                parenDepth++;
                continue;
            }
            if (currentChar == ')') {
                if (parenDepth == 0) {
                    arguments.add(functionCallExpr.substring(argumentStart, i));
                    return arguments;
                }
                parenDepth--;
                continue;
            }
            if (currentChar == '{') {
                braceDepth++;
                continue;
            }
            if (currentChar == '}') {
                if (braceDepth > 0) {
                    braceDepth--;
                }
                continue;
            }
            if (currentChar == '[') {
                bracketDepth++;
                continue;
            }
            if (currentChar == ']') {
                if (bracketDepth > 0) {
                    bracketDepth--;
                }
                continue;
            }
            if (currentChar == ',' && parenDepth == 0 && braceDepth == 0 && bracketDepth == 0) {
                arguments.add(functionCallExpr.substring(argumentStart, i));
                argumentStart = i + 1;
            }
        }

        return arguments;
    }

    private static int findArgumentsStart(String functionCallExpr) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < functionCallExpr.length(); i++) {
            char currentChar = functionCallExpr.charAt(i);
            if (currentChar == '\'' && !inDoubleQuote && !isEscaped(functionCallExpr, i)) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (currentChar == '"' && !inSingleQuote && !isEscaped(functionCallExpr, i)) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote && currentChar == '(') {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsLambdaArrow(String expression) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);
            if (currentChar == '\'' && !inDoubleQuote && !isEscaped(expression, i)) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (currentChar == '"' && !inSingleQuote && !isEscaped(expression, i)) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote &&
                currentChar == '-' && i + 1 < expression.length() && expression.charAt(i + 1) == '>') {
                return true;
            }
        }
        return false;
    }

    private static boolean isLambdaLikeParam(List<Token> paramTokens, NfContext nfContext) {
        if (paramTokens == null || paramTokens.isEmpty()) {
            return false;
        }

        for (Token token : paramTokens) {
            if (token.type == TokenType.ARROW) {
                return true;
            }
        }

        if (paramTokens.size() == 1 && paramTokens.get(0).type == TokenType.IDENTIFIER) {
            String identifier = paramTokens.get(0).value;
            return nfContext.hasFunRef(identifier) || nfContext.hasFunction(identifier);
        }

        return false;
    }

    public static Class<?> resolveClass(String className) {
        try {
            return globalClassCache.computeIfAbsent(className, key -> {
                try {
                    return Class.forName(key);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IllegalStateException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException) {
                throw new NfException(cause, "找不到类: " + className);
            }
            throw e;
        }
    }

    private static PreparedFunctionCall prepareLastFunctionCall(String expression, NfContext nfContext) {
        FunctionCallInfo callInfo = findLastFunctionCall(expression, nfContext);
        if (callInfo == null) {
            return null;
        }

        String functionCallExpr = expression.substring(callInfo.getStartPos(), callInfo.getEndPos());
        try {
            return new PreparedFunctionCall(callInfo, functionCallExpr, tokenizeFunctionCallExpression(functionCallExpr));
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Token> tokenizeFunctionCallExpression(String functionCallExpr) {
        return functionCallTokenCache.get(functionCallExpr, key ->
            Collections.unmodifiableList(new java.util.ArrayList<>(NfToken.tokens(key)))
        );
    }

    /**
     * 执行函数调用
     * 
     * @param preparedCall 已准备好的函数调用
     * @param nfContext NF上下文
     * @return 函数返回值
     */
    private static Object executeFunctionCall(PreparedFunctionCall preparedCall, NfContext nfContext) {
        // 创建函数调用语法节点
        FunCallSyntaxNode funCallNode = new FunCallSyntaxNode();
        funCallNode.setValue(preparedCall.tokens);
        if (!preparedCall.tokens.isEmpty()) {
            funCallNode.setLine(preparedCall.tokens.get(0).getLine());
        }
        
        // 保存当前作用域ID
        String savedScopeIdForPreprocess = nfContext.getCurrentScopeId();
        
        try {
            // 执行函数调用
            return funCallNode.executeFunction(nfContext, funCallNode);
        } catch (NfTimeoutException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = preparedCall.callInfo.isScriptCall() ? "表达式中的导入脚本函数调用执行失败: " : "表达式中的函数调用执行失败: ";
            throw new NfException(e, errorMsg + preparedCall.functionCallExpr);
        } finally {
            if (savedScopeIdForPreprocess != null) {
                nfContext.switchScope(savedScopeIdForPreprocess);
            }
        }
    }

    private static final AtomicLong TEMP_VAR_COUNTER = new AtomicLong();

    private static String nextTempVariableName(String prefix) {
        return prefix + TEMP_VAR_COUNTER.incrementAndGet();
    }

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
