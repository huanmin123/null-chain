// ============================================
// IF语句高级测试
// ============================================

// 1. 复杂条件表达式
Integer a = 10
Integer b = 20
Integer c = 30
if (a + b) > c && a < b {
    echo "条件1满足"
} else {
    echo "条件1不满足"
}

// 2. 字符串比较
String name = "张三"
if name == "张三" {
    echo "姓名匹配"
} else {
    echo "姓名不匹配"
}

// 3. 多条件组合
Integer age = 25
Boolean isStudent = false
if age >= 18 && age <= 60 && isStudent == false {
    echo "符合工作条件"
} else {
    echo "不符合工作条件"
}

// 4. if中的变量赋值
Integer value = 0
if true {
    value = 100
    echo "value = {value}"
}
echo "最终 value = {value}"

// 5. 嵌套if中的变量作用域
Integer outer = 10
if true {
    Integer inner = 20
    if true {
        Integer deep = 30
        outer = 15
        echo "outer = {outer}, inner = {inner}, deep = {deep}"
    }
    echo "outer = {outer}, inner = {inner}"
}
echo "outer = {outer}"

// 6. if-else链式判断
Integer level = 3
String grade = ""
if level == 1 {
    grade = "初级"
} else if level == 2 {
    grade = "中级"
} else if level == 3 {
    grade = "高级"
} else {
    grade = "未知"
}
echo "level = {level}, grade = {grade}"

export grade

