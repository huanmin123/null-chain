package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.jcommon.reflect.BeanCopyUtil;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.ExportSyntaxNode;
import com.gitee.huanminabc.nullchain.language.SyntaxValidator;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 执行语法树最终返回结果
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfRun {

    /**
     * 初始化上下文和作用域
     * 
     * @param context 上下文
     * @param logger 日志
     * @param mainSystemContext 主系统上下文
     * @return 创建的全局作用域
     */
    private static NfContextScope initializeContext(NfContext context, Logger logger, Map<String, Object> mainSystemContext) {
        // 开始执行，记录开始时间用于超时检查
        context.startExecution();
        
        String mainScopeId = NfContext.generateScopeId();
        //设置全局作用域
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        //创建一个作用域(全局)
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        
        //添加日志
        if (logger != null) {
            scope.addVariable(new NfVariableInfo("log", logger, logger.getClass()));
        }

        //设置系统变量
        setupSystemVariables(scope);
        
        //添加系统上下文
        setupMainSystemContext(scope, mainSystemContext);
        
        return scope;
    }

    /**
     * 设置系统变量
     * 
     * @param scope 作用域
     */
    private static void setupSystemVariables(NfContextScope scope) {
        //因为任务的缘故需要把系统上下文传递给语法树 先给默认值,便于测试, 后面的循环会覆盖的如果有的话
        // 使用 $ 前缀标识系统变量，避免与用户定义的变量名冲突
        scope.addVariable(new NfVariableInfo("$params", null, null));
        scope.addVariable(new NfVariableInfo("$preValue", null, null));
        scope.addVariable(new NfVariableInfo("$threadFactoryName", ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME, String.class));
    }

    /**
     * 设置主系统上下文
     * 
     * @param scope 作用域
     * @param mainSystemContext 主系统上下文
     */
    private static void setupMainSystemContext(NfContextScope scope, Map<String, Object> mainSystemContext) {
        //添加系统上下文
        if (mainSystemContext != null) {
            for (Map.Entry<String, Object> entry : mainSystemContext.entrySet()) {
                Object value = entry.getValue();
                Object copiedValue;
                Class<?> valueClass = value != null ? value.getClass() : null;
                
                //优化：对于不可变对象和基本类型包装类，不需要深度复制
                if (value == null || isImmutableType(valueClass)) {
                    copiedValue = value; // 直接使用原值
                } else {
                    //对可变对象进行深度复制,避免被修改
                    copiedValue = BeanCopyUtil.deepCopy(value);
                    valueClass = copiedValue != null ? copiedValue.getClass() : valueClass;
                }
                scope.addVariable(new NfVariableInfo(entry.getKey(), copiedValue, valueClass));
            }
        }
    }

    /**
     * 获取执行结果并清理上下文
     * 
     * @param scope 全局作用域
     * @param context 上下文
     * @return 执行结果
     */
    private static Object getResultAndClear(NfContextScope scope, NfContext context) {
        //获取返回值给调用者
        NfVariableInfo variable = scope.getVariable(ExportSyntaxNode.EXPORT);
        //清除上下文
        context.clear();
        return variable == null ? null : variable.getValue();
    }

    public static Object run(List<SyntaxNode> syntaxNodes, NfContext context, Logger logger, Map<String,Object> mainSystemContext) {
        // 在执行前进行语法验证，提前发现语法错误
        SyntaxValidator.validate(syntaxNodes);
        
        //初始化上下文和作用域
        NfContextScope scope = initializeContext(context, logger, mainSystemContext);

        //执行语法节点
        SyntaxNodeFactory.executeAll(syntaxNodes, context);
        
        //获取结果并清理
        return getResultAndClear(scope, context);
    }
    
    /**
     * 运行语法树（支持性能监控）
     * 
     * @param syntaxNodes 语法节点列表
     * @param context 上下文
     * @param logger 日志
     * @param mainSystemContext 主系统上下文
     * @param enablePerformanceMonitoring 是否启用性能监控
     * @return 执行结果
     */
    public static Object run(List<SyntaxNode> syntaxNodes, NfContext context, Logger logger, 
                            Map<String,Object> mainSystemContext, boolean enablePerformanceMonitoring) {
        NfPerformanceMonitor monitor = enablePerformanceMonitoring ? new NfPerformanceMonitor() : null;
        
        if (monitor != null) {
            monitor.start();
        }
        
        // 在执行前进行语法验证，提前发现语法错误
        SyntaxValidator.validate(syntaxNodes);
        
        //初始化上下文和作用域
        NfContextScope scope = initializeContext(context, logger, mainSystemContext);

        //执行语法节点（如果启用性能监控，传递monitor）
        if (monitor != null) {
            SyntaxNodeFactory.executeAll(syntaxNodes, context, monitor);
        } else {
            SyntaxNodeFactory.executeAll(syntaxNodes, context);
        }
        
        //如果启用性能监控，生成并输出报告
        if (monitor != null) {
            NfPerformanceReport report = monitor.generateReport();
            if (logger != null) {
                logger.info("NF脚本性能报告：\n{}", report);
            }
        }
        
        //获取结果并清理
        return getResultAndClear(scope, context);
    }

    //运行语法树
    public static Object run(List<SyntaxNode> syntaxNodes, Logger logger, Map<String,Object> mainSystemContext) {
        //创建上下文
        NfContext context = new NfContext();
        return run(syntaxNodes, context, logger, mainSystemContext);
    }
    
    /**
     * 运行语法树（支持性能监控）
     * 
     * @param syntaxNodes 语法节点列表
     * @param logger 日志
     * @param mainSystemContext 主系统上下文
     * @param enablePerformanceMonitoring 是否启用性能监控
     * @return 执行结果
     */
    public static Object run(List<SyntaxNode> syntaxNodes, Logger logger, Map<String,Object> mainSystemContext, boolean enablePerformanceMonitoring) {
        //创建上下文
        NfContext context = new NfContext();
        return run(syntaxNodes, context, logger, mainSystemContext, enablePerformanceMonitoring);
    }
    
    public static Object run(List<SyntaxNode> syntaxNodes) {
        //创建上下文
        NfContext context = new NfContext();
        return run(syntaxNodes, context, null, null);
    }

    /**
     * 判断类型是否为不可变类型
     * 不可变类型包括：
     * - 基本类型的包装类（String, Integer, Long, Double, Float, Boolean, Byte, Short, Character）
     * - 数值类型（BigInteger, BigDecimal）
     * - 日期时间类型（Date, LocalDate, LocalDateTime, LocalTime）
     * - Class类型
     * 
     * @param clazz 类型
     * @return 如果是不可变类型返回true，否则返回false
     */
    private static boolean isImmutableType(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        // 基本类型包装类
        if (clazz == String.class || 
            clazz == Integer.class || 
            clazz == Long.class || 
            clazz == Double.class || 
            clazz == Float.class || 
            clazz == Boolean.class || 
            clazz == Byte.class || 
            clazz == Short.class || 
            clazz == Character.class) {
            return true;
        }
        // 数值类型
        if (clazz == BigInteger.class || clazz == BigDecimal.class) {
            return true;
        }
        // 日期时间类型
        if (clazz == Date.class || 
            clazz == LocalDate.class || 
            clazz == LocalDateTime.class || 
            clazz == LocalTime.class) {
            return true;
        }
        // Class类型
        if (clazz == Class.class) {
            return true;
        }
        // 基本类型
        if (clazz.isPrimitive()) {
            return true;
        }
        return false;
    }

}
