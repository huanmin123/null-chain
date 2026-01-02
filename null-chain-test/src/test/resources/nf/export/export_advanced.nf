// ============================================
// EXPORT语句高级测试
// ============================================

// 1. 导出复杂表达式
Integer x = 10
Integer y = 20
Integer z = 30
export (x + y + z) * 2

// 2. 导出条件表达式结果
Integer score = 85
String grade = ""
if score >= 90 {
    grade = "A"
} else if score >= 80 {
    grade = "B"
} else {
    grade = "C"
}
export grade

// 3. 导出循环计算结果
Integer sum = 0
for i in 1..10 {
    sum = sum + i
}
export sum

// 4. 导出模板字符串
String name = "张三"
Integer age = 25
export ```
姓名: {name}
年龄: {age}
```

// 5. 导出字符串拼接表达式
String prefix = "结果: "
Integer value = 100
export prefix + value

// 6. 导出多层嵌套表达式
Integer a = 5
Integer b = 3
Integer c = 2
export ((a + b) * c) - 1

