package com.gitee.huanminabc.nullchain.core;


import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernel;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Null终结操作接口 - 提供链式操作的终结方法
 *
 * <p>该接口定义了Null链的终结操作，包括值获取、条件判断、异常处理等功能。
 * 这些方法是链式操作的终点，用于获取最终结果或执行最终操作。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>值获取：获取链式操作的最终结果</li>
 *   <li>条件判断：判断值是否为空或非空</li>
 *   <li>异常处理：提供异常安全的获取方式</li>
 *   <li>默认值处理：提供默认值机制</li>
 *   <li>消费者操作：支持消费者模式的操作</li>
 * </ul>
 *
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>空值安全：所有操作都处理空值情况</li>
 *   <li>异常安全：提供异常安全的获取方式</li>
 *   <li>灵活配置：支持多种获取和判断方式</li>
 *   <li>可序列化：支持序列化传输</li>
 * </ul>
 *
 * @param <T> 终结操作的值的类型
 * @author huanmin
 * @version 1.1.1
 * @see NullKernel 内核接口
 * @see Serializable 序列化接口
 * @since 1.0.0
 */
public interface NullFinality<T> extends NullKernel<T>, Serializable {

    /**
     * 判断上一个任务的值是否为空
     *
     * <p>该方法用于检查链式操作中上一个任务的结果是否为空值。
     * 返回true表示值为空，false表示值不为空。</p>
     *
     * @return true表示值为空，false表示值不为空
     * @example <pre>{@code
     * boolean isEmpty = Null.of(user)
     *     .map(User::getName)
     *     .is();  // 检查用户名是否为空
     * }</pre>
     */
    boolean is();

    /**
     * 判断上一个任务的值是否不为空
     *
     * <p>该方法用于检查链式操作中上一个任务的结果是否不为空值。
     * 返回true表示值不为空，false表示值为空。</p>
     *
     * @return true表示值不为空，false表示值为空
     * @example <pre>{@code
     * boolean isNotEmpty = Null.of(user)
     *     .map(User::getName)
     *     .non();  // 检查用户名是否不为空
     * }</pre>
     */
    boolean non();

    /**
     * 安全获取值 - 抛出检查异常
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则会抛出NullChainCheckException异常，外部调用者需要捕获异常来处理空值情况。
     * 这个方法适用于需要异常拦截单独处理的场景。</p>
     *
     * @return 上一个任务的结果值
     * @throws NullChainCheckException 当值为空时抛出异常
     * @example <pre>{@code
     * try {
     *     String name = Null.of(user)
     *         .map(User::getName)
     *         .getSafe();  // 获取用户名，如果为空则抛出异常
     * } catch (NullChainCheckException e) {
     *     // 处理空值情况
     * }
     * }</pre>
     */
    T getSafe() throws NullChainCheckException;

    /**
     * 获取值 - 抛出运行时异常
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则会抛出运行时异常，并打印出完整的链路信息，便于调试。</p>
     *
     * @return 上一个任务的结果值
     * @throws RuntimeException 当值为空时抛出异常
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .get();  // 获取用户名，如果为空则抛出运行时异常
     * }</pre>
     */
    T get();

    /**
     * 获取值 - 抛出自定义异常
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则会抛出由异常提供者创建的自定义异常。</p>
     *
     * @param <X>               异常类型
     * @param exceptionSupplier 异常提供者
     * @return 上一个任务的结果值
     * @throws X 当值为空时抛出自定义异常
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .get(() -> new IllegalArgumentException("用户名不能为空"));
     * }</pre>
     */
    <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * 获取值 - 抛出带自定义消息的异常
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则会抛出带自定义消息的运行时异常。</p>
     *
     * @param exceptionMessage 异常消息模板
     * @param args             异常消息参数
     * @return 上一个任务的结果值
     * @throws RuntimeException 当值为空时抛出异常
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .get("用户{}的姓名不能为空", user.getId());
     * }</pre>
     */
    T get(String exceptionMessage, Object... args);

    /**
     * 获取值或返回null
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则返回null。</p>
     *
     * @return 上一个任务的结果值，如果为空则返回null
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .orElseNull();  // 获取用户名，如果为空则返回null
     * }</pre>
     */
    T orElseNull();

    /**
     * 获取值或返回默认值
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则返回提供的默认值。默认值不能是空字符串或null。</p>
     *
     * @param defaultValue 默认值
     * @return 上一个任务的结果值，如果为空则返回默认值
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .orElse("未知用户");  // 获取用户名，如果为空则返回默认值
     * }</pre>
     */
    T orElse(T defaultValue);

    /**
     * 获取值或返回默认值（通过Supplier提供）
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则执行Supplier获取默认值。默认值不能是空字符串或null。</p>
     *
     * @param defaultValue 默认值提供者
     * @return 上一个任务的结果值，如果为空则返回默认值
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .orElse(() -> "用户" + user.getId());  // 动态生成默认值
     * }</pre>
     */
    T orElse(Supplier<T> defaultValue);

    /**
     * 创建收集器 - 用于保留链中不同类型的值
     *
     * <p>该方法用于创建收集器，用于在链式操作中保留不同类型的值。
     * 收集器适用于需要查询A的值，然后利用A的值查询B的值，再利用B的值查询C的值，
     * 之后还需要同时使用A、B、C的值的场景。</p>
     *
     * <p><strong>使用场景：</strong>当需要保留链中多个节点的值时，
     * 使用收集器可以减少代码冗余(少了很多的判空代码)，同时保证空值安全。</p>
     *
     * <p><strong>注意：</strong>收集器只会保留最新类型的值，旧的值会被覆盖。</p>
     *
     * @return 收集器实例
     * @example <pre>{@code
     * NullCollect collect = Null.of(user)
     *     .map(User::getProfile)
     *     .map(Profile::getSettings)
     *     .collect();  // 创建收集器，保留所有中间值
     *
     * // 可以同时访问链中的多个值
     * User user = collect.get(User.class);
     * Profile profile = collect.get(Profile.class);
     * Settings settings = collect.get(Settings.class);
     * }</pre>
     */
    NullCollect collect();



    /**
     * 获取上一个任务的值, 如果上一个任务不是空那么就执行action,否则不执行
     */
    void ifPresent(Consumer<? super T> action);


    void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction);


    /**
     * 抓取未知异常, 自行处理异常逻辑
     * 如果出现空值, 那么也会抛出异常
     */
    void capture(Consumer<Throwable> consumer);

    /**
     * 抓取未知异常, 抛出自定义消息的异常
     * 如果出现空值, 那么也会抛出异常
     *
     * @example <pre>{@code
     * Null.of(user)
     *     .map(User::getName)
     *     .except((e) -> System.out.println("发生异常：" + e.getMessage()),
     *             "获取用户姓名时发生异常：用户{}", user.getId());
     * }</pre>
     */
    void doThrow(Class<? extends RuntimeException> exceptionClass, String exceptionMessage, Object... args);


    /**
     * 获取值的长度, 如果值是null那么返回0
     * 1. 如果8大数据类型那么返回的是toString 的长度
     * 2. 如果是集合和数组那么返回的是 length 或者 size的长度
     * 3. 如果是自定义对象内部有length 或者 size方法那么返回的是length 或者 size的长度
     */
    int length();


}