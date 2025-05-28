package com.gitee.huanminabc.nullchain.core;

import com.alibaba.fastjson2.JSON;
import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.common.reflect.LambdaUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.common.NullReflectionKit;

public class NullToolsBase<T> extends NullFinalityBase<T> implements NullTools<T> {

    public NullToolsBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect,taskList);
    }

    public NullToolsBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect,taskList);
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool) {
        return tool(tool, new Object[]{});
    }

    @Override
    public <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }

            NullTool<T, R> tool1 = NullToolFactory.getTool(tool);
            //如果不存在注册器
            if (tool1 == null) {
                throw new NullChainException(linkLog.append("tool? ").append(tool.getName()).append(" 不存在的转换器").toString());
            }
            try {
                R run = NullBuild.toolRun((T)value, tool1, linkLog, params);
                if (Null.is(run)) {
                    linkLog.append("tool? ");
                    return NullBuild.empty(linkLog, collect, taskList);
                }
                linkLog.append("tool->");
                return NullBuild.noEmpty(run, linkLog, collect, taskList);
            } catch (NullChainCheckException e) {
                throw new NullChainException(e);
            } catch (Exception e) {
                linkLog.append("tool? ").append(tool1.getClass().getName()).append(" 失败: ");
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
        });
        return  NullBuild.busy(this);
    }
}
