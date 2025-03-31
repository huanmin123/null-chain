package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NfSyntaTest {
    static List<Token> tokens;
    static List<SyntaxNode> syntaxNodeList= new ArrayList<>();

    @BeforeClass
    public static void before() {
        String file = TestUtil.readFile("test1.nf");
        tokens = NfToken.tokens(file);
    }
    @AfterClass
    public static void after() {
        for (SyntaxNode syntaxNode : syntaxNodeList) {
            System.out.println(syntaxNode);
        }
    }

    @Test
    public void ImportNode() {
        SyntaxNode syntaxNode = SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.IMPORT_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void TaskNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.TASK_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void AssignExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.ASSIGN_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void DeclareExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.DECLARE_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }
    @Test
    public void RunExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.RUN_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void TaskExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.EXPORT_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void IFExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.IF_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }

    @Test
    public void SwitchExpNode() {
        SyntaxNode syntaxNode= SyntaxNodeFactory.getSyntaxNode(SyntaxNodeType.SWITCH_EXP);
        syntaxNode.buildStatement(tokens,syntaxNodeList);
    }



    @Test
    public  void NfSynta(){
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        for (SyntaxNode syntaxNode : syntaxNodes) {
            System.out.println("=======================================");
            System.out.println(syntaxNode);
        }

    }
}
