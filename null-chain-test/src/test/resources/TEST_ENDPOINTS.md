# SSE 测试服务器端点说明

## 已支持的端点

### 1. `/sse` - 基础SSE流
- **用途**: 基础SSE连接和消息接收测试
- **响应**: 持续发送随机JSON数据的SSE流
- **特点**: 
  - 每条消息都有递增的ID
  - 随机延迟（1-5秒）
  - 支持无限发送

### 2. `/sse-text` - 纯文本SSE流
- **用途**: 测试 `toSSEText()` 方法
- **响应**: 持续发送纯文本数据的SSE流
- **特点**: 
  - 固定延迟2秒
  - 每条消息都有递增的ID

### 3. `/sse-reconnect` - 支持Last-Event-ID的重连测试
- **用途**: 测试Last-Event-ID和自动重连功能
- **响应**: SSE流，支持从指定ID继续发送
- **特点**: 
  - 读取 `Last-Event-ID` 请求头
  - 从指定ID+1开始发送
  - 固定延迟2秒

### 4. `/sse-disconnect` - 模拟连接断开
- **用途**: 测试自动重连功能
- **响应**: SSE流，发送3条消息后主动断开
- **特点**: 
  - 发送3条消息后自动断开连接
  - 用于测试重连机制

### 5. `/non-sse` - 非SSE响应
- **用途**: 测试 `onNonSseResponse()` 回调
- **响应**: JSON格式的普通HTTP响应（200状态码）
- **Content-Type**: `application/json`

### 6. `/nonexistent` - 404错误
- **用途**: 测试HTTP错误处理
- **响应**: 404 Not Found错误
- **状态码**: 404

### 7. `/error-500` - 服务器错误
- **用途**: 测试可重试的HTTP错误
- **响应**: 500 Internal Server Error
- **状态码**: 500（可重试）

### 8. `/error-401` - 认证错误
- **用途**: 测试不可重试的HTTP错误
- **响应**: 401 Unauthorized
- **状态码**: 401（不可重试）

## 测试场景覆盖

### SSEAwaitTest
- ✅ `testAwaitNormalCompletion` - 使用 `/sse`
- ✅ `testAwaitAlreadyCompleted` - 使用 `/sse`
- ✅ `testAwaitWithTimeoutNormalCompletion` - 使用 `/sse`
- ✅ `testAwaitWithTimeoutExpired` - 使用 `/sse`（连接持续运行）
- ✅ `testAwaitWithTimeoutAlreadyCompleted` - 使用 `/sse`
- ✅ `testIsCompleted` - 使用 `/sse`
- ✅ `testAwaitFailedState` - 使用 `/nonexistent`（404错误）
- ✅ `testMultipleAwaitCalls` - 使用 `/sse`

### SSEErrorTest
- ✅ `testNonSseResponse` - 使用 `/non-sse`
- ✅ `testHttp404Error` - 使用 `/nonexistent`
- ✅ `testReconnectExhausted` - 使用 `/nonexistent`
- ✅ `testShouldTerminate` - 使用 `/sse`
- ✅ `testAwaitAfterClose` - 使用 `/sse`
- ✅ `testErrorCodes` - 使用 `/nonexistent`

### SSEBasicTest
- ✅ `testBasicSSE` - 使用 `/sse`
- ✅ `testSSETermination` - 使用 `/sse`
- ✅ `testSSEControllerReturn` - 使用 `/sse`

### SSEAdvancedTest
- ✅ `testLastEventId` - 使用 `/sse-reconnect`
- ✅ `testAutoReconnect` - 使用 `/sse-reconnect`
- ✅ `testConnectionState` - 使用 `/sse`
- ✅ `testSSEController` - 使用 `/sse`
- ✅ `testAutoClose` - 使用 `/sse`

### SSETestDemo
- ✅ 使用 `/sse-text`

## 总结

所有测试场景都已得到服务器端点的支持：
- ✅ 基础SSE功能测试 - `/sse`
- ✅ 文本SSE测试 - `/sse-text`
- ✅ 重连功能测试 - `/sse-reconnect`
- ✅ 连接断开测试 - `/sse-disconnect`
- ✅ 非SSE响应测试 - `/non-sse`
- ✅ HTTP错误测试 - `/nonexistent` (404)
- ✅ 服务器错误测试 - `/error-500` (500)
- ✅ 认证错误测试 - `/error-401` (401)

