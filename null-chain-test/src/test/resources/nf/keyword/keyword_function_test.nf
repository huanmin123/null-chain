// 测试函数名使用关键字
// 这应该报错：函数名不能使用关键字 'if'

fun if(int x)Integer {
    return x
}

Integer result = if(10)
export result
