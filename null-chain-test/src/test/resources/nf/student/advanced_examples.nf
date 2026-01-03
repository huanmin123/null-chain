// ============================================
// NF 脚本高级示例
// 本文件展示了 NF 脚本语言的高级用法和复杂场景
// ============================================

// ========== 导入示例 ==========
// 导入 Java 类型
import type com.gitee.huanminabc.test.nullchain.entity.UserEntity

// 导入任务并起别名
import task com.gitee.huanminabc.test.nullchain.task.Test1Task as test1
import task com.gitee.huanminabc.test.nullchain.task.Test2Task as test2

// ========== 变量定义和复杂表达式 ==========

// 基本类型变量
String name = "张三"
Integer age = 25
Double salary = 5000.50
Boolean isMarried = false

// 复杂表达式计算
Integer year = 2024
Integer birthYear = year - age
Double annualSalary = salary * 12
Double tax = annualSalary * 0.1
Double netSalary = annualSalary - tax

// 字符串操作
String firstName = "张"
String lastName = "三"
String fullName = firstName + lastName
String email = fullName.toLowerCase() + "@example.com"

// ========== 对象操作示例 ==========

// 创建 Map 对象
Map userInfo = new
userInfo.put("name", name)
userInfo.put("age", age)
userInfo.put("salary", salary)

// 创建 List 对象（如果支持）
// List hobbies = new
// hobbies.add("reading")
// hobbies.add("coding")

// ========== 模板字符串高级用法 ==========

// 1. 多行模板字符串
String template1 = ```
个人信息:
姓名: {name}
年龄: {age}
年薪: {annualSalary}
税后收入: {netSalary}
```

echo template1

// 2. 模板字符串中使用表达式
String template2 = ```
计算结果:
出生年份: {birthYear}
税前年薪: {annualSalary}
税额: {tax}
税后年薪: {netSalary}
净收入比例: {(netSalary / annualSalary) * 100}%
```

echo template2

// 3. 模板字符串在赋值中使用
String report = ```
=== 员工报告 ===
姓名: {fullName}
邮箱: {email}
年龄: {age} 岁
出生年份: {birthYear}
月薪: {salary} 元
年薪: {annualSalary} 元
税额: {tax} 元
税后收入: {netSalary} 元
================
```

echo report

// 4. 模板字符串在 export 中使用
String summary = ```
员工摘要:
{name}, {age}岁, 年薪 {annualSalary} 元
```

// ========== 复杂条件判断 ==========

// 嵌套 if 语句
if age >= 18 {
    echo "已成年"
    if age >= 60 {
        echo "已退休"
    } else if age >= 30 {
        echo "中年"
        if isMarried {
            echo "已婚"
        } else {
            echo "未婚"
        }
    } else {
        echo "青年"
    }
} else {
    echo "未成年"
}

// 复杂逻辑表达式
if (age >= 18 && age < 60) && salary > 3000 {
    echo "符合工作条件"
}

if age < 18 || age >= 60 {
    echo "不在工作年龄"
}

// Switch 复杂场景
switch age {
    case 18, 19, 20
        echo "刚成年"
    case 21, 22, 23, 24, 25
        echo "青年早期"
    case 26, 27, 28, 29, 30
        echo "青年后期"
    default
        echo "其他年龄段"
}

// ========== 循环高级用法 ==========

// 1. 循环计算
Integer factorial = 1
for i in 1..5 {
    factorial = factorial * i
    echo "i = {i}, factorial = {factorial}"
}
echo "5的阶乘 = {factorial}"

// 2. 循环累加
Integer sum = 0
for i in 1..100 {
    sum = sum + i
}
echo "1到100的和 = {sum}"

// 3. 循环中条件判断
for i in 1..20 {
    if i % 2 == 0 {
        echo "{i} 是偶数"
    } else {
        echo "{i} 是奇数"
    }
}

// 4. 循环中使用 continue
Integer evenSum = 0
for i in 1..10 {
    if i % 2 != 0 {
        continue
    }
    evenSum = evenSum + i
    echo "偶数: {i}, 累计和: {evenSum}"
}

// 5. 循环中使用 break
Integer firstDivisibleBy7 = 0
for i in 1..100 {
    if i % 7 == 0 {
        firstDivisibleBy7 = i
        echo "找到第一个7的倍数: {i}"
        break
    }
}

// 6. 嵌套循环和 breakall
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

// 7. 循环操作 Map
Map scores = new
for i in 1..5 {
    Integer score = i * 10
    scores.put("student" + i, score)
    echo "学生{i}的分数: {score}"
}
echo "所有分数: {scores}"

// ========== 任务调用示例 ==========

// 1. 基本任务调用
run test1(name, age)

// 2. 任务调用并保存结果
run test1(name, age) -> result1:String

// 3. 多任务并发执行
run test1(name, age), test2(salary, netSalary) -> results:Map

// 4. 循环中调用任务
Map taskResults = new
for i in 1..5 {
    String param = "param" + i
    run test1(param) -> result:String
    String key = "task" + i
    taskResults.put(key, result)
}

// ========== Echo 高级用法 ==========

// 1. 打印对象
echo "用户信息:", userInfo

// 2. 打印复杂表达式
echo "计算:", "年龄+10=", age + 10, "年薪/12=", annualSalary / 12

// 3. 格式化输出
echo "姓名", \t, "年龄", \t, "年薪", \n
echo name, \t, age, \t, annualSalary, \n

// 4. 模板字符串在 echo 中
echo ```
=== 格式化输出 ===
姓名:     {name}
年龄:     {age}
年薪:     {annualSalary}
税后收入: {netSalary}
===============
```

// ========== Export 高级用法 ==========

// 1. 导出计算结果
export annualSalary - tax

// 2. 导出字符串拼接
export "员工: " + name + ", 年龄: " + age

// 3. 导出模板字符串
export ```
员工信息汇总:
姓名: {name}
年龄: {age}
年薪: {annualSalary}
税后: {netSalary}
```

// 4. 导出复杂表达式
export (annualSalary - tax) / 12

// ========== 综合示例：员工管理系统 ==========

// 初始化数据
String employeeName = "李四"
Integer employeeAge = 30
Double monthlySalary = 8000.0
Boolean isFullTime = true

// 计算
Double yearlySalary = monthlySalary * 12
Double bonus = yearlySalary * 0.2
Double totalIncome = yearlySalary + bonus

// 生成报告
String employeeReport = ```
========== 员工报告 ==========
姓名: {employeeName}
年龄: {employeeAge}
职位类型: {isFullTime ? "全职" : "兼职"}
月薪: {monthlySalary} 元
年薪: {yearlySalary} 元
奖金: {bonus} 元
总收入: {totalIncome} 元
============================
```

echo employeeReport

// 根据年龄判断级别
String level = ""
if employeeAge < 25 {
    level = "初级"
} else if employeeAge < 35 {
    level = "中级"
} else if employeeAge < 45 {
    level = "高级"
} else {
    level = "资深"
}

echo "员工级别: {level}"

// 根据收入判断等级
String grade = ""
if totalIncome > 200000 {
    grade = "A级"
} else if totalIncome > 100000 {
    grade = "B级"
} else {
    grade = "C级"
}

echo "收入等级: {grade}"

// 导出最终结果
export ```
员工: {employeeName}
级别: {level}
等级: {grade}
总收入: {totalIncome} 元
```




