// 测试：函数内变量可以遮蔽全局变量
// 预期：应该正常工作，函数内的 globalVar 会遮蔽全局 globalVar

Integer globalVar = 100

fun testFunction()Integer {
    Integer globalVar = 200  // 遮蔽全局的 globalVar
    return globalVar
}

Integer result = testFunction()
export result
