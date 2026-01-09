package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NF Lambda 自动转换测试
 * 测试 NF Lambda 是否可以自动转换为 Java 函数式接口，无需手动创建代理
 *
 * @author huanmin
 * @date 2025/01/08
 */
@Slf4j
public class AutoLambdaConversionTest {

    /**
     * 测试 NF Lambda 自动转换为 Function 并传递给 Java 方法
     */
    @Test
    @DisplayName("✓ NF Lambda 自动转换 -> Java Function")
    public void testAutoLambdaConversion() {
        String script =
            "import type com.gitee.huanminabc.test.nullchain.nf.LambdaTestUtils\n" +
            "\n" +
            "// 定义 Lambda\n" +
            "Fun<Integer : Integer> doubler = (x) -> { return x * 2 }\n" +
            "\n" +
            "// 调用 Java 方法，Lambda 会自动转换\n" +
            "Integer result = LambdaTestUtils.applyFunction(doubler, 5)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：NF Lambda 自动转换为 Java Function =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        assertEquals(10, result);
        log.info("✓ 测试通过: NF Lambda 自动转换成功，结果 = {}", result);
    }

    /**
     * 测试 NF Lambda 在 Stream 中使用（自动转换）
     */
    @Test
    @DisplayName("✓ NF Lambda 在 Stream.map() 中自动转换")
    public void testAutoLambdaInStream() {
        String script =
            "import type com.gitee.huanminabc.test.nullchain.nf.LambdaTestUtils\n" +
            "import type java.util.Arrays\n" +
            "import type java.util.List\n" +
            "\n" +
            "// 定义 Lambda\n" +
            "Fun<Integer : Integer> triple = (x) -> { return x * 3 }\n" +
            "\n" +
            "// 在 Stream 中使用 Lambda（自动转换）\n" +
            "List numbers = Arrays.asList(1, 2, 3, 4, 5)\n" +
            "List result = LambdaTestUtils.streamMap(numbers, triple)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：NF Lambda 在 Stream.map() 中自动转换 =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        assertEquals(Arrays.asList(3, 6, 9, 12, 15), result);
        log.info("✓ 测试通过: Stream.map() 自动转换成功，结果 = {}", result);
    }

    /**
     * 测试多个 Lambda 传递给 Java 方法
     */
    @Test
    @DisplayName("✓ 多个 Lambda 自动转换")
    public void testMultipleLambdaConversion() {
        String script =
            "import type com.gitee.huanminabc.test.nullchain.nf.LambdaTestUtils\n" +
            "import type java.util.Arrays\n" +
            "import type java.util.List\n" +
            "\n" +
            "// 定义多个 Lambda\n" +
            "Fun<Integer : Integer> doubler = (x) -> { return x * 2 }\n" +
            "Fun<Integer : Boolean> isPositive = (x) -> { return x > 0 }\n" +
            "\n" +
            "// 传递给 Java 方法（自动转换）\n" +
            "Integer result1 = LambdaTestUtils.applyFunction(doubler, 10)\n" +
            "Boolean result2 = LambdaTestUtils.testPredicate(isPositive, 5)\n" +
            "List result = Arrays.asList(result1, result2)\n" +
            "\n" +
            "export result";

        log.info("===== 测试：多个 Lambda 自动转换 =====");

        Object result = NfMain.run(script, log, null);

        log.info("Result: {}", result);
        assertEquals(Arrays.asList(20, true), result);
        log.info("✓ 测试通过: 多个 Lambda 自动转换成功");
    }
}
