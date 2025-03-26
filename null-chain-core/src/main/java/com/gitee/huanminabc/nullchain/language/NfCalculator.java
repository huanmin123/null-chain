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
        JexlContext context = new MapContext();
        //获取当前作用域
        NfContextScope currentScope = nfContext.getCurrentScope();
        //合并作用域
        mergeScope(nfContext, currentScope, context);
        //获取类型导入
        Map<String, String> importMap = nfContext.getImportMap();
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

        try {
            return jexl.createExpression(expression).evaluate(context);
        } catch (Exception e) {
            throw new NfException(e, "表达式计算错误: " + expression);
        }
    }

    private static void mergeScope(NfContext nfContext, NfContextScope currentScope, JexlContext context) {
        mergeScope__(nfContext, currentScope, context);
        //合并当前作用域
        Map<String, Object> currentScopeMap = currentScope.toMap();
        for (Map.Entry<String, Object> entry : currentScopeMap.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
    }

    //递归合并父作用域
    private static void mergeScope__(NfContext nfContext, NfContextScope currentScope, JexlContext context) {
        //获取父作用域
        NfContextScope parentScope = nfContext.getScope(currentScope.getParentScopeId());
        if (parentScope != null) {
            //合并父作用域
            mergeScope__(nfContext, parentScope, context);
            //合并当前作用域
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
