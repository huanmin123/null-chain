package com.gitee.huanminabc.test.nullchain.utils;    //读取文件


import com.gitee.huanminabc.jcommon.test.PathUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public  class TestUtil {
    public static String readFile(String fileName) {
        String file = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/" + fileName);
        try {
            // 使用 UTF-8 编码显式读取文件
            return new String(Files.readAllBytes(new File(file).toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }
}

