package com.gitee.huanminabc.nullchain.core;

import com.alibaba.fastjson.JSON;
import com.gitee.huanminabc.jcommon.base.DateUtil;
import com.gitee.huanminabc.jcommon.enums.TimeEnum;
import com.gitee.huanminabc.jcommon.reflect.BeanCopyUtil;
import com.gitee.huanminabc.jcommon.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.NullTaskList;
import com.gitee.huanminabc.jcommon.enums.DateFormatEnum;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;

/**
 * Null类型转换基础实现类 - 提供类型转换、JSON、日期、复制等功能
 * 
 * <p>该类实现了NullConvert接口的所有方法，包括类型转换、JSON操作、日期处理和对象复制等功能。
 * 所有操作都是空值安全的，遇到null值会优雅处理而不会抛出异常。</p>
 * 
 * @param <T> 转换前的值的类型
 * @author huanmin
 * @date 2024/1/11
 * @since 1.1.2
 */
public class NullConvertBase<T> extends NullWorkFlowBase<T> implements NullConvert<T> {


    public NullConvertBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog,taskList);
    }

    @Override
    public <U> NullChain<U> type(Class<U> uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append(TYPE_Q).append(TYPE_CLASS_NULL).toString());
            }
            if (uClass.isInstance(value)) {
                linkLog.append(TYPE_ARROW);
                return NullBuild.noEmpty(uClass.cast(value));
            } else {
                linkLog.append(TYPE_Q).append(TYPE_MISMATCH).append(value.getClass().getName()).append(" vs ").append(uClass.getName());
                throw new NullChainException(linkLog.toString());
            }
        });
        return  NullBuild.busy(this);

    }

    @Override
    public <U> NullChain<U> type(U uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append(TYPE_Q).append(TYPE_CLASS_NULL).toString());
            }
            Class<?> aClass = uClass.getClass();
            //优化：只调用一次getClass()，避免在else分支中重复调用
            Class<?> valueClass = value != null ? value.getClass() : null;
            if (aClass.isInstance(value)) {
                linkLog.append(TYPE_ARROW);
                return NullBuild.noEmpty(aClass.cast(value));
            } else {
                linkLog.append(TYPE_Q).append(TYPE_MISMATCH).append(valueClass != null ? valueClass.getName() : "null").append(" vs ").append(aClass.getName());
                throw new NullChainException(linkLog.toString());
            }
        });
        return  NullBuild.busy(this);
    }

    @Override
    public NullChain<String> json() {
        this.taskList.add((value)->{
            //如果是字符串直接返回
            if (value instanceof String) {
                linkLog.append(JSON_ARROW);
                return NullBuild.noEmpty(value.toString());
            }
            //如果是NullChain那么需要取出来内容
            if (value instanceof NullChain) {
                value = ((NullChain<?>) value).orElseNull();
                if (value == null) {
                    linkLog.append(JSON_Q);
                    return NullBuild.empty();
                }
            }
            try {
                String json = JSON.toJSONString(value);
                //[]{}[{}] 都是空的
                if (json.equals("[]") ||
                        json.equals("{}") ||
                        json.equals("[{}]") ||
                        json.equals("{\"empty\":false}")// 这个是因为继承了NULLCheck里面的isEmpty方法导致的
                ) {
                    linkLog.append(JSON_Q);
                    return NullBuild.empty();
                }
                linkLog.append(JSON_ARROW);
                return NullBuild.noEmpty(json);
            } catch (Exception e) {
                linkLog.append(JSON_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }

    @Override
    public <U> NullChain<U> fromJson(Class<U> uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append(JSON_Q).append(uClass).append(" 不是字符串").toString());
            }
            //判断value是否是字符串
            if (!(value instanceof String)) {
                linkLog.append(JSON_Q);
                throw new NullChainException(linkLog.append(JSON_Q).append(value).append(" 不是字符串").toString());
            }
            try {
                U u = JSON.parseObject(value.toString(), uClass);
                linkLog.append(JSON_ARROW);
                return NullBuild.noEmpty(u);
            } catch (Exception e) {
                linkLog.append(JSON_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> NullChain<U> fromJson(U uClass) {
        return fromJson((Class<U>) uClass.getClass());
    }

    @Override
    public NullChain<String> dateFormat(DateFormatEnum dateFormatEnum) {
        this.taskList.add((value)->{
            String string;
            try {
                string = DateUtil.format(value, dateFormatEnum);
            } catch (Exception e) {
                linkLog.append(DATE_FORMAT_Q).append(value).append(" to ").append(dateFormatEnum.getValue()).append(" 失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (string == null) {
                linkLog.append(DATE_FORMAT_Q).append("转换时间格式失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_FORMAT_ARROW);
            return NullBuild.noEmpty(string);
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum) {
        this.taskList.add((value)->{
            T t;
            try {
                t = DateUtil.offset((T)value, offsetEnum, num, timeEnum);
            } catch (Exception e) {
                linkLog.append(DATE_OFFSET_Q).append(value).append(" 偏移时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (t == null) {
                linkLog.append(DATE_OFFSET_Q).append("偏移时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_OFFSET_ARROW);
            return NullBuild.noEmpty(t);
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum controlEnum, TimeEnum timeEnum) {
        return dateOffset(controlEnum, 1, timeEnum);
    }

    @Override
    public NullChain<Integer> dateCompare(Object date) {
        this.taskList.add((value)->{
            Integer compare;
            try {
                compare = DateUtil.compare(value, date);
            } catch (Exception e) {
                linkLog.append(DATE_COMPARE_Q).append(value).append(" 比较时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (compare == null) {
                linkLog.append(DATE_COMPARE_Q).append("比较时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_COMPARE_ARROW);
            return NullBuild.noEmpty(compare);
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<Long> dateBetween(Object date, TimeEnum timeEnum) {
        this.taskList.add((value)->{
            Long between;
            try {
                between = DateUtil.between(value, date, timeEnum);
            } catch (Exception e) {
                linkLog.append(DATE_BETWEEN_Q).append(value).append(" 计算日期间隔失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (between == null) {
                linkLog.append(DATE_BETWEEN_Q).append("计算日期间隔失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_BETWEEN_ARROW);
            return NullBuild.noEmpty(between);
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> copy() {
        this.taskList.add((value)->{
            try {
                linkLog.append(COPY_ARROW);
                return NullBuild.noEmpty(BeanCopyUtil.copy(value));
            } catch (Exception e) {
                linkLog.append(COPY_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }

    @Override
    public NullChain<T> deepCopy() {
        this.taskList.add((value)->{
            try {
                linkLog.append(DEEP_COPY_ARROW);
                return NullBuild.noEmpty(BeanCopyUtil.deepCopy(value));
            } catch (Exception e) {
                linkLog.append(DEEP_COPY_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }

    @SafeVarargs
    @Override
    public final <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper) {
        this.taskList.add((value)->{
            if (Null.is(mapper)) {
                throw new NullChainException(linkLog.append(PICK_PARAM_NULL).toString());
            }
            try {
                T object = (T) value.getClass().newInstance();
                for (NullFun<? super T, ? extends U> function : mapper) {
                    U apply = function.apply((T)value);
                    //跳过空值
                    if (Null.non(apply)) {
                        String field = LambdaUtil.getFieldName(function);
                        //添加set方法
                        String firstLetter = field.substring(0, 1).toUpperCase();    //将属性的首字母转换为大写
                        String setMethodName = "set" + firstLetter + field.substring(1);
                        //获取方法对象,将值设置进去
                        object.getClass().getMethod(setMethodName, apply.getClass()).invoke(object, apply);
                    }
                }
                linkLog.append(PICK_ARROW);
                return NullBuild.noEmpty(object);
            } catch (Exception e) {
                linkLog.append(PICK_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return NullBuild.busy(this);
    }

}
