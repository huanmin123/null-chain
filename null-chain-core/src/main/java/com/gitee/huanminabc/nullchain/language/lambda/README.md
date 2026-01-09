# NF Lambda 与 Java 函数式接口互操作功能

## 概述

成功实现了 NF 脚本语言的 Lambda 表达式与 Java 函数式接口的互操作功能。这使得 NF 脚本中定义的 Lambda 可以作为参数传递给 Java 方法，特别是用于 Stream API 等函数式编程场景。

## 核心实现

### 1. LambdaProxyFactory 类

**位置**：`null-chain-core/src/main/java/com/gitee/huanminabc/nullchain/language/lambda/LambdaProxyFactory.java`

**核心功能**：
- `isFunctionalInterface(Class<?> clazz)` - 判断是否是函数式接口
- `createProxy(FunRefInfo, Class<T>, NfContext, int)` - 创建动态代理
- `executeLambda(FunRefInfo, Object[], NfContext, int)` - 直接执行 NF Lambda

**技术实现**：
- 使用 JDK 动态代理（`Proxy.newProxyInstance`）
- `InvocationHandler` 拦截方法调用
- 在代理方法被调用时，执行 NF Lambda 的函数体

### 2. 工作流程

```
NF Lambda (FunRefInfo)
    ↓
LambdaProxyFactory.createProxy()
    ↓
动态代理对象 (implements Function/Predicate/etc)
    ↓
Java 方法调用 (如 stream.map())
    ↓
InvocationHandler 拦截
    ↓
执行 NF Lambda 函数体
    ↓
返回结果给 Java
```

## 使用示例

### 场景 1：NF Lambda -> Java Function

```java
// 在 NF 脚本中定义 Lambda
Fun<Integer : Integer> square = (x) -> {
    return x * x
}

// 转换为 Java Function
Function<Integer, Integer> function = LambdaProxyFactory.createProxy(
    funRef, Function.class, context, 0
);

// 在 Java 中调用
Integer result = function.apply(5);  // 返回 25
```

### 场景 2：NF Lambda 用于 Stream.map()

```java
// 创建 NF Lambda
FunRefInfo doubler = ...; // (x) -> { return x * 2 }

// 转换为 Java Function
Function<Integer, Integer> function = LambdaProxyFactory.createProxy(
    doubler, Function.class, context, 0
);

// 用于 Java Stream
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> result = numbers.stream()
    .map(function)  // 使用 NF Lambda
    .collect(Collectors.toList());
// 结果: [2, 4, 6, 8, 10]
```

### 场景 3：多个 Lambda 组合使用

```java
// 创建多个 NF Lambda
FunRefInfo square = ...;     // 平方
FunRefInfo isGreaterThan10 = ...; // > 10 过滤

// 转换
Function<Integer, Integer> squareFunc = LambdaProxyFactory.createProxy(...);
Predicate<Integer> filter = LambdaProxyFactory.createProxy(...);

// 链式调用
List<Integer> result = numbers.stream()
    .map(squareFunc)       // 平方
    .filter(filter)        // 过滤 > 10
    .collect(Collectors.toList());
// 结果: [16, 25] (从 [1,2,3,4,5])
```

## 支持的函数式接口

所有 Java 标准的函数式接口都支持：

- **Function<T, R>** - 转换函数
- **Predicate<T>** - 断言
- **Consumer<T>** - 消费者
- **Supplier<T>** - 供应者
- **UnaryOperator<T>** - 一元操作
- **BinaryOperator<T>** - 二元操作
- **自定义函数式接口** - 只要是函数式接口即可

## 关键特性

### ✅ 已实现

1. **动态代理创建** - 自动将 FunRefInfo 转换为函数式接口
2. **参数传递** - Java 参数自动传递给 NF Lambda
3. **返回值转换** - NF Lambda 返回值自动返回给 Java
4. **闭包支持** - 支持捕获外部变量的 Lambda
5. **类型推断** - 自动识别函数式接口
6. **异常处理** - 完善的错误处理机制

### 📝 待完善

1. **表达式自动转换** - 目前需要手动调用 `createProxy()`
   - 目标：在 `parseParameterValue()` 中自动检测并转换
   - 位置：`FunCallSyntaxNode.parseParameterValue()`

2. **方法签名识别** - 需要通过反射获取目标方法的参数类型
   - 挑战：需要在参数解析时知道目标方法的签名

3. **集成到表达式计算** - 目前需要手动转换
   - 目标：`stream.map((x) -> { return x * 2 })` 直接可用
   - 需要：修改 `NfCalculator.preProcessFunctionCalls()`

## 测试验证

**测试类**：`JavaLambdaInteropDemoTest.java`

**测试场景**：
- ✅ NF Lambda -> Java Function 转换
- ✅ NF Lambda -> Java Predicate 转换
- ✅ NF Lambda 用于 Stream.map()
- ✅ 多个 NF Lambda 组合使用
- ✅ 完整的 Stream 链式调用

## 性能考虑

1. **代理对象创建** - 每次调用 `createProxy()` 会创建新代理
   - 可优化：添加代理对象缓存

2. **反射调用** - 方法调用使用反射，有一定性能开销
   - 可接受：动态代理本身就是基于反射

3. **上下文切换** - 执行 Lambda 需要切换 NF 上下文
   - 可优化：减少不必要的作用域创建和销毁

## 下一步工作

### 方案 A：手动转换（当前实现）

```nf
// NF 脚本中
Fun<Integer : Integer> doubler = (x) -> { return x * 2 }
doubler  // 返回 FunRefInfo

// Java 中
Function<Integer, Integer> func = LambdaProxyFactory.createProxy(doubler, ...)
stream.map(func)
```

### 方案 B：自动转换（目标功能）

```nf
// NF 脚本中直接使用
stream.map((x) -> { return x * 2 })
```

实现方案 B 需要：

1. **修改 `parseParameterValue()`**
   ```java
   private Object parseParameterValue(List<Token> paramTokens, NfContext context, int line) {
       // ... 现有逻辑 ...

       // 新增：检测 FunRefInfo
       if (value instanceof FunRefInfo) {
           // 检查目标参数类型
           Class<?> targetParamType = getTargetParameterType();
           if (LambdaProxyFactory.isFunctionalInterface(targetParamType)) {
               // 自动转换
               value = LambdaProxyFactory.createProxy(
                   (FunRefInfo)value, targetParamType, context, line
               );
           }
       }
       return value;
   }
   ```

2. **获取目标方法签名**
   ```java
   private Class<?> getTargetParameterType() {
       // 通过反射获取当前正在调用的方法的参数类型
       // 挑战：如何在参数解析时知道目标方法？
   }
   ```

## 总结

✅ **已验证可行**：NF Lambda 到 Java 函数式接口的转换完全可行！

🎯 **核心价值**：
- NF 脚本可以无缝使用 Java 生态系统
- 支持所有函数式接口
- 完整的 Stream API 支持

📈 **应用场景**：
- Stream API 操作（map, filter, reduce 等）
- 异步编程（CompletableFuture 等）
- 事件处理（回调函数）
- 自定义函数式接口

---
*文档创建时间：2025-01-08*
*作者：huanmin*
