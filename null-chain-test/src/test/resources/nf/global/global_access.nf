// 测试：使用 global.xxx 访问被遮蔽的全局变量
Integer x = 100

fun test(int x)Integer {
    // 参数 x 遮蔽了全局 x
    // 使用 global.x 访问全局的 x
    return x + global.x
}

Integer result = test(50)
export result
// 预期：result = 50 + 100 = 150
