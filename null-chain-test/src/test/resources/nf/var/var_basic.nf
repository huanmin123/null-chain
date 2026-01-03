// ============================================
// var 关键字基础测试
// ============================================

// 1. 自动类型推导 - 字符串
var name = "12312"
echo "name (自动推导) = {name}, 类型应该是 String"

// 2. 自动类型推导 - 整数
var num = 123
echo "num (自动推导) = {num}, 类型应该是 Integer"

// 3. 自动类型推导 - 浮点数
var price = 99.99
echo "price (自动推导) = {price}, 类型应该是 Double"

// 4. 自动类型推导 - 布尔值
var flag = true
echo "flag (自动推导) = {flag}, 类型应该是 Boolean"

// 5. 手动指定类型 - 字符串
var name2:String = "1231"
echo "name2 (手动指定String) = {name2}"

// 6. 手动指定类型 - 整数
var num2:Integer = 456
echo "num2 (手动指定Integer) = {num2}"

// 7. 手动指定类型 - 浮点数
var price2:Double = 88.88
echo "price2 (手动指定Double) = {price2}"

// 8. 手动指定类型 - 布尔值
var flag2:Boolean = false
echo "flag2 (手动指定Boolean) = {flag2}"

// 9. 自动推导 - 表达式结果
var sum = 10 + 20 + 30
echo "sum (表达式自动推导) = {sum}"

// 10. 手动指定类型 - 表达式结果
var product:Integer = 2 * 3 * 4
echo "product (表达式手动指定) = {product}"

// 11. 字符串拼接自动推导
var greeting = "Hello" + " " + "World"
echo "greeting = {greeting}"

// 12. 手动指定类型 - 字符串拼接
var message:String = "Test" + " " + "Message"
echo "message = {message}"

export "var基础测试完成"

