package com.gitee.huanminabc.nullchain.language.lambda;

import com.gitee.huanminabc.nullchain.language.NfCalculator;
import com.gitee.huanminabc.nullchain.language.NfException;
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
import java.util.List;

/**
 * Lambda 动态代理工厂
 * 将 NF 的 FunRefInfo 转换为 Java 函数式接口
 *
 * @author huanmin
 * @date 2025/01/07
 */
@Slf4j
public class LambdaProxyFactory {

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

        log.info("创建 Lambda 代理: {} -> {}", funRef, functionalInterface.getSimpleName());

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

        log.info("开始执行 Lambda: {}, 函数体节点数: {}", funRef, funDef.getBodyNodes().size());

        // 检查上下文是否有效，如果无效则创建新上下文
        NfContext executionContext = context;
        if (context.getMainScopeId() == null || context.getScope(context.getMainScopeId()) == null) {
            log.info("上下文已被清理，创建新的执行上下文");
            executionContext = new NfContext();
            String mainScopeId = "lambda_main_" + System.nanoTime();
            executionContext.createScope(mainScopeId, null, NfContextScopeType.ALL);
            executionContext.switchScope(mainScopeId);
        }

        // 使用捕获时的作用域作为父作用域
        String parentScopeId = funRef.getCaptureScopeId();
        if (parentScopeId == null || executionContext.getScope(parentScopeId) == null) {
            parentScopeId = executionContext.getMainScopeId();
        }

        log.info("Lambda 执行上下文 - 主作用域: {}, 父作用域: {}", executionContext.getMainScopeId(), parentScopeId);

        // 转换 Java 参数为 NF 参数值
        List<Object> paramValues = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                paramValues.add(arg);
            }
        }

        log.info("Lambda 参数: {}", paramValues);

        // 验证参数数量
        if (paramValues.size() != funDef.getParameters().size()) {
            throw new NfException("Line:{}, Lambda 参数数量不匹配，期望 {} 个，实际 {} 个",
                line, funDef.getParameters().size(), paramValues.size());
        }

        // 创建 Lambda 执行作用域
        String lambdaScopeId = "lambda_" + System.nanoTime();
        NfContextScope lambdaScope = executionContext.createScope(lambdaScopeId, parentScopeId, NfContextScopeType.ALL);

        log.info("创建 Lambda 作用域: {}, 父作用域: {}", lambdaScopeId, parentScopeId);

        // 保存当前作用域ID
        String savedScopeId = executionContext.getCurrentScopeId();

        try {
            // 切换到 Lambda 作用域
            executionContext.switchScope(lambdaScopeId);

            // 设置 Lambda 参数
            setupLambdaParameters(lambdaScope, funDef, paramValues);
            log.info("Lambda 参数设置完成: {}", funDef.getParameters());

            // 设置闭包变量
            if (funRef.getCapturedVariables() != null) {
                log.info("设置闭包变量: {}", funRef.getCapturedVariables().keySet());
                for (String varName : funRef.getCapturedVariables().keySet()) {
                    Object varValue = funRef.getCapturedVariables().get(varName);
                    lambdaScope.addVariable(new NfVariableInfo(varName, varValue, Object.class));
                }
            }

            // 执行 Lambda 函数体
            log.info("开始执行 Lambda 函数体，节点数: {}", funDef.getBodyNodes().size());
            boolean hasReturnStatement = false;
            try {
                for (int i = 0; i < funDef.getBodyNodes().size(); i++) {
                    SyntaxNode node = funDef.getBodyNodes().get(i);
                    log.info("执行节点 [{}/{}]: {} (类型: {})", i + 1, funDef.getBodyNodes().size(),
                        node.getClass().getSimpleName(), node);
                    try {
                        node.run(executionContext, node);
                        log.info("节点 [{}/{}] 执行成功", i + 1, funDef.getBodyNodes().size());
                    } catch (NfReturnException e) {
                        // NfReturnException 是正常的函数返回，不是错误
                        log.info("节点 [{}/{}] 执行并触发函数返回 (NfReturnException)", i + 1, funDef.getBodyNodes().size());
                        hasReturnStatement = true;
                        break; // 提前终止函数体执行
                    } catch (Exception e) {
                        log.error("节点 [{}/{}] 执行失败: {} - {}", i + 1, funDef.getBodyNodes().size(),
                            e.getClass().getSimpleName(), e.getMessage(), e);
                        throw e;
                    }
                }
            } catch (NfReturnException e) {
                // 捕获顶层 NfReturnException
                log.info("捕获到 NfReturnException，表示函数正常返回");
                hasReturnStatement = true;
            }

            // 获取返回值
            Object returnValue = getLambdaReturnValue(lambdaScope);
            log.info("Lambda 执行完成，返回值: {} (类型: {})", returnValue,
                returnValue != null ? returnValue.getClass().getSimpleName() : "null");

            // 恢复之前的作用域
            executionContext.switchScope(savedScopeId);

            // 如果创建了新上下文，清理 Lambda 作用域
            if (executionContext != context) {
                executionContext.removeScope(lambdaScopeId);
            }

            return returnValue;
        } catch (Exception e) {
            log.error("Lambda 执行异常: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);

            // 恢复之前的作用域
            executionContext.switchScope(savedScopeId);

            // 如果创建了新上下文，清理 Lambda 作用域
            if (executionContext != context) {
                executionContext.removeScope(lambdaScopeId);
            }

            throw new NfException("Line:{}, Lambda 执行失败: {}", line, e.getMessage(), e);
        }
    }

    /**
     * 设置 Lambda 参数
     */
    private static void setupLambdaParameters(NfContextScope lambdaScope, FunDefInfo funDef, List<Object> paramValues) {
        List<FunDefInfo.FunParameter> parameters = funDef.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            FunDefInfo.FunParameter param = parameters.get(i);
            Object value = (i < paramValues.size()) ? paramValues.get(i) : null;
            lambdaScope.addVariable(new NfVariableInfo(param.getName(), value, Object.class));
        }
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

        public LambdaInvocationHandler(FunRefInfo funRef, Method functionalMethod, NfContext context, int line) {
            this.funRef = funRef;
            this.functionalMethod = functionalMethod;
            this.context = context;
            this.line = line;
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

            log.info("Lambda 被调用: {}, 参数: {}", funRef, args == null ? "null" : java.util.Arrays.toString(args));

            // 执行 NF Lambda（调用静态方法）
            return LambdaProxyFactory.executeLambda(funRef, args, context, line);
        }
    }
}
