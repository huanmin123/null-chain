package com.gitee.huanminabc.nullchain.tool.file;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @description: 压缩文件到zip中, 返回zip字节数组
 * @author: huanmin
 * @create: 2024-12-30 13:06
 **/
public class ZipToBytesByteTool implements NullTool<Map<String, byte[]>,byte[]> {

    @Override
    public byte[] run(Map<String, byte[]> preValue, NullChain<?>[] params, NullMap<String, Object> context) throws Exception {
        //按照文件的长度进行排序, 因为目录的需要先添加
        Set<String> fileNames = preValue.keySet();
        List<String> fileNamesSort = fileNames.stream().sorted(Comparator.comparingInt(String::length)).collect(Collectors.toList());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos);
        ) {
            for (String fileName : fileNamesSort) {
                if (Null.is(fileName)) {
                    continue;
                }
                byte[] bytes = preValue.get(fileName);
                //如果bytes是空,那么判断fileName结尾必须是/否则就添加
                if (bytes == null && !fileName.endsWith("/")) {
                    fileName = fileName + "/";
                }
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                //如果没有内容那么就是目录不需要添加字节
                if (bytes != null) {
                    zos.write(preValue.get(fileName));
                }
                zos.closeEntry();
            }
            zos.flush();
            zos.finish();
            //返回字节
            return bos.toByteArray();
        }
    }
}
