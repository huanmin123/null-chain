// 数学工具脚本

// 数学常量
Integer PI = 314
Integer E = 271

// 计算两个数的和
fun add(Integer a, Integer b)Integer {
    return a + b
}

// 计算平方
fun square(Integer x)Integer {
    return x * x
}

// 计算立方
fun cube(Integer x)Integer {
    return x * x * x
}

// 计算最大值
fun max(Integer a, Integer b)Integer {
    if a > b {
        return a
    } else {
        return b
    }
}

// 计算最小值
fun min(Integer a, Integer b)Integer {
    if a < b {
        return a
    } else {
        return b
    }
}

// 计算阶乘
fun factorial(Integer n)Integer {
    if n <= 1 {
        return 1
    }
    Integer result = 1
    Integer start = 2
    Integer end = n + 1
    for i in start..end {
        result = result * i
    }
    return result
}

