package com.gitee.huanminabc.test.nullchain.leaf.calculate;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullCalculate计算链测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullCalculateTest {

    // ========== ofCalc() 方法测试 ==========

    @Test
    public void testOfCalcWithInteger() {
        BigDecimal result = Null.ofCalc(100).map(bd -> bd).get();
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    public void testOfCalcWithLong() {
        BigDecimal result = Null.ofCalc(100L).map(bd -> bd).get();
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    public void testOfCalcWithDouble() {
        BigDecimal result = Null.ofCalc(100.5).map(bd -> bd).get();
        assertEquals(new BigDecimal("100.5"), result);
    }

    @Test
    public void testOfCalcWithNull() {
        assertTrue(Null.ofCalc((Number) null).map(bd -> bd).is());
    }

    @Test
    public void testOfCalcWithNullChain() {
        NullChain<Integer> chain = Null.of(100);
        BigDecimal result = Null.ofCalc(chain).map(bd -> bd).get();
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    public void testOfCalcWithNullChainNull() {
        NullChain<Integer> chain = Null.of((Integer) null);
        assertTrue(Null.ofCalc(chain).map(bd -> bd).is());
    }

    // ========== add() 方法测试 ==========

    @Test
    public void testAdd() {
        BigDecimal result = Null.ofCalc(10)
                .add(5)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("15"), result);
    }

    @Test
    public void testAddWithDecimal() {
        BigDecimal result = Null.ofCalc(10.5)
                .add(5.3)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("15.8"), result);
    }

    @Test
    public void testAddWithNull() {
        assertTrue(Null.ofCalc(10)
                .add(null)
                .map(bd -> bd)
                .is());
    }

    // ========== sub() 方法测试 ==========

    @Test
    public void testSub() {
        BigDecimal result = Null.ofCalc(10)
                .sub(5)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    public void testSubWithDecimal() {
        BigDecimal result = Null.ofCalc(10.5)
                .sub(5.3)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("5.2"), result);
    }

    @Test
    public void testSubWithNull() {
        assertTrue(Null.ofCalc(10)
                .sub(null)
                .map(bd -> bd)
                .is());
    }

    // ========== mul() 方法测试 ==========

    @Test
    public void testMul() {
        BigDecimal result = Null.ofCalc(10)
                .mul(5)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("50"), result);
    }

    @Test
    public void testMulWithDecimal() {
        BigDecimal result = Null.ofCalc(10.5)
                .mul(2)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("21.0"), result);
    }

    @Test
    public void testMulWithNull() {
        assertTrue(Null.ofCalc(10)
                .mul(null)
                .map(bd -> bd)
                .is());
    }

    // ========== div() 方法测试 ==========

    @Test
    public void testDiv() {
        BigDecimal result = Null.ofCalc(10)
                .div(5)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("2"), result);
    }

    @Test
    public void testDivWithDecimal() {
        BigDecimal result = Null.ofCalc(10.5)
                .div(2)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("5.25"), result);
    }

    @Test
    public void testDivWithNull() {
        assertTrue(Null.ofCalc(10)
                .div(null)
                .map(bd -> bd)
                .is());
    }

    @Test
    public void testDivWithZero() {
        assertThrows(Exception.class, () -> {
            Null.ofCalc(10)
                    .div(0)
                    .map(bd -> bd)
                    .get();
        });
    }

    // ========== 链式调用测试 ==========

    @Test
    public void testChain() {
        BigDecimal result = Null.ofCalc(10)
                .add(5)
                .mul(2)
                .sub(10)
                .div(2)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    public void testChainWithMap() {
        Double result = Null.ofCalc(10)
                .add(5)
                .mul(2)
                .sub(10)
                .div(2)
                .map(BigDecimal::doubleValue)
                .get();
        assertEquals(10.0, result);
    }

    // ========== 边界情况测试 ==========

    @Test
    public void testWithZero() {
        BigDecimal result = Null.ofCalc(0).map(bd -> bd).get();
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void testWithNegative() {
        BigDecimal result = Null.ofCalc(-10)
                .add(5)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("-5"), result);
    }

    @Test
    public void testWithLargeNumber() {
        BigDecimal result = Null.ofCalc(1000000)
                .mul(1000000)
                .map(bd -> bd)
                .get();
        assertEquals(new BigDecimal("1000000000000"), result);
    }

    // ========== 精度测试 ==========

    @Test
    public void testPrecision() {
        BigDecimal result = Null.ofCalc(1)
                .div(3)
                .map(bd -> bd)
                .get();
        assertNotNull(result);
        assertTrue(result.compareTo(new BigDecimal("0.33")) > 0);
    }
}

