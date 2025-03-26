package com.gitee.huanminabc.nullchain.tool.hash;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.utils.HashUtil;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class MD5Tool implements NullTool<String, String> {

    @Override
    public String run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return HashUtil.md5(preValue);
    }
}
