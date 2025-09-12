package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.core.ext.*;
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

/**
 * Null字节码工具类 - 提供字节码操作的工具功能
 * 
 * <p>该类提供了字节码操作的工具功能，基于ByteBuddy框架实现动态类生成和字节码操作。
 * 通过字节码技术，为Null链操作提供动态性和高性能。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>动态类生成：动态生成类定义</li>
 *   <li>字节码操作：操作字节码结构</li>
 *   <li>类加载：动态加载生成的类</li>
 *   <li>接口实现：动态实现接口</li>
 *   <li>方法拦截：拦截和修改方法调用</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>字节码技术：基于ByteBuddy框架</li>
 *   <li>动态性：支持运行时动态生成</li>
 *   <li>高性能：字节码级别的性能优化</li>
 *   <li>类型安全：通过类型检查保证安全性</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see ByteBuddy ByteBuddy框架
 * @see NullCheck 空值检查接口
 * @see NullExt 扩展接口
 */

public class NullByteBuddy {
    public static final String EMPTY_MEMBER_NAME = "$NULL_MEMBER_EMPTY$"; //空成员变量名
    //用于缓存空对象
    private static final Map<Class<?>, Object> emptyMemberMap = new ConcurrentHashMap<>();

    //创建一个代理对象,并添加一个空成员变量
    public static <T> T createAgencyAddEmptyMember(Class<? extends NullCheck> clazz) {
        return (T) emptyMemberMap.computeIfAbsent(clazz, aClass -> {
            // 创建一个动态类，并添加一个成员变量
            try {
                DynamicType.Unloaded make = new ByteBuddy()
                        // 从 ServiceClass 子类化
                        .subclass(clazz)
                        // 添加一个成员变量
                        .defineField(EMPTY_MEMBER_NAME, boolean.class, Opcodes.ACC_PRIVATE)
                        //1.不匹配NULLExt接口的方法
                        //2.只拦截public方法
                        .method(new NotNULLExtMatcher().and(ElementMatchers.isPublic()))
                        //除了NULLExt接口的方法其他的都抛出异常
                        .intercept(ExceptionMethod.throwing(NullChainException.class, "不能调用空对象的方法"))
                        .make();
                Class<?> dynamicType = make
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

    private static class NotNULLExtMatcher<T extends DeclaredByType> extends ElementMatcher.Junction.AbstractBase<T> {
        private final static Set<String> matchers = new HashSet<>();

        static {
            //Object类和NULLExt接口的方法不拦截
            //Object类的方法是所有类都有的方法,这种方法一般不会涉及到内容的操作都是一些控制操作
            matchers.add(Object.class.getName());
            matchers.add(NullExt.class.getName());
            matchers.add(NullChainExt.class.getName());
            matchers.add(NullConvertExt.class.getName());
            matchers.add(NullWorkFlowExt.class.getName());
            matchers.add(NullFinalityExt.class.getName());
            matchers.add(NullKernelExt.class.getName());
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
