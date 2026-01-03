// ============================================
// 函数基础测试 - 单返回值
// ============================================

// 定义加法函数
fun add(int a, int b)Integer {
    return a + b
}

// 调用函数并赋值
Integer result = add(10, 20)
echo "add(10, 20) = {result}"

// 测试不同类型的参数
fun concat(String a, String b)String {
    return a + b
}

String str = concat("Hello", " World")
echo "concat('Hello', ' World') = {str}"

export "函数基础测试完成"


