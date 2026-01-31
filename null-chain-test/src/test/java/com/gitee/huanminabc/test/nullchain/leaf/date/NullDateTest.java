package com.gitee.huanminabc.test.nullchain.leaf.date;

import com.gitee.huanminabc.jcommon.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.jcommon.enums.DateFormatEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null 日期操作测试
 * 
 * 测试日期相关的功能，包括：
 * - dateOffset 日期偏移
 * - dateFormat 日期格式化
 * - dateBetween 日期差值计算
 * - dateCompare 日期比较
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullDateTest {

    // ========== dateOffset() 方法测试 ==========

    @Test
    public void testDateOffsetAdd() {
        Date now = new Date();
        String result = Null.of(now)
                .dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum.ADD, 1, TimeEnum.DAYS)
                .dateFormat(DateFormatEnum.DATETIME_PATTERN)
                .get();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testDateOffsetStart() {
        Date now = new Date();
        String result = Null.of(now)
                .dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum.START, 1, TimeEnum.MONTHS)
                .dateFormat(DateFormatEnum.DATETIME_PATTERN)
                .get();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testDateOffsetEnd() {
        Date now = new Date();
        String result = Null.of(now)
                .dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum.END, 1, TimeEnum.MONTHS)
                .dateFormat(DateFormatEnum.DATETIME_PATTERN)
                .get();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testDateOffsetWithHours() {
        Date now = new Date();
        NullChain<Date> dateChain = Null.of(now)
                .dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum.START, TimeEnum.HOURS);
        
        Date dateStart = dateChain.get();
        assertNotNull(dateStart);
        
        Date dateEnd = dateChain
                .dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum.SUB, 1, TimeEnum.HOURS)
                .get();
        assertNotNull(dateEnd);
        assertTrue(dateEnd.before(dateStart) || dateEnd.equals(dateStart));
    }

    // ========== dateFormat() 方法测试 ==========

    @Test
    public void testDateFormat() {
        Date now = new Date();
        String result = Null.of(now)
                .dateFormat(DateFormatEnum.DATETIME_PATTERN)
                .get();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ========== dateBetween() 方法测试 ==========

    @Test
    public void testDateBetween() {
        Long seconds = Null.of("2025-12-27")
                .dateBetween("2025-12-31", TimeEnum.SECONDS)
                .get();
        assertNotNull(seconds);
        assertTrue(seconds > 0);
        // 4 天的秒数应该是 4 * 24 * 60 * 60 = 345600
        assertEquals(345600L, seconds);
    }

    @Test
    public void testDateBetweenWithLocalDate() throws NullChainCheckException {
        LocalDate date1 = LocalDate.of(2025, 12, 24);
        LocalDate date2 = LocalDate.now();
        Integer compare = Null.of(date1)
                .dateCompare(date2)
                .getSafe();
        assertNotNull(compare);
    }

    // ========== dateCompare() 方法测试 ==========

    @Test
    public void testDateCompare() throws NullChainCheckException {
        // 比较两个日期
        String dateStr = "2025-12-24";
        Integer compare = Null.of(dateStr)
                .dateCompare(LocalDate.now())
                .getSafe();
        assertNotNull(compare);
    }

    @Test
    public void testDateCompareWithNull() {
        assertThrows(NullChainCheckException.class, () -> {
            Null.of((String) null)
                    .dateCompare(LocalDate.now())
                    .getSafe();
        });
    }
}

