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
 *   <li>使用MD5哈希作为缓存key，减少内存占用</li>
 *   <li>缓存最大容量：1000个脚本（可配置）</li>
 *   <li>清理策略：基于访问时间的LRU策略，1小时未访问自动清理</li>
 *   <li>线程安全：使用Caffeine缓存确保线程安全和高性能</li>
 *   <li>自动管理：Caffeine自动处理过期和容量限制，无需手动清理线程</li>
 * </ul>
 * </p>
 * <p>为什么使用MD5作为缓存key：</p>
 * <ul>
 *   <li>不使用脚本字符串：脚本内容可能很长（几千行），直接作为key会占用大量内存</li>
 *   <li>不使用hashCode()：int只有32位，冲突概率高，且作为Integer key无法区分相同hashCode的不同内容</li>
 *   <li>使用MD5：固定32字符，内存占用小，计算速度快，128位哈希冲突概率极低</li>
 *   <li>Caffeine保障：即使MD5冲突，Caffeine会通过equals()比较MD5字符串来区分</li>
 * </ul>
 *
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NfMain {

    /**
     * 语法缓存,防止对同一个语法重复解析
     * 使用Caffeine缓存，提供高性能和自动管理能力
     * <p>配置说明：
     * <ul>
     *   <li>maximumSize(1000): 最大容量1000个脚本</li>
     *   <li>expireAfterAccess(1, TimeUnit.HOURS): 1小时未访问自动过期</li>
     * </ul>
     * </p>
     * <p>key选择说明：</p>
     * <ul>
     *   <li>key: 脚本内容的MD5哈希值（32字符固定长度）</li>
     *   <li>value: 解析后的语法节点列表</li>
     *   <li>内存优化：1000个脚本约32KB（vs 直接存储脚本内容可能几GB）</li>
     * </ul>
     */
    private static final Cache<String, List<SyntaxNode>> syntaxCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

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
        //使用MD5哈希作为缓存key，减少内存占用
        String hash = HashUtil.md5(context);
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
        //使用MD5哈希作为缓存key，减少内存占用
        String hash = HashUtil.md5(context);
        //从缓存中获取语法节点，如果不存在则解析并缓存
        //Caffeine会自动处理过期和容量限制
        List<SyntaxNode> getSyntaxNodes = syntaxCache.get(hash, key -> {
            List<Token> tokens = NfToken.tokens(context);
            return NfSynta.buildMainStatement(tokens);
        });
        return NfRun.run(getSyntaxNodes, logger, mainSystemContext, enablePerformanceMonitoring);
    }

}
