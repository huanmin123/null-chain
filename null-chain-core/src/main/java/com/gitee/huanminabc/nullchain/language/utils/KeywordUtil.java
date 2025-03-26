package com.gitee.huanminabc.nullchain.language.utils;

import java.util.Arrays;
import java.util.List;

public class KeywordUtil {
    //定义占用的关键字集合 , 不能定义为变量名
   private final static List<String> forbidKeywords = Arrays.asList("threadFactoryName","this","params","preValue");

    //判断是否是禁止的关键字
    public static boolean isForbidKeyword(String name){
        return forbidKeywords.contains(name);
    }

}
