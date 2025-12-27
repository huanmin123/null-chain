package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.jcommon.test.CodeTimeUtil;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
@Slf4j
public class NfRunFxTest {
    static List<Token> tokens;
    static List<Token> tokens1;


    @BeforeEach
    public  void before() {
        String file = TestUtil.readFile("test.nf");
        tokens = NfToken.tokens(file);

        String file1 = TestUtil.readFile("test1.nf");
        tokens1 = NfToken.tokens(file1);
    }

    @Test
    public void NfSynta() {
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
//        for (SyntaxNode syntaxNode : syntaxNodes) {
//            System.out.println("=======================================");
//            System.out.println(syntaxNode);
//        }
//        for (int i = 0; i < 10; i++) {
        for (int i = 0; i < 1; i++) {
            CodeTimeUtil.creator(() -> {
                NfContext context = new NfContext();
                Object run = NfRun.run(syntaxNodes, context,log, null);
                System.out.println(run);
            });
        }

    }
    @Test
    public void NfSynta1() {
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens1);
        for (int i = 0; i < 1; i++) {
            CodeTimeUtil.creator(() -> {
                NfContext context = new NfContext();
                Object run = NfRun.run(syntaxNodes, context,log, null);
                System.out.println(run);
            });
        }
    }
}
