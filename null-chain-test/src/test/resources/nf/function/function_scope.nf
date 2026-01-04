// ============================================
// 函数作用域测试
// ============================================

// 全局变量
Integer globalVar = 100
String globalStr = "global"

// 定义函数，测试作用域隔离
fun testScope(int local)Integer {
    // 函数内部可以访问全局变量
    Integer inner = 50
    Integer result = globalVar + local + inner
    echo "函数内部: globalVar = {globalVar}, local = {local}, inner = {inner}"
    return result
}

// 调用函数
Integer result = testScope(10)
echo "函数调用结果: {result}"

// 验证全局变量未被修改
echo "全局变量 globalVar = {globalVar}"

// 测试函数参数与全局变量同名
Integer x = 5
fun testShadow(int x)Integer {
    // 函数参数会遮蔽全局变量
    return x * 2
}

Integer shadowResult = testShadow(10)
echo "testShadow(10) = {shadowResult}, 全局 x = {x}"

export "函数作用域测试完成"



