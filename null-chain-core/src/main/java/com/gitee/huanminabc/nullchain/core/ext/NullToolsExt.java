package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullTools;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.tool.NullTool;

public interface NullToolsExt<T> extends NullTools<T>,  NullFinalityExt<T>{

    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool){
         NullChain<T> tNullChain = toNULL();
         return tNullChain.tool(tool);
   }

}
