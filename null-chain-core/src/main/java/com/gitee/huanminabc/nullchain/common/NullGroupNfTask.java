package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.common.encryption.HashUtil;
import com.gitee.huanminabc.nullchain.Null;
import lombok.Data;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @program: java-huanmin-utils
 * @description: 组合任务参数
 * @author: huanmin
 * @create: 2025-02-10 10:25
 **/
@Data
public class NullGroupNfTask {
    NullTaskInfo[] list;

    public NullGroupNfTask(NullTaskInfo... list) {
        this.list = list;
    }

    @Data
    public static class NullTaskInfo {
        Object[] params;
        String filePath;
        String nfContext;
        String taskClassName;
        Logger logger;
        String key; //唯一标识

        //获取参数
        public Object[] getParams() {
            return params == null ? new Object[]{} : params;
        }
    }

    public static NullGroupNfTask buildGroup(NullTaskInfo... taskInfos) {
        if (taskInfos == null || taskInfos.length == 0) {
            throw new NullChainException("NullGroupNfTask::buildGroup-> 任务为空");
        }
        //校验是否有同类型的任务 ,校验key
        for (int i = 0; i < taskInfos.length; i++) {
            for (int j = i + 1; j < taskInfos.length; j++) {
                if (taskInfos[i].getKey().equals(taskInfos[j].getKey())) {
                    throw new NullChainException("NullGroupNfTask::buildGroup-> 任务重复{}", taskInfos[j].getKey());
                }
            }
        }
        return new NullGroupNfTask(taskInfos);
    }

    public static NullTaskInfo taskFile(String filePath, Object... params) {
        return taskFile(filePath, null, params);
    }

    public static NullTaskInfo taskFile(String filePath, Logger logger, Object... params) {
        NullTaskInfo info = new NullTaskInfo();
        info.setParams(params);
        info.setLogger(logger);
        String text = readFileStrAll(new File(filePath));
        if (Null.is(text)) {
            throw new NullChainException("NullGroupNfTask::taskFile-> 文件内容为空{}", filePath);
        }
        info.setNfContext(text);
        info.setKey(HashUtil.md5(text));
        info.setFilePath(filePath);
        return info;
    }

    public static NullTaskInfo task(String nfContext, Object... params) {
        return task(nfContext, null, params);
    }

    public static NullTaskInfo task(String nfContext, Logger logger, Object... params) {
        if (Null.is(nfContext)) {
            throw new NullChainException("NullGroupNfTask::task-> nf脚本内容为空");
        }
        NullTaskInfo info = new NullTaskInfo();
        info.setNfContext(nfContext);
        info.setParams(params);
        info.setLogger(logger);
        info.setKey(HashUtil.md5(nfContext));
        return info;
    }


    //获取唯一标识, 用于在map里面拿到任务返回的值
    public  String getKey(String nfContextOrFilePath) {
        String key = HashUtil.md5(nfContextOrFilePath);
        String s = Arrays.stream(list).map(NullTaskInfo::getKey).filter(k -> k.equals(key)).findFirst().orElse(null);
        //如果拿内容没有找到, 那么就拿文件路径找
        if (Null.is(s)) {
            s = Arrays.stream(list).map(NullTaskInfo::getFilePath).filter(k -> k.equals(nfContextOrFilePath)).findFirst().orElse(null);
        }
        if (Null.is(s)) {
            throw new NullChainException("NullGroupNfTask::getKey-> 通过{}没有找到对应任务标识",key);
        }
        return s;
    }


    private static String readFileStrAll(File file) {
        //判断文件存在
        if (!file.exists()) {
            throw new NullChainException("NullGroupNfTask::readFileStrAll-> 文件不存在{}", file.getAbsolutePath());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())));) {
            String lin;
            while ((lin = br.readLine()) != null) {
                // 每次处理一行
                sb.append(lin).append("\n");
            }
        } catch (Exception e) {
            throw new NullChainException(e);
        }
        return sb.toString();

    }

}
