package com.gitee.huanminabc.test.nullchain.leaf.http.websocket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

/**
 * WebSocket 测试基类
 * 
 * <p>提供所有 WebSocket 测试类的公共常量和初始化方法。</p>
 * 
 * <p>运行前需要启动测试服务器：</p>
 * <pre>
 * cd null-chain-test/src/test/resources
 * node websocket-server.js
 * </pre>
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public abstract class WebSocketBaseTest {
    
    /** WebSocket 服务器基础 URL */
    protected static final String BASE_URL = "ws://localhost:3001";
    
    /** 默认超时时间（秒） */
    protected static final int TIMEOUT_SECONDS = 10;
    
    @BeforeAll
    public static void checkServer() throws InterruptedException {
        // 检查服务器是否运行
        // 这里可以添加服务器健康检查逻辑
        Thread.sleep(1000); // 等待服务器启动
    }
}

