// 测试：复杂的嵌套结构
// 预期：应该正常工作

Integer level1 = 100

if true {
    Integer level2 = 200

    for i in 1..3 {
        Integer level3 = 300

        if true {
            Integer level4 = 400
        }
    }
}

export level1
