package com.gitee.huanminabc.test.nullchain.task;

import com.gitee.huanminabc.jcommon.test.PathUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.task.TestTask;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null 任务功能测试
 * 
 * 测试任务相关的功能，包括：
 * - 单个任务执行
 * - 任务组执行
 * - NF 文件任务
 * - 动态加载任务类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullTaskTest {

    // ========== 单个任务测试 ==========

    @Test
    public void testSingleTask() {
        String input = "123131";
        String result = Null.of(input)
                .task(TestTask.class, "123", false)
                .get();
        assertNotNull(result);
    }

    // ========== 任务组测试 ==========

    @Test
    public void testTaskGroup() {
        String input = "123131";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(TestTask.class.getName(), "123213"),
                NullGroupTask.task(Test2Task.class.getName())
        );
        
        Map<String, Object> result = (Map<String, Object>) Null.of(input)
                .task(nullGroupTask)
                .get();
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 验证任务组返回的结果包含任务结果
        assertTrue(result.containsKey(TestTask.class.getName()) || 
                   result.containsKey(Test2Task.class.getName()));
    }

    @Test
    public void testTaskGroupWithMultipleTasks() {
        String preValue = "123131";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        Map<String, Object> result = (Map<String, Object>) Null.of(preValue)
                .task(nullGroupTask)
                .get();
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ========== NF 文件任务测试 ==========

    @Test
    public void testNfFileTask() {
        String file = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/test.nf");
        String input = "123131";
        String result = Null.of(input)
                .nfTask(NullGroupNfTask.taskFile(file))
                .type(String.class)
                .get();
        assertNotNull(result);
    }

    // ========== 动态加载任务类测试 ==========

    @Test
    public void testLoadTaskClass() {
        String preValue = "123131";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        // 第一次执行
        Map<String, Object> result1 = Null.of(preValue)
                .task(nullGroupTask)
                .get();
        assertNotNull(result1);

        // 动态加载任务类
        String classPath = "com.gitee.huanminabc.test.nullchain.task.Test1Task";
        String currentProjectTargetClassAbsolutePath = PathUtil.getCurrentProjectTargetTestClassAbsolutePath(
                classPath.replace(".", "/")) + ".class";
        
        // 加载任务类（用于热更新场景）
        NullTaskFactory.loadTaskClass(classPath, currentProjectTargetClassAbsolutePath);
        
        // 再次执行
        Map<String, Object> result2 = (Map<String, Object>) Null.of(preValue)
                .task(nullGroupTask)
                .get();
        assertNotNull(result2);
    }

    // ========== 任务参数测试 ==========

    @Test
    public void testTaskWithParams() {
        String input = "test";
        String result = Null.of(input)
                .task(TestTask.class, "param1", 123, true)
                .get();
        assertNotNull(result);
    }

    @Test
    public void testTaskWithNullInput() {
        String result = Null.of((String) null)
                .task(TestTask.class, "param")
                .orElse("default");
        assertEquals("default", result);
    }
}

