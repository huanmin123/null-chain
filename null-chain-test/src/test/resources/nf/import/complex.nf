// 复杂脚本：包含多个函数和变量，函数之间相互调用

// 全局变量
Integer baseValue = 10
String baseName = "Base"

// 基础计算函数
fun calculate(Integer x)Integer {
    return x * baseValue
}

// 格式化函数
fun formatResult(Integer value)String {
    return baseName + ":" + value
}

// 组合函数：调用其他函数
fun process(Integer input)String {
    Integer calculated = calculate(input)
    return formatResult(calculated)
}

// 递归函数
fun sum(Integer n)Integer {
    if n <= 0 {
        return 0
    }
    return n + sum(n - 1)
}

// 使用全局变量的函数
fun getName()String {
    return baseName
}

