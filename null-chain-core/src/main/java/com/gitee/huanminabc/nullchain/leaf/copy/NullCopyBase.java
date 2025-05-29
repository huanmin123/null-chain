package com.gitee.huanminabc.nullchain.leaf.copy;

import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.common.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-28 13:39
 **/
@Slf4j
public class NullCopyBase<T> extends NullChainBase<T> implements NullCopy<T> {
    public NullCopyBase(StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(linkLog, collect, taskList);
    }



    @Override
    public NullCopy<T> copy() {
        this.taskList.add((value)->{
            try {
                linkLog.append("copy->");
                return NullBuild.noEmpty(BeanCopyUtil.copy(value));
            } catch (Exception e) {
                linkLog.append("copy? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyCopy(this);
    }

    @Override
    public NullCopy<T> deepCopy() {
        this.taskList.add((value)->{
            try {
                linkLog.append("deepCopy->");
                return NullBuild.noEmpty(BeanCopyUtil.deepCopy(value));
            } catch (Exception e) {
                linkLog.append("deepCopy? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyCopy(this);
    }

    @SafeVarargs
    @Override
    public final <U> NullCopy<T> pick(NullFun<? super T, ? extends U>... mapper) {
        this.taskList.add((value)->{
            if (Null.is(mapper)) {
                throw new NullChainException(linkLog.append("pick? 传参不能为空").toString());
            }
            try {
                T object = (T) value.getClass().newInstance();
                for (NullFun<? super T, ? extends U> nullFun : mapper) {
                    U apply = nullFun.apply((T)value);
                    //跳过空值
                    if (Null.non(apply)) {
                        String field = LambdaUtil.getFieldName(nullFun);
                        //添加set方法
                        String firstLetter = field.substring(0, 1).toUpperCase();    //将属性的首字母转换为大写
                        String setMethodName = "set" + firstLetter + field.substring(1);
                        //获取方法对象,将值设置进去
                        object.getClass().getMethod(setMethodName, apply.getClass()).invoke(object, apply);
                    }
                }
                linkLog.append("pick->");
                return NullBuild.noEmpty(object);
            } catch (Exception e) {
                linkLog.append("pick? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyCopy(this);
    }
}
