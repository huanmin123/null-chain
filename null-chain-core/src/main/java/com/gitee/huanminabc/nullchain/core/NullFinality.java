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
     * 获取值 - 抛出指定类型的异常
     *
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，
     * 则会抛出指定类型的运行时异常，并支持自定义异常消息。</p>
     *
     * @param <X>              异常类型，必须是 RuntimeException 的子类
     * @param exceptionClass   异常类，必须是 RuntimeException 的子类
     * @param exceptionMessage 异常消息模板，支持 {} 占位符
     * @param args             异常消息参数，用于填充占位符
     * @return 上一个任务的结果值
     * @throws X 当值为空时抛出指定类型的异常
     * @example <pre>{@code
     * String name = Null.of(user)
     *     .map(User::getName)
     *     .get(IllegalArgumentException.class, "用户{}的姓名不能为空", user.getId());
     * }</pre>
     */
    <X extends RuntimeException> T get(Class<X> exceptionClass, String exceptionMessage, Object... args) throws X;

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
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，则返回提供的默认值</p>
     *
     * @param defaultValue 默认值 不能是null否则会报错, 支持空字符串, 如果想要返回null可以使用orElseNull
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
     * <p>该方法用于获取链式操作的最终结果。如果上一个任务的结果为空，则执行Supplier获取默认值</p>
     *
     * @param defaultValue 默认值提供者,  不能是null否则会报错, 支持空字符串, 如果想要返回null可以使用orElseNull
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
     * 如果上一个任务的值存在则执行给定的消费操作
     *
     * <p>当链式操作的结果不为空时，执行指定的消费操作。如果值为空，则不执行任何操作。</p>
     *
     * @param action 当值非空时要执行的消费操作
     * 
     * @example
     * <pre>{@code
     * // 当用户名不为空时，打印用户名
     * Null.of(user)
     *     .map(User::getName)
     *     .ifPresent(name -> System.out.println("用户名: " + name));
     * 
     * // 当用户不为空时，保存用户信息
     * Null.of(user)
     *     .ifPresent(u -> userService.save(u));
     * }</pre>
     */
    void ifPresent(Consumer<? super T> action);

    /**
     * 如果上一个任务的值存在则执行action，否则执行emptyAction
     *
     * <p>当链式操作的结果不为空时，执行指定的消费操作；如果值为空，则执行空值处理操作。</p>
     *
     * @param action      当值非空时要执行的消费操作
     * @param emptyAction 当值为空时要执行的操作
     * 
     * @example
     * <pre>{@code
     * // 当用户名不为空时打印用户名，否则打印提示信息
     * Null.of(user)
     *     .map(User::getName)
     *     .ifPresentOrElse(
     *         name -> System.out.println("用户名: " + name),
     *         () -> System.out.println("用户名为空")
     *     );
     * 
     * // 当用户不为空时保存，否则记录日志
     * Null.of(user)
     *     .ifPresentOrElse(
     *         u -> userService.save(u),
     *         () -> log.warn("用户为空，无法保存")
     *     );
     * }</pre>
     */
    void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction);

    /**
     * 抓取链路中的未知异常并交由调用方处理
     *
     * <p>当链式操作执行过程中发生异常时，捕获异常并交由调用方处理。
     * 如果出现空值，也会抛出异常。</p>
     *
     * @param consumer 异常处理逻辑，接收捕获到的异常
     * 
     * @example
     * <pre>{@code
     * // 捕获异常并记录日志
     * Null.of(user)
     *     .map(User::getName)
     *     .map(name -> name.substring(0, 100))  // 可能抛出StringIndexOutOfBoundsException
     *     .capture(e -> log.error("处理用户名时发生异常", e));
     * 
     * // 捕获异常并发送告警
     * Null.of(data)
     *     .map(this::processData)
     *     .capture(e -> {
     *         alertService.sendAlert("数据处理异常: " + e.getMessage());
     *         e.printStackTrace();
     *     });
     * }</pre>
     */
    void capture(Consumer<Throwable> consumer);

    /**
     * 抛出指定类型和消息的运行时异常
     *
     * <p>当链路执行过程中出现异常或空值时，使用给定的异常类型和消息抛出。</p>
     *
     * @param exceptionClass   要抛出的异常类型，必须是RuntimeException的子类
     * @param exceptionMessage 异常消息模板，支持{}占位符
     * @param args             异常消息参数
     * 
     * @example
     * <pre>{@code
     * // 当用户名为空时，抛出带自定义消息的异常
     * Null.of(user)
     *     .map(User::getName)
     *     .doThrow(IllegalArgumentException.class, "用户{}的姓名不能为空", user.getId());
     * 
     * // 当数据为空时，抛出业务异常
     * Null.of(data)
     *     .doThrow(BusinessException.class, "数据不存在，ID: {}", dataId);
     * }</pre>
     */
    void doThrow(Class<? extends RuntimeException> exceptionClass, String exceptionMessage, Object... args);

    /**
     * 获取当前值的"长度"信息
     *
     * <p>规则：</p>
     * <ul>
     *   <li>如果值为null，返回0</li>
     *   <li>基本类型/包装类型：返回{@code toString()}的长度</li>
     *   <li>集合和数组：返回{@code size()}或{@code length}</li>
     *   <li>自定义对象：若存在{@code length()}或{@code size()}方法则调用其结果</li>
     * </ul>
     *
     * @return 长度信息，null时为0
     * 
     * @example
     * <pre>{@code
     * // 获取字符串长度
     * int len = Null.of("Hello")
     *     .length();  // 返回 5
     * 
     * // 获取集合大小
     * int size = Null.of(Arrays.asList(1, 2, 3))
     *     .length();  // 返回 3
     * 
     * // 获取数组长度
     * int arrLen = Null.of(new int[]{1, 2, 3, 4})
     *     .length();  // 返回 4
     * 
     * // 获取数字的字符串长度
     * int numLen = Null.of(12345)
     *     .length();  // 返回 5 (toString()的长度)
     * }</pre>
     */
    int length();


}