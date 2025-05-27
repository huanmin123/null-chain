package com.gitee.huanminabc.nullchain.tool.file;

import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 将字节数组写入文件
 * @author huanmin
 * @date 2024/11/22
 */
public class BytesToWriteFileTool implements NullTool<byte[], Boolean> {
    @Override
    public NullType checkTypeParams() {
        //最小参数为1, 第一个参数为文件路径, 第二个参数为是否追加, 默认为false(覆盖)
        return NullType.params(1, NullType.of("path", String.class), NullType.of("type",false, Boolean.class));
    }

    @Override
    public Boolean run(byte[] preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        String path = context.get("path").type(String.class).get();
        Boolean type = context.get("type").type(Boolean.class).get();
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
        writeByte(preValue, file, type);
        return true;
    }


    //将全部字节写入到文件中
    private static void writeByte(byte[] b, File writePath, boolean append) {
        try (
                FileOutputStream fos1 = new FileOutputStream(writePath, append);
                BufferedOutputStream fos = new BufferedOutputStream(fos1);) {
            fos.write(b, 0, b.length); // 写入数据
        } catch (IOException e) {
            throw new NullChainException(e);
        }
    }
}
