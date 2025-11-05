package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import lombok.Data;

import java.util.*;

/**
 * Null类型定义类 - 提供类型参数的定义和管理功能
 * 
 * <p>该类提供了类型参数的定义和管理功能，支持常用类型的预定义和自定义类型的创建。
 * 通过类型安全的参数管理，为Null链操作提供类型约束和验证能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>类型定义：定义各种类型的参数信息</li>
 *   <li>常用类型：提供常用类型的预定义</li>
 *   <li>类型验证：提供类型验证功能</li>
 *   <li>参数管理：管理类型参数信息</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>预定义类型：提供常用类型的预定义</li>
 *   <li>灵活扩展：支持自定义类型的创建</li>
 *   <li>参数管理：提供统一的参数管理机制</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Null 空值处理入口
 */
@Data
public class NullType {

    //常用的类型
    public static NullType STRING = NullType.params(String.class);
    public static NullType INTEGER = NullType.params(Integer.class);
    public static NullType LONG = NullType.params(Long.class);
    public static NullType DOUBLE = NullType.params(Double.class);
    public static NullType FLOAT = NullType.params(Float.class);
    public static NullType BOOLEAN = NullType.params(Boolean.class);
    public static NullType BYTE = NullType.params(Byte.class);
    public static NullType BYTE_ARRAY = NullType.params(Byte[].class);
    public static NullType SHORT = NullType.params(Short.class);
    public static NullType CHARACTER = NullType.params(Character.class);
    public static NullType DATE = NullType.params(Date.class);
    public static NullType BIG_DECIMAL = NullType.params(java.math.BigDecimal.class);
    public static NullType MAP = NullType.params(Map.class);
    public static NullType LIST = NullType.params(List.class);
    public static NullType SET = NullType.params(Set.class);

    @Data
    public static class NullTypeInfo {
        private String as;//别名
        private Class<?>[] types;//类型, 一个位置只能有一个类型,但是可以兼容多个类型
        private Object defaultValue;//默认值

        public static NullTypeInfo create(String as, Object defaultValue, Class<?>... types) {
            NullTypeInfo nullTypeInfo = new NullTypeInfo();
            nullTypeInfo.setAs(as);
            nullTypeInfo.setDefaultValue(defaultValue);
            if (types == null || types.length == 0) {
                nullTypeInfo.setTypes(new Class<?>[0]);
            } else {
                nullTypeInfo.setTypes(types);
            }
            return nullTypeInfo;
        }
    }

    private Map<Integer, NullTypeInfo> types;
    private int length;


    //组合类型, 兼顾别名  一个位置只能有一个类型,但是可以兼容多个类型
    public static NullType of(String as, Class<?>... types) {
        //检测types是否为空
        if ((NullUtil.isAny(as, types))) {
            throw new NullChainException("as , types 参数不能为空");
        }
        return __of__(as, types);
    }

    //单一类型, 带别名和默认值
    public static <DF> NullType of(String as, DF defaultValue, Class<DF> types) {
        if (NullUtil.isAny(as, types, defaultValue)) {
            throw new NullChainException("as ,defaultValue , types 参数不能为空");
        }
        NullType nullType = new NullType();
        Map<Integer, NullTypeInfo> typesMap = new HashMap<>();
        typesMap.put(0, NullTypeInfo.create(as, defaultValue, types));
        nullType.setTypes(typesMap);
        nullType.setLength(0); //默认值作为主参数可以不传
        return nullType;
    }

    //一个位置可以有多个类型, 任意一个符合即可
    //本质和NullType params(Class<?>... types) 一样 只是为了语义化
    public static NullType of(Class<?>... types) {
        return __of__(null, types);
    }


    //================================================下面是参数列表方法=========================================================

    //全类型,  xxx(类型1,类型2,类型3...)  最小长度为类型的长度
    public static NullType params(Class<?>... types) {
        return __of__(null, types);
    }

    //全类型 限制最小长度,  xxx(3, 类型1,类型2,类型3,类型4)  ,最小长度为3
    public static NullType params(int minLen, Class<?>... types) {
        NullType nullType = params(types);
        nullType.setLength(minLen);
        return nullType;
    }

    //组合类型,一个位置可以有多个类型
    public static NullType params(NullType... types) {
        //检测types是否为空
        if (Null.is(types)) {
            throw new NullChainException("types 参数不能为空");
        }
        NullType nullType = new NullType();
        Map<Integer, NullTypeInfo> typesMap = new HashMap<>();
        for (int i = 0; i < types.length; i++) {
            typesMap.put(i, types[i].getTypes());
        }
        nullType.setTypes(typesMap);
        nullType.setLength(types.length);
        return nullType;
    }

    //全类型 限制最小长度, xxx(3, 类型1,类型2,类型3,类型4)  ,最小长度为3
    //支持复杂模式配置
    public static NullType params(int minLen, NullType... types) {
        if (minLen < 0) {
            throw new NullChainException("minLen 参数不能小于0");
        }
        NullType nullType = params(types);
        nullType.setLength(minLen);
        return nullType;
    }


    //类型校验器, 传入一个Object数组,校验是否符合类型, 返回true表示不符合类型
    public void checkType(Object[] objects, Map<String, Object> context) {
        if (types == null) {
            throw new NullChainException("types 不能为空");
        }
        int minLen;
        if (objects.length < length) {
            //表示用户啥也没输入, 但是类型中是否有默认值
            for (int i = 0; i < types.size(); i++) {
                NullTypeInfo nullTypeInfo = types.get(i);
                if (nullTypeInfo != null && nullTypeInfo.getAs() != null && nullTypeInfo.getDefaultValue() != null) {
                    context.put(nullTypeInfo.getAs(), nullTypeInfo.getDefaultValue());
                }
            }
            //如果添加了内容那么之间返回
            if (!context.isEmpty()) {
                return;
            }
            throw new NullChainException("参数长度不符合要求,期望长度为" + length);
        } else {
            //如果参数大于types的长度,多余的参数不做校验
            minLen = Math.min(objects.length, types.size());
            //如果minLen大于objects的长度,那么就表示参数不符合要求
            if (minLen > objects.length) {
                throw new NullChainException("参数长度不符合要求,最小期望长度为:{},你的长度为:{}" ,minLen, objects.length);
            }
            for (int i = 0; i < minLen; i++) {
                NullTypeInfo nullTypeInfo = types.get(i);
                if (checkType(objects[i], nullTypeInfo) ) {
                    //如果有别名,那么就放入上下文
                    if (nullTypeInfo.getAs() != null){
                        context.put(nullTypeInfo.getAs(), objects[i]);
                    }
                } else {
                    throw new NullChainException("参数类型不符合要求,第{}个参数类型不符合,期望类型为:{}, 你的类型是:{}" , i+1 ,Arrays.toString(nullTypeInfo.getTypes()), objects[i].getClass());
                }
            }
        }

        //如果最小参数大于types的长度,那么就表示objects或者types有多余的部分, 不在校验和取值的范围内
        if (minLen >= types.size()) {
            return;
        }
        //找找多余的部分是否有默认值
        for (int i = minLen; i < types.size(); i++) {
            NullTypeInfo nullTypeInfo = types.get(i);
            if (nullTypeInfo != null && nullTypeInfo.getAs() != null && nullTypeInfo.getDefaultValue() != null) {
                context.put(nullTypeInfo.getAs(), nullTypeInfo.getDefaultValue());
            }
        }
    }


    private static NullType __of__(String as, Class<?>... types) {
        NullType nullType = new NullType();
        Map<Integer, NullTypeInfo> typesMap = new HashMap<>();
        for (int i = 0; i < types.length; i++) {
            typesMap.put(i, NullTypeInfo.create(as, null, types[i]));
        }
        nullType.setTypes(typesMap);
        nullType.setLength(types.length);

        return nullType;
    }


    //一个占位里面可以有多个类型, 任意一个符合即可
    private boolean checkType(Object object, NullTypeInfo nullTypeInfo) {
        if (nullTypeInfo == null) {
            return false;
        }
        for (Class<?> aClass : nullTypeInfo.getTypes()) {
            if (aClass.isInstance(object)) {
                return true;
            }
        }
        return false;
    }

    //获取所以类型合并为一个NullTypeInfo , 取第一个类型的别名
    private NullTypeInfo getTypes() {
        if (Null.isAny(types)) {
            return NullTypeInfo.create(null, null);
        }
        NullTypeInfo nullTypeInfo = new NullTypeInfo();
        NullTypeInfo next = types.values().iterator().next();
        nullTypeInfo.setAs(next.getAs());
        nullTypeInfo.setDefaultValue(next.getDefaultValue());
        List<Class<?>> typeList = new ArrayList<>();
        for (NullTypeInfo value : types.values()) {
            typeList.addAll(Arrays.asList(value.getTypes()));
        }
        nullTypeInfo.setTypes(typeList.toArray(new Class<?>[0]));
        return nullTypeInfo;
    }

}
