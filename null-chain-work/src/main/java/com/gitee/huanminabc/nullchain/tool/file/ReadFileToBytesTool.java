package com.gitee.huanminabc.nullchain.tool.file;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @program: java-huanmin-utils
 * @description: 读取文件内容到字节数组
 * @author: huanmin
 * @create: 2025-02-13 11:06
 **/
public class ReadFileToBytesTool implements NullTool<String,byte[]> {
    @Override
    public void init(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        File file = new File(preValue);
        if (!file.exists()){
            throw new NullChainException(preValue + "文件不存在");
        }
        context.put("file", file);
    }

    @Override
    public byte[] run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        File file = context.get("file").type(File.class).get();
        byte[] data = new byte[(int) file.length()];
        try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(file.toPath()));) {
            int read = fis.read(data);
            if (read != data.length) {
                throw new NullChainException("读取文件失败,读取的长度和文件大小不一致");
            }
        } catch (IOException e) {
            throw new NullChainException("读取文件失败", e);
        }
        return data;
    }
}
