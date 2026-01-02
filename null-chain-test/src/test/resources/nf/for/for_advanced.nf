// ============================================
// FOR语句高级测试
// ============================================

// 1. 循环累加数组元素（模拟）
Integer total = 0
for i in 1..100 {
    total = total + i
}
echo "1到100的和 = {total}"

// 2. 循环中使用continue
Integer evenSum = 0
for i in 1..10 {
    if i % 2 != 0 {
        continue
    }
    evenSum = evenSum + i
    echo "偶数: {i}, 累计和: {evenSum}"
}
echo "偶数总和 = {evenSum}"

// 3. 循环中使用break
Integer firstDivisibleBy7 = 0
for i in 1..100 {
    if i % 7 == 0 {
        firstDivisibleBy7 = i
        echo "找到第一个7的倍数: {i}"
        break
    }
}
echo "第一个7的倍数 = {firstDivisibleBy7}"

// 4. 循环中使用breakall
Integer found = 0
for i in 1..10 {
    for j in 1..10 {
        if i * j == 50 {
            found = i * j
            echo "找到 i={i}, j={j}, 乘积={found}"
            breakall
        }
    }
}
echo "找到的值 = {found}"

// 5. 循环中的变量作用域
Integer outer = 0
for i in 1..5 {
    Integer inner = i * 2
    outer = outer + i
    echo "i = {i}, inner = {inner}, outer = {outer}"
}
echo "最终 outer = {outer}"

// 6. 复杂循环计算
Integer product = 1
for i in 1..5 {
    product = product * i
    if product > 50 {
        echo "乘积超过50，停止计算"
        break
    }
    echo "i = {i}, product = {product}"
}
echo "最终 product = {product}"

export product


