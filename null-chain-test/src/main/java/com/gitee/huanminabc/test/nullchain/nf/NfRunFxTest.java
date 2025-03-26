package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import com.gitee.huanminabc.utils_common.test.CodeTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
@Slf4j
public class NfRunFxTest {
    static List<Token> tokens;


    @BeforeClass
    public static void before() {
        String file = TestUtil.readFile("test1.nf");
        tokens = NfToken.tokens(file);
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
}
