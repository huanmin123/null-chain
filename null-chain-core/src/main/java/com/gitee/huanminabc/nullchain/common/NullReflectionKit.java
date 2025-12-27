package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.jcommon.reflect.ClassIdentifyUtil;
import com.gitee.huanminabc.jcommon.reflect.ClassUtil;
import com.gitee.huanminabc.jcommon.reflect.FieldUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Null反射工具类 - 提供反射操作的工具功能
 * 
 * <p>该类提供了反射操作的工具功能，支持动态调用方法、访问字段、创建实例等反射操作。
 * 通过反射机制，为Null链操作提供动态性和灵活性。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>方法调用：动态调用对象的方法</li>
 *   <li>字段访问：动态访问对象的字段</li>
 *   <li>实例创建：动态创建对象实例</li>
 *   <li>类型检查：检查对象类型和继承关系</li>
 *   <li>数组操作：动态操作数组对象</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>反射支持：提供完整的反射操作支持</li>
 *   <li>异常处理：完善的异常处理机制</li>
 *   <li>类型安全：通过类型检查保证安全性</li>
 *   <li>性能优化：提供高效的反射操作</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see ClassIdentifyUtil 类识别工具
 * @see FieldUtil 字段工具
 */
@Slf4j
public class NullReflectionKit {

    //追加异常链路信息
    public  static  <X extends RuntimeException> X orRuntimeException(X exception, StringBuilder linkLog) throws X {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        Class<? extends Throwable> aClass = exception.getClass();
        updateDetailMessage(exception, linkLog);
        return exception;
    }
    public  static  <X extends Throwable> X orThrowable(X exception, StringBuilder linkLog) throws X {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        Class<? extends Throwable> aClass = exception.getClass();
        updateDetailMessage(exception, linkLog);
        return exception;
    }



    public  static  <X extends Throwable> NullChainException addRunErrorMessage(X exception, StringBuilder linkLog)  {
        //反射取出来最顶级的detailMessage,把链路信息放进去
        updateDetailMessage(exception, linkLog);
        //必须是NullChainException,如果不是就包装成NullChainException返回, 在空链中基本不会出现这种情况,但是为了保险起见拦一道
        if (!(exception instanceof NullChainException)) {
            return new NullChainException(exception);
        }
        return (NullChainException) exception;
    }
    public  static  <X extends RuntimeException> X addRunErrorMessage(Class<? extends RuntimeException> exceptionClass, StackTraceElement[]  stackTraceElements, StringBuilder linkLog)  {
        RuntimeException runtimeException = ClassUtil.newInstance(exceptionClass);
        if (stackTraceElements!=null){
            runtimeException.setStackTrace(stackTraceElements);
        }
        //反射取出来最顶级的detailMessage,把链路信息放进去
        updateDetailMessage(runtimeException, linkLog);
         return (X) runtimeException;
    }

    //反射获取对象内部size 或者length 调用公共的方法就行, 如果没有就返回0
    public static int getSize(Object object) {
        if (object == null) {
            return 0;
        }
        Class<?> aClass = object.getClass();
        //如果是8大数据类型那么勇toString 返回的就是本身的长度
        boolean primitiveOrWrapper = ClassIdentifyUtil.isPrimitiveOrWrapper(aClass);
        if (primitiveOrWrapper){
            return object.toString().length();
        }
        //如果数数组返回数组的长度
        if (aClass.isArray()) {
            return Array.getLength(object);
        }

        //否则取内部的size或者length方法,如果都没有那么返回0
        Method sizeMethod;
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
            sizeMethod.setAccessible(true);
            Object invoke = sizeMethod.invoke(object);
            if (invoke instanceof Integer || invoke instanceof Long) {
                return (int) invoke;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return 0;
        }
        return 0;
    }






    private static  void updateDetailMessage(Throwable exception, StringBuilder linkLog) {
        Field detailMessage = FieldUtil.getField(exception.getClass(),"detailMessage");
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
