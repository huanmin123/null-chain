package com.gitee.huanminabc.nullchain.tool.file;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * 读取文件内容到字符串
 *  @author huanmin
 * @date 2024/11/22
 */
public class ReadFileToStrTool implements NullTool<String,String> {

    @Override
    public void init(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        File file = new File(preValue);
        if (!file.exists()){
            throw new NullChainException(preValue + "文件不存在");
        }
        context.put("file", file);
    }

    @Override
    public String run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        File file = context.get("file").type(File.class).get();
        //读取文件内容
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())));) {
            String lin;
            while ((lin = br.readLine()) != null) {
                // 每次处理一行
                sb.append(lin).append("\n");
            }
        }
        return sb.toString();
    }
}
