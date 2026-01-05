// 测试：do-while块内变量不能和外部重复
// 预期：应该抛出变量重复声明错误

Integer count = 0

do {
    Integer count = 5  // 这里应该报错：count 在外部作用域已声明
} while false

export count
