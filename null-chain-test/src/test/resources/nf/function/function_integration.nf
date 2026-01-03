// ============================================
// 函数集成测试 - 综合场景
// ============================================

// 定义多个函数
fun add(int a, int b)Integer {
    return a + b
}

fun multiply(int a, int b)Integer {
    return a * b
}

fun getInfo(String name, int age)String,Integer {
    String info = "姓名: " + name + ", 年龄: " + age
    return info, age
}

// 函数组合使用
Integer sum = add(10, 20)
Integer product = multiply(5, 6)
Integer total = sum + product
echo "sum = {sum}, product = {product}, total = {total}"

// 函数在表达式中使用
Integer result = multiply(add(2, 3), multiply(4, 5))
echo "multiply(add(2, 3), multiply(4, 5)) = {result}"

// 多返回值函数
var info:String, age:Integer = getInfo("李四", 30)
echo "info = {info}, age = {age}"

// 函数调用结果参与计算
Integer finalResult = add(multiply(2, 3), multiply(4, 5))
echo "finalResult = {finalResult}"

export "函数集成测试完成"



