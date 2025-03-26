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
        for ( int i = 0; i < tokens.size(); i++) {
            //跳过换行和注释
            if (tokens.get(0).type == TokenType.LINE_END||tokens.get(0).type == TokenType.COMMENT) {
                tokens.remove(0);
                i = -1;
                continue;
            }
            boolean b = SyntaxNodeFactory.forEachNode(tokens, syntaxNodeList);
            if (b) {
                i = -1;
            }else{
                throw new NfException("语法构建错误: {} ......", TokenUtil.mergeToken(tokens.subList(0, Math.min(tokens.size(), 20))));
            }
        }
        return syntaxNodeList;
    }
}
