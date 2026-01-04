// 测试：switch块内变量不能和外部重复
// 预期：应该抛出变量重复声明错误

Integer value = 10

switch value {
    case 1 {
        Integer value = 20  // 这里应该报错：value 在外部作用域已声明
    }
    default {
        Integer result = 30
    }
}

export value
