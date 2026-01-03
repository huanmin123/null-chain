// ============================================
// IF语句基础测试
// ============================================

// 1. 基本if语句
Integer age = 18
if age >= 18 {
    echo "已成年"
} else {
    echo "未成年"
}

// 2. if-else if-else
Integer score = 85
if score >= 90 {
    echo "优秀"
} else if score >= 80 {
    echo "良好"
} else if score >= 60 {
    echo "及格"
} else {
    echo "不及格"
}

// 3. 嵌套if语句
Integer x = 10
Integer y = 20
if x > 0 {
    if y > 0 {
        echo "x和y都大于0"
    } else {
        echo "x大于0，但y不大于0"
    }
} else {
    echo "x不大于0"
}

// 4. 逻辑运算符
Boolean a = true
Boolean b = false
if a && b {
    echo "a和b都为true"
} else if a || b {
    echo "a或b为true"
} else {
    echo "a和b都为false"
}

// 5. 比较运算符
Integer num1 = 10
Integer num2 = 20
if num1 < num2 {
    echo "num1小于num2"
}
if num1 == 10 {
    echo "num1等于10"
}
if num2 != 10 {
    echo "num2不等于10"
}

export "if测试完成"




