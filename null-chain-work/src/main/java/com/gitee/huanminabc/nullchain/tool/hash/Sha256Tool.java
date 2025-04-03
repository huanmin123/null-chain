package com.gitee.huanminabc.nullchain.tool.hash;

import com.gitee.huanminabc.common.base.HashUtil;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class Sha256Tool implements NullTool<String, String> {

    @Override
    public String run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return HashUtil.sha256(preValue);
    }
}
