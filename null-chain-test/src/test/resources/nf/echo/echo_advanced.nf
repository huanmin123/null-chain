// ============================================
// ECHO语句高级测试
// ============================================

// 1. echo复杂表达式
Integer x = 10
Integer y = 20
Integer z = 30
echo "计算结果: x={x}, y={y}, z={z}, 总和={x + y + z}"

// 2. echo条件表达式结果
Integer score = 85
String result = ""
if score >= 90 {
    result = "优秀"
} else {
    result = "良好"
}
echo "分数: {score}, 评价: {result}"

// 3. echo循环结果
Integer sum = 0
for i in 1..5 {
    sum = sum + i
    echo "i = {i}, 累计和 = {sum}"
}
echo "最终和 = {sum}"

// 4. echo嵌套模板
String outer = "外层"
String inner = "内层"
echo ```
外层变量: {outer}
内层变量: {inner}
组合: {outer + "_" + inner}
```

// 5. echo格式化输出
String name = "李四"
Integer age = 30
Double salary = 5000.5
echo "员工信息:"
echo "  姓名: {name}"
echo "  年龄: {age}"
echo "  薪资: {salary}"

export sum




