package com.gitee.huanminabc.nullchain.common;

import java.util.regex.Pattern;

/**
 * Null链框架配置常量类
 * 
 * <p>该类包含了Null链框架中所有使用的配置常量，用于统一管理框架配置参数。
 * 通过使用常量而不是硬编码值，提高了代码的可维护性和一致性。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 在代码中使用配置常量
 * StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
 * String result = NullConstants.PLACEHOLDER_PATTERN.matcher(message).replaceAll("%s");
 * }</pre>
 * 
 * @author huanmin
 * @since 1.1.1
 * @version 1.1.1
 */
public final class NullConstants {
    
    /**
     * 私有构造函数，防止实例化
     */
    private NullConstants() {}
    
    /**
     * StringBuilder初始容量，用于优化字符串拼接性能
     * <p>默认值128可以避免大部分场景下的扩容操作，平衡内存使用和性能。
     * 如果链式操作日志较长，可以适当增加此值。</p>
     */
    public static final int STRING_BUILDER_INITIAL_CAPACITY = 64;
    
    /**
     * 占位符正则表达式Pattern，用于将消息模板中的占位符{}替换为%s
     * <p>预编译正则表达式，避免每次调用时重新编译，提升性能。
     * 用于checkNull等方法中的消息格式化。</p>
     */
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\s*}");
    
    /**
     * 空对象数组常量，用于避免重复创建空数组
     * <p>在多个方法中需要处理null参数时，使用此常量替代创建新的空数组，
     * 减少对象创建开销，提升性能。</p>
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
}

