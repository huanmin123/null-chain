package com.gitee.huanminabc.nullchain.leaf.json;

import com.alibaba.fastjson2.JSON;
import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-28 09:48
 **/
@Slf4j
public class NullJsonBase<T> extends NullChainBase<T> implements   NullJson<T> {
    public NullJsonBase(StringBuilder linkLog,  NullTaskList taskList) {
        super(linkLog, taskList);
    }



    @Override
    public NullJson<String> json() {
        this.taskList.add((value)->{
            //如果是字符串直接返回
            if (value instanceof String) {
                linkLog.append("json->");
                return NullBuild.noEmpty(value.toString());
            }
            try {
                String json = JSON.toJSONString(value);
                //[]{}[{}] 都是空的
                if (json.equals("[]") ||
                        json.equals("{}") ||
                        json.equals("[{}]") ||
                        json.equals("{\"empty\":false}")// 这个是因为继承了NULLCheck里面的isEmpty方法导致的
                ) {
                    linkLog.append("json? ");
                    return NullBuild.empty();
                }
                linkLog.append("json->");
                return NullBuild.noEmpty(json);
            } catch (Exception e) {
                linkLog.append("json? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyJson(this);
    }

    @Override
    public <U> NullJson<U> json(Class<U> uClass) {
        this.taskList.add((value)->{
            if (uClass == null) {
                throw new NullChainException(linkLog.append("json? ").append(uClass).append(" 不是字符串").toString());
            }
            //判断value是否是字符串
            if (!(value instanceof String)) {
                linkLog.append("json? ");
                throw new NullChainException(linkLog.append("json? ").append(value).append(" 不是字符串").toString());
            }
            try {
                U u = JSON.parseObject(value.toString(), uClass);
                linkLog.append("json->");
                return NullBuild.noEmpty(u);
            } catch (Exception e) {
                linkLog.append("json? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busyJson(this);
    }

    @Override
    public <U> NullJson<U> json(U uClass) {
        return json((Class<U>) uClass.getClass());
    }


}
