# var 关键字测试脚本

本目录包含 `var` 关键字功能的测试脚本。

## 测试文件说明

### 1. var_basic.nf - 基础测试
测试 `var` 关键字的基本功能：
- 自动类型推导（字符串、整数、浮点数、布尔值）
- 手动指定类型
- 简单表达式

**运行方式：**
```bash
# 在 Java 测试代码中运行
NfMain.run("nf/var/var_basic.nf", logger, context);
```

### 2. var_advanced.nf - 高级测试
测试复杂场景：
- 复杂表达式
- 变量引用
- 字符串模板
- 作用域（if、for）
- 变量重新赋值
- 与传统声明混合使用

### 3. var_type_inference.nf - 类型推导测试
专门测试自动类型推导功能：
- 各种基本类型的推导
- 表达式结果的类型推导
- 字符串拼接类型推导
- 比较和逻辑表达式类型推导

### 4. var_scope.nf - 作用域测试
测试变量作用域：
- 全局作用域
- if 块作用域
- for 循环作用域
- while 循环作用域
- 嵌套作用域
- 变量重新赋值

### 5. var_manual_type.nf - 手动类型指定测试
专门测试手动指定类型功能：
- 各种基本类型的手动指定
- 表达式结果的手动类型指定
- 类型兼容性验证

### 6. var_integration.nf - 集成测试
测试 `var` 与其他功能的集成：
- 与 echo、if、for、while、switch 结合
- 与 export、import 结合
- 与对象创建结合
- 与模板字符串结合
- 与循环控制结合

## 测试建议顺序

1. **首先运行** `var_basic.nf` - 验证基本功能是否正常
2. **然后运行** `var_type_inference.nf` - 验证类型推导是否正确
3. **接着运行** `var_manual_type.nf` - 验证手动类型指定是否正常
4. **运行** `var_scope.nf` - 验证作用域是否正确
5. **运行** `var_advanced.nf` - 验证复杂场景
6. **最后运行** `var_integration.nf` - 验证与其他功能的集成

## 预期结果

所有测试脚本应该能够：
- 正确解析 `var` 关键字
- 正确推导变量类型
- 正确执行表达式
- 正确管理变量作用域
- 正确输出结果

## 常见问题

1. **类型推导错误**：检查 `VarSyntaxNode.run()` 方法中的类型推导逻辑
2. **作用域问题**：检查变量是否添加到正确的作用域
3. **语法解析错误**：检查 `VarSyntaxNode.analystToken()` 和 `buildStatement()` 方法
4. **类型不匹配**：检查手动指定类型时的类型兼容性验证

## 快速测试命令

如果项目中有测试运行器，可以使用以下方式快速测试：

```java
// 测试基础功能
NfMain.run("nf/var/var_basic.nf", logger, context);

// 测试类型推导
NfMain.run("nf/var/var_type_inference.nf", logger, context);

// 测试手动类型
NfMain.run("nf/var/var_manual_type.nf", logger, context);
```

