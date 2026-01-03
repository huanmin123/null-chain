// ============================================
// 函数功能演示脚本
// 展示函数的各种使用场景
// ============================================

echo "========== 函数功能演示 =========="

// 1. 基础函数 - 单返回值
fun add(int a, int b)Integer {
    return a + b
}

Integer sum = add(10, 20)
echo "add(10, 20) = {sum}"

// 2. 多返回值函数
fun getNameAndAge(String name, int age)String,Integer {
    return name, age
}

var name:String, age:Integer = getNameAndAge("张三", 25)
echo "name = {name}, age = {age}"

// 3. 函数在表达式中使用
fun multiply(int a, int b)Integer {
    return a * b
}

Integer result = multiply(5, 6) + multiply(2, 3)
echo "multiply(5, 6) + multiply(2, 3) = {result}"

// 4. 可变参数函数
fun sum(int first, int... rest)Integer {
    Integer total = first
    for item in rest {
        total = total + item
    }
    return total
}

Integer total = sum(1, 2, 3, 4, 5)
echo "sum(1, 2, 3, 4, 5) = {total}"

// 5. 递归函数
fun factorial(int n)Integer {
    if n <= 1 {
        return 1
    }
    return n * factorial(n - 1)
}

Integer fact = factorial(5)
echo "factorial(5) = {fact}"

// 6. 复杂函数 - 多返回值
fun calculate(int a, int b, int c)Integer,Integer {
    Integer sum = a + b + c
    Integer product = a * b * c
    return sum, product
}

var s:Integer, p:Integer = calculate(2, 3, 4)
echo "sum = {s}, product = {p}"

echo "========== 演示完成 =========="
export "函数功能演示完成"

