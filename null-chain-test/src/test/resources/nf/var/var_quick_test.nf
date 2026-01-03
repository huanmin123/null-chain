// ============================================
// var 快速测试脚本
// 用于快速验证 var 关键字功能是否正常
// ============================================

echo "========== var 快速测试开始 =========="

// 1. 测试自动类型推导 - 字符串
var str = "Hello var"
echo "1. 字符串自动推导: str = {str}"

// 2. 测试自动类型推导 - 整数
var num = 42
echo "2. 整数自动推导: num = {num}"

// 3. 测试自动类型推导 - 浮点数
var price = 99.99
echo "3. 浮点数自动推导: price = {price}"

// 4. 测试自动类型推导 - 布尔值
var flag = true
echo "4. 布尔值自动推导: flag = {flag}"

// 5. 测试手动指定类型 - 字符串
var name:String = "test"
echo "5. 手动指定String: name = {name}"

// 6. 测试手动指定类型 - 整数
var count:Integer = 100
echo "6. 手动指定Integer: count = {count}"

// 7. 测试表达式自动推导
var sum = 10 + 20 + 30
echo "7. 表达式自动推导: sum = {sum}"

// 8. 测试手动指定类型 - 表达式
var product:Integer = 2 * 3 * 4
echo "8. 表达式手动类型: product = {product}"

// 9. 测试作用域
var outer = "外部"
if true {
    var inner = "内部"
    echo "9. 作用域测试: outer = {outer}, inner = {inner}"
}

// 10. 测试变量重新赋值
var counter = 0
counter = counter + 1
echo "10. 变量重新赋值: counter = {counter}"

echo "========== var 快速测试完成 =========="
export "var快速测试成功"

