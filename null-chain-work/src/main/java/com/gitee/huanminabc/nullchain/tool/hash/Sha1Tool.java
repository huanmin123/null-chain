package com.gitee.huanminabc.nullchain.tool.hash;

import com.gitee.huanminabc.jcommon.encryption.HashUtil;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class Sha1Tool implements NullTool<String, String> {

    @Override
    public String run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return HashUtil.sha1(preValue);
    }
}
