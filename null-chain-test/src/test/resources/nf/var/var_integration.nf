// ============================================
// var 集成测试 - 与其他功能结合使用
// ============================================

// 1. var 与 echo 结合
var name = "张三"
var age = 25
echo "姓名: {name}, 年龄: {age}"

// 2. var 与 if 结合
var score = 85
if score > 80 {
    var level = "优秀"
    echo "分数: {score}, 等级: {level}"
}

// 3. var 与 for 结合
var total = 0
for i in 1..5 {
    var item = i * 10
    total = total + item
    echo "item={item}, total={total}"
}
echo "最终total={total}"

// 4. var 与 while 结合
var count = 0
while count < 3 {
    var message = "计数: " + count
    echo message
    count = count + 1
}

// 5. var 与 switch 结合
var day = 3
switch day {
    case 1
        var dayName = "星期一"
        echo dayName
    case 2
        var dayName = "星期二"
        echo dayName
    case 3
        var dayName = "星期三"
        echo dayName
    default
        var dayName = "其他"
        echo dayName
}

// 6. var 与 export 结合
var result = 100
var status = "成功"
export result
export status

// 7. var 与 run 结合（需要先定义 task）
// var taskResult = run myTask(param1, param2)

// 8. var 与函数调用结合
import java.util.UUID
var uuid:String = UUID.randomUUID().toString()
echo "UUID: {uuid}"

// 9. var 与对象创建结合（使用显式类型声明）
import java.util.HashMap
HashMap map = new
map.put("key1", "value1")
map.put("key2", "value2")
echo "map = {map}"

// 10. var 与模板字符串结合
var template = ```
这是一个多行模板
变量 name = {name}
变量 age = {age}
变量 score = {score}
```
echo template

// 11. var 与复杂表达式结合
var a = 10
var b = 20
var c = 30
var complexResult = (a + b) * c / 2
echo "复杂结果: {complexResult}"

// 12. var 与类型转换结合
var numStr = "123"
var num:Integer = Integer.parseInt(numStr)
echo "转换后数字: {num}"

// 13. var 与条件表达式结合
var x = 10
var y = 20
var max = 0
if x > y {
    max = x
} else {
    max = y
}
echo "最大值: {max}"

// 14. var 与嵌套结构结合
var outer = "外层"
if true {
    var middle = "中层"
    if true {
        var inner = "内层"
        echo "嵌套: outer={outer}, middle={middle}, inner={inner}"
    }
}

// 15. var 与循环控制结合
var sum = 0
for i in 1..10 {
    if i % 2 == 0 {
        continue
    }
    if i > 7 {
        break
    }
    sum = sum + i
    echo "i={i}, sum={sum}"
}
echo "最终sum={sum}"

export "集成测试完成"

