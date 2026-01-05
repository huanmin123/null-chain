// 测试：块内变量在块外可以重新定义
// 预期：应该正常工作

if true {
    Integer temp = 100
}

Integer temp = 200  // 这是合法的，在不同的作用域

export temp
