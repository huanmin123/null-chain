package com.gitee.huanminabc.test.nullchain.utils;    //读取文件


import com.gitee.huanminabc.jcommon.file.FileReadUtil;
import com.gitee.huanminabc.jcommon.test.PathUtil;

import java.io.File;

public  class TestUtil {
    public static String readFile(String fileName) {
        String file = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/" + fileName);
        return  FileReadUtil.readAllStr(new File(file));
    }
}

