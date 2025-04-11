package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.common.encryption.HashUtil;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NfMain {

    //语法缓存,防止对同一个语法重复解析
    //key: 内容的md5, value: 语法节点
    private static final ConcurrentHashMap<String, List<SyntaxNode>> syntaxCache = new ConcurrentHashMap<>();
    //创建一个定时检查的Map, 用于定时清理缓存
    //key: 内容的md5, value: 上次访问时间
    private static final ConcurrentHashMap<String, Long> timeCheckClearMap = new ConcurrentHashMap<>();
    private static final int timeCheckClearMapSize = 1000 * 60 * 60;//1小时

    static {
        //定时检查缓存,一个文件只有在一段时间内没有访问才会被清理 ,默认1小时
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(timeCheckClearMapSize);//每小时检查一次
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, Long> entry : timeCheckClearMap.entrySet()) {
                        if (now - entry.getValue() > timeCheckClearMapSize) {
                            syntaxCache.remove(entry.getKey());
                            timeCheckClearMap.remove(entry.getKey());
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("定时检查缓存线程被中断");
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 运行脚本
     *
     * @param logger   日志
     * @param filePath 文件路径
     */
    public static Object runLocal(String filePath, Logger logger, NullMap<String,Object> mainSystemContext) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new NfException(filePath + "文件不存在");
        }
        String context = readFileStrAll(file);
        return run(context, logger,mainSystemContext);
    }

    /**
     * @param context 脚本内容
     * @param logger
     */
    public static Object run(String context, Logger logger, NullMap<String,Object> mainSystemContext) {
        //对内容进行md5
        String md5 = HashUtil.md5(context);
        //从缓存中获取语法节点
        List<SyntaxNode> getSyntaxNodes = syntaxCache.computeIfAbsent(md5, (key) -> {
            List<Token> tokens = NfToken.tokens(context);
            List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
            //记录访问时间
            timeCheckClearMap.put(md5, System.currentTimeMillis());
            return syntaxNodes;
        });
        return NfRun.run(getSyntaxNodes, logger, mainSystemContext);
    }



    //字符流 读取文件全部内容
    private static String readFileStrAll(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())));) {
            String lin = null;
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
