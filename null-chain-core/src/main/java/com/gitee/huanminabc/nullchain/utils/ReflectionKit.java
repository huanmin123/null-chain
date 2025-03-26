package com.gitee.huanminabc.nullchain.utils;

/**
 * @Description TODO
 * @Author huanmin
 * @Date 2024/4/28 下午5:17
 */

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.*;
import java.security.AccessController;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>
 * 反射工具类
 * </p>
 *
 * @author Caratacus
 * @since 2016-09-22
 */
@Slf4j
public class ReflectionKit {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap<>(8);

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
    }
    /**
     * 获取字段get方法
     *
     * @param cls   class
     * @param field 字段
     * @return Get方法
     */
    public static Method getMethod(Class<?> cls, Field field) {
        try {
            return cls.getDeclaredMethod(ReflectionKit.getMethodCapitalize(field, field.getName()));
        } catch (NoSuchMethodException e) {
            throw new NullChainException("Error: NoSuchMethod in %s.  Cause:", e, cls.getName());
        }
    }
    /**
     * <p>
     * 反射 method 方法名，例如 getId
     * </p>
     *
     * @param field
     * @param str   属性字符串内容
     */
    public static String getMethodCapitalize(Field field, final String str) {
        Class<?> fieldType = field.getType();
        // fix #176
        return concatCapitalize(boolean.class.equals(fieldType) ? "is" : "get", str);
    }

    /**
     * <p>
     * 反射 method 方法名，例如 setVersion
     * </p>
     *
     * @param str   String JavaBean类的version属性名
     * @return version属性的setter方法名称，e.g. setVersion
     * @deprecated 3.0.8
     */
    @Deprecated
    public static String setMethodCapitalize(final String str) {
        return concatCapitalize("set", str);
    }



    /**
     * <p>
     * 反射对象获取泛型
     * </p>
     *
     * @param clazz  new ArrayList<UserEntity>(){}; 匿名内部类写法才能获取到泛型
     * @param index 泛型所在位置
     * @return Class
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            log.warn(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            log.warn(String.format("Warn: Index: %s, Size of %s's Parameterized Type: %s .", index,
                    clazz.getSimpleName(), params.length));
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(String.format("Warn: %s not set the actual class on superclass generic parameter",
                    clazz.getSimpleName()));
            return Object.class;
        }
        return (Class<?>) params[index];
    }



    private static String concatCapitalize(String concatStr, final String str) {
        if (NullUtil.is(concatStr)) {
            concatStr = "";
        }
        if (str == null || str.length() == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // already capitalized
            return str;
        }

        return concatStr + Character.toTitleCase(firstChar) + str.substring(1);
    }

    /**
     * 设置可访问对象的可访问权限为 true
     *
     * @param object 可访问的对象
     * @param <T>    类型
     * @return 返回设置后的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
    }


    //追加异常链路信息
    public  static  <X extends Throwable> X orThrow(X exception,StringBuilder linkLog) throws X {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        Class<? extends Throwable> aClass = exception.getClass();
        updateDetailMessage(exception, linkLog, aClass);
        return exception;
    }

    public  static  <X extends Throwable> NullChainException addRunErrorMessage(X exception, StringBuilder linkLog)  {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        Class<? extends Throwable> aClass = exception.getClass();
        updateDetailMessage(exception, linkLog, aClass);
        //必须是NullChainException,如果不是就包装成NullChainException返回, 在空链中基本不会出现这种情况,但是为了保险起见拦一道
        if (!(exception instanceof NullChainException)) {
            return new NullChainException(exception);
        }
        return (NullChainException) exception;
    }


    //反射获取对象内部size 或者length 调用公共的方法就行, 如果没有就返回0
    public static int getSize(Object object) {
        if (object == null) {
            return 0;
        }
        //如果是8大数据类型那么勇toString 返回的就是本身的长度
        boolean primitiveOrWrapper = ClassUtils.isPrimitiveOrWrapper(object.getClass());
        if (primitiveOrWrapper){
            return object.toString().length();
        }
        //如果数数组返回数组的长度
        if (object.getClass().isArray()) {
            return Array.getLength(object);
        }

        //否则取内部的size或者length方法,如果都没有那么返回0
        Class<?> aClass = object.getClass();
        Method sizeMethod = null;
        try {
            sizeMethod = aClass.getMethod("size");
        } catch (NoSuchMethodException e) {
            try {
                sizeMethod = aClass.getMethod("length");
            } catch (NoSuchMethodException ex) {
                return 0;
            }
        }
        try {
            Object invoke = sizeMethod.invoke(object);
            if (invoke instanceof Integer||invoke instanceof Long) {
                return (int) invoke;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return 0;
        }
        return 0;
    }






    private static <X extends Throwable> void updateDetailMessage(X exception, StringBuilder linkLog, Class<? extends Throwable> aClass) {
        Field detailMessage = FieldUtils.getField(aClass, "detailMessage",true);
        if (detailMessage != null) {
            detailMessage.setAccessible(true);
            try {
                String o = (String) detailMessage.get(exception);
                if (o == null) {
                    o = "";
                }
                detailMessage.set(exception,   linkLog.toString()+" "+o);
            } catch (IllegalAccessException e) {
                throw new NullChainException(e);
            }
        }
    }

}
