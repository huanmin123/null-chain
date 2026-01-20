package com.gitee.huanminabc.nullchain.common.function;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TransienceUtil {

    //判断字段是否不能被序列化
    public static boolean isNotSerializable(Field field) {
        //排除常亮
        if (Modifier.isFinal(field.getModifiers())) {
            return true;
        }
        //跳过静态字段
        if (Modifier.isStatic(field.getModifiers())) {
            return true;
        }
        //跳过transient字段
        return Modifier.isTransient(field.getModifiers());
    }
}
