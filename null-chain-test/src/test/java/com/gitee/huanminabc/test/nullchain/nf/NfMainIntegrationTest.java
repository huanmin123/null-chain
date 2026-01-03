package com.gitee.huanminabc.test.nullchain.nf;

import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NfMain 集成测试类
 * 合并了缓存功能测试和集成测试
 * 测试性能监控、缓存和脚本执行的集成使用
 * 
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NfMainIntegrationTest {
    

    // ========== 基本功能测试 ==========
    
    @Test
    public void testNormalExecution() {
        String script = "Integer a = 1\n" +
            "String b = \"test\"\n" +
            "echo a\n" +
            "export a\n";
        
        // 正常执行，不使用性能监控
        assertDoesNotThrow(() -> {
            Object result = NfMain.run(script, log, null);
            assertNotNull(result);
            assertEquals(1, result);
        });
    }
    
    @Test
    public void testPerformanceMonitoring() {
        String script = "Integer sum = 0\n" +
            "for i in 1..100 {\n" +
            "    sum = sum + i\n" +
            "}\n" +
            "echo sum\n" +
            "echo sum\n" +
            "export sum\n";
        
        // 启用性能监控
        Object result1 = NfMain.run(script, log, null, false);//预热
        //只能统计第一层的代码  而循环里面的代码不会统计(但是会算总时间)
        Object result = NfMain.run(script, log, null, true);

        assertNotNull(result);
        // 性能报告应该已经输出到日志
    }

    


}
