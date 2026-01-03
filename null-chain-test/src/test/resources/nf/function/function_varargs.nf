// ============================================
// 可变参数函数测试
// ============================================

// 定义可变参数求和函数
fun sum(int first, int... rest)Integer {
    Integer total = first
    for item in rest {
        total = total + item
    }
    return total
}

// 测试可变参数函数调用
Integer result1 = sum(1, 2, 3, 4, 5)
echo "sum(1, 2, 3, 4, 5) = {result1}"

Integer result2 = sum(10)
echo "sum(10) = {result2}"

Integer result3 = sum(1, 2, 3)
echo "sum(1, 2, 3) = {result3}"

// 可变参数字符串拼接
fun join(String separator, String... parts)String {
    String result = ""
    Integer index = 0
    for part in parts {
        if index > 0 {
            result = result + separator
        }
        result = result + part
        index = index + 1
    }
    return result
}

String joined = join("-", "a", "b", "c", "d")
echo "join('-', 'a', 'b', 'c', 'd') = {joined}"

export "可变参数函数测试完成"

