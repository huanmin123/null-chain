package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.internal.ParseScopeTracker;
import com.gitee.huanminabc.nullchain.language.internal.TokenCursor;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器，将Token解析成语法节点
 *
 * @author huanmin
 * @date 2024/11/22
 */
public class NfSynta {
    
    /**
     * 错误上下文最大Token数量
     * 当语法错误发生时，最多显示此数量的Token作为错误上下文
     */
    private static final int MAX_ERROR_CONTEXT_TOKENS = 20;
    
    /**
     * 解析时作用域追踪器（使用ThreadLocal保证线程安全）
     * 用于在解析阶段追踪变量名和函数名，检测重复声明
     */
    private static final ThreadLocal<ParseScopeTracker> trackerThreadLocal = new ThreadLocal<>();
    
    public List<SyntaxNode> syntaxNodeList = new ArrayList<>();

    /**
     * 构建主语句, 也就是把所有的Token解析第一层语法节点,和文件代码顺序一致,但是一些块语句内部的节点还没有解析
     * 
     * <p>注意：此方法会直接修改传入的tokens列表，已解析的tokens会被移除。
     * 这是解析器的核心设计，目的是高效地逐步解析剩余的tokens。
     * 如果需要在解析后保留原始tokens，请在调用前创建副本。</p>
     * 
     * @param tokens Token列表（会被修改，已解析的部分会被移除）
     * @return 解析后的语法节点列表
     * @throws NfSyntaxException 如果遇到语法错误，立即抛出异常
     */
    public static List<SyntaxNode> buildMainStatement(List<Token> tokens) {
        return buildMainStatement(tokens, null);
    }
    
    /**
     * 构建主语句（支持传入tracker，用于嵌套调用）
     * 
     * <p>如果传入的tracker为null，则创建新的tracker；否则使用传入的tracker。
     * 这样可以在块节点的子节点解析时复用父tracker。</p>
     * 
     * @param tokens Token列表（会被修改，已解析的部分会被移除）
     * @param tracker 解析时作用域追踪器（可选，如果为null则创建新的）
     * @return 解析后的语法节点列表
     * @throws NfSyntaxException 如果遇到语法错误，立即抛出异常
     */
    public static List<SyntaxNode> buildMainStatement(List<Token> tokens, ParseScopeTracker tracker) {
        // 如果tracker为null，创建新的tracker；否则使用传入的tracker
        boolean isNewTracker = (tracker == null);
        if (isNewTracker) {
            tracker = new ParseScopeTracker();
        }
        
        // 保存旧的tracker（如果有）
        ParseScopeTracker oldTracker = trackerThreadLocal.get();
        trackerThreadLocal.set(tracker);
        boolean wrappedCursor = !(tokens instanceof TokenCursor);
        List<Token> workingTokens = wrappedCursor ? TokenCursor.wrap(tokens) : tokens;
        
        try {
            List<SyntaxNode> syntaxNodeList = new ArrayList<>();
            //使用while循环替代for循环，因为循环中会修改tokens列表
            //设计说明：直接修改tokens列表是为了高效解析，已解析的部分被移除后，剩余部分继续从开头解析
            while (!workingTokens.isEmpty()) {
                int ignoredCount = clearLeadingIgnorableTokens(workingTokens);
                if (ignoredCount > 0) {
                    if (workingTokens.isEmpty()) {
                        break;
                    }
                }

                Token firstToken = workingTokens.get(0);

                // 跳过可能由子节点清理后残留的空白前缀
                if (firstToken.type == TokenType.LINE_END || firstToken.type == TokenType.COMMENT) {
                    continue;
                }
                
                //尝试识别并构建语法节点
                boolean success = SyntaxNodeFactory.forEachNode(workingTokens, syntaxNodeList);
                if (!success) {
                    //如果无法识别，立即抛出异常
                    int errorTokenCount = Math.min(workingTokens.size(), MAX_ERROR_CONTEXT_TOKENS);
                    String context = TokenUtil.mergeToken(workingTokens.subList(0, errorTokenCount)).toString();
                    String suggestion = "期望: import, task, var, assign, declare, run, export, echo, if, switch, for, while, break, breakAll, continue 等关键字";
                    throw new NfSyntaxException(
                        firstToken.getLine(),
                        "无法识别的语法",
                        "无法识别此位置的语法结构",
                        context,
                        suggestion
                    );
                }
            }
            return syntaxNodeList;
        } finally {
            if (wrappedCursor) {
                syncRemainingTokens(tokens, workingTokens);
            }
            // 如果是新创建的tracker，清理ThreadLocal；否则恢复旧的tracker
            if (isNewTracker) {
                trackerThreadLocal.remove();
            } else {
                trackerThreadLocal.set(oldTracker);
            }
        }
    }

    private static void syncRemainingTokens(List<Token> originalTokens, List<Token> workingTokens) {
        if (originalTokens == workingTokens) {
            return;
        }
        List<Token> remainingTokens = new ArrayList<>(workingTokens);
        originalTokens.clear();
        originalTokens.addAll(remainingTokens);
    }

    private static int clearLeadingIgnorableTokens(List<Token> tokens) {
        int size = tokens.size();
        int removeCount = 0;
        while (removeCount < size) {
            TokenType type = tokens.get(removeCount).type;
            if (type != TokenType.LINE_END && type != TokenType.COMMENT) {
                break;
            }
            removeCount++;
        }
        if (removeCount > 0) {
            tokens.subList(0, removeCount).clear();
        }
        return removeCount;
    }
    
    /**
     * 获取当前线程的解析时作用域追踪器
     * 
     * @return 解析时作用域追踪器，如果当前不在解析阶段则返回null
     */
    public static ParseScopeTracker getCurrentTracker() {
        return trackerThreadLocal.get();
    }
}
