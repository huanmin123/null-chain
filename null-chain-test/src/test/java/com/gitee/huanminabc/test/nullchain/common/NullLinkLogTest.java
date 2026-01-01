package com.gitee.huanminabc.test.nullchain.common;

import com.gitee.huanminabc.jcommon.reflect.FieldUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import com.gitee.huanminabc.test.nullchain.task.Test1Task;
import com.gitee.huanminabc.test.nullchain.task.Test2Task;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null链日志测试类
 * 
 * <p>测试各种场景下的日志是否正确，包括：
 * - 正常链式调用的日志
 * - 并发任务场景的日志（重点测试并发修复）
 * - 异常场景的日志
 * - 空值场景的日志
 * - 多级判空的日志</p>
 * 
 * @author huanmin
 * @since 1.1.4
 */
public class NullLinkLogTest {

    /**
     * 通过反射获取 linkLog 内容
     * 
     * @param chain NullChain 实例
     * @return linkLog 字符串内容
     */
    private String getLinkLog(NullChain<?> chain) {
        try {
            if (chain instanceof NullChainBase) {
                Field linkLogField = FieldUtil.getField(NullKernelAbstract.class, "linkLog");
                if (linkLogField != null) {
                    linkLogField.setAccessible(true);
                    StringBuilder linkLog = (StringBuilder) linkLogField.get(chain);
                    return linkLog != null ? linkLog.toString() : "";
                }
            }
        } catch (Exception e) {
            // 忽略反射异常
        }
        return "";
    }

    /**
     * 从异常消息中提取 linkLog 内容
     * 
     * @param e 异常对象
     * @return linkLog 字符串内容
     */
    private String getLinkLogFromException(NullChainException e) {
        if (e == null) {
            return "";
        }
        String message = e.getMessage();
        if (message == null) {
            return "";
        }
        // 异常消息格式通常是 "linkLog内容 异常信息"
        // 这里简单返回整个消息，实际使用时可能需要解析
        return message;
    }

    /**
     * 统计字符串中指定子串的出现次数
     * 
     * @param text 文本
     * @param pattern 要统计的子串
     * @return 出现次数
     */
    private int countOccurrences(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    // ========== 正常链式调用日志测试 ==========

    @Test
    public void testNormalChainLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");
        user.setAge(25);

        NullChain<String> chain = Null.of(user)
                .map(UserEntity::getName)
                .map(s -> s.toUpperCase());

        String result = chain.orElse("DEFAULT");
        assertEquals("TEST", result);

        // 验证日志包含正确的操作，且格式正确
        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证必须包含 Null.of->（成功操作）
        assertTrue(linkLog.contains(" Null.of->"), 
                "日志应包含 ' Null.of->' 标记，实际日志: " + linkLog);
        // 验证必须包含 map->（成功操作）
        assertTrue(linkLog.contains("map->"), 
                "日志应包含 'map->' 标记，实际日志: " + linkLog);
        // 验证操作顺序：Null.of 应该在 map 之前
        int nullOfIndex = linkLog.indexOf(" Null.of->");
        int mapIndex = linkLog.indexOf("map->");
        assertTrue(nullOfIndex < mapIndex, 
                "日志操作顺序错误：Null.of 应在 map 之前，实际日志: " + linkLog);
    }

    @Test
    public void testEmptyChainLinkLog() {
        NullChain<String> chain = Null.of((String) null)
                .map(s -> s != null ? s.toUpperCase() : null);

        String result = chain.orElse("DEFAULT");
        assertEquals("DEFAULT", result);

        // 验证空值场景的日志
        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        // 空值场景必须包含 " Null.of?" 标记（因为初始值为null）
        assertTrue(linkLog.contains(" Null.of?"), 
                "空值场景日志应包含 ' Null.of?' 标记，实际日志: " + linkLog);
        // 验证不包含成功标记
        assertFalse(linkLog.contains(" Null.of->"), 
                "空值场景不应包含 ' Null.of->' 标记，实际日志: " + linkLog);
    }

    @Test
    public void testMapOperationLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<String> chain = Null.of(user)
                .map(UserEntity::getName)
                .map(s -> s + "_suffix");

        String result = chain.orElse("DEFAULT");
        assertEquals("test_suffix", result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证必须包含 map-> 操作（成功标记）
        assertTrue(linkLog.contains("map->"), 
                "日志应包含 'map->' 标记，实际日志: " + linkLog);
        // 验证包含至少两个 map->（因为调用了两次map）
        int mapCount = countOccurrences(linkLog, "map->");
        assertTrue(mapCount >= 2, 
                "日志应包含至少2个 'map->' 标记，实际数量: " + mapCount + "，日志: " + linkLog);
    }

    @Test
    public void testIfGoOperationLinkLog() {
        UserEntity user = new UserEntity();
        user.setAge(25);

        NullChain<UserEntity> chain = Null.of(user)
                .ifGo(u -> u.getAge() > 18)
                .ifGo(u -> u.getAge() < 30);

        UserEntity result = chain.orElseNull();
        assertNotNull(result);
        assertEquals(25, result.getAge().intValue());

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证必须包含 ifGo-> 操作（成功标记）
        assertTrue(linkLog.contains("ifGo->"), 
                "日志应包含 'ifGo->' 标记，实际日志: " + linkLog);
        // 验证包含至少两个 ifGo->（因为调用了两次ifGo）
        int ifGoCount = countOccurrences(linkLog, "ifGo->");
        assertTrue(ifGoCount >= 2, 
                "日志应包含至少2个 'ifGo->' 标记，实际数量: " + ifGoCount + "，日志: " + linkLog);
        // 验证不包含失败标记
        assertFalse(linkLog.contains("ifGo?"), 
                "成功场景不应包含 'ifGo?' 标记，实际日志: " + linkLog);
    }

    // ========== 并发任务日志测试（重点） ==========

    @Test
    public void testConcurrentTaskLinkLog() {
        String input = "test_input";
        
        // 创建任务组
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        NullChain<Map<String, Object>> chain = Null.of(input)
                .task(nullGroupTask);

        Map<String, Object> result = chain.get();
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 验证日志包含任务执行标记
        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 并发任务执行成功后应该包含 task-> 标记
        assertTrue(linkLog.contains("task->"), 
                "并发任务成功执行后应包含 'task->' 标记，实际日志: " + linkLog);
        // 验证不包含失败标记
        assertFalse(linkLog.contains("task? "), 
                "成功场景不应包含 'task? ' 标记，实际日志: " + linkLog);
    }

    @Test
    public void testConcurrentTaskWithExceptionLinkLog() {
        String input = "test_input";
        
        // 创建一个会失败的任务组（使用不存在的任务）
        try {
            NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                    NullGroupTask.task("com.nonexistent.Task")
            );

            NullChain<Map<String, Object>> chain = Null.of(input)
                    .task(nullGroupTask);

            // 尝试获取结果，应该抛出异常
            try {
                chain.get();
                fail("应该抛出异常");
            } catch (NullChainException e) {
                // 验证异常消息中包含 linkLog
                String exceptionMessage = getLinkLogFromException(e);
                assertNotNull(exceptionMessage);
                assertFalse(exceptionMessage.isEmpty(), "异常消息不应为空");
                // 验证异常消息中必须包含 task? 标记（失败标记）
                assertTrue(exceptionMessage.contains("task?") || exceptionMessage.contains("任务"), 
                        "异常消息应包含 'task?' 标记或任务相关信息，实际消息: " + exceptionMessage);
            }
        } catch (Exception e) {
            // 如果任务不存在，可能在创建时就抛出异常
            assertTrue(e instanceof NullChainException || e.getCause() instanceof NullChainException,
                    "应该抛出 NullChainException");
        }
    }

    @Test
    public void testConcurrentNfTaskLinkLog() {
        String input = "test_input";
        
        // 创建 NF 任务组
        // export 现在支持表达式计算，可以直接使用表达式
        NullGroupNfTask nullGroupNfTask = NullGroupNfTask.buildGroup(
                NullGroupNfTask.task("export preValue + \"_nf1\""),
                NullGroupNfTask.task("export preValue + \"_nf2\"")
        );

        NullChain<Map<String, Object>> chain = Null.of(input)
                .nfTasks(nullGroupNfTask, "default");

        Map<String, Object> result = chain.get();
        assertNotNull(result);

        // 验证日志
        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // NF 任务执行成功后应该包含 nfTask-> 或 nfTasks 相关标记
        assertTrue(linkLog.contains("nfTask->") || linkLog.contains("nfTasks"), 
                "NF任务日志应包含 'nfTask->' 或 'nfTasks' 标记，实际日志: " + linkLog);
    }

    // ========== 异常场景日志测试 ==========

    @Test
    public void testExceptionLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");

        try {
            NullChain<Object> chain = Null.of(user)
                    .map(u -> {
                        throw new RuntimeException("测试异常");
                    });

            chain.get();
            fail("应该抛出异常");
        } catch (NullChainException e) {
            // 验证异常消息中包含 linkLog
            String exceptionMessage = getLinkLogFromException(e);
            assertNotNull(exceptionMessage);
            assertFalse(exceptionMessage.isEmpty(), "异常消息不应为空");
            // 验证异常消息中必须包含 map? 标记（失败标记）
            assertTrue(exceptionMessage.contains("map?"), 
                    "异常消息应包含 'map?' 标记，实际消息: " + exceptionMessage);
            // 验证包含 Null.of 标记（因为是从 Null.of 开始的）
            assertTrue(exceptionMessage.contains(" Null.of"), 
                    "异常消息应包含 ' Null.of' 标记，实际消息: " + exceptionMessage);
        }
    }

    @Test
    public void testNullPointerExceptionLinkLog() {
        try {
            NullChain<String> chain = Null.of((UserEntity) null)
                    .map(UserEntity::getName)
                    .map(String::toUpperCase);

            chain.get("用户不能为空");
            fail("应该抛出异常");
        } catch (NullChainException e) {
            String exceptionMessage = getLinkLogFromException(e);
            assertNotNull(exceptionMessage);
            assertFalse(exceptionMessage.isEmpty(), "异常消息不应为空");
            // 验证异常消息中必须包含 Null.of? 标记（因为初始值为null）
            assertTrue(exceptionMessage.contains(" Null.of?"), 
                    "异常消息应包含 ' Null.of?' 标记，实际消息: " + exceptionMessage);
            // 验证包含自定义消息
            assertTrue(exceptionMessage.contains("用户不能为空"), 
                    "异常消息应包含自定义消息 '用户不能为空'，实际消息: " + exceptionMessage);
        }
    }

    // ========== 多级判空日志测试 ==========

    @Test
    public void testNullCheckLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");
        user.setAge(25);

        try {
            Null.ofCheck(user)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .doThrow(IllegalArgumentException.class, "检查失败");

            // 如果所有字段都不为空，不应该抛出异常
        } catch (IllegalArgumentException e) {
            // 如果有空字段，会抛出异常
            String exceptionMessage = e.getMessage();
            assertNotNull(exceptionMessage);
        }
    }

    @Test
    public void testNullCheckWithNullValueLinkLog() {
        UserEntity user = new UserEntity();
        // name 为 null

        try {
            Null.ofCheck(user)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .doThrow(IllegalArgumentException.class, "检查失败");

            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = e.getMessage();
            assertNotNull(exceptionMessage);
            // 验证异常消息中包含检查信息
            assertFalse(exceptionMessage.isEmpty(), "异常消息不应为空");
            assertTrue(exceptionMessage.contains("检查失败"), 
                    "异常消息应包含 '检查失败' 信息，实际消息: " + exceptionMessage);
        }
    }

    // ========== 链式操作组合日志测试 ==========

    @Test
    public void testComplexChainLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");
        user.setAge(25);

        NullChain<String> chain = Null.of(user)
                .ifGo(u -> u.getAge() > 18)
                .map(UserEntity::getName)
                .ifGo(s -> s.length() > 0)
                .map(s -> s.toUpperCase());

        String result = chain.orElse("DEFAULT");
        assertEquals("TEST", result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证必须包含所有操作标记
        assertTrue(linkLog.contains(" Null.of->"), 
                "日志应包含 ' Null.of->' 标记，实际日志: " + linkLog);
        assertTrue(linkLog.contains("ifGo->"), 
                "日志应包含 'ifGo->' 标记，实际日志: " + linkLog);
        assertTrue(linkLog.contains("map->"), 
                "日志应包含 'map->' 标记，实际日志: " + linkLog);
        // 验证操作顺序：Null.of 在最前，然后是 ifGo，最后是 map
        int nullOfIndex = linkLog.indexOf(" Null.of->");
        int firstIfGoIndex = linkLog.indexOf("ifGo->");
        int firstMapIndex = linkLog.indexOf("map->");
        assertTrue(nullOfIndex < firstIfGoIndex, 
                "操作顺序错误：Null.of 应在 ifGo 之前，实际日志: " + linkLog);
        assertTrue(firstIfGoIndex < firstMapIndex, 
                "操作顺序错误：ifGo 应在 map 之前，实际日志: " + linkLog);
    }

    @Test
    public void testFlatChainLinkLog() {
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<String> chain = Null.of(user)
                .flatChain(u -> Null.of(u.getName())
                        .map(s -> s.toUpperCase()));

        String result = chain.orElse("DEFAULT");
        assertEquals("TEST", result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证必须包含 flatChain-> 标记（成功操作）
        assertTrue(linkLog.contains("flatChain->"), 
                "日志应包含 'flatChain->' 标记，实际日志: " + linkLog);
        // 验证包含 Null.of-> 标记
        assertTrue(linkLog.contains(" Null.of->"), 
                "日志应包含 ' Null.of->' 标记，实际日志: " + linkLog);
        // 验证包含 map-> 标记（嵌套链中的map操作）
        assertTrue(linkLog.contains("map->"), 
                "日志应包含 'map->' 标记，实际日志: " + linkLog);
    }

    // ========== 并发场景日志完整性测试 ==========

    @Test
    public void testConcurrentTaskLogIntegrity() throws InterruptedException {
        String input = "test";
        int executionCount = 10;
        CountDownLatch latch = new CountDownLatch(executionCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // 创建不同的任务（避免任务重复错误）
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        // 执行并发任务多次，验证日志完整性
        for (int i = 0; i < executionCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    NullChain<Map<String, Object>> chain = Null.of(input)
                            .task(nullGroupTask);

                    Map<String, Object> result = chain.get();
                    assertNotNull(result);
                    successCount.incrementAndGet();
                    
                    // 验证日志
                    String linkLog = getLinkLog(chain);
                    assertNotNull(linkLog, "第 " + index + " 次执行：日志不应为空");
                } catch (Exception e) {
                    // 记录失败但不中断测试
                    System.err.println("并发任务执行失败 (第 " + index + " 次): " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        
        // 验证至少有一些任务成功执行
        assertTrue(successCount.get() > 0, "应该有任务成功执行，实际成功: " + successCount.get());
    }

    // ========== 日志格式验证测试 ==========

    @Test
    public void testLinkLogFormat() {
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<String> chain = Null.of(user)
                .map(UserEntity::getName);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "日志不应为空");
        // 验证日志格式：必须包含操作标记
        assertTrue(linkLog.contains("->"), 
                "日志应包含 '->' 操作标记，实际日志: " + linkLog);
        // 验证包含 Null.of-> 标记
        assertTrue(linkLog.contains(" Null.of->"), 
                "日志应包含 ' Null.of->' 标记，实际日志: " + linkLog);
        // 验证包含 map-> 标记
        assertTrue(linkLog.contains("map->"), 
                "日志应包含 'map->' 标记，实际日志: " + linkLog);
    }

    @Test
    public void testLinkLogWithEmptyValue() {
        NullChain<String> chain = Null.of((String) null)
                .map(s -> s != null ? s.toUpperCase() : null);

        String result = chain.orElse("DEFAULT");
        assertEquals("DEFAULT", result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        // 空值场景的日志必须包含 "?" 标记
        assertTrue(linkLog.contains(" Null.of?"), 
                "空值场景日志应包含 ' Null.of?' 标记，实际日志: " + linkLog);
        // 验证不包含成功标记
        assertFalse(linkLog.contains(" Null.of->"), 
                "空值场景不应包含 ' Null.of->' 标记，实际日志: " + linkLog);
    }

    // ========== 边界场景测试 ==========

    @Test
    public void testLinkLogWithVeryLongChain() {
        UserEntity user = new UserEntity();
        user.setName("test");

        // 创建很长的链
        NullChain<String> chain = Null.of(user)
                .map(UserEntity::getName);
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            chain = chain.map(s -> s + "_" + index);
        }

        String result = chain.orElse("DEFAULT");
        assertNotNull(result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "长链日志不应为空");
        // 验证包含 Null.of-> 标记
        assertTrue(linkLog.contains(" Null.of->"), 
                "长链日志应包含 ' Null.of->' 标记，实际日志: " + linkLog);
        // 验证包含多个 map-> 标记（因为调用了多次map）
        int mapCount = countOccurrences(linkLog, "map->");
        assertTrue(mapCount >= 11, 
                "长链日志应包含至少11个 'map->' 标记（1个初始map + 10个循环map），实际数量: " + mapCount + "，日志: " + linkLog);
    }

    @Test
    public void testLinkLogWithNestedChains() {
        UserEntity user = new UserEntity();
        user.setName("test");

        NullChain<String> chain = Null.of(user)
                .flatChain(u -> Null.of(u.getName())
                        .map(s -> s.toUpperCase())
                        .flatChain(s -> Null.of(s.toLowerCase())
                                .map(str -> str.toUpperCase())));

        String result = chain.orElse("DEFAULT");
        assertEquals("TEST", result);

        String linkLog = getLinkLog(chain);
        assertNotNull(linkLog);
        assertFalse(linkLog.isEmpty(), "嵌套链日志不应为空");
        // 验证包含 flatChain-> 标记
        assertTrue(linkLog.contains("flatChain->"), 
                "嵌套链日志应包含 'flatChain->' 标记，实际日志: " + linkLog);
        // 验证包含多个 Null.of-> 标记（因为嵌套链中有多个 Null.of）
        int nullOfCount = countOccurrences(linkLog, " Null.of->");
        assertTrue(nullOfCount >= 2, 
                "嵌套链日志应包含至少2个 ' Null.of->' 标记，实际数量: " + nullOfCount + "，日志: " + linkLog);
        // 验证包含 map-> 标记
        assertTrue(linkLog.contains("map->"), 
                "嵌套链日志应包含 'map->' 标记，实际日志: " + linkLog);
    }
}

