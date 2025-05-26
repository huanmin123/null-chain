package com.gitee.huanminabc.nullchain.common;

/**
 * @Description TODO
 * @Author huanmin
 * @Date 2024/4/28 下午5:17
 */

import com.gitee.huanminabc.common.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.common.reflect.FieldUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * 反射工具类
 * </p>
 *
 * @author Caratacus
 * @since 2016-09-22
 */
@Slf4j
public class NullReflectionKit {

    //追加异常链路信息
    public  static  <X extends RuntimeException> X orRuntimeException(X exception, StringBuilder linkLog) throws X {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        Class<? extends Throwable> aClass = exception.getClass();
        updateDetailMessage(exception, linkLog, aClass);
        return exception;
    }
    public  static  <X extends Throwable> X orThrowable(X exception, StringBuilder linkLog) throws X {
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
        boolean primitiveOrWrapper = ClassIdentifyUtil.isPrimitiveOrWrapper(object.getClass());
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
        Field detailMessage = FieldUtil.getField(aClass,"detailMessage");
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
