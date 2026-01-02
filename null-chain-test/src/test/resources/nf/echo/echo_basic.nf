// ============================================
// ECHO语句基础测试
// ============================================

// 1. 基本echo
echo "Hello World"

// 2. echo变量
String name = "张三"
Integer age = 25
echo "姓名: {name}, 年龄: {age}"

// 3. echo表达式
Integer a = 10
Integer b = 20
echo "a + b = {a + b}"

// 4. echo多个值
echo "值1: ", 100, ", 值2: ", 200

// 5. echo模板字符串
String template = ```
姓名: {name}
年龄: {age}
```
echo template

// 6. echo布尔值
Boolean isTrue = true
Boolean isFalse = false
echo "isTrue = {isTrue}, isFalse = {isFalse}"

// 7. echo对象
Map map = new
map.put("key", "value")
echo "map = {map}"

export "echo测试完成"

