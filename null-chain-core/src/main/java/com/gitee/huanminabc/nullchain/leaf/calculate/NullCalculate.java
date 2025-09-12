package com.gitee.huanminabc.nullchain.leaf.calculate;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.core.NullChain;
import java.util.function.Function;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Null计算操作接口 - 提供空值安全的数值计算功能
 * 
 * <p>该接口提供了对数值计算操作的空值安全封装，支持各种数学运算如加减乘除、取绝对值、取最大值等。
 * 所有计算操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>基本运算：add、subtract、multiply、divide等基本数学运算</li>
 *   <li>数值处理：negate取反、abs取绝对值等</li>
 *   <li>比较运算：max、min等比较操作</li>
 *   <li>幂运算：pow幂运算</li>
 *   <li>舍入运算：round四舍五入</li>
 *   <li>映射运算：map数值映射</li>
 * </ul>
 * 
 * <h3>技术特点：</h3>
 * <ul>
 *   <li>精度保证：底层采用BigDecimal确保计算精度</li>
 *   <li>参数处理：方法入参取值都是doubleValue，避免精度丢失</li>
 *   <li>计算过程：整个计算过程都使用BigDecimal</li>
 *   <li>类型要求：要求实现Number的类必须实现doubleValue方法并返回正确的值</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>精度保证：使用BigDecimal确保计算精度</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>异常处理：完善的异常处理机制</li>
 * </ul>
 * 
 * @param <T> 计算值的类型，必须继承自BigDecimal
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 * @see BigDecimal 高精度数值类型
 */
public interface NullCalculate<T extends BigDecimal> extends  NullKernel<T> {

    /**
     * 加法运算
     * 
     * <p>将当前值与指定值相加，返回新的计算链。</p>
     * 
     * @param t2 要相加的值
     * @param <V> 加数的类型，必须继承自Number
     * @return 新的计算链，包含相加后的结果
     */
    <V extends Number> NullCalculate<T> add(V t2);
    
    /**
     * 加法运算（带默认值）
     * 
     * <p>将当前值与指定值相加，如果当前值为null则使用默认值进行运算。</p>
     * 
     * @param t2 要相加的值
     * @param defaultValue 当前值为null时使用的默认值
     * @param <V> 加数的类型，必须继承自Number
     * @return 新的计算链，包含相加后的结果
     */
    <V extends Number> NullCalculate<T> add(V t2,V defaultValue);

    /**
     * 减法运算
     * 
     * <p>将当前值减去指定值，返回新的计算链。</p>
     * 
     * @param t2 要减去的值
     * @param <V> 减数的类型，必须继承自Number
     * @return 新的计算链，包含相减后的结果
     */
    <V extends Number> NullCalculate<T> sub(V t2);
    
    /**
     * 减法运算（带默认值）
     * 
     * <p>将当前值减去指定值，如果当前值为null则使用默认值进行运算。</p>
     * 
     * @param t2 要减去的值
     * @param defaultValue 当前值为null时使用的默认值
     * @param <V> 减数的类型，必须继承自Number
     * @return 新的计算链，包含相减后的结果
     */
    <V extends Number> NullCalculate<T> sub(V t2, V defaultValue);

    /**
     * 乘法运算
     * 
     * <p>将当前值与指定值相乘，返回新的计算链。</p>
     * 
     * @param t2 要相乘的值
     * @param <V> 乘数的类型，必须继承自Number
     * @return 新的计算链，包含相乘后的结果
     */
    <V extends Number> NullCalculate<T> mul(V t2);

    /**
     * 乘法运算（带默认值）
     * 
     * <p>将当前值与指定值相乘，如果当前值为null则使用默认值进行运算。</p>
     * 
     * @param t2 要相乘的值
     * @param defaultValue 当前值为null时使用的默认值
     * @param <V> 乘数的类型，必须继承自Number
     * @return 新的计算链，包含相乘后的结果
     */
    <V extends Number> NullCalculate<T> mul(V t2, V defaultValue);

    /**
     * 除法运算
     * 
     * <p>将当前值除以指定值，返回新的计算链。</p>
     * 
     * @param t2 要除以的值
     * @param <V> 除数的类型，必须继承自Number
     * @return 新的计算链，包含相除后的结果
     */
    <V extends Number> NullCalculate<T> div(V t2);
    
    /**
     * 除法运算（带默认值）
     * 
     * <p>将当前值除以指定值，如果当前值为null则使用默认值进行运算。</p>
     * 
     * @param t2 要除以的值
     * @param defaultValue 当前值为null时使用的默认值
     * @param <V> 除数的类型，必须继承自Number
     * @return 新的计算链，包含相除后的结果
     */
    <V extends Number> NullCalculate<T> div(V t2, V defaultValue);


    /**
     * 取反运算
     * 
     * <p>将当前值取反，正数变负数，负数变正数。例如：-1.0变成1.0，1.0变成-1.0。</p>
     * 
     * @return 新的计算链，包含取反后的结果
     */
    NullCalculate<T> negate();

    /**
     * 取绝对值运算
     * 
     * <p>将当前值取绝对值，负数变正数，正数保持不变。例如：-1.0变成1.0。</p>
     * 
     * @return 新的计算链，包含绝对值后的结果
     */
    NullCalculate<T> abs();

    /**
     * 取最大值运算
     * 
     * <p>比较当前值与指定值，返回较大的值。</p>
     * 
     * @param t2 要比较的值
     * @return 新的计算链，包含最大值
     */
    NullCalculate<T> max(T t2);

    /**
     * 取最小值运算
     * 
     * <p>比较当前值与指定值，返回较小的值。</p>
     * 
     * @param t2 要比较的值
     * @return 新的计算链，包含最小值
     */
    NullCalculate<T> min(T t2);


    /**
     * 幂运算
     * 
     * <p>将当前值进行n次幂运算，例如：pow(2)表示平方运算。</p>
     * 
     * @param n 幂次
     * @return 新的计算链，包含幂运算后的结果
     */
    NullCalculate<T> pow(int n);


    /**
     * 舍入运算
     * 
     * <p>将当前值按照指定的精度和舍入模式进行舍入处理。</p>
     * 
     * @param newScale 新的精度（小数位数）
     * @param roundingMode 舍入模式
     * @return 新的计算链，包含舍入后的结果
     */
    NullCalculate<T> round(int newScale, RoundingMode roundingMode);

    /**
     * 默认舍入运算
     * 
     * <p>将当前值按照默认精度（保留2位小数）和四舍五入模式进行舍入处理。</p>
     * 
     * @return 新的计算链，包含舍入后的结果
     */
    NullCalculate<T> round();

    /**
     * 数值映射运算
     * 
     * <p>将BigDecimal类型的计算结果映射为其他Number类型的值。</p>
     * 
     * @param pickValue 映射函数，将BigDecimal转换为目标类型
     * @param <V> 目标数值类型，必须继承自Number
     * @return 新的链，包含映射后的结果
     */
    <V extends Number> NullChain<V> map(Function<BigDecimal, V> pickValue);
}
