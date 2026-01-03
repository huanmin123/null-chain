package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NF脚本注册器
 *
 * <p>提供静态方法注册和管理 nf 脚本，支持脚本的注册、查询和移除。
 * 注册时会将脚本内容解析为语法节点列表并缓存，避免重复解析。</p>
 *
 * @author huanmin
 * @date 2024/12/XX
 */
@Slf4j
public class NfScriptRegistry {

    /**
     * 脚本注册表
     * key: 脚本名称
     * value: 脚本的语法节点列表（已解析）
     */
    private static final Map<String, List<SyntaxNode>> scriptMap = new HashMap<>();

    /**
     * 注册 nf 脚本
     *
     * <p>注册时会立即解析脚本内容为语法节点列表并缓存。
     * 如果脚本名称已存在，会覆盖原有脚本。</p>
     *
     * @param name          脚本名称（用于导入时使用）
     * @param scriptContent 脚本内容
     * @throws NfException 如果脚本内容解析失败
     */
    public static void registerScript(String name, String scriptContent) {
        if (name == null || name.trim().isEmpty()) {
            throw new NfException("脚本名称不能为空");
        }
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            throw new NfException("脚本内容不能为空");
        }
        // 检查脚本名称是否已存在
        if (scriptMap.containsKey(name)) {
            throw new NfException(name + " 脚本名称已存在，请更换其他名称");
        }

        try {
            // 解析脚本为语法节点列表
            List<Token> tokens = NfToken.tokens(scriptContent);
            List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

            // 存储到注册表
            scriptMap.put(name, syntaxNodes);
            log.debug("注册NF脚本成功: {}, 语法节点数量: {}", name, syntaxNodes.size());
        } catch (Exception e) {
            throw new NfException(e, "注册NF脚本失败: {}, 错误: {}", name, e.getMessage());
        }
    }

    /**
     * 获取脚本的语法节点列表
     *
     * @param name 脚本名称
     * @return 语法节点列表，如果脚本不存在返回 null
     */
    public static List<SyntaxNode> getScriptSyntaxNodes(String name) {
        return scriptMap.get(name);
    }

    /**
     * 检查脚本是否存在
     *
     * @param name 脚本名称
     * @return 如果脚本存在返回 true，否则返回 false
     */
    public static boolean hasScript(String name) {
        return scriptMap.containsKey(name);
    }

    /**
     * 移除脚本
     *
     * @param name 脚本名称
     * @return 如果脚本存在并成功移除返回 true，否则返回 false
     */
    public static boolean removeScript(String name) {
        List<SyntaxNode> removed = scriptMap.remove(name);
        if (removed != null) {
            log.debug("移除NF脚本: {}", name);
            return true;
        }
        return false;
    }

    /**
     * 清空所有注册的脚本
     */
    public static void clear() {
        scriptMap.clear();
        log.debug("清空所有NF脚本");
    }

    /**
     * 获取已注册的脚本数量
     *
     * @return 已注册的脚本数量
     */
    public static int size() {
        return scriptMap.size();
    }
}

