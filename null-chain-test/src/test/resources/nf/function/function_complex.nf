// ============================================
// 复杂函数场景测试
// ============================================

// 定义复杂计算函数
fun calculate(int a, int b, int c)Integer,Integer {
    Integer sum = a + b + c
    Integer product = a * b * c
    return sum, product
}

// 多返回值函数调用
var sum:Integer, product:Integer = calculate(2, 3, 4)
echo "sum = {sum}, product = {product}"

// 使用返回值进行计算
Integer result = sum + product
echo "sum + product = {result}"

// 定义查找最大值和最小值的函数
fun findMinMax(int... numbers)Integer,Integer {
    Integer count = 0
    Integer min = 0
    Integer max = 0
    for num in numbers {
        if count == 0 {
            min = num
            max = num
        } else {
            if num < min {
                min = num
            }
            if num > max {
                max = num
            }
        }
        count = count + 1
    }
    return min, max
}

var min:Integer, max:Integer = findMinMax(5, 2, 8, 1, 9, 3)
echo "min = {min}, max = {max}"

export "复杂函数测试完成"

