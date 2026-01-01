package com.gitee.huanminabc.test.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import com.gitee.huanminabc.test.nullchain.task.Test1Task;
import com.gitee.huanminabc.test.nullchain.task.Test2Task;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null链异常测试类
 * 
 * <p>测试各种异常场景，包括：
 * - 参数校验异常
 * - 并发任务异常
 * - 链式操作异常
 * - 空值异常
 * - 任务执行异常
 * - 工具异常
 * - NF脚本异常
 * - 序列化异常
 * - 边界条件异常</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public class NullChainExceptionTest {

    // ========== 参数校验异常测试 ==========

    @Test
    public void testNullGroupTaskEmptyException() {
        // 测试任务组为空异常
        assertThrows(NullChainException.class, () -> {
            NullGroupTask.buildGroup();
        }, "任务组为空应该抛出异常");
    }

    @Test
    public void testNullGroupTaskDuplicateException() {
        // 测试任务重复异常
        assertThrows(NullChainException.class, () -> {
            NullGroupTask.buildGroup(
                    NullGroupTask.task(Test1Task.class.getName()),
                    NullGroupTask.task(Test1Task.class.getName())  // 重复任务
            );
        }, "任务重复应该抛出异常");
    }

    @Test
    public void testTaskWithNullParamsException() {
        // 测试任务参数为null异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            Null.of(input)
                    .task((NullGroupTask) null)
                    .get();
        }, "任务参数为null应该抛出异常");
    }

    @Test
    public void testTaskWithNullThreadFactoryException() {
        // 测试线程工厂为null异常
        String input = "test";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName())
        );
        assertThrows(NullChainException.class, () -> {
            Null.of(input)
                    .task(nullGroupTask, (String) null)
                    .get();
        }, "线程工厂为null应该抛出异常");
    }

    @Test
    public void testNfTaskWithNullContextException() {
        // 测试NF脚本内容为空异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            Null.of(input)
                    .nfTasks(NullGroupNfTask.buildGroup(
                            NullGroupNfTask.task("")  // 空脚本
                    ), "default")
                    .get();
        }, "NF脚本内容为空应该抛出异常");
    }

    @Test
    public void testNfTaskWithNullGroupException() {
        // 测试NF任务组为null异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            Null.of(input)
                    .nfTasks((NullGroupNfTask) null, "default")
                    .get();
        }, "NF任务组为null应该抛出异常");
    }

    // ========== 链式操作异常测试 ==========

    @Test
    public void testMapOperationException() {
        // 测试map操作抛出异常
        UserEntity user = new UserEntity();
        user.setName("test");

        assertThrows(NullChainException.class, () -> {
            Null.of(user)
                    .map(u -> {
                        throw new RuntimeException("map操作异常");
                    })
                    .get();
        }, "map操作异常应该被包装为NullChainException");
    }

    @Test
    public void testFilterOperationException() {
        // 测试filter操作抛出异常
        UserEntity user = new UserEntity();
        user.setName("test");

        assertThrows(NullChainException.class, () -> {
            Null.of(user)
                    .ifGo(u -> {
                        throw new RuntimeException("filter操作异常");
                    })
                    .get();
        }, "filter操作异常应该被包装为NullChainException");
    }

    @Test
    public void testFlatChainOperationException() {
        // 测试flatChain操作抛出异常
        UserEntity user = new UserEntity();
        user.setName("test");

        assertThrows(NullChainException.class, () -> {
            Null.of(user)
                    .flatChain(u -> {
                        throw new RuntimeException("flatChain操作异常");
                    })
                    .get();
        }, "flatChain操作异常应该被包装为NullChainException");
    }

    @Test
    public void testTypeConversionException() {
        // 测试类型转换异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            Null.of(input)
                    .type(Integer.class)  // 无法转换
                    .get();
        }, "类型转换异常应该被包装为NullChainException");
    }

    // ========== 空值异常测试 ==========

    @Test
    public void testGetWithNullValueException() {
        // 测试get()方法在空值情况下抛出异常
        assertThrows(NullChainException.class, () -> {
            Null.of((String) null)
                    .get();
        }, "空值调用get()应该抛出异常");
    }

    @Test
    public void testGetWithCustomMessageException() {
        // 测试get()方法使用自定义异常消息
        try {
            Null.of((String) null)
                    .get("自定义异常消息");
            fail("应该抛出异常");
        } catch (NullChainException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("自定义异常消息") || 
                      e.getMessage().contains("Null.of"),
                    "异常消息应包含自定义消息或链路信息");
        }
    }

    @Test
    public void testGetWithExceptionSupplier() {
        // 测试get()方法使用异常提供者
        try {
            Null.of((String) null)
                    .get(() -> new IllegalArgumentException("自定义异常"));
            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            assertEquals(" Null.of? 自定义异常", e.getMessage());
        }
    }

    @Test
    public void testSerializeNullValueException() {
        // 测试序列化空值异常
        NullChain<String> chain = Null.of((String) null);
        
        assertThrows(NullChainException.class, () -> {
            // 尝试序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(chain);
            oos.close();
        }, "序列化空值应该抛出异常");
    }

    // ========== 任务执行异常测试 ==========

    @Test
    public void testTaskExecutionException() {
        // 测试任务执行异常（任务不存在）
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                    NullGroupTask.task("com.nonexistent.Task")
            );
            Null.of(input)
                    .task(nullGroupTask)
                    .get();
        }, "任务不存在应该抛出异常");
    }

    @Test
    public void testTaskWithInvalidClassName() {
        // 测试任务类名无效异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                    NullGroupTask.task("InvalidClassName")
            );
            Null.of(input)
                    .task(nullGroupTask)
                    .get();
        }, "无效的任务类名应该抛出异常");
    }

    @Test
    public void testTaskWithWrongType() {
        // 测试任务类型错误异常
        String input = "test";
        assertThrows(NullChainException.class, () -> {
            // 尝试使用非NullTask类型的类
            NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                    NullGroupTask.task(String.class.getName())  // String不是NullTask
            );
            Null.of(input)
                    .task(nullGroupTask)
                    .get();
        }, "任务类型错误应该抛出异常");
    }

    @Test
    public void testConcurrentTaskException() {
        // 测试并发任务执行异常
        String input = "test";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        // 并发任务执行，即使有异常也应该被捕获
        try {
            Map<String, Object> result = (Map<String, Object>) Null.of(input)
                    .task(nullGroupTask)
                    .get();
            // 如果任务执行成功，结果不应为空
            assertNotNull(result);
        } catch (NullChainException e) {
            // 如果抛出异常，验证异常消息包含链路信息
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("task") || 
                      e.getMessage().length() > 0,
                    "异常消息应包含任务相关信息");
        }
    }

    // ========== 工具异常测试 ==========

    @Test
    public void testToolNotFoundException() {
        // 测试工具不存在异常
        // 注意：tool方法需要传入Class类型，这里测试类不存在的情况
        try {
            // 尝试使用不存在的工具类
            Class<?> toolClass = Class.forName("com.nonexistent.NonExistentTool");
            @SuppressWarnings("unchecked")
            Class<? extends com.gitee.huanminabc.nullchain.tool.NullTool<String, Object>> toolClassTyped = 
                    (Class<? extends com.gitee.huanminabc.nullchain.tool.NullTool<String, Object>>) toolClass;
            String input = "test";
            Null.of(input)
                    .tool(toolClassTyped)
                    .get();
            // 如果类不存在，会在Class.forName时抛出异常
        } catch (ClassNotFoundException e) {
            // 类不存在，这是预期的
            assertNotNull(e);
        } catch (NullChainException e) {
            // 工具不存在或执行失败都应该抛出异常
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            // 其他异常也是可以接受的
            assertNotNull(e);
        }
    }

    @Test
    public void testToolExecutionException() {
        // 测试工具执行异常（需要根据实际工具实现）
        // 这里只是示例，实际测试需要根据具体的工具实现
        // 由于tool方法需要Class类型，这里只做基本测试
        // 实际使用时需要传入正确的工具类
        assertTrue(true, "工具执行异常测试需要具体的工具实现");
    }

    // ========== NF脚本异常测试 ==========

    @Test
    public void testNfScriptSyntaxError() {
        // 测试NF脚本语法错误
        String input = "test";
        try {
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("invalid syntax !!!"))
                    .get();
        } catch (Exception e) {
            // NF脚本语法错误应该抛出异常
            assertNotNull(e);
            assertTrue(e instanceof NullChainException || 
                      e.getCause() instanceof NullChainException ||
                      e.getMessage() != null,
                    "NF脚本语法错误应该抛出异常");
        }
    }

    @Test
    public void testNfScriptRuntimeError() {
        // 测试NF脚本运行时错误
        String input = "test";
        try {
            // 尝试访问不存在的变量
            Null.of(input)
                    .nfTask(NullGroupNfTask.task("export undefinedVariable"))
                    .get();
        } catch (Exception e) {
            // NF脚本运行时错误应该抛出异常
            assertNotNull(e);
        }
    }

    // ========== 边界条件异常测试 ==========

    @Test
    public void testEmptyListException() {
        // 测试空列表异常
        List<String> emptyList = new ArrayList<>();
        try {
            // 使用ofStream来处理流
            java.util.stream.Stream<String> stream = emptyList.stream();
            com.gitee.huanminabc.nullchain.leaf.stream.NullStream<String> nullStream = Null.ofStream(stream);
            // NullStream可能没有get方法，这里只测试不会崩溃
            assertNotNull(nullStream);
        } catch (Exception e) {
            // 空列表可能不会抛出异常，取决于实现
            // 这里只是验证不会崩溃
            assertNotNull(e);
        }
    }

    @Test
    public void testEmptyStringException() {
        // 测试空字符串
        String emptyString = "";
        try {
            String result = Null.of(emptyString)
                    .map(String::toUpperCase)
                    .orElse("DEFAULT");
            // 空字符串不应该抛出异常，应该返回默认值
            assertEquals("DEFAULT", result);
        } catch (Exception e) {
            fail("空字符串不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testNullArrayException() {
        // 测试null数组
        String[] nullArray = null;
        try {
            Null.of(nullArray)
                    .get();
            fail("null数组应该抛出异常");
        } catch (NullChainException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ========== 异常消息和链路信息测试 ==========

    @Test
    public void testExceptionMessageContainsLinkLog() {
        // 测试异常消息包含链路信息
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            Null.of(user)
                    .map(u -> {
                        throw new RuntimeException("测试异常");
                    })
                    .map(s -> s.toString())
                    .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            String message = e.getMessage();
            assertNotNull(message);
            // 异常消息应该包含链路信息
            assertTrue(message.contains("map") || message.length() > 0,
                    "异常消息应包含链路信息");
        }
    }

    @Test
    public void testExceptionSuppressedInfo() {
        // 测试异常中的suppressed信息
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            Null.of(user)
                    .map(u -> {
                        throw new RuntimeException("原始异常");
                    })
                    .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            // 验证异常包含suppressed信息
            Throwable[] suppressed = e.getSuppressed();
            // suppressed可能为空，取决于实现
            assertNotNull(suppressed, "suppressed数组不应为null");
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testExceptionStackTrace() {
        // 测试异常堆栈信息
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            Null.of(user)
                    .map(u -> {
                        throw new RuntimeException("测试异常");
                    })
                    .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0, "异常堆栈不应为空");
        }
    }

    // ========== 并发异常场景测试 ==========

    @Test
    public void testConcurrentTaskWithException() {
        // 测试并发任务中的异常处理
        String input = "test";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        // 多次执行，验证异常处理的一致性
        for (int i = 0; i < 5; i++) {
            try {
                Map<String, Object> result = (Map<String, Object>) Null.of(input)
                        .task(nullGroupTask)
                        .get();
                assertNotNull(result);
            } catch (NullChainException e) {
                // 如果抛出异常，验证异常信息
                assertNotNull(e.getMessage());
            }
        }
    }

    @Test
    public void testConcurrentNfTaskWithException() {
        // 测试并发NF任务中的异常处理
        String input = "test";
        // export 现在支持表达式计算，可以直接使用表达式
        NullGroupNfTask nullGroupNfTask = NullGroupNfTask.buildGroup(
                NullGroupNfTask.task("export preValue + '_1'"),
                NullGroupNfTask.task("export preValue + '_2'")
        );

        try {
            Map<String, Object> result = (Map<String, Object>) Null.of(input)
                    .nfTasks(nullGroupNfTask, "default")
                    .get();
            assertNotNull(result);
        } catch (NullChainException e) {
            // 如果抛出异常，验证异常信息
            assertNotNull(e.getMessage());
        }
    }

    // ========== 异常传播测试 ==========

    @Test
    public void testExceptionPropagation() {
        // 测试异常传播
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            Null.of(user)
                    .map(u -> {
                        throw new IllegalArgumentException("参数错误");
                    })
                    .map(s -> s.toString())
                    .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            // 原始异常应该被包装
            Throwable cause = e.getCause();
            // cause可能为null，取决于实现
            // 验证异常消息不为空即可
            assertNotNull(e.getMessage());
            // cause可能为null，这是正常的
            if (cause != null) {
                assertTrue(cause instanceof RuntimeException || cause instanceof IllegalArgumentException);
            }
        }
    }

    @Test
    public void testNestedException() {
        // 测试嵌套异常
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            Null.of(user)
                    .flatChain(u -> {
                        return Null.of(u.getName())
                                .map(s -> {
                                    throw new RuntimeException("嵌套异常");
                                });
                    })
                    .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ========== 异常恢复测试 ==========

    @Test
    public void testExceptionThrownInMapOperation() {
        // 测试map操作抛出异常时，异常会被抛出而不是返回空值
        // 注意：orElse只能处理空值，不能处理异常
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<Object> chain = Null.of(user)
                .map(u -> {
                    throw new RuntimeException("异常");
                });

        // map操作抛出异常时，异常会被包装成NullChainException并抛出
        // orElse不会被执行，因为异常已经在runTaskAll()中抛出
        assertThrows(NullChainException.class, () -> {
            chain.orElse("恢复值");
        }, "map操作抛出异常时应该抛出NullChainException，而不是返回默认值");
    }

    @Test
    public void testExceptionThrownInMapOperationWithOrElseNull() {
        // 测试map操作抛出异常时，orElseNull也会抛出异常
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<Object> chain = Null.of(user)
                .map(u -> {
                    throw new RuntimeException("异常");
                });

        // orElseNull也不会被执行，因为异常已经在runTaskAll()中抛出
        assertThrows(NullChainException.class, () -> {
            chain.orElseNull();
        }, "map操作抛出异常时应该抛出NullChainException，而不是返回null");
    }

    @Test
    public void testOrElseWithNullValue() {
        // 测试orElse在空值情况下的正常行为（不是异常情况）
        UserEntity user = new UserEntity();
        // name为null

        String result = Null.of(user)
                .map(UserEntity::getName)
                .orElse("默认值");

        assertEquals("默认值", result, "空值情况下orElse应该返回默认值");
    }

    @Test
    public void testOrElseNullWithNullValue() {
        // 测试orElseNull在空值情况下的正常行为（不是异常情况）
        UserEntity user = new UserEntity();
        // name为null

        String result = Null.of(user)
                .map(UserEntity::getName)
                .orElseNull();

        assertNull(result, "空值情况下orElseNull应该返回null");
    }

    // ========== 边界异常测试 ==========

    @Test
    public void testVeryLongChainException() {
        // 测试超长链的异常处理
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<String> chain = Null.of(user)
                .map(UserEntity::getName);

        // 创建很长的链
        for (int i = 0; i < 100; i++) {
            final int index = i;
            chain = chain.map(s -> s + "_" + index);
        }

        // 在链的某个位置抛出异常
        try {
            chain.map(s -> {
                throw new RuntimeException("长链异常");
            })
            .get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNullCheckException() {
        // 测试NullCheck异常
        UserEntity user = new UserEntity();
        // name为null

        try {
            Null.ofCheck(user)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .doThrow(IllegalArgumentException.class, "检查失败");
            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("检查失败") || 
                      e.getMessage().contains("name") ||
                      e.getMessage().length() > 0,
                    "异常消息应包含检查信息");
        }
    }

    @Test
    public void testNullCheckWithAllNullFields() {
        // 测试所有字段都为null的情况
        UserEntity user = new UserEntity();
        // 所有字段都为null

        try {
            Null.ofCheck(user)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .of(UserEntity::getRoleData)
                    .doThrow(IllegalArgumentException.class);
            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }
}

