package com.gitee.huanminabc.test.nullchain.nf.expression;

import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.NfRun;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * instanceof关键字测试类
 * 测试instanceof类型判断功能
 *
 * @author huanmin
 */
@Slf4j
public class InstanceofTest {

    /**
     * 测试instanceof关键字
     */
    @Test
    public void testInstanceof() {
        String code = "Map map = new\n" +
                     "map.put(\"num\", 100)\n" +
                     "map.put(\"str\", \"hello\")\n" +
                     "map.put(\"bool\", true)\n" +
                     "\n" +
                     "for key, value in map {\n" +
                     "    if value instanceof Integer {\n" +
                     "        echo \"{key} is Integer: {value}\"\n" +
                     "    } else if value instanceof String {\n" +
                     "        echo \"{key} is String: {value}\"\n" +
                     "    } else if value instanceof Boolean {\n" +
                     "        echo \"{key} is Boolean: {value}\"\n" +
                     "    }\n" +
                     "}\n";

        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(code);

        System.out.println("Tokens for instanceof test:");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(i + ": " + tokens.get(i).type + " - " + tokens.get(i).value);
        }

        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokens);

        NfContext context = new NfContext();
        Object result = NfRun.run(syntaxNodes, context, log, null);

        assertNotNull(result);
        log.info("instanceof测试通过");
    }
}

