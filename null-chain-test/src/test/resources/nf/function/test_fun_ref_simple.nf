// NF 脚本测试文件：测试函数引用基本功能

// 定义一个简单的加法函数
fun add(int a, int b)Integer {
    return a + b
}

// 创建函数引用（省略类型，自动推导）
Fun addRef = add

// 通过函数引用调用函数
Integer result = addRef(10, 20)
echo "addRef(10, 20) = "
echo result

export result
