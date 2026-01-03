package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.jcommon.encryption.HashUtil;
import com.gitee.huanminabc.jcommon.file.FileReadUtil;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * NF脚本执行入口
 * 提供脚本执行的统一入口，支持脚本缓存机制，避免重复解析
 * 
 * <p>缓存机制说明：
 * <ul>
 *   <li>使用SHA-256哈希作为缓存key，避免MD5碰撞风险</li>
 *   <li>缓存最大容量：1000个脚本（可配置）</li>
 *   <li>清理策略：基于访问时间的LRU策略，1小时未访问自动清理</li>
 *   <li>线程安全：使用Caffeine缓存确保线程安全和高性能</li>
 *   <li>自动管理：Caffeine自动处理过期和容量限制，无需手动清理线程</li>
 * </ul>
 * </p>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NfMain {

    /**
     * 语法缓存,防止对同一个语法重复解析
     * 使用Caffeine缓存，提供高性能和自动管理能力
     * 
     * <p>配置说明：
     * <ul>
     *   <li>maximumSize(1000): 最大容量1000个脚本</li>
     *   <li>expireAfterAccess(1, TimeUnit.HOURS): 1小时未访问自动过期</li>
     *   <li>recordStats(): 记录缓存统计信息，便于监控</li>
     * </ul>
     * </p>
     * 
     * key: 内容的SHA-256哈希, value: 语法节点
     */
    private static final Cache<String, List<SyntaxNode>> syntaxCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .recordStats()
        .build();

    /**
     * 关闭缓存（可选的优雅关闭方法）
     * 通常在应用关闭时调用，确保资源正确释放
     * 
     * <p>注意：Caffeine缓存会自动管理，通常不需要手动关闭。
     * 但如果需要强制清理缓存，可以调用此方法。</p>
     */
    public static void shutdown() {
        syntaxCache.invalidateAll();
        log.info("NF脚本缓存已清理");
    }
    
    /**
     * 获取缓存统计信息
     * 用于监控缓存命中率、大小等信息
     * 
     * @return 缓存统计信息字符串
     */
    public static String getCacheStats() {
        CacheStats stats = syntaxCache.stats();
        return String.format(
            "NF脚本缓存统计: 命中率=%.2f%%, 命中=%d, 未命中=%d, 大小=%d, 驱逐=%d",
            stats.hitRate() * 100,
            stats.hitCount(),
            stats.missCount(),
            syntaxCache.estimatedSize(),
            stats.evictionCount()
        );
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
        //使用SHA-256替代MD5，避免哈希碰撞风险
        String hash = HashUtil.sha256(context);
        //从缓存中获取语法节点，如果不存在则解析并缓存
        //Caffeine会自动处理过期和容量限制
        List<SyntaxNode> getSyntaxNodes = syntaxCache.get(hash, key -> {
            List<Token> tokens = NfToken.tokens(context);
            return NfSynta.buildMainStatement(tokens);
        });
        return NfRun.run(getSyntaxNodes, logger, mainSystemContext);
    }

    /**
     * 运行脚本内容（支持性能监控）
     * 
     * @param context 脚本内容
     * @param logger 日志
     * @param mainSystemContext 主系统上下文
     * @param enablePerformanceMonitoring 是否启用性能监控
     * @return 脚本执行结果
     */
    public static Object run(String context, Logger logger, Map<String,Object> mainSystemContext, boolean enablePerformanceMonitoring) {
        //使用SHA-256替代MD5，避免哈希碰撞风险
        String hash = HashUtil.sha256(context);
        //从缓存中获取语法节点，如果不存在则解析并缓存
        //Caffeine会自动处理过期和容量限制
        List<SyntaxNode> getSyntaxNodes = syntaxCache.get(hash, key -> {
            List<Token> tokens = NfToken.tokens(context);
            return NfSynta.buildMainStatement(tokens);
        });
        return NfRun.run(getSyntaxNodes, logger, mainSystemContext, enablePerformanceMonitoring);
    }

}
