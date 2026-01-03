// 测试 while 循环中的 continue
Integer i = 1
Integer sum = 0
while i <= 10 {
    if i % 2 == 0 {
        i = i + 1
        continue
    }
    sum = sum + i
    i = i + 1
}
export sum
