package com.gitee.huanminabc.test.nullchain.nf;

import java.util.function.Consumer;

/**
 * 用于测试 NF 脚本的 Consumer 实现
 * NF 脚本不支持 Lambda 表达式，需要通过实现类的方式传入
 *
 * @author huanmin
 * @date 2025/01/05
 */
public class PrintConsumer implements Consumer<String> {

    @Override
    public void accept(String item) {
        System.out.println("PrintConsumer 接收到: " + item);
    }
}
