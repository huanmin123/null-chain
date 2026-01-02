package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class BreakSyntaxNode extends LineSyntaxNode {
    public BreakSyntaxNode() {
        super(SyntaxNodeType.BREAK_EXP);
    }
    
    public BreakSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.BREAK;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        Token token = tokens.get(0);
        List<Token> newToken = new ArrayList<>(Collections.singletonList(token));
        //为了防止在break后面还有注释,那么一直删除到不是注释为止
        do {
            tokens.remove(0);
        } while (!tokens.isEmpty() && tokens.get(0).type == TokenType.COMMENT);
        BreakSyntaxNode breakSyntaxNode = new BreakSyntaxNode(SyntaxNodeType.BREAK_EXP);
        breakSyntaxNode.setValue(newToken);
        syntaxNodeList.add(breakSyntaxNode);
        return true;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        //找到上一个作用域类型是for的作用域,设置break为true
        NfContextScope forScope = context.findByTypeScope(NfContextScopeType.FOR);
        if (forScope == null) {
            throw new NfException("Line:{} ,break语句必须在for循环内使用", syntaxNode.getLine());
        }
        forScope.setBreak(true);
    }
}
