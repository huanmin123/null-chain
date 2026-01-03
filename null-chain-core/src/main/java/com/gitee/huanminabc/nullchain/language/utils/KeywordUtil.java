package com.gitee.huanminabc.nullchain.language.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KeywordUtil {
    /**
     * 定义占用的关键字集合，不能定义为变量名
     * 
     * <p>包含所有 NF 脚本语言的关键字，防止用户将这些关键字用作变量名，避免语法解析错误。
     * 
     * <p>注意：
     * <ul>
     *   <li>系统变量（$preValue、$params、$threadFactoryName）使用 $ 前缀，不需要在这里禁止</li>
     *   <li>用户无法定义以 $ 开头的变量名（标识符解析不支持 $ 作为变量名的开头，只有系统可以使用）</li>
     * </ul>
     * 
     * <p>关键字分类：
     * <ul>
     *   <li>导入相关：import, type, task, as</li>
     *   <li>控制流：if, else, switch, case, default, while, for, in, range</li>
     *   <li>循环控制：break, breakall, continue</li>
     *   <li>执行相关：run, export, echo</li>
     *   <li>逻辑运算符：and, or</li>
     *   <li>布尔常量：true, false</li>
     *   <li>对象创建：new</li>
     *   <li>类型判断：instanceof</li>
     *   <li>其他：this</li>
     * </ul>
     * 
     * <p>使用 HashSet 实现，提供 O(1) 时间复杂度的查找性能。
     */
    private final static Set<String> forbidKeywords;
    
    static {
        Set<String> keywords = new HashSet<>();
        // 导入相关
        keywords.add("import");
        keywords.add("task");
        keywords.add("type");
        keywords.add("as");
        // 控制流
        keywords.add("if");
        keywords.add("else");
        keywords.add("switch");
        keywords.add("case");
        keywords.add("default");
        keywords.add("while");
        keywords.add("for");
        keywords.add("do");
        keywords.add("in");
        keywords.add("range");
        // 循环控制
        keywords.add("break");
        keywords.add("breakall");
        keywords.add("continue");
        // 执行相关
        keywords.add("run");
        keywords.add("export");
        keywords.add("echo");
        // 逻辑运算符
        keywords.add("and");
        keywords.add("or");
        // 布尔常量
        keywords.add("true");
        keywords.add("false");
        // 对象创建
        keywords.add("new");
        // 类型判断
        keywords.add("instanceof");
        // 其他
        keywords.add("this");
        forbidKeywords = Collections.unmodifiableSet(keywords);
    }

    /**
     * 判断是否是禁止的关键字
     *
     * @param name 变量名
     * @return 如果是禁止的关键字返回 true，否则返回 false
     */
    public static boolean isForbidKeyword(String name){
        return forbidKeywords.contains(name);
    }

}
