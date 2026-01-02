package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 语法分析器 ,将Token解析成语法节点
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfSynta {
    public List<SyntaxNode> syntaxNodeList = new ArrayList<>();

    //构建主语句, 也就是把所有的Token解析第一层语法节点,和文件代码顺序一致,但是一些块语句内部的节点还没有解析
    public static List<SyntaxNode> buildMainStatement(List<Token> tokens) {
        List<SyntaxNode> syntaxNodeList = new ArrayList<>();
        //使用while循环替代for循环，因为循环中会修改tokens列表
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
                int errorTokenCount = Math.min(tokens.size(), 20);
                throw new NfException("语法构建错误: {} ......", TokenUtil.mergeToken(tokens.subList(0, errorTokenCount)));
            }
        }
        return syntaxNodeList;
    }
}
