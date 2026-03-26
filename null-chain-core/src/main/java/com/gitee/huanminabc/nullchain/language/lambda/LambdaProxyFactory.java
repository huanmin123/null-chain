package com.gitee.huanminabc.nullchain.language.lambda;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfReturnException;
import com.gitee.huanminabc.nullchain.language.internal.FunDefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunRefInfo;
import com.gitee.huanminabc.nullchain.language.internal.FunTypeInfo;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lambda 动态代理工厂
 * 将 NF 的 FunRefInfo 转换为 Java 函数式接口
 *
 * @author huanmin
 * @date 2025/01/07
 */
@Slf4j
public class LambdaProxyFactory {
    private static final AtomicLong LAMBDA_SCOPE_COUNTER = new AtomicLong();

    private static final class DetachedContextSnapshot {
        private static final DetachedContextSnapshot EMPTY = new DetachedContextSnapshot(null);

        private final Map<String, String> importSnapshot;
        private final Map<String, String> taskSnapshot;
        private final Map<String, FunDefInfo> functionSnapshot;
        private final Map<String, FunRefInfo> funRefSnapshot;
        private final Map<Class<?>, Class<?>> interfaceDefaultImplSnapshot;
        private final Map<String, NfContextScope> importedScriptScopeSnapshot;
        private final Map<String, NfContext> importedScriptContextSnapshot;

        private DetachedContextSnapshot(NfContext context) {
            if (context == null) {
                importSnapshot = Collections.emptyMap();
                taskSnapshot = Collections.emptyMap();
                functionSnapshot = Collections.emptyMap();
                funRefSnapshot = Collections.emptyMap();
                interfaceDefaultImplSnapshot = Collections.emptyMap();
                importedScriptScopeSnapshot = Collections.emptyMap();
                importedScriptContextSnapshot = Collections.emptyMap();
                return;
            }

            importSnapshot = new HashMap<>(context.getImportMap());
            taskSnapshot = new HashMap<>(context.getTaskMap());
            functionSnapshot = new HashMap<>(context.getFunctionMap());
            funRefSnapshot = new HashMap<>(context.getFunRefMap());
            interfaceDefaultImplSnapshot = new HashMap<>(context.getInterfaceDefaultImplMap());
            importedScriptScopeSnapshot = new HashMap<>(context.getImportedScriptScopeMap());
            importedScriptContextSnapshot = new HashMap<>(context.getImportedScriptContextMap());
        }

        private NfContext createExecutionContext() {
            NfContext detachedContext = new NfContext();
            NfRun.prepareContext(detachedContext, null, null);
            detachedContext.endExecution();

            detachedContext.getImportMap().clear();
            detachedContext.getImportMap().putAll(importSnapshot);
            detachedContext.setImportVersion(importSnapshot.isEmpty() ? 0L : 1L);
            detachedContext.setResolvedImportCache(Collections.emptyMap());
            detachedContext.setResolvedImportCacheVersion(-1L);

            detachedContext.getTaskMap().clear();
            detachedContext.getTaskMap().putAll(taskSnapshot);
            detachedContext.getFunctionMap().clear();
            detachedContext.getFunctionMap().putAll(functionSnapshot);
            detachedContext.getFunRefMap().clear();
            detachedContext.getFunRefMap().putAll(funRefSnapshot);

            detachedContext.getInterfaceDefaultImplMap().clear();
            detachedContext.getInterfaceDefaultImplMap().putAll(interfaceDefaultImplSnapshot);

            detachedContext.getImportedScriptScopeMap().clear();
            detachedContext.getImportedScriptScopeMap().putAll(importedScriptScopeSnapshot);
            detachedContext.getImportedScriptContextMap().clear();
            detachedContext.getImportedScriptContextMap().putAll(importedScriptContextSnapshot);
            return detachedContext;
        }
    }

    /**
     * 判断是否是函数式接口
     *
     * @param clazz 要检查的接口
     * @return 如果是函数式接口返回 true
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }

        // 检查是否有 @FunctionalInterface 注解
        if (clazz.getAnnotation(FunctionalInterface.class) != null) {
            return true;
        }

        // 如果没有注解，检查是否只有一个抽象方法
        int abstractMethodCount = 0;
        for (Method method : clazz.getMethods()) {
            if (method.isDefault() || method.isSynthetic()) {
                continue;
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                abstractMethodCount++;
                if (abstractMethodCount > 1) {
                    return false;
                }
            }
        }
        return abstractMethodCount == 1;
    }

    /**
     * 将 NF 的 FunRefInfo 转换为 Java 函数式接口的代理对象
     *
     * @param funRef NF 函数引用信息
     * @param functionalInterface Java 函数式接口类
     * @param context NF 上下文
     * @param line 行号（用于错误信息）
     * @return 代理对象
     */
    public static <T> T createProxy(FunRefInfo funRef, Class<T> functionalInterface, NfContext context, int line) {
        if (!isFunctionalInterface(functionalInterface)) {
            throw new NfException("Line:{}, {} 不是函数式接口", line, functionalInterface.getName());
        }

        // 获取函数式接口的抽象方法
        Method functionalMethod = getFunctionalMethod(functionalInterface);

        log.debug("创建 Lambda 代理: {} -> {}", funRef, functionalInterface.getSimpleName());

        // 创建动态代理
        return (T) Proxy.newProxyInstance(
            functionalInterface.getClassLoader(),
            new Class<?>[]{functionalInterface},
            new LambdaInvocationHandler(funRef, functionalMethod, context, line)
        );
    }

    /**
     * 直接执行 NF Lambda（不创建代理）
     *
     * @param funRef NF 函数引用信息
     * @param args Java 参数
     * @param context NF 上下文
     * @param line 行号
     * @return Lambda 返回值
     */
    public static Object executeLambda(FunRefInfo funRef, Object[] args, NfContext context, int line) {
        FunDefInfo funDef = funRef.getFunDefInfo();
        if (funDef == null || funDef.getBodyNodes() == null) {
            throw new NfException("Line:{}, Lambda 的函数体未定义", line);
        }

        log.debug("开始执行 Lambda: {}, 函数体节点数: {}", funRef, funDef.getBodyNodes().size());

        NfContext executionContext = context;
        boolean temporaryContext = false;
        if (!isContextUsable(context)) {
            log.debug("Lambda 使用临时 detached 上下文执行");
            executionContext = DetachedContextSnapshot.EMPTY.createExecutionContext();
            temporaryContext = true;
        }

        // 使用捕获时的作用域作为父作用域
        String parentScopeId = funRef.getCaptureScopeId();
        if (parentScopeId == null || executionContext.getScope(parentScopeId) == null) {
            parentScopeId = executionContext.getMainScopeId();
        }

        log.debug("Lambda 执行上下文 - 主作用域: {}, 父作用域: {}", executionContext.getMainScopeId(), parentScopeId);

        List<Object> paramValues = args == null ? Collections.emptyList() : Arrays.asList(args);

        if (log.isDebugEnabled()) {
            log.debug("Lambda 参数: {}", paramValues);
        }

        validateLambdaParameters(funDef, paramValues, line);

        // 创建 Lambda 执行作用域
        String lambdaScopeId = generateLambdaScopeId();
        NfContextScope lambdaScope = executionContext.createScope(lambdaScopeId, parentScopeId, NfContextScopeType.ALL);

        log.debug("创建 Lambda 作用域: {}, 父作用域: {}", lambdaScopeId, parentScopeId);

        // 保存当前作用域ID
        String savedScopeId = executionContext.getCurrentScopeId();

        try {
            executionContext.startExecution();

            // 切换到 Lambda 作用域
            executionContext.switchScope(lambdaScopeId);

            // 设置 Lambda 参数
            setupLambdaParameters(lambdaScope, funDef, paramValues, executionContext, line);
            log.debug("Lambda 参数设置完成: {}", funDef.getParameters());

            // 设置闭包变量
            if (funRef.getCapturedVariables() != null) {
                log.debug("设置闭包变量: {}", funRef.getCapturedVariables().keySet());
                for (String varName : funRef.getCapturedVariables().keySet()) {
                    Object varValue = funRef.getCapturedVariables().get(varName);
                    lambdaScope.addVariable(new NfVariableInfo(varName, varValue, Object.class));
                }
            }

            // 执行 Lambda 函数体
            log.debug("开始执行 Lambda 函数体，节点数: {}", funDef.getBodyNodes().size());
            try {
                for (int i = 0; i < funDef.getBodyNodes().size(); i++) {
                    SyntaxNode node = funDef.getBodyNodes().get(i);
                    log.debug("执行节点 [{}/{}]: {} (类型: {})", i + 1, funDef.getBodyNodes().size(),
                        node.getClass().getSimpleName(), node);
                    try {
                        node.run(executionContext, node);
                        log.debug("节点 [{}/{}] 执行成功", i + 1, funDef.getBodyNodes().size());
                    } catch (NfReturnException e) {
                        // NfReturnException 是正常的函数返回，不是错误
                        log.debug("节点 [{}/{}] 执行并触发函数返回 (NfReturnException)", i + 1, funDef.getBodyNodes().size());
                        break; // 提前终止函数体执行
                    } catch (Exception e) {
                        log.error("节点 [{}/{}] 执行失败: {} - {}", i + 1, funDef.getBodyNodes().size(),
                            e.getClass().getSimpleName(), e.getMessage(), e);
                        throw e;
                    }
                }
            } catch (NfReturnException e) {
                // 捕获顶层 NfReturnException
                log.debug("捕获到 NfReturnException，表示函数正常返回");
            }

            // 获取返回值
            Object returnValue = getLambdaReturnValue(lambdaScope);
            log.debug("Lambda 执行完成，返回值: {} (类型: {})", returnValue,
                returnValue != null ? returnValue.getClass().getSimpleName() : "null");

            // 恢复之前的作用域
            executionContext.switchScope(savedScopeId);

            return returnValue;
        } catch (Exception e) {
            log.error("Lambda 执行异常: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);

            // 恢复之前的作用域
            executionContext.switchScope(savedScopeId);
            throw new NfException("Line:{}, Lambda 执行失败: {}", line, e.getMessage(), e);
        } finally {
            executionContext.endExecution();
            executionContext.removeScope(lambdaScopeId);
            if (temporaryContext) {
                executionContext.clear();
            }
        }
    }

    /**
     * 验证 Lambda 参数数量。
     */
    private static void validateLambdaParameters(FunDefInfo funDef, List<Object> paramValues, int line) {
        List<FunDefInfo.FunParameter> parameters = funDef.getParameters();
        boolean hasVarArgs = false;
        int fixedParamCount = parameters.size();
        for (FunDefInfo.FunParameter parameter : parameters) {
            if (parameter.isVarArgs()) {
                hasVarArgs = true;
                fixedParamCount = parameters.size() - 1;
                break;
            }
        }

        if (!hasVarArgs) {
            if (paramValues.size() != parameters.size()) {
                throw new NfException("Line:{}, Lambda 参数数量不匹配，期望 {} 个，实际 {} 个",
                    line, parameters.size(), paramValues.size());
            }
            return;
        }

        if (paramValues.size() < fixedParamCount) {
            throw new NfException("Line:{}, Lambda 参数数量不足，期望至少 {} 个，实际 {} 个",
                line, fixedParamCount, paramValues.size());
        }
    }

    /**
     * 设置 Lambda 参数
     */
    private static void setupLambdaParameters(NfContextScope lambdaScope, FunDefInfo funDef, List<Object> paramValues,
                                              NfContext context, int line) {
        List<FunDefInfo.FunParameter> parameters = funDef.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            FunDefInfo.FunParameter param = parameters.get(i);
            Object value;
            if (param.isVarArgs()) {
                List<Object> varArgsValues = new ArrayList<>();
                for (int j = i; j < paramValues.size(); j++) {
                    varArgsValues.add(paramValues.get(j));
                }
                value = varArgsValues;
            } else {
                value = (i < paramValues.size()) ? paramValues.get(i) : null;
            }

            String parameterType = param.getType() == null ? "Object" : param.getType().trim();
            if (parameterType.startsWith("Fun")) {
                if (!(value instanceof FunRefInfo)) {
                    throw new NfException("Line:{}, Lambda 参数 {} 期望 FunRefInfo 类型，实际是 {}",
                        line, param.getName(), describeValueType(value));
                }
                FunRefInfo nestedFunRef = (FunRefInfo) value;
                lambdaScope.addVariable(new NfVariableInfo(param.getName(), value, FunRefInfo.class, true, nestedFunRef));
                context.addFunRef(param.getName(), nestedFunRef);
                continue;
            }

            String importedTypeName = context.getImportType(parameterType);
            if (importedTypeName == null) {
                throw new NfException("Line:{}, Lambda 参数 {} 类型 {} 未找到",
                    line, param.getName(), parameterType);
            }

            Class<?> parameterClass = context.getResolvedImportClass(parameterType, NfCalculator::resolveClass);
            if (parameterClass == null) {
                parameterClass = NfCalculator.resolveClass(importedTypeName);
            }

            if (value instanceof FunRefInfo && isFunctionalInterface(parameterClass)) {
                value = createFunctionalProxy((FunRefInfo) value, parameterClass, context, line);
            }

            lambdaScope.addVariable(new NfVariableInfo(param.getName(), value, parameterClass));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object createFunctionalProxy(FunRefInfo funRef, Class<?> parameterClass, NfContext context, int line) {
        return createProxy(funRef, (Class) parameterClass, context, line);
    }

    private static String describeValueType(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    /**
     * 获取 Lambda 返回值
     */
    private static Object getLambdaReturnValue(NfContextScope lambdaScope) {
        try {
            Object returnValue = lambdaScope.getVariable("$__return__").getValue();
            return returnValue;
        } catch (Exception e) {
            // 没有返回值，返回 null
            return null;
        }
    }

    private static String generateLambdaScopeId() {
        return "lambda_" + LAMBDA_SCOPE_COUNTER.incrementAndGet();
    }

    private static boolean isContextUsable(NfContext context) {
        if (context == null) {
            return false;
        }
        try {
            String mainScopeId = context.getMainScopeId();
            return mainScopeId != null && context.getScope(mainScopeId) != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 获取函数式接口的唯一抽象方法
     */
    private static Method getFunctionalMethod(Class<?> functionalInterface) {
        Method functionalMethod = null;
        for (Method method : functionalInterface.getMethods()) {
            if (method.isDefault() || method.isSynthetic()) {
                continue;
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                if (functionalMethod != null) {
                    throw new NfException("接口 {} 有多个抽象方法，不是函数式接口", functionalInterface.getName());
                }
                functionalMethod = method;
            }
        }
        if (functionalMethod == null) {
            throw new NfException("接口 {} 没有抽象方法", functionalInterface.getName());
        }
        return functionalMethod;
    }

    /**
     * Lambda 调用处理器
     */
    private static class LambdaInvocationHandler implements InvocationHandler {
        private final FunRefInfo funRef;
        private final Method functionalMethod;
        private final NfContext context;
        private final int line;
        private final ThreadLocal<NfContext> detachedContextHolder;

        public LambdaInvocationHandler(FunRefInfo funRef, Method functionalMethod, NfContext context, int line) {
            this.funRef = funRef;
            this.functionalMethod = functionalMethod;
            this.context = context;
            this.line = line;
            DetachedContextSnapshot snapshot = new DetachedContextSnapshot(context);
            this.detachedContextHolder = new ThreadLocal<NfContext>() {
                @Override
                protected NfContext initialValue() {
                    return snapshot.createExecutionContext();
                }
            };
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 只处理函数式接口的抽象方法
            if (!method.equals(functionalMethod)) {
                // 处理 Object 类的方法（equals, hashCode, toString）
                if (method.getDeclaringClass() == Object.class) {
                    // hashCode()
                    if (method.getName().equals("hashCode")) {
                        return System.identityHashCode(proxy);
                    }
                    // equals(Object)
                    if (method.getName().equals("equals") && args != null && args.length == 1) {
                        return proxy == args[0];
                    }
                    // toString()
                    if (method.getName().equals("toString")) {
                        return funRef.toString();
                    }
                    return null;
                }
                throw new NfException("不支持的 方法调用: {}", method.getName());
            }

            if (log.isDebugEnabled()) {
                log.debug("Lambda 被调用: {}, 参数: {}", funRef, args == null ? "null" : Arrays.toString(args));
            }

            // 执行 NF Lambda（调用静态方法）
            NfContext executionContext = isContextUsable(context) ? context : detachedContextHolder.get();
            return LambdaProxyFactory.executeLambda(funRef, args, executionContext, line);
        }
    }
}
