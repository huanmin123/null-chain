package com.gitee.huanminabc.nullchain.tool.object;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.tool.NullTool;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简要描述
 *
 * @Author: huanmin
 * @Date: 2025/3/24 22:18
 * @Version: 1.0
 * @Description: 文件作用详细描述....
 */
public class SerializeTool implements NullTool<Serializable, byte[]> {
    @Override
    public NullType checkTypeParams() {
        //最小参数为1 , 默认为true 开启检查类是否实现Serializable接口
        //但是在有些情况下, 你可能不想检查, 比如lombok生成的Builder类 或者其他插件导致的未知内部类被意外检查出没有实现Serializable接口导致无法序列化
        return NullType.params(NullType.of("open", true, Boolean.class));
    }

    @Override
    public byte[] run(Serializable preValue, NullChain<?>[] params, Map<String, Object> context) throws Exception {

        //检查所有的子类是否实现了Serializable接口
        // 替换成你想要检查的类
        if (Null.of(context.get("open")).type(Boolean.class).get()) {
            Class<?> outerClass = preValue.getClass();
            Class<?> aClass = areAllInnerClassesSerializable(outerClass);
            if (aClass != null) {
                throw new NullChainException("{}的内部类{}必须实现Serializable接口", preValue.getClass().getName(), aClass.getName());
            }
        }

        ObjectOutputStream oos;
        ByteArrayOutputStream baos;
        // 序列化
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(preValue);
        return baos.toByteArray();
    }


    // 检查成员使用了内部类, 并且内部类都实现了Serializable接口,如果没有实现Serializable接口,那么就返回这个类
    // 有一个缺陷,就是如果成员使用的是其他类内部的内部类,那么这里就没有继续深入处理,在序列化的时候,编译器会报错
    public static Class<?> areAllInnerClassesSerializable(Class<?> outerClass) {
        Class<?>[] declaredClasses = outerClass.getDeclaredClasses();
        //将数据结构转换为Map<String, Class<?>>
        Map<String, Class<?>> map = Arrays.stream(declaredClasses)
                .collect(Collectors.toMap(Class::getName, clazz -> clazz));

        Field[] declaredFields = outerClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.getType().isMemberClass() &&
                    !Modifier.isStatic(declaredField.getModifiers()) &&
                    !Modifier.isTransient(declaredField.getModifiers())) {
                Class<?> clazz = map.get(declaredField.getType().getName());
                if (clazz != null) {
                    //如果没有实现Serializable接口,那么就返回这个类
                    if (!Serializable.class.isAssignableFrom(clazz)) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }
}
