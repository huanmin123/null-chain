package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NF 脚本 Lambda 表达式不支持测试
 * 演示 NF 脚本不支持 Lambda 表达式，需要通过实现类的方式传入接口参数
 *
 * @author huanmin
 * @date 2025/01/05
 */
@Slf4j
public class LambdaNotSupportedExceptionTest {

    /**
     * 测试不支持 Lambda 表达式的情况
     * 这个测试会抛出异常，因为 NF 脚本不支持 Lambda 语法
     */
    @Test
    public void testLambdaNotSupported() {
        // 错误示例：使用 Lambda 表达式
        String script = "import type java.util.ArrayList\n" +
                        "import type com.gitee.huanminabc.test.nullchain.nf.PrintConsumer\n" +
                        "\n" +
                        "ArrayList list = new\n" +
                        "list.add(\"item1\")\n" +
                        "list.add(\"item2\")\n" +
                        "list.add(\"item3\")\n" +
                        "\n" +
                        "// 错误：NF 脚本不支持 Lambda 表达式\n" +
                        "list.forEach(item -> echo item)\n";

        // 这个会抛出异常，因为不支持 Lambda
        Exception exception = assertThrows(Exception.class, () -> {
            NfMain.run(script, log, null);
        });

        System.out.println("Lambda 不支持测试 - 预期异常: " + exception.getMessage());
        assertTrue(exception.getMessage() != null);
    }

    /**
     * 测试正确的用法：通过实现类传入接口参数
     * 这个测试演示如何正确使用实现类来替代 Lambda 表达式
     */
    @Test
    public void testCorrectWayWithImplementationClass() {
        // 正确示例：使用实现类
        String script = "import type java.util.ArrayList\n" +
                        "import type com.gitee.huanminabc.test.nullchain.nf.PrintConsumer\n" +
                        "\n" +
                        "ArrayList list = new\n" +
                        "list.add(\"item1\")\n" +
                        "list.add(\"item2\")\n" +
                        "list.add(\"item3\")\n" +
                        "\n" +
                        "// 正确：创建实现类对象并传入\n" +
                        "PrintConsumer printer = new\n" +
                        "list.forEach(printer)\n" +
                        "\n" +
                        "export \"处理完成，共处理 {list.size()} 个元素\"\n";

        // 这个应该正常执行
        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            System.out.println("执行结果: " + result);
            assertNotNull(result);
        });
    }

    /**
     * 测试其他函数式接口的用法示例
     */
    @Test
    public void testFunctionInterface() {
        // 如果需要 Function 接口，也需要实现类
        String script = "import type java.util.HashMap\n" +
                        "import type java.util.function.Function\n" +
                        "\n" +
                        "// 注意：这里需要导入一个实现了 Function 接口的类\n" +
                        "// 比如 UpperCaseFunction implements Function<String, String>\n" +
                        "// 然后创建对象传入\n" +
                        "\n" +
                        "HashMap map = new\n" +
                        "map.put(\"name\", \"zhangsan\")\n" +
                        "map.put(\"age\", \"25\")\n" +
                        "\n" +
                        "// 使用 NF 脚本的 for 循环替代 Lambda\n" +
                        "for key, value in map {\n" +
                        "    echo \"key: {key}, value: {value}\"\n" +
                        "}\n" +
                        "\n" +
                        "export \"遍历完成\"\n";

        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            System.out.println("Function 接口替代方案结果: " + result);
        });
    }

    /**
     * 测试在 NF 脚本中如何处理需要过滤的场景
     * 使用 for 循环 + if 判断替代 Stream filter + Lambda
     */
    @Test
    public void testFilterAlternative() {
        String script = "import type java.util.ArrayList\n" +
                        "\n" +
                        "ArrayList list = new\n" +
                        "list.add(1)\n" +
                        "list.add(2)\n" +
                        "list.add(3)\n" +
                        "list.add(4)\n" +
                        "list.add(5)\n" +
                        "\n" +
                        "// 使用 NF 脚本的 for 循环 + if 替代 Lambda filter\n" +
                        "ArrayList filteredList = new\n" +
                        "for item in list {\n" +
                        "    if item > 2 {\n" +
                        "        filteredList.add(item)\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "echo \"过滤后的结果:\"\n" +
                        "for item in filteredList {\n" +
                        "    echo item\n" +
                        "}\n" +
                        "\n" +
                        "export filteredList.size()\n";

        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            System.out.println("过滤结果数量: " + result);
            assertEquals(3, result); // 3, 4, 5 共3个元素
        });
    }

    /**
     * 测试在 NF 脚本中如何处理需要映射的场景
     * 使用 for 循环替代 Stream map + Lambda
     */
    @Test
    public void testMapAlternative() {
        String script = "import type java.util.ArrayList\n" +
                        "\n" +
                        "ArrayList list = new\n" +
                        "list.add(1)\n" +
                        "list.add(2)\n" +
                        "list.add(3)\n" +
                        "\n" +
                        "// 使用 NF 脚本的 for 循环替代 Lambda map\n" +
                        "ArrayList mappedList = new\n" +
                        "for item in list {\n" +
                        "    mappedList.add(item * 2)\n" +
                        "}\n" +
                        "\n" +
                        "echo \"映射后的结果:\"\n" +
                        "for item in mappedList {\n" +
                        "    echo item\n" +
                        "}\n" +
                        "\n" +
                        "export mappedList.size()\n";

        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            System.out.println("映射结果数量: " + result);
            assertEquals(3, result);
        });
    }
}
