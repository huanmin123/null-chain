package com.gitee.huanminabc.nullchain.tool.base64;


import com.gitee.huanminabc.common.encryption.Base64Util;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class Base64StrEncodeTool implements NullTool<String, String> {

    @Override
    public String run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return Base64Util.encode(preValue);
    }
}
