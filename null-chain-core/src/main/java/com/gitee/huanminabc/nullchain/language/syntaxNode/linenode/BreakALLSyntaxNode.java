package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class BreakALLSyntaxNode extends LineSyntaxNode {
    public BreakALLSyntaxNode() {
        super(SyntaxNodeType.BREAK_ALL_EXP);
    }
    
    public BreakALLSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.BREAK_ALL;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        Token token = tokens.get(0);
        List<Token> newToken = new ArrayList<>(Collections.singletonList(token));
        int removeCount = 1;
        while (removeCount < tokens.size() && tokens.get(removeCount).type == TokenType.COMMENT) {
            removeCount++;
        }
        SyntaxNodeUtil.clearLeadingTokens(tokens, removeCount);
        BreakALLSyntaxNode breakSyntaxNode = new BreakALLSyntaxNode(SyntaxNodeType.BREAK_ALL_EXP);
        breakSyntaxNode.setValue(newToken);
        syntaxNodeList.add(breakSyntaxNode);
        return true;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //设置全局breakAll标志，用于跳出所有FOR循环
        context.setGlobalBreakAll(true);
    }
}
