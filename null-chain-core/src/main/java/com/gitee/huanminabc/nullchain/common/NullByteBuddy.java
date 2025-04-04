package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.base.sync.ext.NullChainExt;
import com.gitee.huanminabc.nullchain.base.sync.ext.NullConvertExt;
import com.gitee.huanminabc.nullchain.base.sync.ext.NullFinalityExt;
import com.gitee.huanminabc.nullchain.base.sync.ext.NullToolsExt;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.DeclaredByType;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NullByteBuddy {
    public static final String EMPTY_MEMBER_NAME = "$NULL_MEMBER_EMPTY$"; //空成员变量名
    //用于缓存空对象
    private static final Map<Class<?>, Object> emptyMemberMap = new ConcurrentHashMap<>();

    //创建一个代理对象,并添加一个空成员变量
    public static <T> T createAgencyAddEmptyMember(Class<? extends NullCheck> clazz) {
        return (T) emptyMemberMap.computeIfAbsent(clazz, aClass -> {
            // 创建一个动态类，并添加一个成员变量
            try ( DynamicType.Unloaded  make = new ByteBuddy()
                    // 从 ServiceClass 子类化
                    .subclass(clazz)
                    // 添加一个成员变量
                    .defineField(EMPTY_MEMBER_NAME, boolean.class, Opcodes.ACC_PRIVATE)
                    //1.不匹配NULLExt接口的方法
                    //2.只拦截public方法
                    .method(new NotNULLExtMatcher().and(ElementMatchers.isPublic()))
                    //除了NULLExt接口的方法其他的都抛出异常
                    .intercept(ExceptionMethod.throwing(NullChainException.class, "不能调用空对象的方法"))
                    .make()){
                Class<?> dynamicType =  make
                        //将创建的代理类放入到类的原始加载器中,而不是自己在创建一个类加载器
                        .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                        .getLoaded();
                // 创建一个实例
                Object instance = dynamicType.newInstance();
                // 设置成员变量的值
                Field declaredField = dynamicType.getDeclaredField(EMPTY_MEMBER_NAME);
                declaredField.setAccessible(true);
                declaredField.set(instance, true);
                return instance;
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                throw new NullChainException(e);
            }
        });
    }

    //反射获取成员变量的值,如果不存在返回false
    public static boolean getEmptyMember(NullCheck obj) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(EMPTY_MEMBER_NAME);
            declaredField.setAccessible(true);
            return declaredField.getBoolean(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    private static class NotNULLExtMatcher <T extends DeclaredByType> extends ElementMatcher.Junction.AbstractBase<T> {
        private final static Set<String> matchers=new HashSet<>();
        static {
            //Object类和NULLExt接口的方法不拦截
            //Object类的方法是所有类都有的方法,这种方法一般不会涉及到内容的操作都是一些控制操作
            matchers.add(Object.class.getName());
            matchers.add(NullExt.class.getName());
            matchers.add(NullChainExt.class.getName());
            matchers.add(NullConvertExt.class.getName());
            matchers.add(NullToolsExt.class.getName());
            matchers.add(NullFinalityExt.class.getName());
        }

        public boolean matches(T target) {
            TypeDefinition declaringType = target.getDeclaringType();
            if (declaringType == null) {
                return false;
            }
            String typeName = declaringType.asGenericType().asRawType().getTypeName();
            return !matchers.contains(typeName);
        }
    }

}
