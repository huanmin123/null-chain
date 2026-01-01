const http = require('http');
const WebSocket = require('ws');

// 支持的子协议列表
const supportedProtocols = ['chat', 'superchat', 'notification'];

// 心跳消息格式
const HEARTBEAT_PING = '{"type":"ping"}';
const HEARTBEAT_PONG = '{"type":"pong"}';

// 创建 HTTP 服务器
const server = http.createServer((req, res) => {
    if (req.url === '/') {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(`
            <html>
                <head><title>WebSocket 测试服务器</title></head>
                <body>
                    <h1>WebSocket 测试服务器运行中</h1>
                    <p>端点：</p>
                    <ul>
                        <li>ws://localhost:3001/ws - 基础 WebSocket</li>
                        <li>ws://localhost:3001/ws-subprotocol - 支持子协议</li>
                        <li>ws://localhost:3001/ws-heartbeat - 支持心跳</li>
                        <li>ws://localhost:3001/ws-full - 完整功能（子协议+心跳）</li>
                        <li>ws://localhost:3001/ws-reconnect - 支持重连测试</li>
                        <li>ws://localhost:3001/ws-heartbeat-timeout - 心跳超时测试（不回复心跳）</li>
                        <li>ws://localhost:3001/ws-heartbeat-timeout-reconnect - 心跳超时重连测试（不回复心跳，允许重连）</li>
                        <li>ws://localhost:3001/ws-heartbeat-binary - 二进制心跳</li>
                        <li>关闭消息类型：</li>
                        <ul>
                            <li>{"type":"close"} - 正常关闭（code 1000）</li>
                            <li>{"type":"close-abnormal"} - 异常关闭（code 1006）</li>
                            <li>{"type":"close-going-away"} - 端点离开（code 1001）</li>
                            <li>{"type":"close-server-error"} - 服务器错误（code 1011）</li>
                        </ul>
                    </ul>
                </body>
            </html>
        `);
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('Not Found');
    }
});

// 创建 WebSocket 服务器
const wss = new WebSocket.Server({ 
    server,
    // 处理子协议协商
    handleProtocols: (protocols, request) => {
        const url = request.url;
        // 只有 /ws-subprotocol 和 /ws-full 端点支持子协议
        if (url === '/ws-subprotocol' || url === '/ws-full') {
            // 检查客户端请求的协议是否在支持列表中
            for (const protocol of protocols) {
                if (supportedProtocols.includes(protocol)) {
                    return protocol; // 返回第一个匹配的协议
                }
            }
            // 如果客户端请求了协议但不匹配，返回 false 拒绝连接
            // 如果客户端未请求协议，返回 false（表示不支持子协议）
            return false;
        }
        // 其他端点不支持子协议：如果客户端请求了协议，返回 false 拒绝；如果未请求，返回 false（表示不支持）
        // 注意：返回 false 表示不支持子协议，但不会拒绝连接（除非客户端强制要求）
        // 为了测试客户端配置协议但服务器不返回的场景，这里返回 false
        return false;
    }
});

// 处理 WebSocket 连接
wss.on('connection', (ws, request) => {
    const url = request.url;
    console.log(`新的 WebSocket 连接: ${url}`);
    
    // 获取选中的子协议
    const protocol = ws.protocol;
    if (protocol) {
        console.log(`选中的子协议: ${protocol}`);
    }
    
    // 发送欢迎消息
    console.log(`[${url}] 发送欢迎消息`);
    ws.send(JSON.stringify({
        type: 'welcome',
        message: '连接成功',
        timestamp: new Date().toISOString(),
        protocol: protocol || null
    }));
    
    // 心跳处理（仅对 /ws-heartbeat 和 /ws-full 端点）
    let heartbeatInterval = null;
    if (url === '/ws-heartbeat' || url === '/ws-full') {
        // 定期发送心跳（可选，主要用于测试）
        // 实际场景中，服务器通常只响应客户端的心跳
    }
    
    // 处理接收到的消息
    ws.on('message', (message, isBinary) => {
        try {
            // 检查是否是二进制消息
            // 注意：即使 isBinary 为 false，message 也可能是 Buffer
            // 但如果是文本消息，应该能正常转换为字符串
            const isBinaryMessage = isBinary === true;
            
            if (isBinaryMessage) {
                // 明确的二进制消息
                handleBinaryMessage(ws, message, url);
                return;
            }
            
            // 尝试作为文本消息处理
            let data;
            if (Buffer.isBuffer(message)) {
                // 如果是 Buffer 但不是二进制标记，尝试转换为文本
                data = message.toString('utf8');
            } else {
                data = message.toString();
            }
            
            console.log(`[${url}] 收到文本消息: ${data}, isBinary=${isBinary}`);
            
            // 处理心跳消息（仅对支持心跳的端点）
            if (url === '/ws-heartbeat' || url === '/ws-full' || url === '/ws-heartbeat-binary') {
                if (data === HEARTBEAT_PING || data.trim() === HEARTBEAT_PING) {
                    // 回复心跳
                    ws.send(HEARTBEAT_PONG);
                    console.log('发送心跳回复: pong');
                    return;
                }
            }
            
            // 心跳超时测试端点：不回复心跳
            if (url === '/ws-heartbeat-timeout' || url === '/ws-heartbeat-timeout-reconnect') {
                // 不回复心跳，用于测试心跳超时
                if (data === HEARTBEAT_PING || data.trim() === HEARTBEAT_PING) {
                    console.log(`[${url}] 收到心跳但不回复（用于测试超时）`);
                    return;
                }
            }
            
            // 尝试解析 JSON
            let jsonData;
            try {
                jsonData = JSON.parse(data);
            } catch (e) {
                // 不是 JSON，作为普通文本处理
                ws.send(JSON.stringify({
                    type: 'echo',
                    message: data,
                    timestamp: new Date().toISOString()
                }));
                return;
            }
            
            // 处理不同类型的消息
            switch (jsonData.type) {
                case 'ping':
                    // 另一种心跳格式（仅对支持心跳的端点回复）
                    if (url === '/ws-heartbeat' || url === '/ws-full' || url === '/ws-heartbeat-binary') {
                        ws.send(JSON.stringify({ type: 'pong' }));
                    } else if (url === '/ws-heartbeat-timeout' || url === '/ws-heartbeat-timeout-reconnect') {
                        // 不回复，用于测试超时
                        console.log(`[${url}] 收到ping但不回复（用于测试超时）`);
                    }
                    break;
                case 'echo':
                    // 回显消息
                    ws.send(JSON.stringify({
                        type: 'echo',
                        original: jsonData,
                        timestamp: new Date().toISOString()
                    }));
                    break;
                case 'close':
                    // 客户端请求关闭（正常关闭）
                    console.log(`[${url}] 收到关闭请求，准备关闭连接，当前状态: ${ws.readyState}`);
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1000, '客户端请求关闭');
                        console.log(`[${url}] 已发送关闭帧（正常关闭）`);
                    } else {
                        console.log(`[${url}] 连接已关闭，无法发送关闭帧，状态: ${ws.readyState}`);
                    }
                    break;
                case 'close-abnormal':
                    // 模拟网络异常关闭（code 1006）
                    console.log(`[${url}] 收到异常关闭请求，准备异常关闭连接，当前状态: ${ws.readyState}`);
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1006, '网络异常');
                        console.log(`[${url}] 已发送关闭帧（异常关闭 code 1006）`);
                    } else {
                        console.log(`[${url}] 连接已关闭，无法发送关闭帧，状态: ${ws.readyState}`);
                    }
                    break;
                case 'close-going-away':
                    // 模拟端点离开（code 1001）
                    console.log(`[${url}] 收到端点离开请求，准备关闭连接，当前状态: ${ws.readyState}`);
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1001, '端点离开');
                        console.log(`[${url}] 已发送关闭帧（端点离开 code 1001）`);
                    } else {
                        console.log(`[${url}] 连接已关闭，无法发送关闭帧，状态: ${ws.readyState}`);
                    }
                    break;
                case 'close-server-error':
                    // 模拟服务器错误（code 1011）
                    console.log(`[${url}] 收到服务器错误请求，准备关闭连接，当前状态: ${ws.readyState}`);
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1011, '服务器错误');
                        console.log(`[${url}] 已发送关闭帧（服务器错误 code 1011）`);
                    } else {
                        console.log(`[${url}] 连接已关闭，无法发送关闭帧，状态: ${ws.readyState}`);
                    }
                    break;
                default:
                    // 默认回显
                    ws.send(JSON.stringify({
                        type: 'response',
                        received: jsonData,
                        timestamp: new Date().toISOString()
                    }));
            }
        } catch (err) {
            console.error('处理消息错误:', err);
            ws.send(JSON.stringify({
                type: 'error',
                message: err.message,
                timestamp: new Date().toISOString()
            }));
        }
    });
    
    // 处理连接关闭
    ws.on('close', (code, reason) => {
        console.log(`[${url}] 连接关闭: code=${code}, reason=${reason || '无原因'}, readyState=${ws.readyState}`);
        if (heartbeatInterval) {
            clearInterval(heartbeatInterval);
        }
    });
    
    // 处理错误
    ws.on('error', (error) => {
        console.error('WebSocket 错误:', error);
    });
    
    // 定期发送测试消息（可选）
    if (url === '/ws' || url === '/ws-subprotocol' || url === '/ws-heartbeat' || url === '/ws-full' || 
        url === '/ws-reconnect' || url === '/ws-heartbeat-binary' || url === '/ws-heartbeat-timeout-reconnect') {
        const testMessageInterval = setInterval(() => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({
                    type: 'notification',
                    message: '这是一条定期测试消息',
                    timestamp: new Date().toISOString(),
                    count: Math.floor(Math.random() * 1000)
                }));
            } else {
                clearInterval(testMessageInterval);
            }
        }, 5000); // 每5秒发送一次
        
        // 连接关闭时清理定时器
        ws.on('close', () => {
            clearInterval(testMessageInterval);
        });
    }
});

/**
 * 处理二进制消息
 */
function handleBinaryMessage(ws, message, url) {
    const buffer = Buffer.isBuffer(message) ? message : Buffer.from(message);
    const data = buffer.toString('utf8');
    console.log(`[${url}] 收到二进制消息: ${data}, 长度: ${buffer.length}`);
    
    // 尝试判断是否是文本消息被误识别为二进制
    // 如果内容看起来像 JSON 文本，尝试作为文本消息处理
    if (data.startsWith('{') || data.startsWith('[') || data.startsWith('"')) {
        console.log(`[${url}] 检测到可能是文本消息，尝试作为文本处理`);
        try {
            const jsonData = JSON.parse(data);
            // 处理不同类型的消息
            switch (jsonData.type) {
                case 'close':
                    console.log(`[${url}] 收到关闭请求（从二进制消息中解析），准备关闭连接，当前状态: ${ws.readyState}`);
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1000, '客户端请求关闭');
                        console.log(`[${url}] 已发送关闭帧（正常关闭）`);
                    }
                    return;
                case 'close-abnormal':
                    // 模拟网络异常关闭（code 1006）
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1006, '网络异常');
                        console.log(`[${url}] 已发送关闭帧（异常关闭 code 1006）`);
                    }
                    return;
                case 'close-going-away':
                    // 模拟端点离开（code 1001）
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1001, '端点离开');
                        console.log(`[${url}] 已发送关闭帧（端点离开 code 1001）`);
                    }
                    return;
                case 'close-server-error':
                    // 模拟服务器错误（code 1011）
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.close(1011, '服务器错误');
                        console.log(`[${url}] 已发送关闭帧（服务器错误 code 1011）`);
                    }
                    return;
                case 'test':
                case 'echo':
                default:
                    // 回显消息
                    ws.send(JSON.stringify({
                        type: 'response',
                        received: jsonData,
                        timestamp: new Date().toISOString()
                    }));
                    return;
            }
        } catch (e) {
            // 不是 JSON，继续作为二进制处理
            console.log(`[${url}] 解析 JSON 失败，作为二进制消息处理: ${e.message}`);
        }
    }
    
    // 二进制心跳处理（仅对 /ws-heartbeat-binary 端点）
    if (url === '/ws-heartbeat-binary') {
        if (data === 'PING') {
            // 回复二进制心跳
            ws.send(Buffer.from('PONG', 'utf8'));
            console.log('发送二进制心跳回复: PONG');
            return;
        }
    }
    
    // 其他二进制消息：直接回显二进制数据
    // 这样客户端可以通过 onMessage(controller, byte[]) 接收
    ws.send(buffer);
}

const PORT = 3001;
server.listen(PORT, () => {
    console.log(`WebSocket 测试服务器已启动：http://localhost:${PORT}`);
    console.log(`WebSocket 端点：`);
    console.log(`  - ws://localhost:${PORT}/ws`);
    console.log(`  - ws://localhost:${PORT}/ws-subprotocol`);
    console.log(`  - ws://localhost:${PORT}/ws-heartbeat`);
    console.log(`  - ws://localhost:${PORT}/ws-full`);
    console.log(`  - ws://localhost:${PORT}/ws-reconnect`);
    console.log(`  - ws://localhost:${PORT}/ws-heartbeat-timeout`);
    console.log(`  - ws://localhost:${PORT}/ws-heartbeat-timeout-reconnect`);
    console.log(`  - ws://localhost:${PORT}/ws-heartbeat-binary`);
    console.log(`\n关闭消息类型：`);
    console.log(`  - {"type":"close"} - 正常关闭（code 1000）`);
    console.log(`  - {"type":"close-abnormal"} - 异常关闭（code 1006）`);
    console.log(`  - {"type":"close-going-away"} - 端点离开（code 1001）`);
    console.log(`  - {"type":"close-server-error"} - 服务器错误（code 1011）`);
});

