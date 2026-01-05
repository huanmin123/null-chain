// 测试：if块内变量不能和外部重复
// 预期：应该抛出变量重复声明错误

Integer outer = 100

if true {
    Integer outer = 200  // 这里应该报错：outer 在外部作用域已声明
}

export outer
