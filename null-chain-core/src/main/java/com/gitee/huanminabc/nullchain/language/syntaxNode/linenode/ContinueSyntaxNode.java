package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeStructType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class ContinueSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public ContinueSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }
    @Override
    public boolean analystToken(List<Token> tokens) {
        return  tokens.get(0).type == TokenType.CONTINUE;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        Token token = tokens.get(0);
        List<Token> newToken = new ArrayList(Collections.singletonList(token));
        //为了防止在break后面还有注释,那么一直删除到不是注释为止
        do {
            tokens.remove(0);
        } while (!tokens.isEmpty() && tokens.get(0).type == TokenType.COMMENT);
        ContinueSyntaxNode continueSyntaxNode = new ContinueSyntaxNode(SyntaxNodeType.CONTINUE_EXP);
        continueSyntaxNode.setValue(newToken);
        syntaxNodeList.add(continueSyntaxNode);
        return true;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return  syntaxNode instanceof ContinueSyntaxNode;
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
