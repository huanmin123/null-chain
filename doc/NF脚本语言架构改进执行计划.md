# NF脚本语言架构改进执行计划

## 执行原则

1. **分阶段实施**：先修复高优先级问题，再逐步改进
2. **充分测试**：每个改进都要有充分的测试覆盖
3. **向后兼容**：确保改进不影响现有功能
4. **小步快跑**：每次聚焦一个独立小功能，不并行跨功能开发

---

## 第一阶段：高优先级修复（必须修复）

### 步骤1：移除不必要的 ThreadLocal ⚠️ **优先级：最高**

**目标**：简化代码，符合架构设计

**任务清单**：
1. 在 `NfContext` 中添加字段：
   - `Map<String, Object> tempVarStorage = new HashMap<>()`
   - `int recursionDepth = 0`
2. 修改 `NfCalculator.arithmetic()` 方法：
   - 移除 `ThreadLocal<Map<String, Object>> tempVarStorage`
   - 移除 `ThreadLocal<Integer> recursionDepth`
   - 将所有 `tempVarStorage.get()` 改为 `nfContext.getTempVarStorage()`
   - 将所有 `recursionDepth.get()/set()` 改为 `nfContext.getRecursionDepth()/setRecursionDepth()`
3. 在 `NfContext.clear()` 中清理这些字段：
   - `tempVarStorage.clear()`
   - `recursionDepth = 0`
4. 运行测试，确保功能正常

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`

**预计工作量**：2-3小时

---

### 步骤2：添加全局脚本执行超时控制 ⚠️ **优先级：最高**

**目标**：防止恶意脚本和资源耗尽

**任务清单**：
1. 创建超时异常类 `NfTimeoutException`：
   - 继承 `NfException`
   - 添加构造函数
2. 在 `NfContext` 中添加全局超时配置：
   - `private static long globalTimeoutMillis = 5 * 60 * 1000;` // 默认5分钟
   - `public static void setGlobalTimeout(long timeoutMillis)`
   - `public static long getGlobalTimeout()`
3. 在 `NfContext` 中添加执行时间跟踪：
   - `private long executionStartTime = 0;`
   - `public void startExecution()`
   - `public void checkTimeout()` // 检查并抛出异常
4. 在 `NfRun.run()` 开始时调用 `context.startExecution()`
5. 在关键位置添加超时检查：
   - `SyntaxNodeFactory.executeAll()`：每个节点执行前后检查
   - `ForSyntaxNode`、`WhileSyntaxNode`、`DoWhileSyntaxNode`：每次循环迭代检查
   - `NfCalculator.arithmetic()`：表达式计算前检查
6. 编写测试用例验证超时功能

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfTimeoutException.java` (新建)
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfRun.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/SyntaxNodeFactory.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/blocknode/ForSyntaxNode.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/blocknode/WhileSyntaxNode.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/blocknode/DoWhileSyntaxNode.java`

**预计工作量**：4-6小时

---

### 步骤3：改进内存管理 - clear() 方法安全性 ⚠️ **优先级：高**

**目标**：防止 clear() 后误用导致的 NPE

**任务清单**：
1. 在 `NfContext` 中添加状态标志：
   - `private boolean cleared = false;`
2. 修改 `NfContext.clear()` 方法：
   - 设置 `cleared = true`
   - Map 清空但不置 null（或置为空集合 `Collections.emptyMap()`）
3. 在所有公共方法中添加检查：
   - `getCurrentScope()`
   - `getMainScope()`
   - `getScope(String id)`
   - `getVariable(String name)`
   - `getFunction(String functionName)`
   - `createScope()`
   - `switchScope()`
   - 其他可能被外部调用的方法
4. 在 `NfContextScope` 中应用相同策略：
   - 添加 `cleared` 标志
   - 修改 `clear()` 方法
   - 在 `toMap()`, `getVariable()`, `addVariable()` 等方法中检查
5. 编写测试用例验证 clear() 后的行为

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContextScope.java`

**预计工作量**：3-4小时

---

### 步骤4：改进错误处理 ⚠️ **优先级：高**

**目标**：使用日志框架，统一异常信息格式

**任务清单**：
1. 修改 `NfCalculator.arithmetic()` 中的错误处理：
   - 移除 `System.err.println()` 和 `printStackTrace()`
   - 使用 SLF4J 日志框架（`log.error()`）
   - 添加日志级别控制
2. 统一异常信息格式：
   - 检查所有异常抛出位置
   - 确保包含足够的上下文信息（行号、变量名、作用域ID、表达式等）
   - 创建异常信息构建工具类（可选）
3. 检查并改进其他位置的错误处理
4. 编写测试用例验证错误信息

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`
- 其他抛出异常的文件

**预计工作量**：3-4小时

---

## 第二阶段：代码重构（建议修复）

### 步骤5：重构 NfRun.run() 重复代码 ⚠️ **优先级：中**

**目标**：消除代码重复，提高可维护性

**任务清单**：
1. 提取公共方法：
   - `initializeContext()` - 初始化上下文和作用域
   - `setupSystemVariables()` - 设置系统变量
   - `setupMainSystemContext()` - 设置主系统上下文
2. 重构所有 `run()` 重载方法，使用公共方法
3. 运行测试确保功能正常

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfRun.java`

**预计工作量**：2-3小时

**依赖**：步骤1-4完成后进行

---

### 步骤6：重构 FunCallSyntaxNode 重复代码 ⚠️ **优先级：中**

**目标**：消除函数执行逻辑的重复

**任务清单**：
1. 提取公共方法：
   - `executeFunctionBody()` - 执行函数体的公共逻辑
   - `setupFunctionParameters()` - 设置函数参数
   - `getReturnValue()` - 获取返回值
2. 重构 `executeFunction()` 和 `executeScriptFunction()` 使用公共方法
3. 运行测试确保功能正常

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/linenode/FunCallSyntaxNode.java`

**预计工作量**：3-4小时

**依赖**：步骤5完成后进行

---

### 步骤7：拆分 NfCalculator 复杂方法 ⚠️ **优先级：中**

**目标**：提高代码可读性和可维护性

**任务清单**：
1. 拆分 `preProcessFunctionCalls()` 方法：
   - `findLastFunctionCall()` - 查找最后一个函数调用
   - `extractFunctionCallExpression()` - 提取函数调用表达式
   - `executeFunctionCall()` - 执行函数调用
   - `replaceFunctionCallWithTempVar()` - 替换为临时变量
2. 添加详细注释说明每个方法的作用
3. 运行测试确保功能正常

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`

**预计工作量**：4-5小时

**依赖**：步骤1完成后进行（因为涉及 tempVarStorage）

---

## 第三阶段：性能优化（建议修复）

### 步骤8：优化作用域链遍历 ⚠️ **优先级：中**

**目标**：提高变量查找性能

**任务清单**：
1. 分析当前作用域链遍历的性能瓶颈
2. 实现变量查找缓存机制（可选）：
   - 缓存键：`scopeId + variableName`
   - 缓存失效：作用域变化时
3. 或限制作用域嵌套深度（如果缓存实现复杂）
4. 性能测试和对比

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`

**预计工作量**：4-6小时

**依赖**：步骤1-7完成后进行

---

### 步骤9：表达式结果缓存（可选） ⚠️ **优先级：低**

**目标**：避免重复计算相同表达式

**任务清单**：
1. 分析表达式缓存的需求和场景
2. 设计缓存策略：
   - 缓存键：表达式字符串 + 变量值哈希
   - 缓存失效：变量变化时
3. 实现可选的表达式缓存机制
4. 性能测试和对比

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/NfCalculator.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`

**预计工作量**：5-8小时

**依赖**：步骤8完成后进行

---

## 第四阶段：功能增强（可选）

### 步骤10：添加脚本依赖管理 ⚠️ **优先级：低**

**目标**：防止循环导入

**任务清单**：
1. 在 `ImportSyntaxNode` 中添加依赖跟踪
2. 实现循环依赖检测算法（拓扑排序）
3. 限制导入深度
4. 编写测试用例

**涉及文件**：
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/syntaxNode/linenode/ImportSyntaxNode.java`
- `null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/internal/NfContext.java`

**预计工作量**：4-6小时

---

### 步骤11：代码质量改进 ⚠️ **优先级：低**

**目标**：提高代码可读性

**任务清单**：
1. 补充缺失的方法注释（JavaDoc）
2. 提取魔法数字和字符串为常量
3. 代码格式统一

**涉及文件**：所有相关文件

**预计工作量**：持续进行

---

## 执行时间表

| 阶段 | 步骤 | 优先级 | 预计时间 | 依赖 |
|------|------|--------|----------|------|
| 第一阶段 | 步骤1：移除 ThreadLocal | 最高 | 2-3小时 | 无 |
| 第一阶段 | 步骤2：添加超时控制 | 最高 | 4-6小时 | 无 |
| 第一阶段 | 步骤3：改进内存管理 | 高 | 3-4小时 | 无 |
| 第一阶段 | 步骤4：改进错误处理 | 高 | 3-4小时 | 无 |
| 第二阶段 | 步骤5：重构 NfRun | 中 | 2-3小时 | 步骤1-4 |
| 第二阶段 | 步骤6：重构 FunCallSyntaxNode | 中 | 3-4小时 | 步骤5 |
| 第二阶段 | 步骤7：拆分 NfCalculator | 中 | 4-5小时 | 步骤1 |
| 第三阶段 | 步骤8：优化作用域链 | 中 | 4-6小时 | 步骤1-7 |
| 第三阶段 | 步骤9：表达式缓存 | 低 | 5-8小时 | 步骤8 |
| 第四阶段 | 步骤10：依赖管理 | 低 | 4-6小时 | 无 |
| 第四阶段 | 步骤11：代码质量 | 低 | 持续 | 无 |

**第一阶段总时间**：12-17小时  
**第二阶段总时间**：9-12小时  
**第三阶段总时间**：9-14小时  
**第四阶段总时间**：持续进行

---

## 注意事项

1. **每个步骤完成后**：
   - 运行所有测试用例
   - 进行代码审查
   - 更新相关文档

2. **如果遇到问题**：
   - 先回滚到上一个稳定版本
   - 分析问题根源
   - 重新设计解决方案

3. **性能优化步骤**：
   - 先进行性能基准测试
   - 记录优化前后的性能数据
   - 确保优化不降低功能正确性

4. **向后兼容**：
   - 所有改进都要保持 API 兼容
   - 如果必须破坏兼容性，要提供迁移指南

---

## 验收标准

每个步骤完成后，需要满足以下条件：

1. ✅ 所有现有测试用例通过
2. ✅ 新增功能有对应的测试用例
3. ✅ 代码符合项目规范
4. ✅ 相关文档已更新
5. ✅ 代码审查通过

---

**最后更新时间**：2024-12-XX  
**计划版本**：v1.0

