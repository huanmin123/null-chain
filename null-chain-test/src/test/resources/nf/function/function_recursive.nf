// ============================================
// 递归函数测试
// ============================================

// 定义阶乘函数（递归）
fun factorial(int n)Integer {
    if n <= 1 {
        return 1
    }
    return n * factorial(n - 1)
}

// 测试递归函数
Integer result1 = factorial(5)
echo "factorial(5) = {result1}"

Integer result2 = factorial(0)
echo "factorial(0) = {result2}"

Integer result3 = factorial(1)
echo "factorial(1) = {result3}"

// 定义斐波那契函数（递归）
fun fibonacci(int n)Integer {
    if n <= 1 {
        return n
    }
    return fibonacci(n - 1) + fibonacci(n - 2)
}

Integer fib = fibonacci(7)
echo "fibonacci(7) = {fib}"

export "递归函数测试完成"

