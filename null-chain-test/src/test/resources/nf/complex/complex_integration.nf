// ============================================
// 复杂组合测试 - 所有表达式混合使用
// ============================================

// 1. 变量声明和赋值
String name = "张三"
Integer age = 25
Double salary = 5000.5
Boolean isMarried = false

// 2. 复杂计算
Integer year = 2024
Integer birthYear = year - age
Double annualSalary = salary * 12
Double tax = annualSalary * 0.1
Double netSalary = annualSalary - tax

// 3. 条件判断和赋值
String status = ""
if age >= 18 {
    status = "已成年"
    if age >= 60 {
        status = status + "，已退休"
    } else if age >= 30 {
        status = status + "，中年"
    } else {
        status = status + "，青年"
    }
} else {
    status = "未成年"
}
echo "状态: {status}"

// 4. switch语句
String level = ""
switch age {
    case 18, 19, 20
        level = "刚成年"
    case 21, 22, 23, 24, 25
        level = "青年早期"
    case 26, 27, 28, 29, 30
        level = "青年后期"
    default
        level = "其他年龄段"
}
echo "年龄段: {level}"

// 5. 循环计算
Integer factorial = 1
for i in 1..5 {
    factorial = factorial * i
}
echo "5的阶乘 = {factorial}"

Integer sum = 0
for i in 1..100 {
    sum = sum + i
}
echo "1到100的和 = {sum}"

// 6. 循环中的条件判断
Integer evenSum = 0
for i in 1..20 {
    if i % 2 == 0 {
        evenSum = evenSum + i
    }
}
echo "1到20的偶数和 = {evenSum}"

// 7. 循环控制
Integer firstDivisibleBy7 = 0
for i in 1..100 {
    if i % 7 == 0 {
        firstDivisibleBy7 = i
        break
    }
}
echo "第一个7的倍数: {firstDivisibleBy7}"

// 8. 嵌套循环和breakall
Integer found = 0
for i in 1..10 {
    for j in 1..10 {
        if i * j == 50 {
            found = i * j
            echo "找到 i={i}, j={j}, 乘积={found}"
            breakall
        }
    }
}

// 9. 对象操作
Map userInfo = new
userInfo.put("name", name)
userInfo.put("age", age)
userInfo.put("salary", salary)
echo "用户信息: {userInfo}"

// 10. 模板字符串
String report = ```
=== 员工报告 ===
姓名: {name}
年龄: {age} 岁
出生年份: {birthYear}
月薪: {salary} 元
年薪: {annualSalary} 元
税额: {tax} 元
税后收入: {netSalary} 元
================
```
echo report

// 11. 复杂表达式导出
export (annualSalary - tax) / 12


