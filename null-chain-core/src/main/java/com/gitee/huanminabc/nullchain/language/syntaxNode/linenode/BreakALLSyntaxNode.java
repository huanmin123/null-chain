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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class BreakALLSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public BreakALLSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }
    @Override
    public boolean analystToken(List<Token> tokens) {
        return  tokens.get(0).type == TokenType.BREAK_ALL;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        Token token = tokens.get(0);
        List<Token> newToken = new ArrayList<>(Collections.singletonList(token));
        //为了防止在break后面还有注释,那么一直删除到不是注释为止
        do {
            tokens.remove(0);
        } while (!tokens.isEmpty() && tokens.get(0).type == TokenType.COMMENT);
        BreakALLSyntaxNode breakSyntaxNode = new BreakALLSyntaxNode(SyntaxNodeType.BREAK_ALL_EXP);
        breakSyntaxNode.setValue(newToken);
        syntaxNodeList.add(breakSyntaxNode);
        return true;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return  syntaxNode instanceof BreakALLSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //设置全部的form为breakall
        List<NfContextScope> byTypeScopeList = context.findByTypeScopeList(NfContextScopeType.FOR);
        for (NfContextScope nfContextScope : byTypeScopeList) {
            nfContextScope.setBreakAll(true);
        }
    }
}
