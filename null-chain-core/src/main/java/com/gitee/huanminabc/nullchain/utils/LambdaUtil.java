package com.gitee.huanminabc.nullchain.utils;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.google.common.collect.Maps;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * lambda工具类
 * @author huanmin
 * @date 2024/1/10
 */
public class LambdaUtil {

    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_CLAZZ;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            Field member = classDirectMethodHandle.getDeclaredField("member");
            member.setAccessible(true);

            FIELD_MEMBER_NAME = ReflectionKit.setAccessible(classDirectMethodHandle.getDeclaredField("member"));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_CLAZZ = ReflectionKit.setAccessible(classMemberName.getDeclaredField("clazz"));
            FIELD_MEMBER_NAME_NAME = ReflectionKit.setAccessible(classMemberName.getDeclaredField("name"));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new NullChainException(e);
        }
    }


    public static<T,U> String fieldInvocation(Function<T,U> myFun) {
        Map<String, Object> stringStringMap = lambdaInvocation(myFun);
        return stringStringMap.get("fieldName").toString();
    }

    public static<T,U> String methodInvocation(Function<T,U> myFun) {
        Map<String, Object> stringStringMap = lambdaInvocation(myFun);
        String methodName = stringStringMap.get("methodName").toString();
        if (methodName.startsWith("lambda$")) {
            throw new NullChainException("methodInvocation获取方法名失败:{}",myFun);
        }
        return  methodName;
    }

    /**
     * 获取lambda的 方法名,属性名,这样就可以使用反射了 , 这个性能上没啥问题10万次调用耗时 10毫秒
     * methodInvocation(ProbationPageSearchParam::getProcessInstanceId);
     * @param myFun
     * @throws Exception
     */
    public static<T,U> Map<String,Object> lambdaInvocation(Function<T, U> myFun) {
        Map<String,Object> map = Maps.newHashMap();
        map.put("methodName","");
        map.put("fieldName","");
        map.put("clazz",null);
        try {
            if (myFun == null) {
                throw new NullChainException("methodInvocation获取方法名失败:{}",myFun);
            }
            // 1. IDEA 调试模式下 lambda 表达式是一个代理
            if (myFun instanceof Proxy) {
                Proxy proxy = (Proxy) myFun;
                InvocationHandler handler = Proxy.getInvocationHandler(proxy);
                try {
                    Object dmh = ReflectionKit.setAccessible(handler.getClass().getDeclaredField("val$target")).get(handler);
                    Object member = FIELD_MEMBER_NAME.get(dmh);
                    map.put("methodName",  FIELD_MEMBER_NAME_NAME.get(member));
                    map.put("fieldName", methodToProperty((String) FIELD_MEMBER_NAME_NAME.get(member)));
                    map.put("clazz", FIELD_MEMBER_NAME_CLAZZ.get(member));//获取类
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new NullChainException(e);
                }
                return map;
            }
            // 直接调用writeReplace
            Method writeReplace = myFun.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(myFun);
            SerializedLambda serializedLambda = (SerializedLambda) sl;
            map = Maps.newHashMap();
            map.put("methodName",serializedLambda.getImplMethodName());
            map.put("fieldName", methodToProperty(serializedLambda.getImplMethodName()));
            map.put("clazz",Class.forName(serializedLambda.getImplClass().replace("/", ".")));
        } catch (Exception e) {
            throw new NullChainException(e);
        }
        return map;
    }


    //通过方法名获属性名称
    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        }else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }else{
            throw new NullChainException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }
        if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }



}
