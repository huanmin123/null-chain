// while循环continue测试
// 计算1-10中奇数的和
Integer i = 1
Integer sum = 0
while i <= 10 {
    if (i % 2 == 0) {
        i = i + 1
        continue
    }
    sum = sum + i
    i = i + 1
}
export sum
