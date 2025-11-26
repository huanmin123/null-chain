package com.gitee.huanminabc.nullchain;


import com.alibaba.fastjson.JSONObject;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.leaf.copy.NullCopy;
import com.gitee.huanminabc.nullchain.leaf.date.NullDate;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.json.NullJson;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import java.math.BigDecimal;
import java.util.*;
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
        return new NullChainBase<>(new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY), new NullTaskList());
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
    public static <T> NullChain<JSONObject> of(JSONObject o) {
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



    public static <T> NullChain<Queue<T>> of(Queue<T> queue) {
        return ofInternal(queue);
    }

    public static <T> NullChain<Deque<T>> of(Deque<T> queue) {
        return ofInternal(queue);
    }
    public static <T> NullChain<Set<T>> of(Set<T> set) {
        return ofInternal(set);
    }



    public static <T> NullChain<List<T>> of(List<T> list) {
        return ofInternal(list);
    }


    public static <K, V> NullChain<Map<K, V>> of(Map<K, V> map) {
        return ofInternal(map);
    }



    //将Optional转为NullChain
    @SuppressWarnings("all")
    public static <O> NullChain<O> of(Optional<O> optional) {
        return Null.of(optional.orElse(null));
    }


    public static <T> NullDate<T> ofDate(T value) {
        return ofLeaf(value, OF_DATE_Q, OF_DATE_ARROW, NullBuild::busyDate);
    }

    public static <T> NullDate<T> ofDate(NullChain<T> value) {
        return ofLeaf(value, OF_DATE_Q, OF_DATE_ARROW, NullBuild::busyDate);
    }

    public static <T> NullJson<T> ofJson(T value) {
        return ofLeaf(value, OF_JSON_Q, OF_JSON_ARROW, NullBuild::busyJson);
    }

    public static <T> NullJson<T> ofJson(NullChain<T> value) {
        return ofLeaf(value, OF_JSON_Q, OF_JSON_ARROW, NullBuild::busyJson);
    }

    public static <T> NullCopy<T> ofCopy(T value) {
        return ofLeaf(value, OF_COPY_Q, OF_COPY_ARROW, NullBuild::busyCopy);
    }

    public static <T> NullCopy<T> ofCopy(NullChain<T> value) {
        return ofLeaf(value, OF_COPY_Q, OF_COPY_ARROW, NullBuild::busyCopy);
    }





    //将Stream转为NullChain
    public static <S> NullStream<S> ofStream(Stream<S> stream) {
        return ofStreamInternal(stream, OF_STREAM_ARROW);
    }



    //将Collection转为NullStream
    public static <S> NullStream<S> ofStream(Collection<S> collection) {
        return ofStreamInternal(collection, TO_STREAM_ARROW);
    }

    public static <T> NullStream<T> ofStream(NullChain<? extends Collection<T>> nullChain) {
        return ofStreamInternalChain(nullChain);
    }

    public static <T> NullStream<T> ofStreamArray(NullChain<? extends T[]> nullChain) {
        return ofStreamInternalArray(nullChain);
    }




    public static <N extends Number> NullCalculate<BigDecimal> ofCalc(N n) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            if (Null.is(n)) {
                linkLog.append(OF_CALC_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_CALC_ARROW);
            return NullBuild.noEmpty(BigDecimal.valueOf(n.doubleValue()));
        });
        return NullBuild.busyCalc(linkLog, nullTaskList);
    }

    public static <NUM extends Number> NullCalculate<BigDecimal> ofCalc(NullChain<NUM> nullChain) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            if (Null.is(nullChain)) {
                linkLog.append(OF_CALC_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_CALC_ARROW);
            return NullBuild.noEmpty(BigDecimal.valueOf(nullChain.get().doubleValue()));
        });
        return NullBuild.busyCalc(linkLog, nullTaskList);
    }


    public static <T> OkHttp<T> ofHttp(String url) {
        return ofHttpInternal(url, null);
    }
    public static <T> OkHttp<T> ofHttp(String url, T value) {
        return ofHttpInternal(url, value);
    }

    public static <T> OkHttp<T> ofHttp(String httpName, String url, T value) {
        return ofHttpInternal(httpName, url, value);
    }

    public static <T> OkHttp<T> ofHttp(String url, NullChain<T> value) {
        return ofHttpInternal(url, value);
    }

    public static <T> OkHttp<T> ofHttp(String httpName, String url, NullChain<T> value) {
        return ofHttpInternal(httpName, url, value);
    }





    // log fragments moved to NullLog

    interface LeafBuilder<L> {
        L build(StringBuilder linkLog, NullTaskList tasks);
    }

    /**
     * 创建Leaf对象的通用辅助方法
     * 
     * @param valueSupplier 提供实际值的函数，如果为null或空则返回null
     * @param nullLog 空值日志
     * @param okLog 正常值日志
     * @param builder 构建器
     * @return 构建的Leaf对象
     */
    private static <X, L> L createLeaf(java.util.function.Supplier<X> valueSupplier, String nullLog, String okLog, LeafBuilder<L> builder) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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

    static <X, L> L ofLeaf(X value, String nullLog, String okLog, LeafBuilder<L> builder) {
        // 快速路径：对于明显的null值，直接返回空链，避免创建对象
        if (value == null) {
            NullTaskList nullTaskList = new NullTaskList();
            StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
            linkLog.append(nullLog);
            nullTaskList.add((__) -> NullBuild.empty());
            return builder.build(linkLog, nullTaskList);
        }
        
        return createLeaf(() -> value, nullLog, okLog, builder);
    }

    static <X, L> L ofLeaf(NullChain<X> value, String nullLog, String okLog, LeafBuilder<L> builder) {
        return createLeaf(() -> Null.is(value) ? null : value.get(), nullLog, okLog, builder);
    }


    @SuppressWarnings("unchecked")
    static <S> NullStream<S> ofStreamInternal(Object source, String okLog) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            linkLog.append(TO_STREAM_ARROW);
            if (Null.is(nullChain)) {
                return NullBuild.noEmpty((Stream.empty()));
            }
            return NullBuild.noEmpty(Arrays.stream(nullChain.get()).filter(Null::non));
        });
        return NullBuild.busyStream(linkLog, nullTaskList);
    }

    static <T> OkHttp<T> ofHttpInternal(String url, Object value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            if (value == null) {
                if (Null.is(url)) {
                    linkLog.append(OF_HTTP_Q);
                    return NullBuild.empty();
                }
                linkLog.append(OF_HTTP_ARROW);
                return NullBuild.noEmpty(Void.TYPE);
            }
            if (Null.isAny(url, value)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_HTTP_ARROW);
            Object body = (value instanceof NullChain) ? ((NullChain<?>) value).get() : value;
            return NullBuild.noEmpty(body);
        });
        return NullBuild.busyHttp(url, linkLog, nullTaskList);
    }

    static <T> OkHttp<T> ofHttpInternal(String httpName, String url, Object value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        nullTaskList.add((__) -> {
            if (Null.isAny(url, value)) {
                linkLog.append(OF_HTTP_Q);
                return NullBuild.empty();
            }
            linkLog.append(OF_HTTP_ARROW);
            Object body = (value instanceof NullChain) ? ((NullChain<?>) value).get() : value;
            return NullBuild.noEmpty(body == null ? Void.TYPE : body);
        });
        return NullBuild.busyHttp(httpName, url, linkLog, nullTaskList);
    }




    static <X> NullChain<X> ofInternal(X value) {
        // 快速路径：对于明显的null值，直接返回空链，避免创建对象
        if (value == null) {
            NullTaskList nullTaskList = new NullTaskList();
            StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
            linkLog.append(OF_Q);
            nullTaskList.add((__) -> NullBuild.empty());
            return NullBuild.busy(linkLog, nullTaskList);
        }
        
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
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
