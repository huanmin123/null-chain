# var 关键字测试总结

## 📋 测试文件列表

| 文件名 | 说明 | 测试重点 |
|--------|------|----------|
| `var_quick_test.nf` | 快速测试 | 基本功能快速验证 |
| `var_basic.nf` | 基础测试 | 自动推导、手动类型指定 |
| `var_type_inference.nf` | 类型推导测试 | 各种类型的自动推导 |
| `var_manual_type.nf` | 手动类型测试 | 手动指定类型功能 |
| `var_scope.nf` | 作用域测试 | 变量作用域管理 |
| `var_advanced.nf` | 高级测试 | 复杂场景和表达式 |
| `var_integration.nf` | 集成测试 | 与其他功能集成 |

## 🚀 快速开始

### 方式一：使用 Java 测试类

运行 `VarExpressionTest.java` 中的测试方法：

```java
@Test
public void testVarQuickTest() {
    String file = TestUtil.readFile("var/var_quick_test.nf");
    Map<String, Object> context = new HashMap<>();
    Object result = NfMain.run(file, log, context);
    assertEquals("var快速测试成功", result);
}
```

### 方式二：直接运行脚本

```java
import com.gitee.huanminabc.nullchain.language.NfMain;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;

// 运行快速测试
String script = TestUtil.readFile("var/var_quick_test.nf");
Map<String, Object> context = new HashMap<>();
Object result = NfMain.run(script, logger, context);
System.out.println("测试结果: " + result);
```

## 📝 测试用例说明

### 1. var_quick_test.nf
**用途**：快速验证基本功能是否正常  
**测试内容**：
- ✅ 字符串自动推导
- ✅ 整数自动推导
- ✅ 浮点数自动推导
- ✅ 布尔值自动推导
- ✅ 手动类型指定
- ✅ 表达式推导
- ✅ 作用域测试
- ✅ 变量重新赋值

**预期输出**：
```
========== var 快速测试开始 ==========
1. 字符串自动推导: str = Hello var
2. 整数自动推导: num = 42
3. 浮点数自动推导: price = 99.99
4. 布尔值自动推导: flag = true
5. 手动指定String: name = test
6. 手动指定Integer: count = 100
7. 表达式自动推导: sum = 60
8. 表达式手动类型: product = 24
9. 作用域测试: outer = 外部, inner = 内部
10. 变量重新赋值: counter = 1
========== var 快速测试完成 ==========
```

### 2. var_basic.nf
**用途**：全面测试基础功能  
**测试内容**：12个基础测试用例

### 3. var_type_inference.nf
**用途**：专门测试类型推导  
**测试内容**：12种不同类型的推导场景

### 4. var_manual_type.nf
**用途**：专门测试手动类型指定  
**测试内容**：12种手动类型指定场景

### 5. var_scope.nf
**用途**：测试变量作用域  
**测试内容**：8种作用域场景

### 6. var_advanced.nf
**用途**：测试复杂场景  
**测试内容**：13个高级测试用例

### 7. var_integration.nf
**用途**：测试与其他功能的集成  
**测试内容**：15个集成测试用例

## ✅ 测试检查清单

运行测试后，检查以下内容：

- [ ] 所有脚本都能正常解析（无语法错误）
- [ ] 自动类型推导正确（字符串、整数、浮点数、布尔值）
- [ ] 手动类型指定正确（String、Integer、Double、Boolean）
- [ ] 表达式计算正确
- [ ] 作用域管理正确（if、for、while 块）
- [ ] 变量重新赋值正确
- [ ] 与其他功能集成正常（echo、if、for、export 等）
- [ ] 错误处理正确（类型不匹配、变量不存在等）

## 🐛 常见问题排查

### 问题1：语法解析错误
**症状**：`无法识别的语法` 错误  
**检查**：
- 确认 `VarSyntaxNode` 已正确注册
- 确认 `var` 关键字已添加到 `IdentifierToken`
- 检查语法识别优先级（VarSyntaxNode 应在 AssignSyntaxNode 之前）

### 问题2：类型推导错误
**症状**：变量类型不正确  
**检查**：
- 查看 `VarSyntaxNode.run()` 方法中的类型推导逻辑
- 确认 `arithmetic.getClass()` 返回正确的类型
- 检查表达式计算结果

### 问题3：作用域问题
**症状**：变量在不同作用域中无法访问  
**检查**：
- 确认变量添加到正确的作用域
- 检查 `NfContextScope` 的作用域管理
- 验证父作用域变量访问逻辑

### 问题4：手动类型不匹配
**症状**：`值类型和声明的类型不匹配` 错误  
**检查**：
- 确认类型兼容性检查逻辑
- 验证 `Class.isAssignableFrom()` 的使用
- 检查类型转换是否正确

## 📊 测试结果示例

成功运行后，应该看到类似输出：

```
========== var 快速测试开始 ==========
1. 字符串自动推导: str = Hello var
2. 整数自动推导: num = 42
...
========== var 快速测试完成 ==========
测试结果: var快速测试成功
```

## 🎯 下一步

1. **运行快速测试**：先运行 `var_quick_test.nf` 验证基本功能
2. **运行完整测试**：依次运行所有测试文件
3. **检查日志**：查看是否有错误或警告
4. **验证结果**：确认所有测试用例的输出符合预期

## 💡 提示

- 如果某个测试失败，查看错误信息中的行号，定位问题
- 使用 `echo` 语句输出中间结果，便于调试
- 检查 `export` 的返回值，验证最终结果
- 对比自动推导和手动指定的结果，确保一致性

