// 测试：while块内变量不能和外部重复
// 预期：应该抛出变量重复声明错误

Integer index = 0
Integer limit = 5

while index < limit {
    Integer limit = 10  // 这里应该报错：limit 在外部作用域已声明
    index = index + 1
}

export index
