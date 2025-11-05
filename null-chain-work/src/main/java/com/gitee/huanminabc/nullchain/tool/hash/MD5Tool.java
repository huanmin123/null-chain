package com.gitee.huanminabc.nullchain.tool.hash;

import com.gitee.huanminabc.jcommon.encryption.HashUtil;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;

import java.util.Map;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class MD5Tool implements NullTool<String, String> {

    @Override
    public String run(String preValue, NullChain<?>[] params, Map<String, Object> context) throws Exception {
        return HashUtil.md5(preValue);
    }
}
