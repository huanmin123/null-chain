package com.gitee.huanminabc.nullchain;


import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.base.sync.stream.NullStream;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullCollect;
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

    public static <T> NullChain<T> of(T o) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(o)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(o, linkLog, new NullCollect());
    }

    public static <T> NullChain<T[]> of(T[] array) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(array)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(array, linkLog, new NullCollect());
    }

    public static <T> NullChain<Collection<T>> of(Collection<T> list) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(list)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(list, linkLog, new NullCollect());
    }

    public static <T> NullChain<NullCollection<T>> of(NullCollection<T> list) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(list)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(list, linkLog, new NullCollect());
    }

    public static <T> NullChain<Queue<T>> of(Queue<T> queue) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(queue)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(queue, linkLog, new NullCollect());
    }

    public static <T> NullChain<Deque<T>> of(Deque<T> queue) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(queue)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(queue, linkLog, new NullCollect());
    }

    public static <T> NullChain<NullDeque<T>> of(NullDeque<T> queue) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(queue)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(queue, linkLog, new NullCollect());
    }

    public static <T> NullChain<NullQuery<T>> of(NullQuery<T> queue) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(queue)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(queue, linkLog, new NullCollect());
    }


    public static <T> NullChain<Set<T>> of(Set<T> set) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(set)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(set, linkLog, new NullCollect());
    }

    public static <T> NullChain<NullSet<T>> of(NullSet<T> set) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(set)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(set, linkLog, new NullCollect());
    }


    public static <T> NullChain<List<T>> of(List<T> list) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(list)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(list, linkLog, new NullCollect());
    }


    public static <T> NullChain<NullList<T>> of(NullList<T> list) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(list)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(list, linkLog, new NullCollect());
    }


    public static <K, V> NullChain<Map<K, V>> of(Map<K, V> map) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(map)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(map, linkLog, new NullCollect());
    }

    public static <K, V> NullChain<NullMap<K, V>> of(NullMap<K, V> map) {
        StringBuilder linkLog = new StringBuilder();
        if (Null.is(map)) {
            linkLog.append(" Null.of?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        return NullBuild.noEmpty(map, linkLog, new NullCollect());
    }


    //将Optional转为NullChain
    @SuppressWarnings("all")
    public static <O> NullChain<O> of(Optional<O> optional) {
        return Null.of(optional.orElse(null));
    }

    //将Stream转为NullChain
    public static <S> NullStream<S> of(Stream<S> stream) {
        StringBuilder linkLog = new StringBuilder();
        if (stream == null) {
            linkLog.append(" Null.of?");
            return NullBuild.emptyStream(linkLog, new NullCollect());
        }
        linkLog.append(" Null.of->");
        NullCollect collect = new NullCollect();
        return NullBuild.noEmptyStream((S) stream, linkLog, collect);
    }


    public static <S extends Number> NullCalculate<BigDecimal> ofCalc(S s, S defaultValue) {
        StringBuilder linkLog = new StringBuilder();
        if (s == null) {
            if (defaultValue == null) {
                linkLog.append(" Null.ofCalc?");
                return NullBuild.emptyCalc(linkLog, new NullCollect());
            }
            s = defaultValue;
        }
        linkLog.append(" Null.ofCalc->");
        return NullBuild.noEmptyCalc(BigDecimal.valueOf(s.doubleValue()), linkLog, new NullCollect());

    }

    public static <S extends Number> NullCalculate<BigDecimal> ofCalc(S s) {
        StringBuilder linkLog = new StringBuilder();
        if (s == null) {
            linkLog.append(" Null.ofCalc?");
            return NullBuild.emptyCalc(linkLog, new NullCollect());
        }
        linkLog.append(" Null.ofCalc->");
        return NullBuild.noEmptyCalc(BigDecimal.valueOf(s.doubleValue()), linkLog, new NullCollect());
    }


    //将Collection转为NullStream
    public static <S> NullStream<S> ofStream(Collection<S> collection) {
        StringBuilder linkLog = new StringBuilder();
        if (collection == null) {
            linkLog.append(" Null.toStream?");
            return NullBuild.emptyStream(linkLog, new NullCollect());
        }
        linkLog.append(" Null.toStream->");
        NullCollect collect = new NullCollect();
        return NullBuild.noEmptyStream((S) collection.stream(), linkLog, collect);
    }

    public static <S> NullStream<S> ofStream(NullCollection<S> collection) {
        StringBuilder linkLog = new StringBuilder();
        if (collection == null) {
            linkLog.append(" Null.toStream?");
            return NullBuild.emptyStream(linkLog, new NullCollect());
        }
        linkLog.append(" Null.toStream->");
        NullCollect collect = new NullCollect();
        return NullBuild.noEmptyStream((S) collection.stream(), linkLog, collect);
    }

    //将数组转为NullStream
    public static <S> NullStream<S> ofStream(S[] array) {
        StringBuilder linkLog = new StringBuilder();
        if (array == null) {
            return NullBuild.emptyStream(linkLog, new NullCollect());
        }
        linkLog.append(" Null.toStream->");
        NullCollect collect = new NullCollect();
        return NullBuild.noEmptyStream((S) Arrays.stream(array), linkLog, collect);
    }


    public static <T> NullChain<T> empty() {
        return NullBuild.empty(new StringBuilder(), new NullCollect());
    }


}
