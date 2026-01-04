// 测试：for块内变量不能和外部重复
// 预期：应该抛出变量重复声明错误

Integer counter = 100

for i in 1..5 {
    Integer counter = i  // 这里应该报错：counter 在外部作用域已声明
}

export counter
