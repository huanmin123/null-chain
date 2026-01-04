// 测试：不同函数之间的变量可以重名
// 预期：应该正常工作

fun funcA()Integer {
    Integer result = 100
    return result
}

fun funcB()Integer {
    Integer result = 200  // 不同的函数，result 可以重名
    return result
}

Integer a = funcA()
Integer b = funcB()
Integer sum = a + b

export sum
