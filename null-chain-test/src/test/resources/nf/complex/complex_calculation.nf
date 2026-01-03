// ============================================
// 复杂计算测试 - 各种计算场景
// ============================================

// 1. 数学运算
Integer a = 10
Integer b = 3
Integer add = a + b
Integer sub = a - b
Integer mul = a * b
Integer div = a / b
Integer mod = a % b
echo "a = {a}, b = {b}"
echo "add = {add}, sub = {sub}, mul = {mul}, div = {div}, mod = {mod}"

// 2. 复杂表达式
Integer result1 = (10 + 20) * 2 - 5
Integer result2 = 10 + 20 * 2 - 5
Integer result3 = ((10 + 20) * 2) / 5
echo "result1 = {result1}, result2 = {result2}, result3 = {result3}"

// 3. 字符串操作
String str1 = "Hello"
String str2 = "World"
String combined = str1 + " " + str2
String repeated = str1 + str1 + str1
echo "combined = {combined}, repeated = {repeated}"

// 4. 循环计算
Integer sum = 0
Integer product = 1
for i in 1..10 {
    sum = sum + i
    product = product * i
    if product > 1000 {
        break
    }
}
echo "sum = {sum}, product = {product}"

// 5. 条件计算
Integer score = 85
Double bonus = 0.0
if score >= 90 {
    bonus = 1000.0
} else if score >= 80 {
    bonus = 500.0
} else {
    bonus = 0.0
}
Double total = score * 10.0 + bonus
echo "score = {score}, bonus = {bonus}, total = {total}"

// 6. 嵌套计算
Integer x = 5
Integer y = 3
Integer z = 2
Integer nested = ((x + y) * z) - (x - y) + z
echo "nested = {nested}"

// 7. 累加计算
Integer accumulator = 0
for i in 1..100 {
    accumulator = accumulator + i
    if accumulator > 1000 {
        echo "累加超过1000，当前值: {accumulator}, i = {i}"
        break
    }
}
echo "最终 accumulator = {accumulator}"

// 8. 平均值计算
Integer count = 0
Integer total = 0
for i in 1..10 {
    total = total + i
    count = count + 1
}
Integer average = total / count
echo "total = {total}, count = {count}, average = {average}"

export average



