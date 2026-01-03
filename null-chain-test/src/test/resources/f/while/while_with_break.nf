// 测试 while 循环中的 break
Integer i = 1
Integer product = 1
while i <= 10 {
    product = product * i
    if product > 100 {
        break
    }
    i = i + 1
}
export product
