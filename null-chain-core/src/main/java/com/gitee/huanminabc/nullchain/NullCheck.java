package com.gitee.huanminabc.nullchain;

/**
 * @program: java-huanmin-utils
 * @description: 对自定义的类和容器等相关的提供可接入空链的校验
 * @author: huanmin
 * @create: 2024-12-27 09:32
 **/
public interface NullCheck {

    /**
     * 判断是否为空,由子类实现
     * @return
     */
    boolean isEmpty();
}
