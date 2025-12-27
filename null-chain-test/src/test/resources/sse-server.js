const http = require('http');

// 定义随机JSON数据的数据源（用于生成随机内容）
const randomMessages = [
    "用户登录成功",
    "新订单创建",
    "数据同步完成",
    "系统资源预警",
    "用户注销操作",
    "文件上传成功",
    "接口调用超时",
    "数据库备份完成"
];
const randomTypes = [
    "info",
    "success",
    "warning",
    "error"
];

const server = http.createServer((req, res) => {
    if (req.url === '/sse') {
        // 1. 规范的SSE响应头配置（含CORS跨域支持，更通用）
        res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('Access-Control-Allow-Origin', '*'); // 允许所有跨域请求，可按需限制
        res.flushHeaders(); // 立即发送响应头，建立长连接

        // 用于存储定时器ID，方便后续清理
        let timeoutId = null;

        // 2. 核心：随机发送JSON数据的递归函数
        const sendRandomSSE = () => {
            // 生成随机数据
            const randomJson = {
                id: Math.floor(Math.random() * 10000), // 随机ID
                timestamp: new Date().toISOString(), // 时间戳
                message: randomMessages[Math.floor(Math.random() * randomMessages.length)], // 随机消息
                type: randomTypes[Math.floor(Math.random() * randomTypes.length)], // 随机消息类型
                value: Math.floor(Math.random() * 100), // 随机数值
                status: Math.random() > 0.5 ? "active" : "inactive" // 随机状态
            };

            // 3. 规范的SSE消息格式（支持id/event/data字段，兼容所有SSE客户端）
            // id：消息唯一标识，客户端可通过lastEventId重连
            // event：自定义事件类型，客户端可按需监听
            // data：消息体（JSON字符串格式）
            const sseMessage = [
                `id: ${Date.now()}`, // 用时间戳作为唯一ID，更可靠
                `event: randomJsonEvent`, // 自定义事件名
                `data: ${JSON.stringify(randomJson)}`,
            ].join('\n');

            // 发送SSE消息
            try {
                res.write(sseMessage+'\n\n'); //必须以\n\n结尾
                console.log('发送随机JSON：', randomJson);
            } catch (err) {
                // 客户端断开连接时，写入会报错，直接清理资源
                clearTimeout(timeoutId);
                return;
            }

            // 4. 随机延迟（1000ms ~ 5000ms 之间，可按需调整范围）
            const randomDelay = Math.floor(Math.random() * 4000) + 1000;
            // 递归设置定时器，实现无限随机间隔发送
            timeoutId = setTimeout(sendRandomSSE, randomDelay);
        };

        // 立即执行第一次发送
        sendRandomSSE();

        // 5. 客户端断开连接时，清理定时器（防止内存泄漏）
        const cleanUp = () => {
            clearTimeout(timeoutId);
            res.end();
            console.log('客户端断开SSE连接，已清理资源');
        };

        req.on('close', cleanUp); // 客户端正常断开
        req.on('abort', cleanUp); // 客户端异常中断
    } else {
        res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('规范SSE服务运行中，端点：/sse');
    }
});

const PORT = 3000;
server.listen(PORT, () => {
    console.log(`规范SSE服务已启动：http://localhost:${PORT}`);
    console.log(`SSE端点：http://localhost:${PORT}/sse`);
});