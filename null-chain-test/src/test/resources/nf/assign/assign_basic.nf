// ============================================
// 赋值表达式基础测试
// ============================================

// 1. 基本类型赋值
Integer a = 10
Double b = 3.14
String c = "hello"
Boolean d = true

echo "a = {a}, b = {b}, c = {c}, d = {d}"

// 2. 表达式赋值
Integer sum = 1 + 2 + 3
Integer product = 2 * 3 * 4
Double division = 10.0 / 3.0
Integer remainder = 10 % 3

echo "sum = {sum}, product = {product}, division = {division}, remainder = {remainder}"

// 3. 变量重新赋值
Integer x = 5
echo "x = {x}"
x = 10
echo "x = {x}"
x = x + 5
echo "x = {x}"

// 4. 字符串拼接赋值
String str1 = "Hello"
String str2 = "World"
String combined = str1 + " " + str2
echo "combined = {combined}"

// 5. 复杂表达式赋值
Integer result = (10 + 20) * 2 - 5
echo "result = {result}"

export result


