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
public class ContinueSyntaxNode extends LineSyntaxNode {
    public ContinueSyntaxNode() {
        super(SyntaxNodeType.CONTINUE_EXP);
    }
    
    public ContinueSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.CONTINUE;
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
        ContinueSyntaxNode continueSyntaxNode = new ContinueSyntaxNode(SyntaxNodeType.CONTINUE_EXP);
        continueSyntaxNode.setValue(newToken);
        syntaxNodeList.add(continueSyntaxNode);
        return true;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //设置当前作用域为continue
        List<NfContextScope> byTypeScopeListRangeBefore = context.findByTypeScopeListRange(NfContextScopeType.FOR);
        for (NfContextScope nfContextScope : byTypeScopeListRangeBefore) {
            nfContextScope.setContinue(true);
        }
    }
}
