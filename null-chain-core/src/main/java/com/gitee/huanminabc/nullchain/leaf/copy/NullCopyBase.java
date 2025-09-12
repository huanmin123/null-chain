package com.gitee.huanminabc.nullchain.leaf.copy;

import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.common.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import java.util.function.Function;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import lombok.extern.slf4j.Slf4j;

/**
 * Null复制操作基础实现类
 * 
 * @param <T> 复制值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
@Slf4j
public class NullCopyBase<T> extends NullChainBase<T> implements  NullCopy<T> {
    public NullCopyBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }



    @Override
    public NullCopy<T> copy() {
        this.taskList.add((value)->{
            try {
                linkLog.append(COPY_ARROW);
                return NullBuild.noEmpty(BeanCopyUtil.copy(value));
            } catch (Exception e) {
                linkLog.append(COPY_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyCopy(this);
    }

    @Override
    public NullCopy<T> deepCopy() {
        this.taskList.add((value)->{
            try {
                linkLog.append(DEEP_COPY_ARROW);
                return NullBuild.noEmpty(BeanCopyUtil.deepCopy(value));
            } catch (Exception e) {
                linkLog.append(DEEP_COPY_Q);
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyCopy(this);
    }

    @SafeVarargs
    @Override
    public final <U> NullCopy<T> pick(Function<? super T, ? extends U>... mapper) {
        this.taskList.add((value)->{
            if (Null.is(mapper)) {
                throw new NullChainException(linkLog.append(PICK_PARAM_NULL).toString());
            }
            try {
                T object = (T) value.getClass().newInstance();
                for (Function<? super T, ? extends U> function : mapper) {
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
        return  NullBuild.busyCopy(this);
    }
}
