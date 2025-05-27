package com.gitee.huanminabc.nullchain.tool.other;

import com.gitee.huanminabc.common.base.NumberToCn;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NumberToCnTool implements NullTool<Integer,String> {

    @Override
    public String run(Integer preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return NumberToCn.toChineseLower(preValue);
    }
}
