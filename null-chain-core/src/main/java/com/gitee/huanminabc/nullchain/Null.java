package com.gitee.huanminabc.nullchain;


import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.jcommon.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.jcommon.str.StringUtil;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.leaf.check.NullCheckBase;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import com.gitee.huanminabc.nullchain.leaf.check.NullCheck;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Null链式编程工具类 - 空值安全的流式API
 *
 * <p>这是一个强大的空值安全编程工具，提供了类似Optional但功能更丰富的链式API。
 * 通过Null链，可以优雅地处理可能为空的值，避免NullPointerException。</p>
 *
 * <h3>主要特性：</h3>
 * <ul>
 *   <li>空值安全的链式操作</li>
 *   <li>支持多种数据类型（基本类型、集合、数组等）</li>
 *   <li>提供丰富的转换和操作方法</li>
 *   <li>支持异步操作</li>
 *   <li>完整的日志追踪</li>
 * </ul>
 *
 * <h3>使用要求：</h3>
 * <ol>
 *   <li>类必须实现get/set方法 (强制必须实现,否则你编译不通过)</li>
 *   <li>类必须有空构造方法 (必须)</li>
 *   <li>类中全部字段必须使用包装类型 (必须)</li>
 *   <li>可以使用lombok的@Data注解来实现</li>
 * </ol>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 基本使用
 * String result = Null.of(user)
 *     .map(User::getName)
 *     .map(String::toUpperCase)
 *     .orElse("UNKNOWN");
 *
 * // 集合操作
 * List<String> names = Null.of(users)
 *     .stream()
 *     .map(User::getName)
 *     .filter(Objects::nonNull)
 *     .collect(Collectors.toList());
 * }</pre>
 *
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullChain 链式操作接口
 * @see NullUtil 工具方法基类
 */
public class Null extends NullUtil {


    /**
     * 创建一个空的Null链
     *
     * @param <T> 链中值的类型
     * @return 空的Null链，执行任何操作都会返回空值
     */
    public static <T> NullChain<T> empty() {
        return new NullChainBase<>(newLinkLog(), newTaskList());
    }

    /**
     * 创建一个包含指定值的Null链
     *
     * @param <T> 值的类型
     * @param o 要包装的值，可以为null
     * @return 包含指定值的Null链
     */
    public static <T> NullChain<T> of(T o) {
        return ofInternal(o);
    }

    public static NullChain<JSONObject> of(JSONObject o) {
        return ofInternal(o);
    }
    /**
     * 创建一个包含数组的Null链
     *
     * @param <T> 数组元素类型
     * @param array 要包装的数组，可以为null
     * @return 包含数组的Null链
     */
    public static <T> NullChain<T[]> of(T[] array) {
        return ofInternal(array);
    }

    /**
     * 创建一个包含集合的Null链
     *
     * @param <T> 集合元素类型
     * @param list 要包装的集合，可以为null
     * @return 包含集合的Null链
     */
    public static <T> NullChain<Collection<T>> of(Collection<T> list) {
        return ofInternal(list);
    }



    /**
     * 创建一个包含队列的Null链
     *
     * @param <T> 队列元素类型
     * @param queue 要包装的队列，可以为null
     * @return 包含队列的Null链
     */
    public static <T> NullChain<Queue<T>> of(Queue<T> queue) {
        return ofInternal(queue);
    }

    /**
     * 创建一个包含双端队列的Null链
     *
     * @param <T> 双端队列元素类型
     * @param queue 要包装的双端队列，可以为null
     * @return 包含双端队列的Null链
     */
    public static <T> NullChain<Deque<T>> of(Deque<T> queue) {
        return ofInternal(queue);
    }

    /**
     * 创建一个包含集合的Null链
     *
     * @param <T> 集合元素类型
     * @param set 要包装的集合，可以为null
     * @return 包含集合的Null链
     */
    public static <T> NullChain<Set<T>> of(Set<T> set) {
        return ofInternal(set);
    }

    /**
     * 创建一个包含列表的Null链
     *
     * @param <T> 列表元素类型
     * @param list 要包装的列表，可以为null
     * @return 包含列表的Null链
     */
    public static <T> NullChain<List<T>> of(List<T> list) {
        return ofInternal(list);
    }

    /**
     * 创建一个包含Map的Null链
     *
     * @param <K> Map键的类型
     * @param <V> Map值的类型
     * @param map 要包装的Map，可以为null
     * @return 包含Map的Null链
     */
    public static <K, V> NullChain<Map<K, V>> of(Map<K, V> map) {
        return ofInternal(map);
    }



    /**
     * 将Optional转为NullChain
     * 
     * <p>该方法用于将Java 8的Optional对象转换为NullChain，便于在Null链中使用Optional的值。</p>
     * 
     * @param <O> Optional中值的类型
     * @param optional 要转换的Optional对象，可以为null
     * @return 包含Optional值的NullChain，如果Optional为空则返回空链
     * 
     * @example
     * <pre>{@code
     * Optional<String> optional = Optional.of("Hello");
     * Null.of(optional)
     *     .map(String::toUpperCase)
     *     .ifPresent(System.out::println);
     * }</pre>
     */
    @SuppressWarnings("all")
    public static <O> NullChain<O> of(Optional<O> optional) {
        return Null.of(optional.orElse(null));
    }



    /**
     * 创建多级判空工具
     *
     * <p>该方法用于创建多级判空工具，与 `NullChain.of()` 不同，该工具会**全部判定一遍**所有节点，
     * 收集所有为空的节点信息，然后统一处理。</p>
     *
     * @param <T> 检查对象的类型
     * @param value 要检查的对象
     * @return 多级判空工具实例
     *
     * @example
     * <pre>{@code
     * Null.ofCheck(user)
     *     .isNull(User::getId, "用户ID为空")
     *     .isNull(User::getName, "用户名为空")
     *     .isNull(User::getEmail, "邮箱为空")
     *     .doThrow(IllegalArgumentException.class);
     * }</pre>
     */
    public static <T> NullCheck<T> ofCheck(T value) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        List<NullCheckBase.NullCheckNode> nullCheckNodes = new ArrayList<>();
        nullTaskList.add((__) -> {
            NullCheckBase.NullCheckNode node = new NullCheckBase.NullCheckNode();
            if (Null.is(value)) {
                linkLog.append(OF_CHECK_Q);
                node.setNull( true);
                node.setPath(OF_CHECK);
                nullCheckNodes.add(node);
                return NullBuild.empty();
            }
            linkLog.append(OF_CHECK_ARROW);
            node.setNull(false);
            node.setPath(OF_CHECK);
            nullCheckNodes.add(node);
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyCheck(linkLog, nullTaskList,nullCheckNodes);

    }





    /**
     * 将Stream转为NullStream
     * 
     * <p>该方法用于将Java 8的Stream对象转换为NullStream，便于在Null链中进行流式操作。</p>
     * 
     * @param <S> Stream元素类型
     * @param stream 要转换的Stream对象，可以为null
     * @return NullStream对象，如果stream为null则返回空流
     * 
     * @example
     * <pre>{@code
     * Stream<String> stream = Stream.of("a", "b", "c");
     * Null.ofStream(stream)
     *     .filter(s -> s.length() > 1)
     *     .map(String::toUpperCase)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static <S> NullStream<S> ofStream(Stream<S> stream) {
        return ofStreamInternal(stream, OF_STREAM_ARROW);
    }

    /**
     * 将Collection转为NullStream
     * 
     * <p>该方法用于将Collection对象转换为NullStream，便于在Null链中进行流式操作。</p>
     * 
     * @param <S> Collection元素类型
     * @param collection 要转换的Collection对象，可以为null
     * @return NullStream对象，如果collection为null或空则返回空流
     * 
     * @example
     * <pre>{@code
     * List<String> list = Arrays.asList("a", "b", "c");
     * Null.ofStream(list)
     *     .filter(s -> s.length() > 1)
     *     .map(String::toUpperCase)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static <S> NullStream<S> ofStream(Collection<S> collection) {
        return ofStreamInternal(collection, TO_STREAM_ARROW);
    }

    /**
     * 将NullChain包装的Collection转为NullStream
     * 
     * <p>该方法用于将NullChain中包装的Collection对象转换为NullStream，适用于链式操作中。</p>
     * 
     * @param <T> Collection元素类型
     * @param nullChain 包含Collection的NullChain，可以为空链
     * @return NullStream对象，如果nullChain为空或包含的Collection为空则返回空流
     * 
     * @example
     * <pre>{@code
     * Null.of(users)
     *     .ofStream()
     *     .map(User::getName)
     *     .filter(Objects::nonNull)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static <T> NullStream<T> ofStream(NullChain<? extends Collection<T>> nullChain) {
        return ofStreamInternalChain(nullChain);
    }

    /**
     * 将NullChain包装的数组转为NullStream
     * 
     * <p>该方法用于将NullChain中包装的数组对象转换为NullStream，适用于链式操作中。</p>
     * 
     * @param <T> 数组元素类型
     * @param nullChain 包含数组的NullChain，可以为空链
     * @return NullStream对象，如果nullChain为空或包含的数组为空则返回空流
     * 
     * @example
     * <pre>{@code
     * String[] array = {"a", "b", "c"};
     * Null.of(array)
     *     .ofStreamArray()
     *     .map(String::toUpperCase)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static <T> NullStream<T> ofStreamArray(NullChain<? extends T[]> nullChain) {
        return ofStreamInternalArray(nullChain);
    }




    /**
     * 创建数值计算链 - 从Number对象创建
     * 
     * <p>该方法用于创建数值计算链，将Number对象转换为BigDecimal，便于进行精确的数值计算。</p>
     * 
     * @param <N> Number类型或其子类型
     * @param n 要转换的Number对象，可以为null
     * @return 数值计算链对象，如果n为null则返回空链
     * 
     * @example
     * <pre>{@code
     * Null.ofCalc(100)
     *     .add(50)
     *     .multiply(2)
     *     .get();
     * // 结果：300
     * }</pre>
     */
    public static <N extends Number> NullCalculate<BigDecimal> ofCalc(N n) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            if (Null.is(n)) {
                linkLog.append(OF_CALC_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_CALC_ARROW);
            return NullBuild.noEmpty(new BigDecimal(n.toString()));
        });
        return NullBuild.busyCalc(linkLog, nullTaskList);
    }

    /**
     * 创建数值计算链 - 从NullChain包装的Number创建
     * 
     * <p>该方法用于创建数值计算链，从NullChain中提取Number对象并转换为BigDecimal，适用于链式操作中。</p>
     * 
     * @param <NUM> Number类型或其子类型
     * @param nullChain 包含Number的NullChain，可以为空链
     * @return 数值计算链对象，如果nullChain为空则返回空链
     * 
     * @example
     * <pre>{@code
     * Null.of(100)
     *     .ofCalc()
     *     .add(50)
     *     .multiply(2)
     *     .get();
     * // 结果：300
     * }</pre>
     */
    public static <NUM extends Number> NullCalculate<BigDecimal> ofCalc(NullChain<NUM> nullChain) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            if (Null.is(nullChain)) {
                linkLog.append(OF_CALC_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_CALC_ARROW);
            return NullBuild.noEmpty(new BigDecimal(nullChain.get().toString()));
        });
        return NullBuild.busyCalc(linkLog, nullTaskList);
    }


    /**
     * 创建HTTP请求链 - 仅指定URL
     * 
     * <p>该方法用于创建HTTP请求链，适用于GET请求等不需要请求体的场景。</p>
     * 
     * @param <T> 响应类型
     * @param url HTTP请求URL，不能为空
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * Null.ofHttp("https://api.example.com/users")
     *     .get()
     *     .toStr()
     *     .ifPresent(System.out::println);
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String url) {
        return ofHttpInternal(url, null);
    }

    /**
     * 创建HTTP请求链 - 指定URL和请求体
     * 
     * <p>该方法用于创建HTTP请求链，适用于POST、PUT等需要请求体的场景。</p>
     * 
     * @param <T> 响应类型
     * @param url HTTP请求URL，不能为空
     * @param value 请求体数据，可以为null（表示无请求体）
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * User user = new User("John", "john@example.com");
     * Null.ofHttp("https://api.example.com/users", user)
     *     .post()
     *     .toStr(User.class)
     *     .orElseNull();
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String url, T value) {
        return ofHttpInternal(url, value);
    }

    /**
     * 创建HTTP请求链 - 指定HTTP名称、URL和请求体
     * 
     * <p>该方法用于创建HTTP请求链，支持指定HTTP客户端名称，适用于需要区分不同HTTP客户端的场景。</p>
     * 
     * @param <T> 响应类型
     * @param httpName HTTP客户端名称，用于区分不同的HTTP客户端配置
     * @param url HTTP请求URL，不能为空
     * @param value 请求体数据，可以为null（表示无请求体）
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * Null.ofHttp("apiClient", "https://api.example.com/users", user)
     *     .post()
     *     .toStr(User.class)
     *     .orElseNull();
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String httpName, String url, T value) {
        return ofHttpInternal(httpName, url, value);
    }

    /**
     * 创建HTTP请求链 - 指定HTTP名称和URL
     * 
     * <p>该方法用于创建HTTP请求链，支持指定HTTP客户端名称，适用于GET请求等不需要请求体的场景。</p>
     * 
     * @param <T> 响应类型
     * @param httpName HTTP客户端名称，用于区分不同的HTTP客户端配置
     * @param url HTTP请求URL，不能为空
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * Null.ofHttp("apiClient", "https://api.example.com/users")
     *     .get()
     *     .toStr()
     *     .ifPresent(System.out::println);
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String httpName, String url) {
        return ofHttpInternal(httpName, url, null);
    }

    /**
     * 创建HTTP请求链 - 指定URL和NullChain请求体
     * 
     * <p>该方法用于创建HTTP请求链，请求体来自NullChain，适用于链式操作中传递请求体的场景。</p>
     * 
     * @param <T> 响应类型
     * @param url HTTP请求URL，不能为空
     * @param value NullChain包装的请求体数据，可以为空链（表示无请求体）
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .map(User::toJson)
     *     .flatChain(json -> Null.ofHttp("https://api.example.com/users", json))
     *     .post()
     *     .toStr()
     *     .orElseNull();
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String url, NullChain<T> value) {
        return ofHttpInternal(url, value);
    }

    /**
     * 创建HTTP请求链 - 指定HTTP名称、URL和NullChain请求体
     * 
     * <p>该方法用于创建HTTP请求链，支持指定HTTP客户端名称，请求体来自NullChain。</p>
     * 
     * @param <T> 响应类型
     * @param httpName HTTP客户端名称，用于区分不同的HTTP客户端配置
     * @param url HTTP请求URL，不能为空
     * @param value NullChain包装的请求体数据，可以为空链（表示无请求体）
     * @return HTTP请求链对象
     * 
     * @example
     * <pre>{@code
     * Null.of(user)
     *     .map(User::toJson)
     *     .flatChain(json -> Null.ofHttp("apiClient", "https://api.example.com/users", json))
     *     .post()
     *     .toStr()
     *     .orElseNull();
     * }</pre>
     */
    public static <T> OkHttp<T> ofHttp(String httpName, String url, NullChain<T> value) {
        return ofHttpInternal(httpName, url, value);
    }





    // log fragments moved to NullLog

    interface LeafBuilder<L> {
        L build(StringBuilder linkLog, NullTaskList tasks);
    }

    /**
     * 创建新的NullTaskList实例
     * 
     * <p>辅助方法，统一创建NullTaskList对象，便于后续优化（如对象池等）。</p>
     * 
     * @return 新的NullTaskList实例
     */
    private static NullTaskList newTaskList() {
        return new NullTaskList();
    }

    /**
     * 创建新的StringBuilder实例，使用默认初始容量
     * 
     * <p>辅助方法，统一创建StringBuilder对象，便于后续优化（如对象池等）。</p>
     * 
     * @return 新的StringBuilder实例
     */
    private static StringBuilder newLinkLog() {
        return new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
    }

    /**
     * 创建Leaf对象的通用辅助方法
     *
     * @param valueSupplier 提供实际值的函数，如果为null或空则返回null
     * @param nullLog 空值日志
     * @param okLog 正常值日志
     * @param builder 构建器
     * @return 构建的Leaf对象
     * @throws NullPointerException 如果valueSupplier或builder为null
     */
    private static <X, L> L createLeaf(Supplier<X> valueSupplier, String nullLog, String okLog, LeafBuilder<L> builder) {
        if (valueSupplier == null) {
            throw new NullPointerException("valueSupplier不能为null");
        }
        if (builder == null) {
            throw new NullPointerException("builder不能为null");
        }
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            X actualValue = valueSupplier.get();
            if (Null.is(actualValue)) {
                linkLog.append(nullLog);
                return NullBuild.empty();
            }
            linkLog.append(okLog);
            return NullBuild.noEmpty(actualValue);
        });
        return builder.build(linkLog, nullTaskList);
    }

    /**
     * 创建Leaf对象 - 从值创建
     * 
     * @param <X> 值的类型
     * @param <L> Leaf类型
     * @param value 要包装的值，可以为null
     * @param nullLog 空值日志
     * @param okLog 正常值日志
     * @param builder 构建器，不能为null
     * @return 构建的Leaf对象
     * @throws NullPointerException 如果builder为null
     */
    static <X, L> L ofLeaf(X value, String nullLog, String okLog, LeafBuilder<L> builder) {
        if (builder == null) {
            throw new NullPointerException("builder不能为null");
        }
        // 快速路径：对于明显的null值，直接返回空链，避免创建对象
        if (value == null) {
            NullTaskList nullTaskList = newTaskList();
            StringBuilder linkLog = newLinkLog();
            linkLog.append(nullLog);
            nullTaskList.add((__) -> NullBuild.empty());
            return builder.build(linkLog, nullTaskList);
        }

        return createLeaf(() -> value, nullLog, okLog, builder);
    }

    /**
     * 创建Leaf对象 - 从NullChain创建
     * 
     * @param <X> 值的类型
     * @param <L> Leaf类型
     * @param value NullChain包装的值，可以为空链
     * @param nullLog 空值日志
     * @param okLog 正常值日志
     * @param builder 构建器，不能为null
     * @return 构建的Leaf对象
     * @throws NullPointerException 如果builder为null
     */
    static <X, L> L ofLeaf(NullChain<X> value, String nullLog, String okLog, LeafBuilder<L> builder) {
        if (builder == null) {
            throw new NullPointerException("builder不能为null");
        }
        return createLeaf(() -> Null.is(value) ? null : value.get(), nullLog, okLog, builder);
    }


    @SuppressWarnings("unchecked")
    static <S> NullStream<S> ofStreamInternal(Object source, String okLog) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            linkLog.append(okLog);
            if (Null.is(source)) {
                return NullBuild.noEmpty((Stream.empty()));
            }
            Stream<S> stream;
            // 优化：将最常用的Collection类型检查放在最前面，提升性能
            if (source instanceof Collection) {
                stream = ((Collection<S>) source).stream().filter(Null::non);
            } else if (source instanceof Stream) {
                stream = ((Stream<S>) source).filter(Null::non);
            }  else {
                throw new NullChainException(linkLog.append(OF_STREAM_UNSUPPORTED_SOURCE).toString());
            }
            return NullBuild.noEmpty(stream);
        });
        return NullBuild.busyStream(linkLog, nullTaskList);
    }

    static <T> NullStream<T> ofStreamInternalChain(NullChain<? extends Collection<T>> nullChain) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            linkLog.append(TO_STREAM_ARROW);
            if (Null.is(nullChain)) {
                return NullBuild.noEmpty((Stream.empty()));
            }
            return NullBuild.noEmpty((nullChain.get()).stream().filter(Null::non));
        });
        return NullBuild.busyStream(linkLog, nullTaskList);
    }

    static <T> NullStream<T> ofStreamInternalArray(NullChain<? extends T[]> nullChain) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            linkLog.append(TO_STREAM_ARROW);
            if (Null.is(nullChain)) {
                return NullBuild.noEmpty((Stream.empty()));
            }
            return NullBuild.noEmpty(Arrays.stream(nullChain.get()).filter(Null::non));
        });
        return NullBuild.busyStream(linkLog, nullTaskList);
    }

    /**
     * 内部方法：创建HTTP请求链（无HTTP名称）
     * 
     * <p>该方法实现了创建HTTP请求链的核心逻辑，统一处理URL和请求体的空值检查。</p>
     * 
     * <h3>处理逻辑：</h3>
     * <ul>
     *   <li>如果URL为空，返回空链</li>
     *   <li>如果value为null，返回Void.TYPE（表示无请求体，适用于GET请求）</li>
     *   <li>如果value不为null但为空（空字符串、空集合等），返回空链</li>
     *   <li>否则返回实际的请求体</li>
     * </ul>
     * 
     * @param <T> 响应类型
     * @param url HTTP请求URL
     * @param value 请求体数据，可以为null
     * @return HTTP请求链对象
     */
    static <T> OkHttp<T> ofHttpInternal(String url, Object value) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            // URL为空，返回空链
            if (StringUtil.isEmpty(url)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            
            // value为null，表示无请求体（GET请求等），返回Void.TYPE
            if (value == null) {
                linkLog.append(OF_HTTP_ARROW);
                return NullBuild.noEmpty(Void.TYPE);
            }
            
            // value不为null，检查是否为空（空字符串、空集合等）
            if (Null.is(value)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            
            // 提取实际的请求体（可能是NullChain包装的值）
            linkLog.append(OF_HTTP_ARROW);
            Object body = (value instanceof NullChain) ? ((NullChain<?>) value).get() : value;
            return NullBuild.noEmpty(body);
        });
        return NullBuild.busyHttp(url, linkLog, nullTaskList);
    }

    /**
     * 内部方法：创建HTTP请求链（指定HTTP名称）
     * 
     * <p>该方法实现了创建HTTP请求链的核心逻辑，支持指定HTTP客户端名称。</p>
     * 
     * <h3>处理逻辑：</h3>
     * <ul>
     *   <li>如果URL为空，返回空链</li>
     *   <li>如果value为null，返回Void.TYPE（表示无请求体，适用于GET请求）</li>
     *   <li>如果value不为null但为空（空字符串、空集合等），返回空链</li>
     *   <li>否则返回实际的请求体</li>
     * </ul>
     * 
     * @param <T> 响应类型
     * @param httpName HTTP客户端名称，用于区分不同的HTTP客户端配置
     * @param url HTTP请求URL
     * @param value 请求体数据，可以为null
     * @return HTTP请求链对象
     */
    static <T> OkHttp<T> ofHttpInternal(String httpName, String url, Object value) {
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            // URL为空，返回空链
            if (StringUtil.isEmpty(url)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            
            // value为null，表示无请求体（GET请求等），返回Void.TYPE
            if (value == null) {
                linkLog.append(OF_HTTP_ARROW);
                return NullBuild.noEmpty(Void.TYPE);
            }
            
            // value不为null，检查是否为空（空字符串、空集合等）
            if (Null.is(value)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            
            // 提取实际的请求体（可能是NullChain包装的值）
            linkLog.append(OF_HTTP_ARROW);
            Object body = (value instanceof NullChain) ? ((NullChain<?>) value).get() : value;
            return NullBuild.noEmpty(body);
        });
        return NullBuild.busyHttp(httpName, url, linkLog, nullTaskList);
    }




    /**
     * 内部方法：创建Null链的核心实现
     * 
     * <p>该方法实现了创建Null链的核心逻辑，包括快速路径优化和完整空值检查。</p>
     * 
     * <h3>优化策略：</h3>
     * <ul>
     *   <li>快速路径：对于null值，直接返回空链，避免创建任务对象</li>
     *   <li>基本类型优化：对于非null的基本类型，直接返回非空链，因为基本类型只有null和有值两种情况</li>
     *   <li>完整检查：对于其他类型，在任务中调用Null.is()进行完整检查（空字符串、空集合等）</li>
     * </ul>
     * 
     * @param <X> 值的类型
     * @param value 要包装的值，可以为null
     * @return 包含指定值的Null链
     */
    static <X> NullChain<X> ofInternal(X value) {
        // 快速路径：对于明显的null值，直接返回空链，避免创建对象
        if (value == null) {
            NullTaskList nullTaskList = newTaskList();
            StringBuilder linkLog = newLinkLog();
            linkLog.append(OF_Q);
            nullTaskList.add((__) -> NullBuild.empty());
            return NullBuild.busy(linkLog, nullTaskList);
        }

        // 优化：对于非null的基本类型，直接返回非空链，避免在任务中再次检查
        // 因为基本类型只有null和有值两种情况，不需要检查空字符串、空集合等
        if (ClassIdentifyUtil.isPrimitiveOrWrapper(value.getClass())) {
            NullTaskList nullTaskList = newTaskList();
            StringBuilder linkLog = newLinkLog();
            linkLog.append(OF_ARROW);
            nullTaskList.add((__) -> NullBuild.noEmpty(value));
            return NullBuild.busy(linkLog, nullTaskList);
        }

        // 对于其他类型（字符串、集合、数组等），在任务中进行完整检查
        NullTaskList nullTaskList = newTaskList();
        StringBuilder linkLog = newLinkLog();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(OF_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_ARROW);
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }
}
