// while循环break测试
// 计算5的阶乘，使用break提前退出
Integer i = 1
Integer product = 1
while true {
    product = product * i
    if (i >= 5) {
        break
    }
    i = i + 1
}
export product
