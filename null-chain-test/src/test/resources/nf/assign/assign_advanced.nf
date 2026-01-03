// ============================================
// 赋值表达式高级测试
// ============================================

// 1. 对象创建和赋值
Map map1 = new
map1.put("key1", "value1")
map1.put("key2", 100)
echo "map1 = {map1}"

// 2. 链式赋值（通过变量）
Integer base = 10
Integer value1 = base
Integer value2 = base
Integer value3 = base
echo "value1 = {value1}, value2 = {value2}, value3 = {value3}"

// 3. 条件表达式赋值（通过if语句）
Integer score = 85
String grade = ""
if score >= 90 {
    grade = "A"
} else if score >= 80 {
    grade = "B"
} else {
    grade = "C"
}
echo "score = {score}, grade = {grade}"

// 4. 循环中的赋值
Integer total = 0
for i in 1..10 {
    total = total + i
}
echo "total = {total}"

// 5. 嵌套表达式赋值
Integer a = 5
Integer b = 3
Integer c = (a + b) * 2
Integer d = c - a
echo "a = {a}, b = {b}, c = {c}, d = {d}"

// 6. 字符串模板赋值
String name = "张三"
Integer age = 25
String info = "姓名: {name}, 年龄: {age}"
echo info

// 7. 多变量计算赋值
Integer x = 10
Integer y = 20
Integer z = 30
Integer sum = x + y + z
Integer avg = sum / 3
echo "sum = {sum}, avg = {avg}"

export sum



