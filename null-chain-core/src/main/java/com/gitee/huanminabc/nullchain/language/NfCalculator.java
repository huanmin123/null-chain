package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
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
        System.err.println("[NfCalculator] expression=" + expression + ", currentScope=" + currentScope + ", currentScopeId=" + nfContext.getCurrentScopeId());
        //检查当前作用域是否为null
        if (currentScope == null) {
            throw new NfException("当前作用域为null，无法计算表达式: " + expression + "，currentScopeId: " + nfContext.getCurrentScopeId());
        }
        //合并作用域
        mergeScope(nfContext, currentScope, context);
        System.err.println("[NfCalculator] After mergeScope, context vars: " + context.get("i") + ", " + context.get("product"));
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

        try {
            // 预处理表达式，转换 instanceof 等语法（传入 importMap 用于类型解析）
            InstanceofProcessResult processResult = preProcessExpression(expression, importMap);
            String processedExpr = processResult.processedExpression;

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

            return jexl.createExpression(processedExpr).evaluate(context);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NfException(e, "表达式计算错误: " + expression);
        }
    }

    private static void mergeScope(NfContext nfContext, NfContextScope currentScope, JexlContext context) {
        mergeScopeRecursively(nfContext, currentScope, context);
        // 如果当前作用域为null，直接返回
        if (currentScope == null) {
            return;
        }
        //合并当前作用域
        Map<String, Object> currentScopeMap = currentScope.toMap();
        for (Map.Entry<String, Object> entry : currentScopeMap.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 递归合并父作用域
     * 从当前作用域开始，向上遍历所有父作用域，将变量合并到JexlContext中
     *
     * @param nfContext NF上下文
     * @param currentScope 当前作用域
     * @param context Jexl上下文
     */
    private static void mergeScopeRecursively(NfContext nfContext, NfContextScope currentScope, JexlContext context) {
        // 如果当前作用域为null，直接返回
        if (currentScope == null) {
            return;
        }
        //获取父作用域
        NfContextScope parentScope = nfContext.getScope(currentScope.getParentScopeId());
        if (parentScope != null) {
            //先递归合并父作用域（确保父作用域的变量先被设置）
            mergeScopeRecursively(nfContext, parentScope, context);
            //合并当前父作用域的变量
            Map<String, Object> parentScopeMap = parentScope.toMap();
            for (Map.Entry<String, Object> entry : parentScopeMap.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void main(String[] args) {
        Object arithmetic = NfCalculator.arithmetic("2 * (3 + 4)", new HashMap<>());
        System.out.println(arithmetic);

        int a = 1;
        int b = 2;
        Map<String, Object> map = new HashMap();
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
