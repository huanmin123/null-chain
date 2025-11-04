package com.gitee.huanminabc.nullchain.common;


import com.gitee.huanminabc.common.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.common.str.StringUtil;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.core.NullChain;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Null工具类 - 提供各种类型的空值判断功能
 * 
 * <p>该类提供了判断各种类型是否为空的功能，包括基本类型、集合类型、Map类型、数组类型等。
 * 通过统一的空值判断接口，为开发者提供便捷的空值检查能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>基本类型判断：判断基本类型是否为空</li>
 *   <li>集合类型判断：判断Collection、Map等集合类型是否为空</li>
 *   <li>数组类型判断：判断数组类型是否为空</li>
 *   <li>字符串判断：判断字符串是否为空或空白</li>
 *   <li>日期判断：判断日期类型是否为空</li>
 *   <li>对象判断：判断任意对象是否为空</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 *   <li>全面覆盖：支持各种类型的空值判断</li>
 *   <li>性能优化：提供高效的判断算法</li>
 * </ul>
 * 
 * @author 胡安民
 * @since 1.0.0
 * @version 1.1.1
 * @see NullCheck 空值检查接口
 * @see NullChain 链式操作接口
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
            //优化：先检查长度，如果长度不是4，可以快速跳过"null"字符串检查
            if (!blank && str instanceof String && str.length() == 4) {
                //优化：如果长度已经是4，直接比较整个字符串，避免substring操作
                return "null".equalsIgnoreCase((String) str);
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
        //优化：直接检查数组长度，避免调用完整的is()方法
        if (o == null || o.length == 0) {
            return true;
        }
        for (Object o1 : o) {
            if (is(o1)) {
                return true;
            }
        }
        return false;
    }

    //判断全部是空返回true,只要有一个不为空就返回false
    public static boolean isAll(Object... o) {
        //优化：直接检查数组长度，避免调用完整的is()方法
        if (o == null) {
            return true;
        }
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
        //优化：直接检查数组长度，避免调用完整的is()方法
        if (o == null || o.length == 0) {
            return false;
        }
        for (Object o1 : o) {
            if (is(o1)) {
                return false;
            }
        }
        return true;
    }


    //如果有空那么必然返回false
    public static boolean eq(Object a, Object b) {
        if (isAny(a, b)) {
            return false;
        }
        if (a == b) {
            return true; //如果是同一个对象那么必然相等
        }
        Object realValueA=a;
        Object realValueB=b;
        //优化：使用instanceof替代isInstance，性能更好
        if (a instanceof NullKernelAbstract){
            realValueA=((NullKernelAbstract<?>)a).taskList.runTaskAll().value;
        }
        if (b instanceof NullKernelAbstract){
            realValueB=((NullKernelAbstract<?>)b).taskList.runTaskAll().value;
        }
        return realValueA == realValueB || realValueA.equals(realValueB);
    }

    public static boolean eqAny(Object a, Object... b) {
        if (isAny(a, b)) {
            return false;
        }
        for (Object o : b) {
            if (eq(a, o)) {
                return true;
            }
        }
        return false;
    }

    //如果不相等那么返回true
    public static boolean notEq(Object a, Object b) {
        if (isAny(a, b)) {
            return false;
        }
        return !eq(a, b);
    }

    //全部不相等返回true,只要有一个相等就返回false
    public static boolean notEqAll(Object a, Object... b) {
        if (isAny(a, b)) {
            return false;
        }
        for (Object o : b) {
            if (eq(a, o)) {
                return false;
            }
        }
        return true;
    }


    //如果是空那么返回null ,请type和obj的类型一致否则返回的是null
    public static <T> T orElseNull(Object obj, Class<T> type) {
        if (is(obj)) {
            return null;
        }
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        return null;
    }

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

    //如果值为空那么就创建一个空的对象, 请不要使用不能被newInstance的类,否则会报错, 可以理解为必须有一个公开的无参构造函数
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
                //优化：使用预编译的Pattern，避免每次调用时重新编译正则表达式
                String format = String.format(NullConstants.PLACEHOLDER_PATTERN.matcher(message).replaceAll("%s"), params);
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