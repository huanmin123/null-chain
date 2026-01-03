// ============================================
// var 关键字高级测试
// ============================================

// 1. 复杂表达式自动推导
var result = (10 + 20) * 2 - 5
echo "result = {result}"

// 2. 变量引用自动推导
var a = 100
var b = 200
var total = a + b
echo "total = {total}"

// 3. 字符串模板自动推导
var template = ```
这是一个模板字符串
变量 a = {a}
变量 b = {b}
```
echo template

// 4. 手动指定类型 - 复杂表达式
var calculation:Integer = (5 + 3) * 2
echo "calculation = {calculation}"

// 5. 作用域测试 - 在 if 块中
var outer = "外部变量"
echo "outer = {outer}"

if true {
    var inner = "内部变量"
    echo "inner = {inner}"
    echo "outer in if = {outer}"
}

// 6. 作用域测试 - 在 for 循环中
var loopVar = "循环外部"
echo "loopVar before loop = {loopVar}"

for i in 1..3 {
    var loopInner = "循环内部 " + i
    echo "loopInner = {loopInner}"
    echo "loopVar in loop = {loopVar}"
}

// 7. 变量重新赋值（var 定义的变量）
var counter = 0
echo "counter = {counter}"
counter = counter + 1
echo "counter after increment = {counter}"

// 8. 混合使用 var 和传统声明
var varNum = 10
Integer traditionalNum = 20
var sum = varNum + traditionalNum
echo "sum of var and traditional = {sum}"

// 9. 手动指定类型 - 类型兼容性测试
var number:Integer = 100
echo "number = {number}"

// 10. 嵌套表达式自动推导
var nested = (1 + 2) * (3 + 4)
echo "nested = {nested}"

// 11. 字符串和数字混合表达式
var mixed = "结果: " + (10 + 20)
echo "mixed = {mixed}"

// 12. 布尔表达式自动推导
var isGreater = 10 > 5
echo "isGreater = {isGreater}"

// 13. 手动指定类型 - 布尔表达式
var isEqual:Boolean = 10 == 10
echo "isEqual = {isEqual}"

export "var高级测试完成"

