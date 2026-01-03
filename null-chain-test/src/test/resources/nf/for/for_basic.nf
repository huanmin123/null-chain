// ============================================
// FOR语句基础测试
// ============================================

// 1. 基本for循环
Integer sum = 0
for i in 1..10 {
    sum = sum + i
}
echo "1到10的和 = {sum}"

// 2. 循环打印
for i in 1..5 {
    echo "i = {i}"
}

// 3. 循环计算阶乘
Integer factorial = 1
for i in 1..5 {
    factorial = factorial * i
    echo "i = {i}, factorial = {factorial}"
}
echo "5的阶乘 = {factorial}"

// 4. 循环中的条件判断
for i in 1..10 {
    if i % 2 == 0 {
        echo "{i} 是偶数"
    } else {
        echo "{i} 是奇数"
    }
}

// 5. 嵌套循环
for i in 1..3 {
    for j in 1..3 {
        echo "i = {i}, j = {j}, 乘积 = {i * j}"
    }
}

export factorial




