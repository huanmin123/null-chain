package com.gitee.huanminabc.nullchain.leaf.check;

import com.gitee.huanminabc.nullchain.common.NullKernel;
import com.gitee.huanminabc.nullchain.common.function.NullFun;

/**
 * Null多级判空工具接口 - 提供多级空值检查功能
 * 
 * <p>该接口提供了多级判空工具，与现有的 `NullChain.of()` 不同，该工具会**全部判定一遍**所有节点，
 * 收集所有为空的节点信息，然后统一处理。适用于需要检查多个字段是否为空，并统一记录日志和抛出异常的场景。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>多级判空：支持链式调用，检查多个字段是否为空</li>
 *   <li>全部判定：不是遇到第一个空就终止，而是全部判定一遍所有节点</li>
 *   <li>链路跟踪：显示 `a.b.c?.d.e?.f` 格式（`?` 表示空节点）</li>
 *   <li>统一处理：收集所有空节点信息后统一抛出异常或返回结果</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 示例：基本使用
 * Null.ofCheck(user)
 *     .of(User::getId)
 *     .of(User::getName)
 *     .of(User::getEmail)
 *     .doThrow(IllegalArgumentException.class);
 * 
 * // 示例3：返回布尔值
 * boolean hasNull = Null.ofCheck(user)
 *     .of(User::getId)
 *     .of(User::getName)
 *     .of(User::getEmail)
 *     .is();
 * }</pre>
 * 
 * @param <T> 检查对象的类型
 * @author huanmin
 * @since 1.1.2
 * @version 1.1.2
 * @see NullKernel 内核接口
 */
public interface NullCheck<T> extends NullKernel<T> {

    /**
     * 检查指定字段是否为空
     * 
     * <p>该方法会对当前值应用字段访问函数，检查字段是否为空。
     * 与 `NullChain.of()` 不同，该方法不会因为遇到空值而中断，而是继续执行后续检查。</p>
     * 
     * @param <U> 检查字段的类型
     * @param function 字段访问函数
     * @return 新的NullCheck链，可以继续链式调用
     * 
     * @example
     * <pre>{@code
     * Null.ofCheck(user)
     *     .of(User::getId)  // 检查用户ID是否为空
     *     .of(User::getName)  // 继续检查用户名为空
     *     .of(User::getEmail)  // 继续检查邮箱为空
     *     .is();  // 返回是否有空值
     * }</pre>
     */
    <U> NullCheck<T> of(NullFun<? super T, ? extends U> function);

    /**
     * 映射到内部对象并继续判空
     * 
     * <p>该方法会对当前值应用映射函数，进入内部对象继续判空检查。
     * 与 `of()` 不同，`map()` 会改变当前检查对象的类型，可以继续对映射后的对象进行判空检查。</p>
     * 
     * <p>例如：检查 `user.address.city` 时，可以先 `map(User::getAddress)` 进入 Address 对象，
     * 然后继续 `of(Address::getCity)` 检查城市是否为空。</p>
     * 
     * @param <U> 映射后的对象类型
     * @param function 映射函数，将当前对象映射到内部对象
     * @return 新的NullCheck链，类型变为映射后的类型，可以继续链式调用
     * 
     * @example
     * <pre>{@code
     * Null.ofCheck(user)
     *     .map(User::getAddress)  // 进入 Address 对象
     *     .of(Address::getCity)  // 检查城市是否为空
     *     .of(Address::getStreet)  // 检查街道是否为空
     *     .is();  // 返回是否有空值
     * }</pre>
     */
    <U> NullCheck<U> map(NullFun<? super T, ? extends U> function);

    /**
     * 抛出异常（如果有空值）
     * 
     * <p>该方法会检查所有已检查的节点，如果有任何节点为空，则抛出指定类型的异常。
     * 异常消息会包含完整的链路信息（如 `a.b.c?.d.e?.f`）和所有空节点的描述信息。</p>
     * 
     * @param exceptionClass 异常类型，必须是 RuntimeException 的子类
     * @throws RuntimeException 当有任何节点为空时抛出异常
     * 
     * @example
     * <pre>{@code
     * Null.ofCheck(user)
     *     .of(User::getId)
     *     .of(User::getName)
     *     .of(User::getEmail)
     *     .doThrow(IllegalArgumentException.class);  // 如果有空值，抛出异常
     * }</pre>
     */
    void doThrow(Class<? extends RuntimeException> exceptionClass, String prefixMessage);
    void doThrow( String prefixMessage);
    void doThrow();
    void doThrow(Class<? extends RuntimeException> exceptionClass);


    /**
     * 判断是否有空值
     * 
     * <p>该方法会检查所有已检查的节点，如果有任何节点为空，则返回 true，否则返回 false。</p>
     * 
     * @return 如果有任何节点为空则返回 true，否则返回 false
     * 
     * @example
     * <pre>{@code
     * boolean hasNull = Null.ofCheck(user)
     *     .of(User::getId)
     *     .of(User::getName)
     *     .of(User::getEmail)
     *     .is();  // 返回是否有空值
     * }</pre>
     */
    boolean is();
}

