package com.gitee.huanminabc.nullchain.leaf.stream;

import com.gitee.huanminabc.nullchain.common.NullKernel;

/**
 * Null双精度流操作接口 - 提供空值安全的双精度流聚合功能
 * 
 * <p>该接口提供了对双精度流操作的空值安全封装，支持各种聚合操作如求和、求最值、计数、求平均值等。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>求和：计算双精度流中所有元素的总和</li>
 *   <li>最值：获取双精度流中的最大值和最小值</li>
 *   <li>计数：统计双精度流中元素的数量</li>
 *   <li>平均值：计算双精度流中所有元素的平均值</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>类型安全：专门处理Double类型的流操作</li>
 *   <li>高效聚合：提供高效的数值聚合操作</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 */
public interface NullDoubleStream extends NullKernel<Double> {

    /**
     * 求和操作 - 计算双精度流中所有元素的总和
     * 
     * <p>该方法用于计算当前双精度流中所有元素的总和。
     * 如果流为空或包含null值，会返回0.0。</p>
     * 
     * @return 所有元素的总和
     * 
     * @example
     * <pre>{@code
     * double total = Null.of(Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5))
     *     .stream()
     *     .mapToDouble(Double::doubleValue)
     *     .sum();  // 计算总和：16.5
     * }</pre>
     */
    double sum();
    
    /**
     * 求最小值操作 - 获取双精度流中的最小值
     * 
     * <p>该方法用于获取当前双精度流中的最小值。
     * 如果流为空或包含null值，会返回Double.MAX_VALUE。</p>
     * 
     * @return 流中的最小值
     * 
     * @example
     * <pre>{@code
     * double min = Null.of(Arrays.asList(5.5, 2.2, 8.8, 1.1, 9.9))
     *     .stream()
     *     .mapToDouble(Double::doubleValue)
     *     .min();  // 获取最小值：1.1
     * }</pre>
     */
    double min();
    
    /**
     * 求最大值操作 - 获取双精度流中的最大值
     * 
     * <p>该方法用于获取当前双精度流中的最大值。
     * 如果流为空或包含null值，会返回Double.MIN_VALUE。</p>
     * 
     * @return 流中的最大值
     * 
     * @example
     * <pre>{@code
     * double max = Null.of(Arrays.asList(5.5, 2.2, 8.8, 1.1, 9.9))
     *     .stream()
     *     .mapToDouble(Double::doubleValue)
     *     .max();  // 获取最大值：9.9
     * }</pre>
     */
    double max();
    
    /**
     * 计数操作 - 统计双精度流中元素的数量
     * 
     * <p>该方法用于统计当前双精度流中元素的数量。
     * null值会被忽略，不计入总数。</p>
     * 
     * @return 流中元素的数量
     * 
     * @example
     * <pre>{@code
     * long count = Null.of(Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5))
     *     .stream()
     *     .mapToDouble(Double::doubleValue)
     *     .count();  // 统计数量：5
     * }</pre>
     */
    long count();
    
    /**
     * 求平均值操作 - 计算双精度流中所有元素的平均值
     * 
     * <p>该方法用于计算当前双精度流中所有元素的平均值。
     * 如果流为空或包含null值，会返回0.0。</p>
     * 
     * @return 所有元素的平均值
     * 
     * @example
     * <pre>{@code
     * double avg = Null.of(Arrays.asList(1.1, 2.2, 3.3, 4.4, 5.5))
     *     .stream()
     *     .mapToDouble(Double::doubleValue)
     *     .average();  // 计算平均值：3.3
     * }</pre>
     */
    double average();
}
