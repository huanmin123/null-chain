// 工具脚本：提供常用的工具函数和变量

// 全局变量
String prefix = "User"
Integer defaultAge = 18
String separator = "-"

// 格式化用户信息
fun formatUser(String name, Integer age)String {
    return prefix + separator + name + separator + age
}

// 计算两个数的和
fun add(Integer a, Integer b)Integer {
    return a + b
}

// 计算两个数的乘积
fun multiply(Integer a, Integer b)Integer {
    return a * b
}

// 获取默认年龄
fun getDefaultAge()Integer {
    return defaultAge
}

