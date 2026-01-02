package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.jcommon.encryption.HashUtil;
import com.gitee.huanminabc.jcommon.file.FileReadUtil;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NF脚本执行入口
 * 提供脚本执行的统一入口，支持脚本缓存机制，避免重复解析
 *
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
    private static final int TIME_CHECK_CLEAR_INTERVAL = 1000 * 60 * 60;//1小时

    //守护线程控制标志，用于优雅关闭
    private static volatile boolean cacheCleanerRunning = true;
    private static Thread cacheCleanerThread;

    static {
        //定时检查缓存,一个文件只有在一段时间内没有访问才会被清理 ,默认1小时
        cacheCleanerThread = new Thread(() -> {
            while (cacheCleanerRunning) {
                try {
                    Thread.sleep(TIME_CHECK_CLEAR_INTERVAL);//每小时检查一次
                    //检查是否被唤醒后需要退出
                    if (!cacheCleanerRunning) {
                        break;
                    }
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, Long> entry : timeCheckClearMap.entrySet()) {
                        if (now - entry.getValue() > TIME_CHECK_CLEAR_INTERVAL) {
                            syntaxCache.remove(entry.getKey());
                            timeCheckClearMap.remove(entry.getKey());
                        }
                    }
                } catch (InterruptedException e) {
                    //线程被中断，检查是否需要退出
                    if (!cacheCleanerRunning) {
                        log.info("缓存清理线程正常退出");
                        break;
                    }
                    log.warn("定时检查缓存线程被中断，将继续运行");
                    Thread.currentThread().interrupt();
                }
            }
            log.info("缓存清理线程已停止");
        }, "NF-CacheCleaner");
        cacheCleanerThread.setDaemon(true);
        cacheCleanerThread.start();
    }

    /**
     * 关闭缓存清理线程（可选的优雅关闭方法）
     * 通常在应用关闭时调用，确保资源正确释放
     */
    public static void shutdown() {
        cacheCleanerRunning = false;
        if (cacheCleanerThread != null && cacheCleanerThread.isAlive()) {
            cacheCleanerThread.interrupt();
            try {
                cacheCleanerThread.join(1000); //最多等待1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        //清理缓存
        syntaxCache.clear();
        timeCheckClearMap.clear();
    }

    /**
     * 运行脚本
     *
     * @param logger   日志
     * @param filePath 文件路径
     * @param mainSystemContext 主系统上下文
     * @return 脚本执行结果
     */
    public static Object runLocal(String filePath, Logger logger, Map<String,Object> mainSystemContext) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new NfException(filePath + "文件不存在");
        }
        String context = FileReadUtil.readAllStr(file);
        return run(context, logger,mainSystemContext);
    }

    /**
     * 运行脚本内容
     *
     * @param context 脚本内容
     * @param logger 日志
     * @param mainSystemContext 主系统上下文
     * @return 脚本执行结果
     */
    public static Object run(String context, Logger logger, Map<String,Object> mainSystemContext) {
        //对内容进行md5
        String md5 = HashUtil.md5(context);
        //从缓存中获取语法节点
        List<SyntaxNode> getSyntaxNodes = syntaxCache.computeIfAbsent(md5, (key) -> {
            List<Token> tokens = NfToken.tokens(context);
            return NfSynta.buildMainStatement(tokens);
        });
        //记录访问时间
        timeCheckClearMap.put(md5, System.currentTimeMillis());
        return NfRun.run(getSyntaxNodes, logger, mainSystemContext);
    }

}
