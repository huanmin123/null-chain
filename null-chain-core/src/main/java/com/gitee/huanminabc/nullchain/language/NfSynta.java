package com.gitee.huanminabc.nullchain.language;

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
     */
    public static List<SyntaxNode> buildMainStatement(List<Token> tokens) {
        List<SyntaxNode> syntaxNodeList = new ArrayList<>();
        //使用while循环替代for循环，因为循环中会修改tokens列表
        //设计说明：直接修改tokens列表是为了高效解析，已解析的部分被移除后，剩余部分继续从开头解析
        while (!tokens.isEmpty()) {
            //跳过换行和注释
            Token firstToken = tokens.get(0);
            if (firstToken.type == TokenType.LINE_END || firstToken.type == TokenType.COMMENT) {
                tokens.remove(0);
                continue;
            }
            //尝试识别并构建语法节点
            boolean success = SyntaxNodeFactory.forEachNode(tokens, syntaxNodeList);
            if (!success) {
                //如果无法识别，抛出异常
                int errorTokenCount = Math.min(tokens.size(), MAX_ERROR_CONTEXT_TOKENS);
                String context = TokenUtil.mergeToken(tokens.subList(0, errorTokenCount)).toString();
                String suggestion = "期望: import, task, assign, declare, run, export, echo, if, switch, for, while, break, breakAll, continue 等关键字";
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
    }
}
