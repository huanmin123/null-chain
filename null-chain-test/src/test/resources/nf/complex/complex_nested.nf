// ============================================
// 复杂嵌套测试 - 多层嵌套结构
// ============================================

// 1. 多层if嵌套
Integer value = 10
String result = ""
if value > 0 {
    if value > 5 {
        if value > 8 {
            result = "大于8"
        } else {
            result = "5到8之间"
        }
    } else {
        result = "0到5之间"
    }
} else {
    result = "小于等于0"
}
echo "result = {result}"

// 2. if中嵌套for
Integer total = 0
if true {
    for i in 1..10 {
        total = total + i
    }
}
echo "total = {total}"

// 3. for中嵌套if
Integer count = 0
for i in 1..20 {
    if i % 2 == 0 {
        if i % 3 == 0 {
            count = count + 1
        }
    }
}
echo "count = {count}"

// 4. for中嵌套switch
String output = ""
for i in 1..5 {
    switch i {
        case 1
            output = output + "一"
        case 2
            output = output + "二"
        case 3
            output = output + "三"
        default
            output = output + "其他"
    }
}
echo "output = {output}"

// 5. switch中嵌套for
Integer switchSum = 0
switch 1 {
    case 1
        for i in 1..5 {
            switchSum = switchSum + i
        }
    case 2
        switchSum = 0
}
echo "switchSum = {switchSum}"

// 6. 三层嵌套循环
Integer deepSum = 0
for i in 1..3 {
    for j in 1..3 {
        for k in 1..3 {
            deepSum = deepSum + i * j * k
        }
    }
}
echo "deepSum = {deepSum}"

// 7. 复杂条件嵌套
Integer a = 10
Integer b = 20
Integer c = 30
String complex = ""
if a > 0 {
    if b > 10 {
        if c > 20 {
            for i in 1..3 {
                if i == 2 {
                    complex = "复杂条件满足"
                    break
                }
            }
        }
    }
}
echo "complex = {complex}"

// 8. 变量作用域测试
Integer outer = 10
if true {
    Integer middle = 20
    for i in 1..3 {
        Integer inner = 30
        outer = outer + i
        middle = middle + i
        echo "i = {i}, outer = {outer}, middle = {middle}, inner = {inner}"
    }
    echo "middle = {middle}"
}
echo "最终 outer = {outer}"

export outer




