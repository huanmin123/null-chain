package com.gitee.huanminabc.nullchain.base.sync;

import com.alibaba.fastjson2.JSON;
import com.gitee.huanminabc.common.base.BeanCopyUtil;
import com.gitee.huanminabc.common.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.http.sync.OkHttp;
import com.gitee.huanminabc.nullchain.http.sync.OkHttpChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

public class NullToolsBase<T> extends NullFinalityBase<T> implements NullTools<T> {

    public NullToolsBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullToolsBase(T object, StringBuilder linkLog, NullCollect collect) {
        super(object, linkLog, collect);
    }

    //将时间类型(Date,LocalDate,LocalDateTime), 10或13位时间戳, 转换为指定格式的时间字符串
    @Override
    public NullChain<String> dateFormat(DateFormatEnum dateFormatEnum) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }

        String string;
        try {
            string = NullDateFormat.toString(value, dateFormatEnum);
        } catch (Exception e) {
            linkLog.append("dateFormat? ").append(value).append(" to ").append(dateFormatEnum.getValue()).append(" 失败:").append(e.getMessage());
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (string == null) {
            linkLog.append("dateFormat? 转换时间格式失败数据格式不正确");
            throw new NullChainException(linkLog.toString());
        }
        linkLog.append("dateFormat->");
        return NullBuild.noEmpty(string, linkLog, collect);
    }

    @Override
    public NullChain<T> dateOffset(DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        T t;
        try {
            t = NullDateFormat.dateOffset(value, offsetEnum, num, timeEnum);
        } catch (Exception e) {
            linkLog.append("dateOffset? ").append(value).append(" 偏移时间失败:").append(e.getMessage());
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (t == null) {
            linkLog.append("dateOffset? 偏移时间失败数据格式不正确");
            throw new NullChainException(linkLog.toString());
        }
        linkLog.append("dateOffset->");
        return NullBuild.noEmpty(t, linkLog, collect);
    }

    @Override
    public NullChain<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum) {
        return dateOffset(controlEnum, 0, timeEnum);
    }

    @Override
    public NullChain<Integer> dateCompare(Object date) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }

        Integer compare;
        try {
            compare = NullDateFormat.dateCompare(value, date);
        } catch (Exception e) {
            linkLog.append("dateCompare? ").append(value).append(" 比较时间失败:").append(e.getMessage());
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
        if (compare == null) {
            linkLog.append("dateCompare? 比较时间失败数据格式不正确");
            throw new NullChainException(linkLog.toString());
        }
        linkLog.append("dateCompare->");
        return NullBuild.noEmpty(compare, linkLog, collect);
    }


    @Override
    public NullChain<String> json() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        //如果是字符串直接返回
        if (value instanceof String) {
            linkLog.append("json->");
            return NullBuild.noEmpty(value.toString(), linkLog, collect);
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
                return NullBuild.empty(linkLog, collect);
            }
            linkLog.append("json->");
            return NullBuild.noEmpty(json, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("json? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @Override
    public <U> NullChain<U> json(Class<U> uClass) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
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
            return NullBuild.noEmpty(u, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("json? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @Override
    public <U> NullChain<U> json(U uClass) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        return json((Class<U>) uClass.getClass());
    }

    @Override
    public NullChain<T> copy() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        try {
            linkLog.append("copy->");
            return NullBuild.noEmpty(BeanCopyUtil.copy(value), linkLog, collect);
        } catch (Exception e) {
            linkLog.append("copy? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @Override
    public NullChain<T> deepCopy() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        try {
            linkLog.append("deepCopy->");
            return NullBuild.noEmpty(BeanCopyUtil.deepCopy(value), linkLog, collect);
        } catch (Exception e) {
            linkLog.append("deepCopy? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @SafeVarargs
    @Override
    public final <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (Null.is(mapper)) {
            throw new NullChainException(linkLog.append("pick? 传参不能为空").toString());
        }
        try {
            T object = (T) value.getClass().newInstance();
            for (NullFun<? super T, ? extends U> nullFun : mapper) {
                U apply = nullFun.apply(value);
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
            return NullBuild.noEmpty(object, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("pick? ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }

    @Override
    public OkHttpChain http(String url) {
        if (isNull) {
            return OkHttp.empty(linkLog);
        }
        OkHttp<T> http = new OkHttp<>();
        http.setUrl(url);
        http.setValue(value);
        http.setLinkLog(linkLog);
        http.setNull(isNull);
        http.setCollect(collect);
        return http;
    }


    @Override
    public OkHttpChain http(String httpName, String url) {
        if (isNull) {
            return OkHttp.empty(linkLog);
        }
        OkHttp<T> http = new OkHttp<>(httpName);
        http.setUrl(url);
        http.setValue(value);
        http.setLinkLog(linkLog);
        http.setNull(isNull);
        http.setCollect(collect);
        return http;
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        return tool(tool, new Object[]{});
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }

        NullTool<T, R> tool1 = NullToolFactory.getTool(tool);
        //如果不存在注册器
        if (tool1 == null) {
            throw new NullChainException(linkLog.append("tool? ").append(tool.getName()).append(" 不存在的转换器").toString());
        }
        try {
            R run = NullBuild.toolRun(value, tool1, linkLog, params);
            if (Null.is(run)) {
                linkLog.append("tool? ");
                return NullBuild.empty(linkLog, collect);
            }
            linkLog.append("tool->");
            return NullBuild.noEmpty(run, linkLog, collect);
        } catch (NullChainCheckException e) {
            throw new NullChainException(e);
        } catch (Exception e) {
            linkLog.append("tool? ").append(tool1.getClass().getName()).append(" 失败: ");
            throw NullReflectionKit.addRunErrorMessage(e, linkLog);
        }
    }


}
