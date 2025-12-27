package com.gitee.huanminabc.test.nullchain.common;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullUtil工具类测试
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullUtilTest {

    // ========== is() 方法测试 ==========

    @Test
    public void testIsWithNull() {
        assertTrue(Null.is(null));
    }

    @Test
    public void testIsWithPrimitive() {
        assertFalse(Null.is(123));
        assertFalse(Null.is(123L));
        assertFalse(Null.is(123.45));
        assertFalse(Null.is(true));
    }

    @Test
    public void testIsWithEmptyString() {
        assertTrue(Null.is(""));
        assertTrue(Null.is("   "));
        assertTrue(Null.is("null"));
        assertTrue(Null.is("NULL"));
        assertFalse(Null.is("test"));
    }

    @Test
    public void testIsWithCollection() {
        assertTrue(Null.is(new ArrayList<>()));
        assertFalse(Null.is(Arrays.asList("a", "b")));
    }

    @Test
    public void testIsWithMap() {
        assertTrue(Null.is(new HashMap<>()));
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertFalse(Null.is(map));
    }

    @Test
    public void testIsWithArray() {
        // 对象数组测试
        assertTrue(Null.is(new String[0]));
        assertFalse(Null.is(new String[]{"a", "b"}));
        
        // 基本类型数组测试 - 修复空数组判断问题
        assertTrue(Null.is(new byte[0]));
        assertFalse(Null.is(new byte[]{1, 2, 3}));
        
        assertTrue(Null.is(new int[0]));
        assertFalse(Null.is(new int[]{1, 2, 3}));
        
        assertTrue(Null.is(new long[0]));
        assertFalse(Null.is(new long[]{1L, 2L, 3L}));
        
        assertTrue(Null.is(new short[0]));
        assertFalse(Null.is(new short[]{1, 2, 3}));
        
        assertTrue(Null.is(new float[0]));
        assertFalse(Null.is(new float[]{1.0f, 2.0f, 3.0f}));
        
        assertTrue(Null.is(new double[0]));
        assertFalse(Null.is(new double[]{1.0, 2.0, 3.0}));
        
        assertTrue(Null.is(new char[0]));
        assertFalse(Null.is(new char[]{'a', 'b', 'c'}));
        
        assertTrue(Null.is(new boolean[0]));
        assertFalse(Null.is(new boolean[]{true, false}));
        
        // 包装类型数组测试 - 验证包装类型数组也能正确判断
        assertTrue(Null.is(new Byte[0]));
        assertFalse(Null.is(new Byte[]{1, 2, 3}));
        
        assertTrue(Null.is(new Integer[0]));
        assertFalse(Null.is(new Integer[]{1, 2, 3}));
        
        assertTrue(Null.is(new Long[0]));
        assertFalse(Null.is(new Long[]{1L, 2L, 3L}));
        
        assertTrue(Null.is(new Short[0]));
        assertFalse(Null.is(new Short[]{1, 2, 3}));
        
        assertTrue(Null.is(new Float[0]));
        assertFalse(Null.is(new Float[]{1.0f, 2.0f, 3.0f}));
        
        assertTrue(Null.is(new Double[0]));
        assertFalse(Null.is(new Double[]{1.0, 2.0, 3.0}));
        
        assertTrue(Null.is(new Character[0]));
        assertFalse(Null.is(new Character[]{'a', 'b', 'c'}));
        
        assertTrue(Null.is(new Boolean[0]));
        assertFalse(Null.is(new Boolean[]{true, false}));
    }

    // ========== isAny() 方法测试 ==========

    @Test
    public void testIsAnyWithNull() {
        assertTrue(Null.isAny((Object[]) null));
        assertTrue(Null.isAny());
    }

    @Test
    public void testIsAnyWithEmpty() {
        assertTrue(Null.isAny("", "test"));
        assertTrue(Null.isAny("test", ""));
        assertFalse(Null.isAny("test1", "test2"));
    }

    @Test
    public void testIsAnyWithNullValue() {
        assertTrue(Null.isAny("test", null));
        assertTrue(Null.isAny(null, "test"));
        assertFalse(Null.isAny("test1", "test2"));
    }

    // ========== isAll() 方法测试 ==========

    @Test
    public void testIsAllWithNull() {
        assertTrue(Null.isAll((Object[]) null));
    }

    @Test
    public void testIsAll() {
        assertTrue(Null.isAll("", null, ""));
        assertFalse(Null.isAll("", "test", ""));
    }

    // ========== non() 方法测试 ==========

    @Test
    public void testNon() {
        assertTrue(Null.non("test"));
        assertFalse(Null.non(""));
        assertFalse(Null.non(null));
    }

    // ========== nonAll() 方法测试 ==========

    @Test
    public void testNonAll() {
        assertTrue(Null.nonAll("test1", "test2"));
        assertFalse(Null.nonAll("test1", ""));
        assertFalse(Null.nonAll((Object[]) null));
    }

    // ========== eq() 方法测试 ==========

    @Test
    public void testEq() {
        assertTrue(Null.eq("test", "test"));
        assertFalse(Null.eq("test1", "test2"));
        assertFalse(Null.eq(null, "test"));
        assertFalse(Null.eq("test", null));
        assertFalse(Null.eq(null, null));
    }

    @Test
    public void testEqWithSameObject() {
        String str = "test";
        assertTrue(Null.eq(str, str));
    }

    @Test
    public void testEqWithNullChain() {
        String str = "test";
        assertTrue(Null.eq(Null.of(str), Null.of(str)));
    }

    // ========== eqAny() 方法测试 ==========

    @Test
    public void testEqAny() {
        assertTrue(Null.eqAny("test", "test1", "test", "test2"));
        assertFalse(Null.eqAny("test", "test1", "test2"));
        assertFalse(Null.eqAny(null, "test1", "test2"));
    }

    // ========== notEq() 方法测试 ==========

    @Test
    public void testNotEq() {
        assertTrue(Null.notEq("test1", "test2"));
        assertFalse(Null.notEq("test", "test"));
        assertFalse(Null.notEq(null, "test"));
    }

    // ========== notEqAll() 方法测试 ==========

    @Test
    public void testNotEqAll() {
        assertTrue(Null.notEqAll("test", "test1", "test2"));
        assertFalse(Null.notEqAll("test", "test1", "test"));
    }


    // ========== orElse() 方法测试 ==========

    @Test
    public void testOrElseWithValue() {
        String result = Null.orElse("test", "default");
        assertEquals("test", result);
    }

    @Test
    public void testOrElseWithNull() {
        String result = Null.orElse(null, "default");
        assertEquals("default", result);
    }

    @Test
    public void testOrElseWithNullDefault() {
        assertThrows(NullChainException.class, () -> {
            Null.orElse(null, (String) null);
        });
    }

    @Test
    public void testOrElseWithSupplier() {
        String result = Null.orElse("test", () -> "default");
        assertEquals("test", result);
    }

    @Test
    public void testOrElseWithSupplierAndNull() {
        String result = Null.orElse(null, () -> "default");
        assertEquals("default", result);
    }

    @Test
    public void testOrElseWithNullSupplier() {
        assertThrows(NullChainException.class, () -> {
            java.util.function.Supplier<String> supplier = null;
            Null.orElse(null, supplier);
        });
    }

    @Test
    public void testOrElseWithSupplierReturningNull() {
        assertThrows(NullChainException.class, () -> {
            Null.orElse(null, () -> null);
        });
    }

    // ========== orThrow() 方法测试 ==========

    @Test
    public void testOrThrowWithValue() {
        String result = Null.orThrow("test", () -> new RuntimeException("error"));
        assertEquals("test", result);
    }

    @Test
    public void testOrThrowWithNull() {
        assertThrows(RuntimeException.class, () -> {
            Null.orThrow(null, () -> new RuntimeException("error"));
        });
    }

    // ========== checkNull() 方法测试 ==========

    @Test
    public void testCheckNullWithValue() {
        assertDoesNotThrow(() -> {
            Null.checkNull("test");
        });
    }

    @Test
    public void testCheckNullWithNull() {
        assertThrows(NullChainException.class, () -> {
            Null.checkNull(null);
        });
    }

    @Test
    public void testCheckNullWithMessage() {
        assertThrows(NullChainException.class, () -> {
            Null.checkNull(null, "参数不能为空");
        });
    }

    @Test
    public void testCheckNullWithMessageAndParams() {
        assertThrows(NullChainException.class, () -> {
            Null.checkNull(null, "参数{}不能为空", "name");
        });
    }

    @Test
    public void testCheckNullWithSupplier() {
        assertThrows(RuntimeException.class, () -> {
            Null.checkNull(null,  () -> new RuntimeException( "参数不能为空"));
        });
    }

    // ========== orEmpty() 方法测试 ==========
    // 注意：orEmpty和createEmpty方法要求类型必须实现NullCheck接口
    // UserEntity不实现NullCheck接口，所以这些测试暂时跳过
}

