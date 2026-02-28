package com.gitee.huanminabc.test.nullchain.common;

import com.gitee.huanminabc.nullchain.common.NullTaskList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullTaskList 基础行为测试
 */
public class NullTaskListTest {

    @Test
    public void testNullNodeConstructorWithNullValue() {
        NullTaskList.NullNode<Object> node = new NullTaskList.NullNode<>(null);
        assertTrue(node.isNull);
        assertNull(node.value);
    }

    @Test
    public void testNullNodeConstructorWithNonNullValue() {
        NullTaskList.NullNode<String> node = new NullTaskList.NullNode<>("ok");
        assertFalse(node.isNull);
        assertEquals("ok", node.value);
    }
}
