package com.gitee.huanminabc.nullchain;


import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.leaf.copy.NullCopy;
import com.gitee.huanminabc.nullchain.leaf.date.NullDate;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.json.NullJson;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import com.gitee.huanminabc.nullchain.common.NullUtil;
import com.gitee.huanminabc.nullchain.vessel.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

/**
 * 使用Null工具类的要求
 * 1.类必须实现get/set方法 (强制必须实现,否则你编译不通过)
 * 2.类必须有空构造方法 (必须)
 * 3.类中全部字段必须使用  包装类型  (必须)
 * 可以使用lombok的@Data注解来实现
 *
 * @author huanmin
 * @date 2024/1/11
 */
public class Null extends NullUtil {

    public static <T> NullChain<T> empty() {
        return NullBuild.empty(new StringBuilder(), new NullCollect(), new NullTaskList());
    }

    public static <T> NullChain<T> of(T o) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(o)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(o);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullChain<T[]> of(T[] array) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(array)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(array);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullChain<Collection<T>> of(Collection<T> list) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(list)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(list);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullChain<NullCollection<T>> of(NullCollection<T> list) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(list)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(list);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullChain<Queue<T>> of(Queue<T> queue) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(queue)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(queue);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullChain<Deque<T>> of(Deque<T> queue) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(queue)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(queue);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullChain<NullDeque<T>> of(NullDeque<T> queue) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(queue)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(queue);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);
    }

    public static <T> NullChain<NullQuery<T>> of(NullQuery<T> queue) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(queue)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(queue);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }


    public static <T> NullChain<Set<T>> of(Set<T> set) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(set)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(set);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullChain<NullSet<T>> of(NullSet<T> set) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(set)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(set);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }


    public static <T> NullChain<List<T>> of(List<T> list) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(list)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(list);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }


    public static <T> NullChain<NullList<T>> of(NullList<T> list) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(list)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(list);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }


    public static <K, V> NullChain<Map<K, V>> of(Map<K, V> map) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(map)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(map);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }

    public static <K, V> NullChain<NullMap<K, V>> of(NullMap<K, V> map) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(map)) {
                linkLog.append(" Null.of?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.of->");
            return NullBuild.noEmpty(map);
        });
        return NullBuild.busy(linkLog, nullCollect, nullTaskList);

    }


    //将Optional转为NullChain
    @SuppressWarnings("all")
    public static <O> NullChain<O> of(Optional<O> optional) {
        return Null.of(optional.orElse(null));
    }




    public static <T> NullDate<T> ofDate(T value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofDate?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofDate->");
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyDate(linkLog,nullCollect,nullTaskList);

    }

    public static <T> NullDate<T> ofDate(NullChain<T> value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofDate?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofDate->");
            return NullBuild.noEmpty(value.get());
        });
        return NullBuild.busyDate( linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullJson<T> ofJson(T value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofJson?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofJson->");
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyJson( linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullJson<T> ofJson(NullChain<T> value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofJson?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofJson->");
            return NullBuild.noEmpty(value.get());
        });
        return NullBuild.busyJson( linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullCopy<T> ofCopy(T value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofCopy?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofCopy->");
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyCopy(linkLog, nullCollect, nullTaskList);
    }
    public static <T> NullCopy<T> ofCopy(NullChain<T> value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(value)) {
                linkLog.append(" Null.ofCopy?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofCopy->");
            return NullBuild.noEmpty(value.get());
        });
        return NullBuild.busyCopy(linkLog, nullCollect, nullTaskList);
    }


    //将Stream转为NullChain
    public static <S> NullStream<S> ofStream(Stream<S> stream) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(stream)) {
                linkLog.append(" Null.ofStream?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofStream->");
            return NullBuild.noEmpty((S) stream);
        });
        return NullBuild.busyStream( linkLog, nullCollect, nullTaskList);
    }


    //将Collection转为NullStream
    public static <S> NullStream<S> ofStream(Collection<S> collection) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(collection)) {
                linkLog.append(" Null.toStream?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.toStream->");
            return NullBuild.noEmpty((S) collection.stream());
        });
        return NullBuild.busyStream( linkLog, nullCollect, nullTaskList);
    }
    public static <S> NullStream<S> ofStream(NullCollection<S> collection) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(collection)) {
                linkLog.append(" Null.toStream?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.toStream->");
            return NullBuild.noEmpty((S) collection.stream());
        });
        return NullBuild.busyStream( linkLog, nullCollect, nullTaskList);

    }

    //将数组转为NullStream
    public static <T> NullStream<T> ofStream(NullChain<? extends Collection<T>> nullChain) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(nullChain)) {
                linkLog.append(" Null.toStream?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.toStream->");
            return NullBuild.noEmpty((T) nullChain.get().stream());
        });
        return NullBuild.busyStream( linkLog, nullCollect, nullTaskList);

    }

    public static <T> NullStream<T> ofStreamNull(NullChain<? extends NullCollection<T>> nullChain) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(nullChain)) {
                linkLog.append(" Null.toStream?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.toStream->");
            return NullBuild.noEmpty((T) nullChain.get().stream());
        });
        return NullBuild.busyStream( linkLog, nullCollect, nullTaskList);

    }

    public static <N extends Number> NullCalculate<BigDecimal> ofCalc(N n) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(n)) {
                linkLog.append(" Null.ofCalc?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofCalc->");
            NullCollect collect = new NullCollect();
            return NullBuild.noEmpty(BigDecimal.valueOf(n.doubleValue()));
        });
        return NullBuild.busyCalc( linkLog, nullCollect, nullTaskList);
    }

    public static <NUM extends Number> NullCalculate<BigDecimal> ofCalc(NullChain<NUM> nullChain) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.is(nullChain)) {
                linkLog.append(" Null.ofCalc?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofCalc->");
            return NullBuild.noEmpty(BigDecimal.valueOf(nullChain.get().doubleValue()));
        });
        return NullBuild.busyCalc( linkLog, nullCollect, nullTaskList);
    }


    public static <T> OkHttp ofHttp(String url, T value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.isAny(url, value)) {
                linkLog.append(" Null.ofHttp?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofHttp->");
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyHttp( url,linkLog, nullCollect, nullTaskList);

    }

    public static <T> OkHttp ofHttp(String httpName, String url, T value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.isAny(url, value)) {
                linkLog.append(" Null.ofHttp?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofHttp->");
            return NullBuild.noEmpty(value);
        });
        return NullBuild.busyHttp(httpName, url,linkLog, nullCollect, nullTaskList);

    }

    public static <T> OkHttp ofHttp(String url, NullChain<T> value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.isAny(url, value)) {
                linkLog.append(" Null.ofHttp?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofHttp->");
            return NullBuild.noEmpty( value.get());
        });
        return NullBuild.busyHttp(url,linkLog, nullCollect, nullTaskList);

    }

    public static <T> OkHttp ofHttp(String httpName, String url, NullChain<T> value) {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        NullCollect nullCollect = new NullCollect();
        nullTaskList.add((__) -> {
            if (Null.isAny(url, value)) {
                linkLog.append(" Null.ofHttp?");
                return NullBuild.empty();
            }
            linkLog.append(" Null.ofHttp->");
            return NullBuild.noEmpty(value.get());
        });
        return NullBuild.busyHttp(httpName,url,linkLog, nullCollect, nullTaskList);

    }





}
