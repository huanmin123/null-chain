package com.gitee.huanminabc.nullchain.tool.object;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * 简要描述
 *
 * @Author: huanmin
 * @Date: 2025/3/24 22:20
 * @Version: 1.0
 * @Description: 文件作用详细描述....
 */
public class DeserializeTool implements NullTool<byte[],Object> {
    @Override
    public Object run(byte[] preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        // 反序列化
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(preValue);
        ObjectInputStream ois = new ObjectInputStream(arrayInputStream);
        return  ois.readObject();
    }
}
