package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.common.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.common.str.StringUtil;
import com.gitee.huanminabc.nullchain.NullCheck;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 判断各种类型是否为空
 *
 * @author 胡安民
 * @Description: 判断是否为空的工具栏
 */
public class NullUtil {


    //判断不等于空并且如果是字符串类型那么也不能是空
    public static boolean is(Object o) {
        if (o == null) {
            return true;
        }
        //把8大基本类型和字符串排在前面判断,因为这些类型是最常用的

        //如果是8大基本类型或者包装类型,那么直接返回不是空 ,因为只有null和有值两种情况
        if (ClassIdentifyUtil.isPrimitiveOrWrapper(o.getClass())) {
            return false;
        }

        //字符串相关的验证空情况
        if (o instanceof CharSequence) {
            CharSequence str = (CharSequence) o;
            boolean blank = StringUtil.isEmpty(str);
            //如果不是空那么判断不能是null字符串,在有些情况下比如String.valueOf(null)返回的是null字符串，这会导致计算和显示的时候误解
            if (!blank && str instanceof String && str.length() == 4) {
                //截取前4个字符,如果是null字符串那么就返回true
                return "null".equalsIgnoreCase(((String) str).substring(0, 4));
            }
            return blank;
        }


        if (o instanceof Collection) {
            return ((Collection<?>) o).isEmpty();
        }
        if (o instanceof Map) {
            return ((Map<?, ?>) o).isEmpty();
        }
        if (o instanceof Object[]) {
            return ((Object[]) o).length == 0;
        }
        if (o instanceof NullCheck) {
            return ((NullCheck) o).isEmpty();
        }

        return false;
    }

    //只要有一个为空就返回true
    public static boolean isAny(Object... o) {
        for (Object o1 : o) {
            if (is(o1)) {
                return true;
            }
        }
        return false;
    }

    //判断全部是空返回true,只要有一个不为空就返回false
    public static boolean isAll(Object... o) {
        for (Object o1 : o) {
            if (non(o1)) {
                return false;
            }
        }
        return true;
    }


    //判断不是空返回true
    public static boolean non(Object o) {
        return !is(o);
    }

    //全部不为空返回true,只要有一个为空就返回false
    public static boolean nonAll(Object... o) {
        for (Object o1 : o) {
            if (is(o1)) {
                return false;
            }
        }
        return true;
    }


    public static boolean eq(Object a, Object b) {
        return Objects.equals(a, b);
    }

    //如果是空那么返回null
    public static <T> T orElseNull(T obj) {
        if (is(obj)) {
            return null;
        }
        return obj;
    }

    //判断对象是否为空,如果为空返回默认值 , 默认值不允许为空
    public static <T> T orElse(T obj, T defaultValue) {
        if (is(obj)) {
            //如果默认值也是空那么就返回异常
            if (is(defaultValue)) {
                throw new NullChainException("默认值不能是空");
            }
            return defaultValue;
        }
        return obj;
    }

    public static <T> T orElse(T obj, Supplier<T> defaultValue) {
        if (is(obj)) {
            if (defaultValue == null) {
                throw new NullChainException("默认值参数不能为空");
            }
            T t = defaultValue.get();
            if (is(t)) {
                throw new NullChainException("默认值不能是空");
            }
            return t;
        }
        return obj;
    }

    //如果值为空那么就创建一个空的对象
    public static <T extends NullCheck> T orEmpty(T obj, Class<? extends T> clazz) {
        return is(obj) ? createEmpty(clazz) : obj;
    }

    public static <T, X extends Throwable> T orThrow(T obj, Supplier<? extends X> exceptionSupplier) throws X {
        if (is(obj)) {
            throw exceptionSupplier.get();
        }
        return obj;
    }

    //一般搭配NULLExt使用, 如果不能保证对象的变量不为空那么就先使用这个方法拦截一下
    public static void checkNull(Object obj) {
        if (obj == null) {
            throw new NullChainException();
        }
    }

    public static void checkNull(Object obj, String message, Object... params) {
        if (obj == null) {
            if (params == null || params.length == 0) {
                throw new NullChainException(message);
            } else {
                String format = String.format(message.replaceAll("\\{\\s*}", "%s"), params);
                throw new NullChainException(format);
            }
        }
    }

    public static void checkNull(Object obj, Supplier<String> message) {
        if (obj == null) {
            throw new NullChainException(message.get());
        }
    }

    //创建一个空的对象, 好处就是可以放心使用空链的方法不会空指针, 如果返回null那么不能使用空链的方法了
    public static <T extends NullCheck> T createEmpty(Class<? extends T> clazz) {
        return NullByteBuddy.createAgencyAddEmptyMember(clazz);
    }
}