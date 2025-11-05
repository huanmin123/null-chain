package com.gitee.huanminabc.nullchain.tool.base64;


import com.gitee.huanminabc.jcommon.encryption.Base64Util;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import java.util.Map;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class Base64StrDecodeTool implements NullTool<String, String> {
    @Override
    public String run(String preValue, NullChain<?>[] params, Map<String, Object> context) throws Exception {
        return Base64Util.decode(preValue);
    }
}
