package com.gitee.huanminabc.nullchain.tool.base64;


import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.utils.Base64Util;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class Base64ByteEncodeTool implements NullTool<byte[], String> {

    @Override
    public String run(byte[] preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        return Base64Util.encodeToString(preValue);
    }

}
