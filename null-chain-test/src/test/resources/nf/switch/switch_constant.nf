// ============================================
// SWITCH常量测试
// ============================================

// 1. switch使用整数常量
Integer result = 0
switch 1 {
    case 1
        result = 10
    case 2
        result = 20
    default
        result = 0
}
echo "result = {result}"

// 2. switch使用字符串常量
String grade = ""
switch "B" {
    case "A"
        grade = "优秀"
    case "B"
        grade = "良好"
    default
        grade = "其他"
}
echo "grade = {grade}"

export result




