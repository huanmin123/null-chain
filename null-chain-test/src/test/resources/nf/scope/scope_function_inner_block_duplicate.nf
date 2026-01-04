// 测试：函数内块级变量不能和函数作用域内变量重复
// 预期：应该抛出变量重复声明错误

fun testFunction()Integer {
    Integer funcVar = 50

    if true {
        Integer funcVar = 100  // 这里应该报错：funcVar 在函数作用域已声明
    }

    return funcVar
}

Integer result = testFunction()
export result
