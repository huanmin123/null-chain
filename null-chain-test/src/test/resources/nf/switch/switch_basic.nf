// ============================================
// SWITCH语句基础测试
// ============================================

// 1. 基本switch语句
Integer day = 3
switch day {
    case 1
        echo "星期一"
    case 2
        echo "星期二"
    case 3
        echo "星期三"
    case 4
        echo "星期四"
    case 5
        echo "星期五"
    default
        echo "周末"
}

// 2. 多case匹配
Integer month = 2
switch month {
    case 1, 2, 3
        echo "第一季度"
    case 4, 5, 6
        echo "第二季度"
    case 7, 8, 9
        echo "第三季度"
    case 10, 11, 12
        echo "第四季度"
    default
        echo "无效月份"
}

// 3. switch中的变量赋值
Integer switchValue = 2
Integer value = 0
switch switchValue {
    case 1
        value = 10
    case 2
        value = 20
    case 3
        value = 30
    default
        value = 0
}
echo "value = {value}"

// 4. 字符串switch
String color = "red"
switch color {
    case "red"
        echo "红色"
    case "green"
        echo "绿色"
    case "blue"
        echo "蓝色"
    default
        echo "其他颜色"
}

// 5. default分支
Integer num = 99
switch num {
    case 1, 2, 3
        echo "小数字"
    default
        echo "其他数字: {num}"
}

export value

