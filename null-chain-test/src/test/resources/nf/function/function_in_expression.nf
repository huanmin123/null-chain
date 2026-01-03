// ============================================
// 函数在表达式中的使用测试
// ============================================

// 定义乘法函数
fun multiply(int a, int b)Integer {
    return a * b
}

// 函数调用参与表达式计算
Integer result1 = multiply(5, 6) + multiply(2, 3)
echo "multiply(5, 6) + multiply(2, 3) = {result1}"

// 函数调用在复杂表达式中
Integer result2 = multiply(2, 3) * multiply(4, 5) - multiply(1, 2)
echo "multiply(2, 3) * multiply(4, 5) - multiply(1, 2) = {result2}"

// 函数调用与其他变量混合
Integer x = 10
Integer y = 20
Integer result3 = multiply(x, y) + multiply(5, 5)
echo "multiply({x}, {y}) + multiply(5, 5) = {result3}"

export "表达式中的函数调用测试完成"

