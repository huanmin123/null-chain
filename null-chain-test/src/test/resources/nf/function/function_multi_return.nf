// ============================================
// 函数多返回值测试
// ============================================

// 定义返回多个值的函数
fun getNameAndAge(String name, int age)String,Integer {
    return name, age
}

// 多返回值函数调用
var name:String, age:Integer = getNameAndAge("张三", 25)
echo "name = {name}, age = {age}"

// 测试计算函数返回多个值
fun calculate(int a, int b)Integer,Integer {
    Integer sum = a + b
    Integer product = a * b
    return sum, product
}

var sum:Integer, product:Integer = calculate(5, 6)
echo "sum = {sum}, product = {product}"

export "多返回值测试完成"

