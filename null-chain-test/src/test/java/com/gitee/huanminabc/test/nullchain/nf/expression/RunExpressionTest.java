package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.test.nullchain.task.RunCaptureTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RunExpressionTest {

    @BeforeEach
    public void resetCaptureTask() {
        RunCaptureTask.reset();
    }

    @Test
    public void testRunWithoutBindingExecutesTaskAndConvertsParams() {
        Object result = runScript(
            "import task com.gitee.huanminabc.test.nullchain.task.RunCaptureTask as capture\n" +
                "String name = \"张三\"\n" +
                "run capture(name, 18, \"hello {name}\", true, 3.5)\n" +
                "export \"done\"\n"
        );

        assertEquals("done", result);
        assertEquals(1, RunCaptureTask.getRunCount());
        assertEquals(Arrays.asList("张三", 18, "hello 张三", true, 3.5d), RunCaptureTask.getLastParams());
    }

    @Test
    public void testRunWithResultBinding() {
        Object result = runScript(
            "import task com.gitee.huanminabc.test.nullchain.task.RunValueTaskOne as test1\n" +
                "String name = \"张三\"\n" +
                "Integer age = 25\n" +
                "run test1(name, age) -> result:String\n" +
                "export result\n"
        );

        assertEquals("33333333", result);
    }

    @Test
    public void testRunParallelTasksWithMapBinding() {
        Object result = runScript(
            "import type java.util.Map\n" +
                "import task com.gitee.huanminabc.test.nullchain.task.RunValueTaskOne as test1\n" +
                "import task com.gitee.huanminabc.test.nullchain.task.RunValueTaskTwo as test2\n" +
                "String name = \"张三\"\n" +
                "Integer age = 25\n" +
                "run test1(name, age), test2(3.5, 4.5) -> results:Map\n" +
                "export results\n"
        );

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals(2, resultMap.size());
        assertEquals("33333333", resultMap.get("com.gitee.huanminabc.test.nullchain.task.RunValueTaskOne"));
        assertEquals("2222222222", resultMap.get("com.gitee.huanminabc.test.nullchain.task.RunValueTaskTwo"));
    }

    private Object runScript(String script) {
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(script);
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);
        return NfRun.run(syntaxNodes, new NfContext(), null, null);
    }
}
