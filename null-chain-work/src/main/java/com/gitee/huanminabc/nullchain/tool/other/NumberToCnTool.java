package com.gitee.huanminabc.nullchain.tool.other;

import com.gitee.huanminabc.jcommon.base.NumberToCn;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import java.util.Map;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NumberToCnTool implements NullTool<Integer,String> {

    @Override
    public String run(Integer preValue, NullChain<?>[] params, Map<String, Object> context) throws Exception {
        return NumberToCn.toChineseLower(preValue);
    }
}
