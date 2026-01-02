// ============================================
// 动态范围循环测试 - for i in start..end
// ============================================

// 测试1: 基础动态范围（变量作为范围值）
Integer start = 1
Integer end = 5
Integer sum = 0
for i in start..end {
    sum = sum + i
}
echo "测试1 - 基础动态范围循环: sum = {sum}"
// 期望: sum = 1+2+3+4+5 = 15

// 测试2: 动态范围与计算结合
Integer a = 10
Integer b = 2
Integer start2 = a - b  // 8
Integer end2 = a + b    // 12
Integer sum2 = 0
for i in start2..end2 {
    sum2 = sum2 + i
}
echo "测试2 - 计算后的动态范围: sum2 = {sum2}"
// 期望: sum2 = 8+9+10+11+12 = 50

// 测试3: 动态范围在嵌套循环中的应用
Integer outerStart = 1
Integer outerEnd = 3
Integer innerStart = 1
Integer innerEnd = 2
Integer product = 1
for x in outerStart..outerEnd {
    for y in innerStart..innerEnd {
        product = product * x
    }
}
echo "测试3 - 嵌套动态范围循环: product = {product}"
// x=1时 product=1*1*1=1, x=2时 product=1*2*2=4, x=3时 product=4*3*2=24

export sum
