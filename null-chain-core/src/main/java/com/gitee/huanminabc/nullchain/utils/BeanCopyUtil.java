package com.gitee.huanminabc.nullchain.utils;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 1.一次处理建议在1000个以内copy对象时使用 ,大量对象使用BeanCopierUtil
 * 2.对象内必须有get set方法
 * </p>
 *
 * @author zhanghao
 */
public class BeanCopyUtil  {


    private static String[] getNullPropertyNames(Object source, String... ignoreProperties) {
        try {
            Set<String> list = new HashSet<>();
            Class<?> aClass = source.getClass();
            //获取全部的属性
            Field[] declaredFields = aClass.getDeclaredFields();
            //遍历属性
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Object object = declaredField.get(source);
                if (NullUtil.non(object)) {
                    list.add(declaredField.getName());
                }
            }
            //添加额外忽略的属性
            list.addAll(Arrays.asList(ignoreProperties));
            return list.toArray(new String[list.size()]);
        } catch (IllegalAccessException e) {
            throw new NullChainException(e);
        }
    }



    //深拷贝(序列化方式)
    public static <T> T deepCopy(T obj)  {
        if (obj instanceof Serializable){
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                return (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new NullChainException(e);
            }
        }else{
            throw new NullChainException("该对象不支持序列化,无法进行深拷贝,请实现Serializable接口");
        }
    }


    /**
     * @param source 源文件的
     * @param target 目标文件
     * @param ignoreProperties 忽略的属性
     * @return
     * @throws Exception
     */
    // 该方法实现对Customer对象的拷贝操作
    public static <T> void copy(T source, T target, String... ignoreProperties) {
        try {
            Class<?> classType = source.getClass();
            //获得对象的所有成员变量
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                //获取成员变量的名字
                String name = field.getName();//获取成员变量的名字，此处为id，name,age
                //过滤serialVersionUID
                if ("serialVersionUID".equals(name)) {
                    continue;
                }
                //排除ignoreProperties
                if (ignoreProperties != null && ignoreProperties.length > 0) {
                    boolean flag = false;
                    for (String ignoreProperty : ignoreProperties) {
                        if (ignoreProperty.equals(name)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    }
                }

                //获取get和set方法的名字
                String firstLetter = name.substring(0, 1).toUpperCase();    //将属性的首字母转换为大写
                String getMethodName = "get" + firstLetter + name.substring(1);
                String setMethodName = "set" + firstLetter + name.substring(1);
                //获取方法对象
                Method getMethod = classType.getMethod(getMethodName);
                Method setMethod = classType.getMethod(setMethodName, field.getType());//注意set方法需要传入参数类型

                //调用get方法获取旧的对象的值
                Object value = getMethod.invoke(source);

                //调用set方法将这个值复制到新的对象中去
                setMethod.invoke(target, value);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new NullChainException(e);
        }

    }

    /**
     * 拷贝对象,忽略null值
     * @param source
     * @param target
     * @param <T>
     */
    public static <T> void copyIgnoreNull(T source, T target) {
        copy(source, target, getNullPropertyNames(source));
    }

    public static <T> T copy(T source) {
        try {
            Class<?> classType = source.getClass();
            //用第二个带参数的构造方法生成对象
            /**
             *     Constructor cons2 = classType.getConstructor(new Class[] {String.class, int.class});
             *         Object obj2 = cons2.newInstance(new Object[] {"ZhangSan",20});
             */
            Object objectCopy = source.getClass().getConstructor(new Class[]{}).newInstance(new Object[]{});
            //获得对象的所有成员变量
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                //获取成员变量的名字
                String name = field.getName();    //获取成员变量的名字，此处为id，name,age
                if ("serialVersionUID".equals(name)) {
                    continue;
                }
                //获取get和set方法的名字
                String firstLetter = name.substring(0, 1).toUpperCase();    //将属性的首字母转换为大写
                String getMethodName = "get" + firstLetter + name.substring(1);
                String setMethodName = "set" + firstLetter + name.substring(1);
                //获取方法对象
                Method getMethod = classType.getMethod(getMethodName);
                Method setMethod = classType.getMethod(setMethodName, field.getType());//注意set方法需要传入参数类型
                //调用get方法获取旧的对象的值
                Object value = getMethod.invoke(source, new Object[]{});
                //调用set方法将这个值复制到新的对象中去
                setMethod.invoke(objectCopy, new Object[]{value});
            }
            return (T) objectCopy;
        } catch (NoSuchMethodException e) {
            //如果没有找到方法,那么就进行属性拷贝
            return  copyField(source);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new NullChainException(e);
        }
    }

    //属性拷贝
    public static <T> T copyField(T source) {
        try {
            Class<?> classType = source.getClass();
            Object objectCopy = source.getClass().getConstructor(new Class[]{}).newInstance();
            //获得对象的所有成员变量
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                //获取成员变量的名字
                String name = field.getName();    //获取成员变量的名字，此处为id，name,age
                if ("serialVersionUID".equals(name)) {
                    continue;
                }
                //跳过静态属性和final属性
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                field.set(objectCopy, field.get(source));
            }
            return (T) objectCopy;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new NullChainException(e);
        }
    }


}
