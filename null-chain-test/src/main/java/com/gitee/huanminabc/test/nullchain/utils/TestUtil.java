package com.gitee.huanminabc.test.nullchain.utils;    //读取文件

import com.gitee.huanminabc.utils_common.file.PathUtil;
import com.gitee.huanminabc.utils_common.file.ReadFileLineUtil;

import java.io.File;

public  class TestUtil {
    public static String readFile(String fileName) {
        String file = PathUtil.getCurrentProjectTestResourcesAbsoluteFile("nf/" + fileName);
        return  ReadFileLineUtil.readFileStrAll(new File(file));
    }
}

