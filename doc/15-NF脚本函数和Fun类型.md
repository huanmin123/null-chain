# NF 脚本函数和 Fun 类型

本文档详细介绍 NF 脚本中的函数定义、Fun 类型和 Lambda 表达式的使用方法。

## 概述

NF 脚本提供了强大的函数式编程能力，支持：
- **函数定义和调用**（单返回值、多返回值、可变参数、递归）
- **Fun 类型**（函数类型，用于表示函数的签名）
- **Lambda 表达式**（匿名函数）
- **高阶函数**（函数作为参数、函数返回函数）
- **函数引用**（将已有函数作为引用传递）
- **闭包**（Lambda 捕获外部变量）
- **函数组合**（组合多个函数创建复杂逻辑）

## 函数定义

### 基本语法

**语法**：`fun 函数名(参数列表)返回值类型列表 { 函数体 }`

### 单返回值函数

```nf
// 定义单返回值函数
fun add(int a, int b) Integer {
    return a + b
}

// 调用函数
Integer result = add(10, 20)  // result = 30
```

### 多返回值函数

```nf
// 定义多返回值函数
fun getUserInfo() String, Integer {
    return "张三", 25
}

// 调用多返回值函数
var name, age = getUserInfo()
echo "姓名: {name}, 年龄: {age}"  // 输出: 姓名: 张三, 年龄: 25
```

### 可变参数函数

```nf
// 可变参数函数（参数后跟 ...）
fun sum(int... values) Integer {
    Integer total = 0
    for v in values {
        total = total + v
    }
    return total
}

// 调用可变参数函数
Integer result = sum(1, 2, 3, 4, 5)  // result = 15
```

### 递归函数

```nf
// 递归函数：计算阶乘
fun factorial(int n) Integer {
    if n <= 1 {
        return 1
    }
    return n * factorial(n - 1)
}

Integer fact = factorial(5)  // fact = 120
```

## Fun 类型（函数类型）

### 基本语法

**Fun 类型**用于表示函数的类型签名。

**语法**：`Fun<参数类型列表 : 返回值类型>`

### Fun 类型声明

```nf
// 单参数函数类型
Fun<Integer : Integer>     // 接收一个 Integer 参数，返回 Integer
Fun<String : Boolean>      // 接收一个 String 参数，返回 Boolean

// 多参数函数类型
Fun<Integer, Integer : Integer>    // 接收两个 Integer 参数，返回 Integer
Fun<String, Integer : Boolean>     // 接收 String 和 Integer 参数，返回 Boolean

// 简化的函数类型声明（不带类型参数）
Fun
```

### Fun 类型变量声明

```nf
// 声明 Fun 类型变量并赋值 Lambda 表达式
Fun<Integer : Integer> doubler = (x) -> {
    return x * 2
}

// 声明 Fun 类型变量（不指定类型参数）
Fun addFunc = add

// 使用 Fun 类型变量
Integer result = doubler(10)  // result = 20
```

## 函数引用

### 创建函数引用

**函数引用**可以将已定义的函数作为值传递给变量或参数。

**语法**：`Fun 变量名 = 函数名`

```nf
// 定义函数
fun add(int a, int b) Integer {
    return a + b
}

fun multiply(int a, int b) Integer {
    return a * b
}

// 创建函数引用
Fun addFunc = add
Fun mulFunc = multiply

// 通过函数引用调用
Integer sum = addFunc(5, 10)      // sum = 15
Integer product = mulFunc(5, 10)  // product = 50
```

### 函数引用作为参数

```nf
// 定义接收函数参数的函数
fun execute(
    Fun<Integer, Integer : Integer> operation,
    Integer a,
    Integer b
) Integer {
    return operation(a, b)
}

// 传入函数引用
Integer result1 = execute(add, 10, 20)      // result1 = 30
Integer result2 = execute(multiply, 10, 20) // result2 = 200
```

### 函数引用作为返回值

```nf
// 返回函数引用
fun getOperation(String op) Fun<Integer, Integer : Integer> {
    if op == "add" {
        Fun opFunc = add
        return opFunc
    }
    Fun opFunc = multiply
    return opFunc
}

// 使用返回的函数引用
Fun operation = getOperation("add")
Integer result = operation(10, 20)  // result = 30
```

## Lambda 表达式

### 基本语法

**Lambda 表达式**用于创建匿名函数。

**语法**：`(参数列表) -> { 函数体 }`

### 单参数 Lambda

```nf
// 单参数 Lambda
Fun<Integer : Integer> doubler = (x) -> {
    return x * 2
}

Integer result = doubler(10)  // result = 20
```

### 多参数 Lambda

```nf
// 双参数 Lambda
Fun<Integer, Integer : Integer> add = (a, b) -> {
    return a + b
}

Integer sum = add(100, 42)  // sum = 142

// 三参数 Lambda
Fun<Integer, Integer, Integer : Integer> sum3 = (a, b, c) -> {
    return a + b + c
}

Integer total = sum3(10, 20, 30)  // total = 60
```

### Lambda 作为参数传递

```nf
// 定义接收函数参数的函数
fun applyOperation(
    Fun<Integer, Integer : Integer> operation,
    Integer x,
    Integer y
) Integer {
    return operation(x, y)
}

// 直接传递 Lambda 表达式
Integer result1 = applyOperation((a, b) -> {
    return a + b
}, 10, 20)  // result1 = 30

Integer result2 = applyOperation((a, b) -> {
    return a * b
}, 10, 20)  // result2 = 200
```

### Lambda 的类型推断

Lambda 表达式的参数类型可以从 Fun 类型声明中推断：

```nf
// 参数类型从 Fun<Integer, Integer : Integer> 中推断
Fun<Integer, Integer : Integer> add = (a, b) -> {
    return a + b
}

// 等价于显式声明类型
Fun<Integer, Integer : Integer> add = (Integer a, Integer b) -> {
    return a + b
}
```

## 高阶函数

**高阶函数**是指接收函数作为参数或返回函数的函数。

### 函数作为参数

```nf
// 高阶函数：接收函数并执行
fun executeTwice(Fun<Integer : Integer> func, Integer value) Integer {
    Integer result1 = func(value)
    Integer result2 = func(result1)
    return result2
}

// 定义函数
Fun<Integer : Integer> increment = (x) -> { return x + 1 }
Fun<Integer : Integer> square = (x) -> { return x * x }

// 调用高阶函数
Integer result1 = executeTwice(increment, 5)  // 5 + 1 + 1 = 7
Integer result2 = executeTwice(square, 3)    // (3 * 3) * 3 = 27
```

### 函数返回函数

```nf
// 返回 Lambda 表达式
fun createMultiplier(int factor) Fun<Integer : Integer> {
    Fun<Integer : Integer> multiplier = (x) -> {
        return x * factor
    }
    return multiplier
}

// 使用返回的函数
Fun times3 = createMultiplier(3)
Fun times5 = createMultiplier(5)

Integer result1 = times3(10)  // result1 = 30
Integer result2 = times5(10)  // result2 = 50
```

### 返回已定义的函数引用

```nf
// 定义基础函数
fun add(int a, int b) Integer {
    return a + b
}

fun multiply(int a, int b) Integer {
    return a * b
}

// 返回函数引用
fun getAdder() Fun<Integer, Integer : Integer> {
    Fun adder = add
    return adder
}

fun getMultiplier() Fun<Integer, Integer : Integer> {
    Fun mul = multiply
    return mul
}

// 使用返回的函数引用
Fun addFunc = getAdder()
Fun mulFunc = getMultiplier()

Integer sum = addFunc(5, 10)      // sum = 15
Integer product = mulFunc(5, 10)  // product = 50
```

## 函数组合

### 基本组合

将多个函数组合在一起，创建更复杂的函数。

```nf
// 定义组合函数
fun compose(
    Fun<Integer : Integer> f1,
    Fun<Integer : Integer> f2
) Fun<Integer : Integer> {
    return (x) -> {
        return f2(f1(x))
    }
}

// 定义基础函数
Fun<Integer : Integer> increment = (x) -> { return x + 1 }
Fun<Integer : Integer> double = (x) -> { return x * 2 }

// 组合函数
Fun<Integer : Integer> process = compose(increment, double)

// 调用组合函数
Integer result = process(5)  // double(increment(5)) = double(6) = 12
```

### 链式组合

```nf
// 定义链式组合
fun andThen(
    Fun<Integer : Integer> f1,
    Fun<Integer : Integer> f2
) Fun<Integer : Integer> {
    return (x) -> {
        Integer temp = f1(x)
        return f2(temp)
    }
}

// 使用链式组合
Fun<Integer : Integer> add5 = (x) -> { return x + 5 }
Fun<Integer : Integer> mul3 = (x) -> { return x * 3 }

Fun<Integer : Integer> pipeline = andThen(add5, mul3)

Integer result = pipeline(10)  // (10 + 5) * 3 = 45
```

### 多函数组合

```nf
// 组合三个函数
fun compose3(
    Fun<Integer : Integer> f1,
    Fun<Integer : Integer> f2,
    Fun<Integer : Integer> f3
) Fun<Integer : Integer> {
    return (x) -> {
        return f3(f2(f1(x)))
    }
}

Fun<Integer : Integer> add1 = (x) -> { return x + 1 }
Fun<Integer : Integer> mul2 = (x) -> { return x * 2 }
Fun<Integer : Integer> sub5 = (x) -> { return x - 5 }

Fun<Integer : Integer> complex = compose3(add1, mul2, sub5)

Integer result = complex(10)  // ((10 + 1) * 2) - 5 = 17
```

## 工厂模式

使用高阶函数实现工厂模式，根据参数返回不同的函数。

### 简单工厂

```nf
// 操作工厂
fun createOperation(String op) Fun<Integer, Integer : Integer> {
    if op == "add" {
        return (a, b) -> {
            return a + b
        }
    }
    if op == "multiply" {
        return (a, b) -> {
            return a * b
        }
    }
    return (a, b) -> {
        return a - b
    }
}

// 使用工厂
Fun adder = createOperation("add")
Fun multiplier = createOperation("multiply")
Fun subtractor = createOperation("subtract")

Integer sum = adder(10, 20)         // sum = 30
Integer product = multiplier(10, 20) // product = 200
Integer diff = subtractor(20, 10)   // diff = 10
```

### 策略工厂

```nf
// 策略工厂：根据条件返回不同的处理函数
fun getStrategy(String type) Fun<Integer : Boolean> {
    if type == "positive" {
        return (x) -> {
            return x > 0
        }
    }
    if type == "negative" {
        return (x) -> {
            return x < 0
        }
    }
    return (x) -> {
        return x == 0
    }
}

// 使用策略工厂
Fun isPositive = getStrategy("positive")
Fun isNegative = getStrategy("negative")
Fun isZero = getStrategy("zero")

Boolean r1 = isPositive(10)   // r1 = true
Boolean r2 = isNegative(-5)   // r2 = true
Boolean r3 = isZero(0)        // r3 = true
```

## 闭包（变量捕获）

Lambda 表达式支持**闭包**，可以捕获外部作用域的变量。

### 基本闭包

```nf
// Lambda 捕获外部变量
fun createAdder(int offset) Fun<Integer : Integer> {
    // Lambda 捕获了外部变量 offset
    return (x) -> {
        return x + offset
    }
}

// 创建不同的加法器
Fun add5 = createAdder(5)
Fun add10 = createAdder(10)

Integer result1 = add5(100)   // result1 = 105
Integer result2 = add10(100)  // result2 = 110
```

### 多变量捕获

```nf
// 捕获多个外部变量
fun createCalculator(int addOffset, int mulOffset) Fun<Integer : Integer> {
    return (x) -> {
        return (x + addOffset) * mulOffset
    }
}

Fun calc1 = createCalculator(5, 2)
Fun calc2 = createCalculator(10, 3)

Integer result1 = calc1(10)  // (10 + 5) * 2 = 30
Integer result2 = calc2(10)  // (10 + 10) * 3 = 60
```

### 闭包与循环

```nf
// 在循环中创建闭包
fun createCounters() ArrayList {
    ArrayList counters = new

    for i in 1..3 {
        Fun counter = () -> {
            return i
        }
        counters.add(counter)
    }

    return counters
}

// 使用返回的计数器
ArrayList counters = createCounters()
for counter in counters {
    echo counter()  // 输出: 1, 2, 3
}
```

### 闭包规则

- Lambda 会捕获其定义作用域中可访问的所有变量
- 捕获的变量值在 Lambda 调用时使用
- Lambda 参数名不能与捕获的变量名冲突
- 闭包捕获的变量是按值捕获的

```nf
// 闭包变量与参数名冲突
fun test() Integer {
    Integer x = 100

    // 错误：Lambda 参数 x 与外部变量冲突
    // Fun conflict = (x) -> { return x + 1 }

    // 正确：使用不同的参数名
    Fun noConflict = (y) -> {
        return y + x  // 可以访问外部 x
    }

    return noConflict(10)  // 10 + 100 = 110
}
```

## 常见使用场景

### 回调函数

将函数作为参数传递，实现回调机制。

```nf
// 定义遍历函数
fun forEach(ArrayList list, Fun<String : > action) {
    for item in list {
        action(item)
    }
}

// 使用 Lambda 回调
ArrayList names = new
names.add("张三")
names.add("李四")
names.add("王五")

forEach(names, (name) -> {
    echo "姓名: {name}"
})
```

### 过滤和映射

```nf
// 过滤函数
fun filter(ArrayList list, Fun<Integer : Boolean> predicate) ArrayList {
    ArrayList result = new
    for item in list {
        if predicate(item) {
            result.add(item)
        }
    }
    return result
}

// 映射函数
fun map(ArrayList list, Fun<Integer : Integer> mapper) ArrayList {
    ArrayList result = new
    for item in list {
        result.add(mapper(item))
    }
    return result
}

// 使用示例
ArrayList numbers = new
numbers.add(1)
numbers.add(2)
numbers.add(3)
numbers.add(4)
numbers.add(5)

// 过滤偶数
Fun isEven = (x) -> { return x % 2 == 0 }
ArrayList evens = filter(numbers, isEven)  // [2, 4]

// 映射为平方
Fun square = (x) -> { return x * x }
ArrayList squares = map(numbers, square)  // [1, 4, 9, 16, 25]
```

### 延迟执行

创建函数并在需要时调用。

```nf
// 创建延迟执行的函数
fun defer(Fun< : > func) {
    echo "准备执行..."
    func()
    echo "执行完成"
}

// 使用延迟执行
defer(() -> {
    echo "延迟执行的逻辑"
})
```

### 柯里化（Currying）

将多参数函数转换为单参数函数的链式调用。

```nf
// 柯里化加法
fun curriedAdd(int a) Fun<Integer : Integer> {
    return (b) -> {
        return a + b
    }
}

// 使用柯里化
Fun add5 = curriedAdd(5)
Fun add10 = curriedAdd(10)

Integer result1 = add5(20)   // result1 = 25
Integer result2 = add10(20)  // result2 = 30
```

### 记忆化（Memoization）

缓存函数结果，避免重复计算。

```nf
// 记忆化包装器
fun memoize(Fun<Integer : Integer> func) Fun<Integer : Integer> {
    Map cache = new

    return (x) -> {
        if cache.containsKey(x) {
            return cache.get(x)
        }
        Integer result = func(x)
        cache.put(x, result)
        return result
    }
}

// 定义计算密集的函数
Fun<Integer : Integer> fibonacci = (n) -> {
    if n <= 1 {
        return n
    }
    Fun<Integer : Integer> fib = fibonacci  // 递归引用
    return fib(n - 1) + fib(n - 2)
}

// 使用记忆化
Fun<Integer : Integer> memoFib = memoize(fibonacci)

Integer result = memoFib(40)  // 快速计算，使用了缓存
```

## 注意事项

### 类型匹配

Lambda 参数类型和返回值类型必须与 Fun 类型声明匹配。

```nf
// 正确：类型匹配
Fun<Integer : Integer> valid = (x) -> {
    return x * 2
}

// 错误：返回类型不匹配
// Fun<Integer : Integer> invalid = (x) -> {
//     return "hello"  // 返回 String，但期望 Integer
// }
```

### 参数数量

Lambda 参数数量必须与 Fun 类型声明的参数数量一致。

```nf
// 正确：参数数量匹配
Fun<Integer, Integer : Integer> valid = (a, b) -> {
    return a + b
}

// 错误：参数数量不匹配
// Fun<Integer, Integer : Integer> invalid = (a) -> {
//     return a
// }
```

### Lambda 语法

NF 脚本的 Lambda 语法为 `(params) -> { body }`，**不支持** Java 8 的简化语法。

**支持的语法**：
```nf
// ✅ 正确：NF 脚本 Lambda 语法
(x) -> { return x * 2 }
(a, b) -> { return a + b }
() -> { echo "no params" }
```

**不支持的语法**：
```nf
// ❌ 错误：Java 8 简化语法，NF 脚本不支持
x -> x * 2
x -> { return x * 2 }
```

### Java 函数式接口

如果需要使用 Java 的函数式接口（如 `Consumer<T>`、`Function<T,R>`、`Predicate<T>` 等），仍需要先实现具体的类。

**使用 Java 函数式接口的正确做法**：

```nf
// 1. 先在 Java 代码中实现接口
public class PrintConsumer implements Consumer<String> {
    @Override
    public void accept(String item) {
        System.out.println(item);
    }
}

// 2. 在 NF 脚本中导入并使用对象
import type com.example.PrintConsumer

PrintConsumer printer = new
list.forEach(printer)  // 传入对象而非 Lambda
```

**使用 NF 脚本 Lambda 的替代方案**：

```nf
// 使用 NF 脚本的 Fun 类型替代 Java 函数式接口
fun processList(Fun<String : > action, ArrayList list) {
    for item in list {
        action(item)
    }
}

// 调用并传递 Lambda
processList((item) -> { echo item }, list)
```

### Fun 类型变量

Fun 类型变量可以存储函数引用或 Lambda 表达式。

```nf
// 定义函数
fun add(int a, int b) Integer {
    return a + b
}

// Fun 类型变量存储函数引用
Fun var1 = add

// Fun 类型变量存储 Lambda 表达式
Fun var2 = (a, b) -> {
    return a + b
}

// 两者可以互换使用
Integer result1 = var1(10, 20)  // 30
Integer result2 = var2(10, 20)  // 30
```

## 完整示例

### 综合示例：数据处理管道

```nf
// 定义工具函数
fun filter(ArrayList data, Fun<Integer : Boolean> predicate) ArrayList {
    ArrayList result = new
    for item in data {
        if predicate(item) {
            result.add(item)
        }
    }
    return result
}

fun map(ArrayList data, Fun<Integer : Integer> mapper) ArrayList {
    ArrayList result = new
    for item in data {
        result.add(mapper(item))
    }
    return result
}

fun reduce(ArrayList data, Integer init, Fun<Integer, Integer : Integer> accumulator) Integer {
    Integer result = init
    for item in data {
        result = accumulator(result, item)
    }
    return result
}

// 创建数据处理管道
fun createPipeline() Fun<ArrayList : Integer> {
    return (data) -> {
        // 过滤：保留偶数
        ArrayList step1 = filter(data, (x) -> { return x % 2 == 0 })

        // 映射：平方
        ArrayList step2 = map(step1, (x) -> { return x * x })

        // 归约：求和
        Integer final = reduce(step2, 0, (acc, x) -> { return acc + x })

        return final
    }
}

// 使用管道
Fun pipeline = createPipeline()

ArrayList numbers = new
numbers.add(1)
numbers.add(2)
numbers.add(3)
numbers.add(4)
numbers.add(5)

// 处理：(2² + 4²) = 4 + 16 = 20
Integer result = pipeline(numbers)

echo "处理结果: {result}"  // 输出: 处理结果: 20
```

## 相关文档

- **[NF 脚本](./11-NF脚本.md)** - NF 脚本语言完整文档
- **[NF 脚本默认导入类型](./14-NF脚本默认导入类型.md)** - 默认导入类型列表
