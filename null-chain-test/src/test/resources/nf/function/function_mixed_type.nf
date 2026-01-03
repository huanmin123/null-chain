// var混合类型声明测试脚本
// 测试多返回值函数调用时，不同变量的类型声明可以是可选的

// 定义一个返回姓名和年龄的函数
fun getPersonInfo()String,Integer {
    return "张三", 25
}

// 测试1: 全部自动推导
var name1, age1 = getPersonInfo()
echo "测试1 - 全部自动推导: name1 = {name1}, age1 = {age1}"

// 测试2: 第一个自动推导，第二个指定类型
var name2, age2:Integer = getPersonInfo()
echo "测试2 - 第一个自动推导，第二个指定类型: name2 = {name2}, age2 = {age2}"

// 测试3: 第一个指定类型，第二个自动推导
var name3:String, age3 = getPersonInfo()
echo "测试3 - 第一个指定类型，第二个自动推导: name3 = {name3}, age3 = {age3}"

// 测试4: 全部指定类型（向后兼容）
var name4:String, age4:Integer = getPersonInfo()
echo "测试4 - 全部指定类型: name4 = {name4}, age4 = {age4}"

// 定义一个返回多个计算结果的函数
fun calculate(int a, int b)Integer,Integer,Integer {
    Integer sum = a + b
    Integer diff = a - b
    Integer product = a * b
    return sum, diff, product
}

// 测试5: 三返回值，混合类型声明
var result1, result2:Integer, result3 = calculate(10, 5)
echo "测试5 - 三返回值混合类型: result1 = {result1}, result2 = {result2}, result3 = {result3}"

// 测试6: 三返回值，全部自动推导
var r1, r2, r3 = calculate(20, 8)
echo "测试6 - 三返回值全部自动推导: r1 = {r1}, r2 = {r2}, r3 = {r3}"

// 导出测试结果
echo "var混合类型声明测试完成"
export "测试完成"
