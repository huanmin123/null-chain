// 测试：正常的嵌套块，不重复变量名
// 预期：应该正常工作

Integer outer = 100

if true {
    Integer inner1 = 200

    if true {
        Integer inner2 = 300
        Integer result = outer + inner1 + inner2
    }
}

for i in 1..5 {
    Integer loopVar = i
}

export outer
