// NF 脚本测试文件：测试函数引用基本功能
// 注意：暂不支持高阶函数（函数引用作为参数）、闭包等高级功能

// 1. 定义一个简单的加法函数
fun add(int a, int b)Integer {
    return a + b
}

// 2. 定义一个乘法函数
fun multiply(int x, int y)Integer {
    return x * y
}

// 3. 创建函数引用（省略类型，自动推导）
Fun addRef = add
Fun mulRef = multiply

// 4. 通过函数引用调用函数
Integer result1 = addRef(10, 20)
echo "addRef(10, 20) = "
echo result1

Integer result2 = mulRef(5, 6)
echo "mulRef(5, 6) = "
echo result2

