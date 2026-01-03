// ============================================
// NF 脚本基础语法示例
// 本文件展示了 NF 脚本语言的所有基础语法特性
// ============================================

// ========== 1. 注释 ==========
// 单行注释：使用 // 开头
// 注释可以出现在代码的任何位置

// ========== 2. 导入类型 ==========
// 导入自定义 Java 类型（常用内置类型如 String、Integer 等无需导入）
// import type com.example.UserEntity

// ========== 3. 导入任务 ==========
// 导入任务并起别名，方便后续调用
// import task com.example.TestTask as testTask

// ========== 4. 变量声明和赋值 ==========

// 4.1 基本类型变量声明
String str = "Hello World"
Integer num = 100
Double price = 99.99
Boolean isActive = true
Boolean isDisabled = false

// 4.2 变量赋值（使用已声明的变量）
String greeting = "Welcome"
Integer count = 0

// 4.3 表达式赋值
Integer sum = 10 + 20
Integer product = 5 * 6
Integer difference = 100 - 50
Double quotient = 100.0 / 3.0

// 4.4 字符串拼接
String fullName = "John" + " " + "Doe"
String message = greeting + ", " + str

// 4.5 对象创建和操作
// Map map = new
// map.put("key", "value")
// map.put("count", count)

// ========== 5. 打印输出 (echo) ==========

// 5.1 基本打印
echo "Hello, NF Script!"

// 5.2 打印多个参数（用逗号分隔）
echo "Name:", fullName, "Age:", num

// 5.3 打印变量
echo "Count:", count
echo "Sum:", sum

// 5.4 字符串模板占位符（使用 {变量名}）
echo "Greeting: {greeting}, Message: {message}"

// 5.5 表达式占位符
echo "Sum: {sum + 10}, Product: {product * 2}"

// 5.6 特殊字符：\t (制表符) 和 \n (换行符)
echo "Column1", \t, "Column2", \t, "Column3", \n

// 5.7 模板字符串（多行字符串，保留换行格式）
echo ```
多行内容示例：
第一行: {greeting}
第二行: {message}
第三行: 计算结果 = {sum + product}
```

// ========== 6. 条件判断 (if/else) ==========

// 6.1 简单 if 语句
if isActive {
    echo "状态: 激活"
}

// 6.2 if-else 语句
if count > 10 {
    echo "Count 大于 10"
} else {
    echo "Count 小于等于 10"
}

// 6.3 if-else if-else 语句
if sum > 100 {
    echo "Sum 大于 100"
} else if sum > 50 {
    echo "Sum 大于 50 但小于等于 100"
} else {
    echo "Sum 小于等于 50"
}

// 6.4 逻辑运算符：&& (and), || (or)
if isActive && count > 0 {
    echo "状态激活且计数大于0"
}

if isActive || isDisabled {
    echo "状态为激活或禁用"
}

// 6.5 比较运算符：>, <, >=, <=, ==, !=
if num > 50 {
    echo "Num 大于 50"
}

if num == 100 {
    echo "Num 等于 100"
}

if num != 0 {
    echo "Num 不等于 0"
}

// ========== 7. 多分支判断 (switch) ==========

// 7.1 基本 switch 语句
switch num {
    case 10
        echo "Num 是 10"
    case 50
        echo "Num 是 50"
    case 100
        echo "Num 是 100"
    default
        echo "Num 是其他值"
}

// 7.2 多个 case 值
switch str {
    case "Hello", "Hi"
        echo "打招呼"
    case "Goodbye", "Bye"
        echo "告别"
    default
        echo "其他"
}

// ========== 8. 循环 (for) ==========

// 8.1 基本 for 循环
for i in 1..5 {
    echo "循环次数: {i}"
}

// 8.2 循环中使用变量
Integer total = 0
for i in 1..10 {
    total = total + i
    echo "i = {i}, total = {total}"
}

// 8.3 嵌套循环
for i in 1..3 {
    echo "外层循环: {i}"
    for j in 1..2 {
        echo "  内层循环: {j}"
    }
}

// 8.4 循环控制：continue（跳过本次循环）
for i in 1..10 {
    if i == 5 {
        continue
    }
    echo "i = {i}"
}

// 8.5 循环控制：break（跳出当前循环）
for i in 1..10 {
    if i == 7 {
        break
    }
    echo "i = {i}"
}

// 8.6 循环控制：breakall（跳出所有嵌套循环）
for i in 1..5 {
    for j in 1..5 {
        if j == 3 {
            breakall
        }
        echo "i={i}, j={j}"
    }
}

// ========== 9. 任务调用 (run) ==========

// 9.1 基本任务调用（需要先导入任务）
// run testTask()

// 9.2 带参数的任务调用
// run testTask(str, num)

// 9.3 任务调用并绑定返回值
// run testTask() -> result:String

// 9.4 多任务并发执行
// run testTask1(), testTask2() -> results:Map

// ========== 10. 导出变量 (export) ==========

// 10.1 导出变量
export total

// 10.2 导出表达式计算结果
export sum + product

// 10.3 导出字符串拼接结果
export greeting + " " + message

// 10.4 导出模板字符串
export ```
最终结果:
总和: {sum}
乘积: {product}
总计: {total}
```

// ========== 11. 一行多语句 ==========
// 使用分号 ; 分隔多个语句
Integer a = 1 + 1
Integer b = 2 + 2
echo "a = {a}, b = {b}"
export b



