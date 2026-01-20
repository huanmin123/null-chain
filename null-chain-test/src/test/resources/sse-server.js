const http = require('http');

/**
 * 解析请求体（Node.js http模块默认不解析请求体，需要手动收集数据流）
 * @param {http.IncomingMessage} req - 请求对象
 * @returns {Promise<string>} 请求体字符串
 */
function parseRequestBody(req) {
    return new Promise((resolve, reject) => {
        let body = '';
        // 监听数据事件，收集数据块
        req.on('data', (chunk) => {
            body += chunk.toString();
        });
        // 数据接收完成
        req.on('end', () => {
            resolve(body);
        });
        // 处理错误
        req.on('error', (err) => {
            reject(err);
        });
    });
}

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

const server = http.createServer(async (req, res) => {
    if (req.url === '/sse') {
        // 解析请求体
        let requestBody = null;
        try {
            if (req.method === 'POST') {
                const bodyStr = await parseRequestBody(req);
                if (bodyStr) {
                    try {
                        requestBody = JSON.parse(bodyStr);
                    } catch (e) {
                        console.log('请求体不是有效的JSON格式:', bodyStr);
                        requestBody = bodyStr;
                    }
                }
            }
        } catch (err) {
            console.error('解析请求体失败:', err);
        }

        // 打印请求头信息
        console.log('请求头：', req.headers);
        console.log('请求方法:', req.method);
        console.log('请求体:', requestBody);

        // 1. 规范的SSE响应头配置（含CORS跨域支持，更通用）
        res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('Access-Control-Allow-Origin', '*'); // 允许所有跨域请求，可按需限制
        res.flushHeaders(); // 立即发送响应头，建立长连接

        // 用于存储定时器ID，方便后续清理
        let timeoutId = null;
        let messageId = 0; // 消息ID计数器

        // 2. 核心：随机发送JSON数据的递归函数
        const sendRandomSSE = () => {
            messageId++;
            // 生成随机数据
            const randomJson = {
                id: messageId, // 递增的ID，用于Last-Event-ID测试
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
                `id: ${messageId}`, // 递增的ID
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
    } else if (req.url === '/sse-text') {
        // 返回纯文本数据的SSE端点，用于测试toSSEText
        res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.flushHeaders();

        let timeoutId = null;
        let messageId = 0;

        const sendTextSSE = () => {
            messageId++;
            const textMessage = `这是第 ${messageId} 条文本消息 - ${new Date().toLocaleTimeString()}`;

            const sseMessage = [
                `id: ${messageId}`,
                `event: textEvent`,
                `data: ${textMessage}`,
            ].join('\n');

            try {
                res.write(sseMessage + '\n\n');
                console.log('发送文本消息 ID:', messageId);
            } catch (err) {
                clearTimeout(timeoutId);
                return;
            }

            // 固定延迟2秒
            timeoutId = setTimeout(sendTextSSE, 2000);
        };

        sendTextSSE();

        const cleanUp = () => {
            clearTimeout(timeoutId);
            res.end();
            console.log('客户端断开SSE文本连接');
        };

        req.on('close', cleanUp);
        req.on('abort', cleanUp);
    } else if (req.url === '/sse-reconnect') {
        // 支持Last-Event-ID的端点，用于测试重连和断点续传
        const lastEventId = req.headers['last-event-id'];
        let startId = 1;
        let isReconnect = false;
        
        if (lastEventId) {
            startId = parseInt(lastEventId, 10) + 1;
            isReconnect = true;
            console.log(`收到Last-Event-ID: ${lastEventId}，从ID ${startId}开始发送（重连）`);
        } else {
            console.log('新的SSE重连测试连接');
        }

        res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.flushHeaders();

        let timeoutId = null;
        let messageId = startId - 1;
        // 重连后重置messageCount，确保重连后继续发送消息
        let messageCount = isReconnect ? 0 : 0;

        const sendSSE = () => {
            messageId++;
            messageCount++;
            const data = {
                id: messageId,
                timestamp: new Date().toISOString(),
                message: `消息 ${messageId}`,
                type: "info"
            };

            const sseMessage = [
                `id: ${messageId}`,
                `event: reconnectEvent`,
                `data: ${JSON.stringify(data)}`,
            ].join('\n');

            try {
                res.write(sseMessage + '\n\n');
                console.log(`发送消息 ID: ${messageId} (${isReconnect ? '重连' : '首次'})`);
                
                // 如果是首次连接（非重连），发送3条消息后主动断开，用于测试自动重连
                if (!isReconnect && messageCount >= 3) {
                    setTimeout(() => {
                        console.log('模拟连接断开（用于测试自动重连）');
                        clearTimeout(timeoutId);
                        // 使用destroy()强制关闭连接，触发IOException以触发重连
                        res.destroy();
                    }, 1000);
                    return;
                }
            } catch (err) {
                clearTimeout(timeoutId);
                return;
            }

            // 固定延迟2秒
            timeoutId = setTimeout(sendSSE, 2000);
        };

        sendSSE();

        const cleanUp = () => {
            clearTimeout(timeoutId);
            res.end();
            console.log('客户端断开SSE重连测试连接');
        };

        req.on('close', cleanUp);
        req.on('abort', cleanUp);
    } else if (req.url === '/sse-disconnect') {
        // 模拟连接断开的端点，用于测试自动重连
        res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.flushHeaders();

        let messageCount = 0;
        const sendAndDisconnect = () => {
            messageCount++;
            const data = {
                id: messageCount,
                timestamp: new Date().toISOString(),
                message: `消息 ${messageCount}（将在3条后断开）`,
                type: "info"
            };

            const sseMessage = [
                `id: ${messageCount}`,
                `event: disconnectEvent`,
                `data: ${JSON.stringify(data)}`,
            ].join('\n');

            try {
                res.write(sseMessage + '\n\n');
                console.log(`发送消息 ID: ${messageCount}`);

                // 发送3条消息后主动断开连接
                if (messageCount >= 3) {
                    setTimeout(() => {
                        console.log('模拟连接断开');
                        res.end();
                    }, 1000);
                } else {
                    setTimeout(sendAndDisconnect, 1000);
                }
            } catch (err) {
                console.log('连接已断开');
            }
        };

        sendAndDisconnect();
    } else if (req.url === '/non-sse') {
        // 返回非SSE格式的响应，用于测试onNonSseResponse回调
        res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({
            message: '这是一个非SSE响应',
            type: 'json',
            timestamp: new Date().toISOString()
        }));
        console.log('返回非SSE响应');
    } else if (req.url === '/nonexistent' || req.url.startsWith('/nonexistent')) {
        // 返回404错误，用于测试HTTP错误处理
        res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('Not Found: The requested resource does not exist.');
        console.log('返回404错误');
    } else if (req.url === '/error-500') {
        // 返回500错误，用于测试服务器错误
        res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('Internal Server Error');
        console.log('返回500错误');
    } else if (req.url === '/error-401') {
        // 返回401错误，用于测试认证错误（不可重试）
        res.writeHead(401, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('Unauthorized');
        console.log('返回401错误');
    } else {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(`
            <html>
                <head><title>SSE 测试服务器</title></head>
                <body>
                    <h1>SSE 测试服务器运行中</h1>
                    <p>端点：</p>
                    <ul>
                        <li>http://localhost:3000/sse - 基础SSE流（随机JSON数据）</li>
                        <li>http://localhost:3000/sse-text - 纯文本SSE流（用于测试toSSEText）</li>
                        <li>http://localhost:3000/sse-reconnect - 支持Last-Event-ID的重连测试</li>
                        <li>http://localhost:3000/sse-disconnect - 模拟连接断开（用于测试自动重连）</li>
                        <li>http://localhost:3000/non-sse - 非SSE响应（用于测试onNonSseResponse）</li>
                        <li>http://localhost:3000/nonexistent - 404错误（用于测试HTTP错误处理）</li>
                        <li>http://localhost:3000/error-500 - 500错误（用于测试服务器错误）</li>
                        <li>http://localhost:3000/error-401 - 401错误（用于测试认证错误）</li>
                    </ul>
                </body>
            </html>
        `);
    }
});

const PORT = 3000;
server.listen(PORT, () => {
    console.log(`规范SSE服务已启动：http://localhost:${PORT}`);
    console.log(`SSE端点：http://localhost:${PORT}/sse`);
});