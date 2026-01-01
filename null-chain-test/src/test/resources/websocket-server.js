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
                        <li>ws://localhost:3001/ws-heartbeat-binary - 二进制心跳</li>
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
            if (isBinary || Buffer.isBuffer(message)) {
                handleBinaryMessage(ws, message, url);
                return;
            }
            
            const data = message.toString();
            console.log(`收到消息: ${data}`);
            
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
            if (url === '/ws-heartbeat-timeout') {
                // 不回复心跳，用于测试心跳超时
                if (data === HEARTBEAT_PING || data.trim() === HEARTBEAT_PING) {
                    console.log('收到心跳但不回复（用于测试超时）');
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
                    } else if (url === '/ws-heartbeat-timeout') {
                        // 不回复，用于测试超时
                        console.log('收到ping但不回复（用于测试超时）');
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
                    // 客户端请求关闭
                    ws.close(1000, '客户端请求关闭');
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
        console.log(`连接关闭: code=${code}, reason=${reason || '无原因'}`);
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
        url === '/ws-reconnect' || url === '/ws-heartbeat-binary') {
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
    console.log(`收到二进制消息: ${data}, 长度: ${buffer.length}`);
    
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
    console.log(`  - ws://localhost:${PORT}/ws-heartbeat-binary`);
});

