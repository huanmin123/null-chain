// 测试基本NF语法，确保函数引用功能没有破坏现有功能

// 1. 基本变量赋值
Integer a = 10
String str = "hello"
echo "a = "
echo a
echo "str = "
echo str

// 2. 基本函数定义和调用
fun testAdd(int x, int y)Integer {
    return x + y
}

Integer result = testAdd(5, 3)
echo "testAdd(5, 3) = "
echo result

// 3. 多参数函数
fun testMulti(int a, int b, int c)Integer {
    return a + b + c
}

Integer result2 = testMulti(1, 2, 3)
echo "testMulti(1, 2, 3) = "
echo result2

export result2
