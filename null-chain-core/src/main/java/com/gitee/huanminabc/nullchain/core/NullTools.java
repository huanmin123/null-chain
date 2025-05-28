package com.gitee.huanminabc.nullchain.core;

import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.tool.NullTool;


/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 **/
public interface NullTools<T> extends NullFinality<T> {




    /**
     * 自定义工具
     * @param tool
     * @param <R>
     * @return
     * @
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool);
    /**
     * @param params 工具的参数
     * @return
     * @param <R>
     */
    <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool, Object... params);


}
