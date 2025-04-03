package com.gitee.huanminabc.nullchain.base.async;

import com.alibaba.fastjson.JSON;
import com.gitee.huanminabc.common.base.BeanCopyUtil;
import com.gitee.huanminabc.common.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.http.async.OkHttpAsync;
import com.gitee.huanminabc.nullchain.http.async.OkHttpAsyncChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.util.concurrent.CompletableFuture;

public class NullToolsAsyncBase<T> extends NullFinalityAsyncBase<T> implements NullToolsAsync<T> {


    public NullToolsAsyncBase(StringBuilder linkLog, boolean isNull, NullCollect collect) {
        super(linkLog, isNull, collect);
    }

    public NullToolsAsyncBase(CompletableFuture<T> completableFuture, StringBuilder linkLog, String threadFactoryName, NullCollect collect) {
        super(completableFuture, linkLog, threadFactoryName, collect);
    }

    @Override
    public NullChainAsync<String> dateFormat(DateFormatEnum dateFormatEnum) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<String> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (dateFormatEnum==null){
                throw new NullChainException(linkLog.append("dateFormat? 传参数不能是空").toString());
            }

            String string;
            try {
                string = NullDateFormat.toString(value, dateFormatEnum);
            } catch (Exception e) {
                linkLog.append("dateFormat? ").append(value).append(" to ").append(dateFormatEnum.getValue()).append(" 失败:");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (string == null) {
                linkLog.append("dateFormat? 转换时间格式失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("dateFormat->");
            return string;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> dateOffset(DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (offsetEnum==null||timeEnum==null){
                throw new NullChainException(linkLog.append("dateOffset? 传参数不能是空").toString());
            }
            if (num<0) {
                throw new NullChainException(linkLog.append("dateOffset? num不能小于0").toString());
            }
            T string;
            try {
                string = NullDateFormat.dateOffset(value, offsetEnum, num, timeEnum);
            } catch (Exception e) {
                linkLog.append("dateOffset? ").append(value).append(" 偏移时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (string == null) {
                linkLog.append("dateOffset? 偏移时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("dateOffset->");
            return string;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum) {
        return dateOffset(controlEnum, 0, timeEnum);
    }

    @Override
    public NullChainAsync<Integer> dateCompare(Object date) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<Integer> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.is(date)) {
                throw new NullChainException(linkLog.append("dateCompare? 传参数不能是空").toString());
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
            return compare;
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @Override
    public NullChainAsync<String> json() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<String> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            //如果是字符串直接返回
            if (value instanceof String) {
                linkLog.append("json->");
                return value.toString();
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
                    return null;
                }
                linkLog.append("json->");
                return json;
            } catch (Exception e) {
                linkLog.append("json? 失败:");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public <U> NullChainAsync<U> json(Class<U> uClass) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<U> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            //判断value是否是字符串
            if (!(value instanceof String)) {
                linkLog.append("json? ");
                throw new NullChainException(linkLog.append("json? ").append(value).append(" 不是字符串").toString());
            }
            try {
                U u = JSON.parseObject(value.toString(), uClass);
                linkLog.append("json->");
                return u;
            } catch (Exception e) {
                linkLog.append("json? 失败:");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public <U> NullChainAsync<U> json(U uClass) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        if (uClass==null){
            completableFuture.completeExceptionally(new NullChainException( linkLog.append("json? 传参数不能是空").toString()));
            return (NullChainAsync) NullBuild.noEmptyAsync(completableFuture, linkLog, super.currentThreadFactoryName, collect);
        }
        return json((Class<U>) uClass.getClass());
    }

    @Override
    public NullChainAsync<T> copy() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            try {
                linkLog.append("copy->");
                return BeanCopyUtil.copy(t);
            } catch (Exception e) {
                linkLog.append("copy? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public NullChainAsync<T> deepCopy() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((t) -> {
            if (Null.is(t)) {
                return null;
            }
            try {
                linkLog.append("deepCopy->");
                return BeanCopyUtil.deepCopy(t);
            } catch (Exception e) {
                linkLog.append("deepCopy? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }

        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }


    @SafeVarargs
    @Override
    public final <U> NullChainAsync<T> pick(NullFun<? super T, ? extends U>... mapper) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<T> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (Null.is(mapper)) {
                throw new NullChainException(linkLog.append("pick? ").append("传参不能为空").toString());
            }
            try {
                T object = (T) value.getClass().newInstance();
                for (NullFun<? super T, ? extends U> nullFun : mapper) {
                    U apply = nullFun.apply(value);
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
                return object;
            } catch (Exception e) {
                linkLog.append("pick? ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

    @Override
    public OkHttpAsyncChain http(String url) {
        if (isNull) {
            return OkHttpAsync.empty(linkLog, collect);
        }
        OkHttpAsync http =  new OkHttpAsync();
        http.setUrl(url);
        http.setLinkLog(linkLog);
        http.setCollect(collect);
        http.setCurrentThreadFactoryName(currentThreadFactoryName);
        http.setNull(isNull);
        http.setCompletableFuture((CompletableFuture<Object>) completableFuture);
        return http;
    }


    @Override
    public OkHttpAsyncChain http(String httpName, String url) {
        if (isNull) {
            return OkHttpAsync.empty(linkLog, collect);
        }
        OkHttpAsync http =  new OkHttpAsync(httpName);
        http.setUrl(url);
        http.setLinkLog(linkLog);
        http.setCollect(collect);
        http.setCurrentThreadFactoryName(currentThreadFactoryName);
        http.setNull(isNull);
        http.setCompletableFuture((CompletableFuture<Object>) completableFuture);
        return http;
    }


    @Override
    public <R> NullChainAsync<R> tool(Class<? extends NullTool<T, R>> tool) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        return tool(tool, NullMap.newHashMap());
    }

    @Override
    public <R> NullChainAsync<R> tool(Class<? extends NullTool<T, R>> tool, Object... params) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog, collect);
        }
        CompletableFuture<R> uCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            if (tool==null) {
                throw new NullChainException(linkLog.append("tool? 传参数不能是空").toString());
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
                    return null;
                }
                linkLog.append("tool->");
                return run;
            } catch (NullChainCheckException e) {
                throw new NullChainException(e);
            } catch (Exception e) {
                linkLog.append("tool? ").append(tool1.getClass().getName()).append("失败: ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        }, getCT());
        return NullBuild.noEmptyAsync(uCompletableFuture, linkLog, super.currentThreadFactoryName, collect);
    }

}
