// ============================================
// 控制语句基础测试（break, continue, breakall）
// ============================================

// 1. break语句
Integer found = 0
for i in 1..10 {
    if i == 5 {
        found = i
        break
    }
}
echo "找到的值: {found}"

// 2. continue语句
Integer evenSum = 0
for i in 1..10 {
    if i % 2 != 0 {
        continue
    }
    evenSum = evenSum + i
}
echo "偶数和: {evenSum}"

// 3. breakall语句
Integer result = 0
for i in 1..5 {
    for j in 1..5 {
        if i * j == 12 {
            result = i * j
            echo "找到 i={i}, j={j}, 乘积={result}"
            breakall
        }
    }
}
echo "最终结果: {result}"

// 4. break在if中
Integer count = 0
for i in 1..10 {
    count = count + 1
    if count >= 5 {
        break
    }
}
echo "count = {count}"

// 5. continue跳过某些值
Integer sum = 0
for i in 1..10 {
    if i == 3 || i == 7 {
        continue
    }
    sum = sum + i
}
echo "跳过3和7的和: {sum}"

export sum




