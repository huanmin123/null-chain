package com.gitee.huanminabc.nullchain.tool.file;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 *  将字符串写入文件
 * @author huanmin
 * @date 2024/11/22
 */
public class StrToWriteFileTool implements NullTool<String,Boolean> {

    @Override
    public NullType checkTypeParams() {
        //最小参数为1, 第一个参数为文件路径, 第二个参数为是否追加, 默认为false(覆盖)
        return NullType.params(1, NullType.of("path", String.class), NullType.of("type",false, Boolean.class));
    }

    @Override
    public Boolean run(String preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        String path = context.get("path").type(String.class).get();
        Boolean type =context.get("type").type(Boolean.class).get();
        File file = new File(path);
        if (!file.exists()) {
            // 获取文件的父目录
            File parentDir = file.getParentFile();
            // 如果父目录不存在，则创建它
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            boolean newFile = file.createNewFile();
            if (!newFile) {
                throw new NullChainException(path + "文件创建失败");
            }
        }
        //将字符串写入文件
        try (BufferedWriter bw =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, type)));) {
            bw.write(preValue);
        }
        return true;
    }
}
