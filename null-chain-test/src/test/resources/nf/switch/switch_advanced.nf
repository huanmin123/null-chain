// ============================================
// SWITCH语句高级测试
// ============================================

// 1. switch中的复杂逻辑
Integer score = 85
String grade = ""
switch score {
    case 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100
        grade = "A"
    case 80, 81, 82, 83, 84, 85, 86, 87, 88, 89
        grade = "B"
    case 70, 71, 72, 73, 74, 75, 76, 77, 78, 79
        grade = "C"
    default
        grade = "D"
}
echo "score = {score}, grade = {grade}"

// 2. 连续case值
Integer level = 5
String desc = ""
switch level {
    case 1, 2, 3
        desc = "初级"
    case 4, 5, 6
        desc = "中级"
    case 7, 8, 9
        desc = "高级"
    default
        desc = "未知"
}
echo "level = {level}, desc = {desc}"

export desc
